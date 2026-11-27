package dev.poc.aaprojectm;

import android.content.res.AssetManager;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.GLES30;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.car.app.SurfaceCallback;
import androidx.car.app.SurfaceContainer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/** Owns the GL thread that draws projectM into the NavigationTemplate surface. */
final class VizRenderer implements SurfaceCallback {
    private static final String TAG = "viz";
    private static final long PRESET_SECONDS = 30;
    static final int FPS = 30;  // calibration knob: head-unit video encode is the bottleneck, not GL

    private final AssetManager assets;
    private final String[] presets;
    private final Random rnd = new Random();
    private Thread thread;
    private volatile boolean running;
    volatile boolean paused;          // set by the speed guard
    private volatile boolean nextRequested;

    VizRenderer(AssetManager assets) {
        this.assets = assets;
        String[] p;
        try { p = assets.list("presets"); } catch (IOException e) { p = new String[0]; }
        presets = p;
    }

    void next() { nextRequested = true; }

    @Override public void onSurfaceAvailable(@NonNull SurfaceContainer c) {
        Surface s = c.getSurface();
        if (s == null) return;
        stop();
        int w = c.getWidth(), h = c.getHeight();
        running = true;
        thread = new Thread(() -> loop(s, w, h), "viz-gl");
        thread.start();
    }

    @Override public void onSurfaceDestroyed(@NonNull SurfaceContainer c) { stop(); }

    // ponytail: visible/stable area ignored, visuals run full-bleed under the action strip.

    void stop() {
        running = false;
        if (thread != null) {
            try { thread.join(); } catch (InterruptedException ignored) { }
            thread = null;
        }
    }

    private void loop(Surface surface, int w, int h) {
        EGLDisplay dpy = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        EGL14.eglInitialize(dpy, null, 0, null, 0);
        EGLConfig[] cfg = new EGLConfig[1];
        int[] n = new int[1];
        EGL14.eglChooseConfig(dpy, new int[]{
                EGL14.EGL_RENDERABLE_TYPE, EGLExt.EGL_OPENGL_ES3_BIT_KHR,
                EGL14.EGL_RED_SIZE, 8, EGL14.EGL_GREEN_SIZE, 8, EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_NONE}, 0, cfg, 0, 1, n, 0);
        if (n[0] == 0) { Log.e(TAG, "no GLES3 EGL config"); return; }
        EGLContext ctx = EGL14.eglCreateContext(dpy, cfg[0], EGL14.EGL_NO_CONTEXT,
                new int[]{EGL14.EGL_CONTEXT_CLIENT_VERSION, 3, EGL14.EGL_NONE}, 0);
        // Retry: right after our process restarts, the host's surface can still be held by the dead one (EGL_BAD_ALLOC).
        EGLSurface win = EGL14.EGL_NO_SURFACE;
        for (int i = 0; i < 20 && running && win == EGL14.EGL_NO_SURFACE; i++) {
            win = EGL14.eglCreateWindowSurface(dpy, cfg[0], surface, new int[]{EGL14.EGL_NONE}, 0);
            if (win == EGL14.EGL_NO_SURFACE) try { Thread.sleep(250); } catch (InterruptedException ignored) { }
        }
        if (win == EGL14.EGL_NO_SURFACE || !EGL14.eglMakeCurrent(dpy, win, win, ctx)) {
            Log.e(TAG, "EGL setup failed: 0x" + Integer.toHexString(EGL14.eglGetError()));
            release(dpy, ctx, win);
            return;
        }

        if (Native.create(w, h, FPS)) {
            loadRandom(false);
            long lastSwitch = System.nanoTime(), frames = 0, fpsT = lastSwitch;
            while (running) {
                long t0 = System.nanoTime();
                if (nextRequested || t0 - lastSwitch > PRESET_SECONDS * 1_000_000_000L) {
                    nextRequested = false;
                    loadRandom(true);
                    lastSwitch = t0;
                }
                if (paused) {
                    GLES30.glClearColor(0, 0, 0, 1);
                    GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
                } else {
                    Native.render();
                }
                EGL14.eglSwapBuffers(dpy, win);
                if (++frames == 300) {  // frame-rate readout for head-unit testing
                    Log.i(TAG, String.format("%dx%d %.1f fps", w, h, 300e9 / (System.nanoTime() - fpsT)));
                    frames = 0; fpsT = System.nanoTime();
                }
                long sleepMs = (paused ? 100 : 1000 / FPS) - (System.nanoTime() - t0) / 1_000_000;
                if (sleepMs > 0) try { Thread.sleep(sleepMs); } catch (InterruptedException ignored) { }
            }
            Native.destroy();
        }
        release(dpy, ctx, win);
    }

    private static void release(EGLDisplay dpy, EGLContext ctx, EGLSurface win) {
        EGL14.eglMakeCurrent(dpy, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        if (win != EGL14.EGL_NO_SURFACE) EGL14.eglDestroySurface(dpy, win);
        EGL14.eglDestroyContext(dpy, ctx);
        EGL14.eglTerminate(dpy);
    }

    private void loadRandom(boolean smooth) {
        if (presets.length == 0) return;
        String name = presets[rnd.nextInt(presets.length)];
        try (InputStream in = assets.open("presets/" + name)) {
            byte[] b = in.readAllBytes();
            Native.loadPreset(b, smooth);
            Log.i(TAG, "preset " + name);
        } catch (IOException e) {
            Log.w(TAG, "preset load failed " + name, e);
        }
    }
}

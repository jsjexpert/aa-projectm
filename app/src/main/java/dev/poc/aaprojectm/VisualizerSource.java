package dev.poc.aaprojectm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.Visualizer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Default audio source: taps post-mix output with the Visualizer API, which Spotify cannot opt out of
 * (unlike AudioPlaybackCapture). Low-res: mono 8-bit, ~20 snapshots/s.
 * Follows the session a player announces (Spotify does, for EQ apps) so nav prompts stay out;
 * falls back to session 0, the global output mix.
 */
final class VisualizerSource {
    private static final String TAG = "viz";
    private static Visualizer vis;
    private static int session;
    private static BroadcastReceiver receiver;
    private static final Handler main = new Handler(Looper.getMainLooper());
    private static int wanted;  // session we are trying to reach (retries target this)

    static synchronized void start(Context ctx) {
        if (receiver != null) return;
        // Players announce only when playback starts, often before we exist; reuse the last one we heard.
        android.content.SharedPreferences prefs = ctx.getApplicationContext().getSharedPreferences("viz", Context.MODE_PRIVATE);
        tryAttach(prefs.getInt("session", 0), 5);
        receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context c, Intent i) {
                int s = i.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, 0);
                Log.i(TAG, i.getAction() + " pkg=" + i.getStringExtra(AudioEffect.EXTRA_PACKAGE_NAME) + " session=" + s);
                synchronized (VisualizerSource.class) {
                    if (receiver == null) return;
                    boolean open = AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION.equals(i.getAction());
                    if (open) prefs.edit().putInt("session", s).apply();
                    if (open) tryAttach(s, 5);
                    else if (s == session || s == wanted) tryAttach(0, 0);
                }
            }
        };
        IntentFilter f = new IntentFilter(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
        f.addAction(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        Context app = ctx.getApplicationContext();
        if (Build.VERSION.SDK_INT >= 33) app.registerReceiver(receiver, f, Context.RECEIVER_EXPORTED);
        else app.registerReceiver(receiver, f);
    }

    static synchronized void stop(Context ctx) {
        if (receiver != null) { ctx.getApplicationContext().unregisterReceiver(receiver); receiver = null; }
        main.removeCallbacksAndMessages(null);
        release();
    }

    /**
     * Players announce a session before their track lands on its output; under Android Auto the track then
     * moves to the projection (remote submix) output and attaching too early fails with -38. So retry.
     */
    private static void tryAttach(int s, int retries) {
        wanted = s;
        main.removeCallbacksAndMessages(null);
        if (attach(s) || s == 0) return;
        attach(0);  // keep something flowing meanwhile
        if (retries > 0) main.postDelayed(() -> {
            synchronized (VisualizerSource.class) { if (receiver != null && wanted == s) tryAttach(s, retries - 1); }
        }, 1000);
    }

    private static boolean attach(int s) {
        release();
        try {
            vis = new Visualizer(s);
            vis.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            vis.setScalingMode(Visualizer.SCALING_MODE_NORMALIZED);  // volume-independent
            final int[] n = {0};
            final float[] peak = {0};
            vis.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                @Override public void onWaveFormDataCapture(Visualizer v, byte[] wave, int rate) {
                    if (!CaptureService.running) Native.pcmU8(wave, wave.length);  // playback capture wins when on
                    for (byte b : wave) peak[0] = Math.max(peak[0], Math.abs((b & 0xff) - 128) / 128f);
                    if (++n[0] % 100 == 0) { Log.i(TAG, "vis peak " + peak[0] + " session " + s); peak[0] = 0; }
                }
                @Override public void onFftDataCapture(Visualizer v, byte[] fft, int rate) { }
            }, Visualizer.getMaxCaptureRate(), true, false);
            vis.setEnabled(true);
            session = s;
            Log.i(TAG, "Visualizer on session " + s);
            return true;
        } catch (RuntimeException e) {  // RECORD_AUDIO missing, or session not on an output yet
            Log.w(TAG, "Visualizer failed on session " + s + ": " + e.getMessage());
            release();
            return false;
        }
    }

    private static void release() {
        if (vis != null) { vis.setEnabled(false); vis.release(); vis = null; }
    }
}

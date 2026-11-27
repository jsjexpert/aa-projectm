package dev.poc.aaprojectm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.AudioRecord;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.IBinder;
import android.util.Log;

/** Captures other apps' playback (AudioPlaybackCapture) and feeds it to projectM. */
public final class CaptureService extends Service {
    static final String EXTRA_CODE = "code", EXTRA_DATA = "data";
    static volatile boolean running;

    private MediaProjection projection;
    private AudioRecord rec;
    private Thread thread;

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (running || intent == null) return START_NOT_STICKY;
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel("cap", "Audio capture", NotificationManager.IMPORTANCE_LOW));
        Notification n = new Notification.Builder(this, "cap")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setContentTitle("Visualizer listening").build();
        startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION); // must precede getMediaProjection on 14+

        projection = getSystemService(MediaProjectionManager.class)
                .getMediaProjection(intent.getIntExtra(EXTRA_CODE, 0), intent.getParcelableExtra(EXTRA_DATA));
        if (projection == null) { stopSelf(); return START_NOT_STICKY; }
        projection.registerCallback(new MediaProjection.Callback() {
            @Override public void onStop() { stopSelf(); }
        }, null);

        AudioPlaybackCaptureConfiguration cfg = new AudioPlaybackCaptureConfiguration.Builder(projection)
                .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                .addMatchingUsage(AudioAttributes.USAGE_GAME)
                .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                .build();
        AudioFormat fmt = new AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                .setSampleRate(44100)
                .setChannelMask(AudioFormat.CHANNEL_IN_STEREO)
                .build();
        try {
            rec = new AudioRecord.Builder().setAudioFormat(fmt).setAudioPlaybackCaptureConfig(cfg)
                    .setBufferSizeInBytes(16384).build();
        } catch (SecurityException | UnsupportedOperationException e) {
            Log.e("viz", "AudioRecord failed (RECORD_AUDIO granted?)", e);
            stopSelf();
            return START_NOT_STICKY;
        }
        rec.startRecording();
        running = true;
        thread = new Thread(this::pump, "viz-pcm");
        thread.start();
        return START_NOT_STICKY;
    }

    private void pump() {
        float[] buf = new float[1024 * 2];
        float peak = 0;
        long lastLog = System.currentTimeMillis();
        while (running) {
            int n = rec.read(buf, 0, buf.length, AudioRecord.READ_BLOCKING);
            if (n <= 0) continue;
            Native.pcm(buf, n / 2);
            for (int i = 0; i < n; i++) peak = Math.max(peak, Math.abs(buf[i]));
            if (System.currentTimeMillis() - lastLog > 2000) {
                // Spike check: peak stays 0.0 if the source app (e.g. Spotify) opts out of capture.
                Log.i("viz", "capture peak " + peak);
                peak = 0;
                lastLog = System.currentTimeMillis();
            }
        }
    }

    @Override public void onDestroy() {
        running = false;
        if (rec != null) rec.stop();
        if (thread != null) try { thread.join(); } catch (InterruptedException ignored) { }
        if (rec != null) rec.release();
        if (projection != null) projection.stop();
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent i) { return null; }
}

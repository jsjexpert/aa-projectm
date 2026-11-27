package dev.poc.aaprojectm;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Phone-side: grants permissions and starts the capture service (consent can only be shown here). */
public final class MainActivity extends Activity {
    private TextView status;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        String[] perms = Build.VERSION.SDK_INT >= 33
                ? new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS}
                : new String[]{Manifest.permission.RECORD_AUDIO};
        requestPermissions(perms, 0);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 96, 48, 48);
        status = new TextView(this);
        Button start = new Button(this);
        start.setText("Optional: stereo playback capture (not Spotify)");
        start.setOnClickListener(v -> startActivityForResult(
                getSystemService(MediaProjectionManager.class).createScreenCaptureIntent(), 1));
        Button stop = new Button(this);
        stop.setText("Stop");
        stop.setOnClickListener(v -> {
            stopService(new Intent(this, CaptureService.class));
            refresh();
        });
        root.addView(status);
        root.addView(start);
        root.addView(stop);
        setContentView(root);
    }

    @Override protected void onResume() { super.onResume(); refresh(); }


    @Override protected void onActivityResult(int req, int code, Intent data) {
        if (req == 1 && code == RESULT_OK) {
            startForegroundService(new Intent(this, CaptureService.class)
                    .putExtra(CaptureService.EXTRA_CODE, code).putExtra(CaptureService.EXTRA_DATA, data));
        }
        status.postDelayed(this::refresh, 500);
    }

    private void refresh() {
        status.setText(CaptureService.running
                ? "Capturing. Play music, then open AA projectM on the head unit.\nCheck: adb logcat -s viz"
                : "Audio comes from the Visualizer API by default (works with Spotify).\nPlayback capture is an optional higher-quality source for apps that allow it.");
    }
}

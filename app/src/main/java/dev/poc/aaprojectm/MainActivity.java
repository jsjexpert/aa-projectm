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

/** Phone-side: grants the microphone permission the Visualizer API requires, and shows licenses. */
public final class MainActivity extends Activity {
    private TextView status;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        // Visualizer API needs RECORD_AUDIO granted; it is not logged as microphone use.
        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 0);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 96, 48, 48);
        status = new TextView(this);
        /* DISABLED: stereo playback capture (see CaptureService).
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
        root.addView(start);
        root.addView(stop);
        */
        root.addView(status);
        Button licenses = new Button(this);
        licenses.setText("Open-source licenses");
        licenses.setOnClickListener(v -> showLicenses());
        root.addView(licenses);
        setContentView(root);
    }

    @Override protected void onResume() { super.onResume(); refresh(); }

    /** Shows the license texts bundled under assets/licenses/ (copied from the repo at build time). */
    private void showLicenses() {
        StringBuilder sb = new StringBuilder();
        for (String f : new String[]{"LICENSE.txt", "THIRD_PARTY_NOTICES.md", "projectM-LGPL-2.1.txt", "Apache-2.0.txt"}) {
            try (java.io.InputStream in = getAssets().open("licenses/" + f)) {
                sb.append("==== ").append(f).append(" ====\n\n")
                  .append(new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8)).append("\n\n");
            } catch (java.io.IOException e) {
                sb.append("==== ").append(f).append(": missing ====\n\n");
            }
        }
        TextView t = new TextView(this);
        t.setText(sb);
        t.setTextIsSelectable(true);
        t.setPadding(32, 32, 32, 32);
        android.widget.ScrollView sv = new android.widget.ScrollView(this);
        sv.addView(t);
        new android.app.AlertDialog.Builder(this).setTitle("Open-source licenses").setView(sv)
                .setPositiveButton(android.R.string.ok, null).show();
    }

    /* DISABLED: stereo playback capture (see CaptureService).
    @Override protected void onActivityResult(int req, int code, Intent data) {
        if (req == 1 && code == RESULT_OK) {
            startForegroundService(new Intent(this, CaptureService.class)
                    .putExtra(CaptureService.EXTRA_CODE, code).putExtra(CaptureService.EXTRA_DATA, data));
        }
        status.postDelayed(this::refresh, 500);
    }
    */

    private void refresh() {
        status.setText("Play music in Spotify, then open AA projectM on the car screen.\n"
                + "Audio comes from Spotify's announced session (Visualizer API, mono). Other players are not supported.");
    }
}

# AA projectM (POC, sideload only)

projectM (MilkDrop-compatible) drawn into an Android Auto `NavigationTemplate` surface.

Audio: the **Visualizer API** by default (mono 8-bit, ~20 Hz, works with Spotify). It attaches to the session a player announces (remembered across launches, retried while Android Auto moves the track to its projection output). The global-mix fallback (session 0) is silent during projection, since media then plays on the remote-submix output. `AudioPlaybackCapture` (stereo float) is optional from the phone app and takes over while running, but Spotify blocks it.

## Build
```bash
git submodule update --init --recursive
./setup-sdk.sh            # you accept the SDK licenses
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Run on the DHU
1. Phone: Android Auto settings → tap *Version* 10× → menu → *Developer settings* → enable *Unknown sources* → *Start head unit server*.
2. Open **AA projectM** on the phone once and grant permissions. Play music.
3. `./run-dhu.sh` (forwards port 5277 and starts the DHU)
4. On the DHU, open AA projectM. Tap the visuals once for the next preset (auto-switches every 30 s).
5. Triple-tap quickly for test mode: projectM's audio test presets (oscilloscope line; waveform + spectrum), held in place. Triple-tap again for the next test preset, single-tap to leave. Paused audio gives a flat line.

Check with `adb logcat -s viz`:
- `Visualizer on session N` shows which audio session is being visualized (0 = global mix).
- `capture peak` lines appear only when optional playback capture is on; 0.0 means the source app opts out.
- `... fps` lines are frame-rate readouts per 300 frames.

## Known limits
- Reinstalling during a DHU session leaves the car surface held by the killed process (`EGL_BAD_ALLOC`, nothing drawn). Restart the DHU after each install.
- Spotify 9.2.x blocks playback capture (verified on SM-N986W / Android 13), hence the Visualizer default. For playback capture, start it *before* pressing play, since routing is fixed when the app opens its stream.
- First DHU connect needs "Add new cars to Android Auto" on and the car approved on the unlocked phone.
- No `CAR_SPEED` permission, so the speed guard never receives speed and the visuals always render. Restore the permission (manifest + runtime request) before any demo in a car that can move.
- `HostValidator.ALLOW_ALL_HOSTS_VALIDATOR` is for sideloading only.
- Presets: 200 random texture-free picks from [presets-cream-of-the-crop](https://github.com/projectM-visualizer/presets-cream-of-the-crop) (7,925 qualify; all of them would add ~27 MB to the APK). Regenerate with `tools/pick-presets.py <checkout> [N] [seed]`. They play as a shuffled playlist. Some may not compile on GLES and render poorly; not screened yet. Visible/stable area is ignored, so the visuals run under the action strip.

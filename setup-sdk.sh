#!/usr/bin/env bash
# Installs the Android SDK pieces this project needs. You answer the license prompts yourself.
set -euo pipefail
SDK="${ANDROID_HOME:-$HOME/Android/Sdk}"
if [ ! -x "$SDK/cmdline-tools/latest/bin/sdkmanager" ]; then
  mkdir -p "$SDK/cmdline-tools" && cd "$SDK/cmdline-tools"
  curl -fsSL -o ct.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
  unzip -q ct.zip && mv cmdline-tools latest && rm ct.zip
  cd - >/dev/null
fi
SM="$SDK/cmdline-tools/latest/bin/sdkmanager"
"$SM" --licenses
"$SM" "platform-tools" "platforms;android-35" "build-tools;35.0.0" "ndk;27.2.12479018" "cmake;3.22.1" "extras;google;auto"
echo "sdk.dir=$SDK" > "$(dirname "$0")/local.properties"
echo "Done. Build with: ./gradlew assembleDebug"

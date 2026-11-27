#!/usr/bin/env bash
# Starts the Desktop Head Unit against the phone's head unit server (phone must have it started).
set -euo pipefail
SDK="${ANDROID_HOME:-$HOME/Android/Sdk}"
"$SDK/platform-tools/adb" forward tcp:5277 tcp:5277
# DHU needs host libc++; the NDK ships one, so no system package is required.
export LD_LIBRARY_PATH="$(ls -d "$SDK"/ndk/*/toolchains/llvm/prebuilt/linux-x86_64/lib | head -1)${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}"
cd "$SDK/extras/google/auto"
exec ./desktop-head-unit "$@"

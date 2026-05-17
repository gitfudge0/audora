#!/usr/bin/env bash
set -e

./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n dev.gitfudge.musicworkbench.debug/dev.gitfudge.musicworkbench.MainActivity

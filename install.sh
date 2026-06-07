#!/usr/bin/env bash
set -e

: "${ANDROID_HOME:=$HOME/Android/Sdk}"
export ANDROID_HOME

./gradlew :app:assembleRelease

OUT=app/build/outputs/apk/release
SIGNED="$OUT/app-release.apk"
UNSIGNED="$OUT/app-release-unsigned.apk"

if [ -f "$SIGNED" ]; then
    # Release signing config was present (signing env vars set).
    APK="$SIGNED"
else
    # No release keystore configured; sign the unsigned release APK with the
    # local Android debug keystore so it installs for on-device testing.
    BUILD_TOOLS=$(ls -d "$ANDROID_HOME"/build-tools/* | sort -V | tail -1)
    APK="$OUT/app-release-debugsigned.apk"

    "$BUILD_TOOLS/zipalign" -f -p 4 "$UNSIGNED" "$APK"
    "$BUILD_TOOLS/apksigner" sign \
        --ks "$HOME/.android/debug.keystore" \
        --ks-pass pass:android \
        --ks-key-alias androiddebugkey \
        --key-pass pass:android \
        "$APK"
fi

adb install -r "$APK"

#!/usr/bin/env bash
#MISE description="Install the required Android SDK platform support"
set -e

yes | sdkmanager "tools" "platform-tools" "platforms;android-36" "build-tools;36.0.0" "system-images;android-35;default;x86_64"
yes | sdkmanager --licenses

avdmanager create avd --name "Pixel9_15_default" --device "pixel_9" --package "system-images;android-35;default;x86_64" --force

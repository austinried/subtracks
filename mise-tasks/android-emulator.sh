#!/usr/bin/env bash
#MISE description="Install the required Android SDK platform support"
set -e

emulator -avd Pixel9_15_default -netdelay none -netspeed full -gpu on -memory 4096 -cores 4

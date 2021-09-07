#!/bin/bash
set -e

dir=$(dirname "$(readlink -f "$0")")
root=$(realpath "$dir/..")

FFMPEG_EXT_PATH="$root/submodules/ExoPlayer/extensions/ffmpeg/src/main/jni"
HOST_PLATFORM="linux-x86_64"
ENABLED_DECODERS=(alac)

ndkLocalPath="$root/ndk/android-ndk-r21e"
ndkDownloadZip="$root/ndk/ndk.zip"

if [ -z "$NDK_PATH" ] && [ ! -e "$ndkLocalPath" ]; then
    mkdir -p "$root/ndk"
    wget https://dl.google.com/android/repository/android-ndk-r21e-linux-x86_64.zip -O "$ndkDownloadZip"
    unzip -d "$root/ndk" "$ndkDownloadZip"
    rm "$ndkDownloadZip"
fi

if [ -z "$NDK_PATH" ] && [ -e "$ndkLocalPath" ]; then
    NDK_PATH="$root/ndk/android-ndk-r21e"
fi

cd "${FFMPEG_EXT_PATH}"
./build_ffmpeg.sh "${FFMPEG_EXT_PATH}" "${NDK_PATH}" "${HOST_PLATFORM}" "${ENABLED_DECODERS[@]}"
"${NDK_PATH}/ndk-build" APP_ABI="armeabi-v7a arm64-v8a x86 x86_64" -j4

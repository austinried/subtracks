# Building from source
Instructions are provided here for Linux, specifically Ubuntu 20.04, but other operating systems should be fine as long as the requirements are met.

## Requirements
### Base
- git
- wget
- unzip
- node.js (14.x)
- yarn (1.x)

### React Native
Subtracks is a React Native application, which means you need to make sure you are set up for that.  Follow the guide here to set up your development environment (React Native CLI, not Expo): 

https://reactnative.dev/docs/environment-setup

When setting up your environment, make sure to install the following dependency versions:
- Node 14
- Java JDK 11 (tested with openjdk-11-jdk)

### ExoPlayer
We also need the requirements to build ExoPlayer's ffmpeg extension for Android, which should be covered by:
- build-essential (make, etc.)

More information on building ffmpeg can be found here (this is what the `prepare` script does):

https://github.com/google/ExoPlayer/tree/r2.11.4/extensions/ffmpeg

## Building
Clone the repository, making sure to recurse submodules, and change into the directory:
```sh
git clone --recursive https://github.com/austinried/subtracks.git
cd subtracks
```

Install the yarn packages and prepare the ExoPlayer ffmpeg extension by running the yarn script:
```sh
yarn install
yarn prepare
```

### Debug
In a separate terminal start Metro, which will be an ongoing process that serves javascript bundles to the build:
```sh
yarn start
```

Run the android debug build script, which will start the build and also start your default emulator and push the build to it (or push the build to a running emulator/already connected device):
```sh
yarn android
```

### Release
Similar to the debug build, except we don't need to run Metro first:
```sh
yarn android:release
```

The generated APK will be written to `android/app/build/outputs/apk/release/app-release.apk`

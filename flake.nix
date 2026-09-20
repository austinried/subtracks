{
  description = "Subtracks - Kotlin/Jetpack Compose Android development shell";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/6b316287bae2ee04c9b93c8c858d930fd07d7338";
  };

  outputs =
    { nixpkgs, ... }:
    let
      systems = [ "x86_64-linux" ];
      forEachSystem = nixpkgs.lib.genAttrs systems;
    in
    {
      devShells = forEachSystem (
        system:
        let
          pkgs = import nixpkgs {
            inherit system;
            config = {
              android_sdk.accept_license = true;
              allowUnfree = true;
            };
          };
          android = pkgs.androidenv.composeAndroidPackages {
            platformVersions = [ "35" ];
            buildToolsVersions = [ "37.0.0" ];
            platformToolsVersion = "37.0.0";
            includeEmulator = false;
            includeSystemImages = false;
            includeNDK = false;
            includeSources = false;
            includeCmake = false;
          };
          androidSdk = android.androidsdk;
        in
        {
          default = pkgs.mkShell {
            packages = [
              pkgs.jdk21
              pkgs.gradle
              pkgs.git
              androidSdk
              android.platform-tools
            ];

            ANDROID_HOME = "${androidSdk}/libexec/android-sdk";
            ANDROID_SDK_ROOT = "${androidSdk}/libexec/android-sdk";

            shellHook = ''
              echo "subtracks dev shell"
              echo "  java:        $(java -version 2>&1 | head -n1)"
              echo "  ANDROID_HOME: $ANDROID_HOME"
            '';
          };
        }
      );
    };
}

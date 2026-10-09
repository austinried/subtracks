{
  description = "Subtracks - Kotlin/Jetpack Compose Android development shell";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    nixpkgs-stable.url = "github:NixOS/nixpkgs/nixos-26.05";
  };

  outputs =
    {
      nixpkgs,
      nixpkgs-stable,
      ...
    }:
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
          servers = import nixpkgs-stable { inherit system; };
          android = pkgs.androidenv.composeAndroidPackages {
            platformVersions = [ "37" ];
            buildToolsVersions = [ "37.0.0" ];
            platformToolsVersion = "37.0.1";
            includeEmulator = false;
            includeSystemImages = false;
            includeNDK = false;
            includeSources = false;
            includeCmake = false;
          };
          androidSdk = android.androidsdk;

          basePackages = [
            pkgs.jdk21
            pkgs.gradle_9
            pkgs.git
            pkgs.curl
            pkgs.unzip
            pkgs.nushell
            androidSdk
            android.platform-tools
          ];

          shellHook = ''
            echo "subtracks dev shell"
            echo "  java:        $(java -version 2>&1 | head -n1)"
            echo "  ANDROID_HOME: $ANDROID_HOME"
          '';
        in
        {
          default = pkgs.mkShell {
            packages = basePackages;

            ANDROID_HOME = "${androidSdk}/libexec/android-sdk";
            ANDROID_SDK_ROOT = "${androidSdk}/libexec/android-sdk";

            inherit shellHook;
          };

          integration = pkgs.mkShell {
            packages = basePackages ++ [
              servers.navidrome
              servers.gonic
              servers.lms
              servers.sqlite
              servers.php83
            ];

            ANDROID_HOME = "${androidSdk}/libexec/android-sdk";
            ANDROID_SDK_ROOT = "${androidSdk}/libexec/android-sdk";
            NEXTCLOUD_DIR = "${servers.nextcloud34}";
            NEXTCLOUD_MUSIC_APP = "${servers.nextcloud34Packages.apps.music}";

            inherit shellHook;
          };

          release = pkgs.mkShell {
            packages = [ pkgs.fastlane ];
          };
        }
      );
    };
}

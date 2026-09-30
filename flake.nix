{
  description = "Subtracks - Kotlin/Jetpack Compose Android development shell";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    # The integration shell runs real servers; pin them to a stable release. lms 3.80 (the
    # version in unstable) segfaults at startup with wt 4.14.3, so the server packages come from
    # here instead. The default shell never touches this input.
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

          # The integration harness starts real Subsonic servers, none of which the app build or
          # unit tests need. Kept out of the default shell so those stay free of the server deps.
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
        }
      );
    };
}

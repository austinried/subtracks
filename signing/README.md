# Verifying a subtracks download

subtracks ships through four channels:

- **GitHub releases** — the APK is signed with the release key. Every release
  attaches the public certificate `release-certificate.pem` and a `SHA256SUMS`.
- **Google Play** — uses Play App Signing, so Google re-signs the app it
  delivers. The release key is only the *upload* key there: it signs the AAB we
  send to Play, not the APK users install. The installed app verifies against
  Google's app signing certificate (Play Console → App integrity), and the upload
  key only proves the AAB we uploaded.
- **`nightly` builds** (the `subtracks-nightly` artifact from CI) — signed with
  the public debug key committed here as
  [`nightly-certificate.pem`](nightly-certificate.pem).
- **F-Droid** — re-signed by F-Droid with its own key; see F-Droid's published
  signing key.

## `nightly` builds

Certificate SHA-256:

```
DF:31:3F:18:58:E9:AD:F4:7F:44:65:B2:80:8F:DC:E5:B9:F8:8A:48:35:DC:D0:79:95:19:61:C3:05:05:08:37
```

To check a `nightly` APK:

```sh
apksigner verify --print-certs subtracks-nightly.apk
```

The `SHA-256 digest` line must equal the fingerprint above (the certificate is
committed at [`nightly-certificate.pem`](nightly-certificate.pem)).

## GitHub releases

Each release attaches the APK, the checksums and the certificate:

```sh
sha256sum -c SHA256SUMS
apksigner verify --print-certs subtracks.apk              # compare with the .pem
keytool -printcert -file release-certificate.pem          # prints its SHA-256
```

The signing certificate SHA-256 is also listed in the release notes.

## Google Play

The AAB we upload is signed with the release (upload) key, and that is what Play
checks; Google then re-signs the delivered APKs with its own app signing key. So
a Play-installed app is verified against Play's app signing certificate (shown in
Play Console → App integrity), not `release-certificate.pem`.

To check the AAB that was sent:

```sh
keytool -printcert -jarfile app-release.aab
```

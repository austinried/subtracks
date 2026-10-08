# Verifying a subtracks download

subtracks ships through four channels:

- **GitHub releases** — the APK and the AAB are signed with the release key. The public certificate is committed here as [`release-certificate.pem`](release-certificate.pem) and attached to each release, along with a `SHA256SUMS`.
- **Google Play** — uses Play App Signing, so Google re-signs the app it delivers. The release key is only the *upload* key there: it signs the AAB we send to Play, not the APK users install. The installed app is signed by Google's app signing key, and the upload key only proves the AAB we uploaded.
- **F-Droid** — re-signed by F-Droid with its own key; see [F-Droid's signing keys](https://f-droid.org/docs/Release_Channels_and_Signing_Keys/).
- **`nightly` builds** (the `subtracks-nightly` artifact from CI) — signed with the public debug key committed at [`app/debug.keystore`](../app/debug.keystore), whose certificate is [`nightly-certificate.pem`](nightly-certificate.pem).

## GitHub releases

The APK and the AAB are signed with the release key. Certificate SHA-256:

```
# keytool format
02:DA:2C:B7:B8:21:4E:53:BD:BF:B4:55:69:8D:CB:96:52:22:F9:1C:95:4B:52:10:AE:AC:96:FE:32:9C:8F:01
# apksigner format
02da2cb7b8214e53bdbfb455698dcb965222f91c954b5210aeac96fe329c8f01
```

Each release attaches the APK, the AAB, the checksums and the certificate (committed here as [`release-certificate.pem`](release-certificate.pem)):

```sh
sha256sum -c SHA256SUMS
apksigner verify --print-certs subtracks.apk              # compare with the .pem
keytool -printcert -jarfile subtracks.aab                 # the AAB
keytool -printcert -file release-certificate.pem          # prints its SHA-256
```

The `.aab` is the exact bundle uploaded to Google Play (install it by generating device APKs with [bundletool](https://github.com/google/bundletool)); the `.apk` is the universal APK for sideloading. The signing certificate SHA-256 is also listed in the release notes.

## Google Play

The AAB we upload is signed with the release (upload) key, and that is what Play checks; Google then re-signs the delivered APKs with its own app signing key. So a Play-installed app is signed by Google's key, not `release-certificate.pem`.

To check the AAB that was sent:

```sh
keytool -printcert -jarfile app-release.aab
```

## `nightly` builds

The signing key is the committed [`app/debug.keystore`](../app/debug.keystore) (a public key: alias `androiddebugkey`, password `android`). Certificate SHA-256:

```
# keytool format
DF:31:3F:18:58:E9:AD:F4:7F:44:65:B2:80:8F:DC:E5:B9:F8:8A:48:35:DC:D0:79:95:19:61:C3:05:05:08:37
# apksigner format
df313f1858e9adf47f4465b2808fdce5b9f88a4835dcd079951961c305050837
```

To check a `nightly` APK:

```sh
apksigner verify --print-certs subtracks-nightly.apk
```

The `SHA-256 digest` line must equal the fingerprint above (the certificate is committed at [`nightly-certificate.pem`](nightly-certificate.pem)).

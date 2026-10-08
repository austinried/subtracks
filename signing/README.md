# Verifying a subtracks download

subtracks ships through three channels, each with its own signer:

- **GitHub releases and Google Play** — signed with the release key. Every GitHub
  release attaches `subtracks.apk`, a `SHA256SUMS` file and the public certificate
  `release-certificate.pem`.
- **`next` builds** (the `subtracks-next` artifact from CI) — signed with the
  public debug key committed here as [`next-certificate.pem`](next-certificate.pem).
- **F-Droid** — re-signed by F-Droid with its own key; see F-Droid's published
  signing key.

## `next` builds

Certificate SHA-256:

```
DF:31:3F:18:58:E9:AD:F4:7F:44:65:B2:80:8F:DC:E5:B9:F8:8A:48:35:DC:D0:79:95:19:61:C3:05:05:08:37
```

To check a `next` APK:

```sh
apksigner verify --print-certs subtracks-next.apk
```

The `SHA-256 digest` line must equal the fingerprint above (the certificate is
committed at [`next-certificate.pem`](next-certificate.pem)).

## GitHub releases

Each release attaches the APK, the checksums and the certificate:

```sh
sha256sum -c SHA256SUMS
apksigner verify --print-certs subtracks.apk              # compare with the .pem
keytool -printcert -file release-certificate.pem          # prints its SHA-256
```

The signing certificate SHA-256 is also listed in the release notes.

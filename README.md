# Product Android wrapper

Thin Android wrapper around a product PWA, using the Herald notification client.
Its app configuration and assets identify the installed product; reusable
registration, delivery and subscription bridge behavior comes from pinned core.

## Build

Requires JDK 21 and Android SDK platform 37.2/build-tools 37.0.0.

```sh
./gradlew :app:assembleDebug :app:assembleRelease :app:lint
```

The versioned library ZIP is committed under `vendor/`; Gradle extracts it into
a local Maven repository before building. Dependency metadata handles public
Android/Firebase dependencies. No private source checkout is required.

`gradle.properties` records application ID, app/core versions and expected
signing certificate. `app/src/main/assets/herald.json` supplies broker/application
and PWA origin settings. Firebase client configuration is public app configuration;
server service-account credentials and APK signing keys remain private.

## Releases

CI builds debug and minified release APKs on pushes and pull requests. A manual
release takes explicit version name/code, signs with repository secrets and
checks the expected application ID/certificate before publishing checksums and
APK. Version codes must increase beyond every previous release of this app.

Configure signing secrets through repository settings and preserve an independent
keystore backup. APK publication stays disabled until the operator sets
`ANDROID_ROLLOUT_ENABLED=true` after physical-device verification.
Set `herald.signingCertificate` to the existing certificate for
an app already installed by users. App ID, certificate and Digital Asset Links
must stay compatible for in-place upgrades and verified TWA/bridge behavior.

Source tag and vendored library provenance identify release source. Checksums
verify downloaded bytes; independently building verifies the source pipeline.
A locally signed debug APK cannot replace an installation signed by release key.

## Device verification

Before release, upgrade an existing installation in place; check saved
subscriptions, notification permission, actual delivery, tap routing and PWA
subscription changes. Save source/dependency versions, APK/certificate checksums
and a redacted verification report. Packaging checks alone do not cover these
real-device flows.

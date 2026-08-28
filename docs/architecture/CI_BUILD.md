# TrailCharter CI / Build Foundation

Status: **AGREE**

## Platform
- GitHub Actions on `ubuntu-latest`.
- JDK 17 using Eclipse Temurin.
- Project build through the committed Gradle Wrapper.
- Gradle wrapper validation is performed by `gradle/actions/setup-gradle`.

## Triggers
- Pull requests that change Android/build files.
- Pushes to `main` that change Android/build files.
- Manual `workflow_dispatch` when required.
- Documentation-only changes do not consume Android CI minutes by default.

## Required build gate
CI must fail if any of the following fail:
1. Debug unit tests: `testDebugUnitTest`
2. Android lint: `lintDebug`
3. Debug assembly: `assembleDebug`

The workflow runs these through one Gradle invocation:

`./gradlew testDebugUnitTest lintDebug assembleDebug --stacktrace --no-daemon`

## Artifacts
- Successful runs upload the debug APK.
- Debug APK artifact names include the TrailCharter identity and GitHub run number.
- Debug APK retention is 14 days.
- Failure reports/logs are uploaded where available and retained for 7 days.

## Development signing continuity
- Authoritative APKs produced from `main` use one persistent **development-only** signing identity so later development APKs can install as updates without deleting local TrailCharter data.
- The private PKCS12 keystore is **not committed to the public repository**. It is held as GitHub Actions secrets.
- The Gradle debug signing configuration reads its keystore path and credentials only from CI environment variables.
- The workflow reconstructs the keystore only inside the ephemeral GitHub runner.
- Before an authoritative APK is uploaded, CI verifies its signing-certificate SHA-256 against the committed expected public fingerprint.
- A push to `main` fails if continuity-signing secrets are missing. This prevents GitHub from quietly publishing another disposable-signature APK.
- Pull requests from forks do not receive repository secrets. They may still run tests/lint/build with Android's normal disposable debug signer, but such APKs are not authoritative TrailCharter test builds.
- The continuity key is **not a production/release signing identity** and must never be reused for Google Play or another production distribution channel.

Expected continuity debug certificate SHA-256:

`B0:C7:53:F9:B4:6C:1C:A2:5A:0A:E5:46:E4:AE:FE:81:8A:95:DC:83:F9:5B:E7:4B:CC:D3:CF:17:E9:47:D2:6C`

Required GitHub Actions secrets:
- `TRAILCHARTER_CI_KEYSTORE_B64`
- `TRAILCHARTER_CI_KEYSTORE_PASSWORD`

The alias `trailcharter-ci-debug` is deliberately non-secret and fixed by the workflow.

## Release signing separation
- Release signing remains a separate concern and must not reuse development/debug credentials as a production identity.
- No automatic Google Play publishing is included at this stage.
- Workflow permissions remain read-only unless a future task has an explicit need for additional access.

## Reliability principles
- The Gradle Wrapper is authoritative for the project Gradle version.
- Dependency caching is handled by the official Gradle setup action.
- Concurrent superseded runs on the same ref are cancelled to avoid wasting CI time.
- CI configuration should remain understandable and minimal; additional jobs are added only when they provide meaningful assurance.
- Repeated failures should be solved at root cause rather than by incremental trial-and-error workarounds.

## Current implementation state
The Android CI workflow is operational at `.github/workflows/android-ci.yml`. Persistent development signing is introduced before TrailCharter begins storing meaningful Adventure data so physical-device testing can preserve that data across normal APK upgrades.

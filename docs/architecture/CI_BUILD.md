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

The initial workflow runs these through one Gradle invocation:

`./gradlew testDebugUnitTest lintDebug assembleDebug --stacktrace --no-daemon`

## Artifacts
- Successful runs upload the debug APK.
- Debug APK artifact names include the TrailCharter identity and GitHub run number.
- Debug APK retention is initially 14 days to control repository storage use.
- Failure reports/logs are uploaded where available and retained for 7 days.

## Security / signing
- Normal CI/debug builds require no release signing secrets.
- Release signing is a separate concern and must not reuse development/debug credentials as a production identity.
- No automatic Google Play publishing is included at this stage.
- Workflow permissions are read-only unless a future task has an explicit need for additional access.

## Reliability principles
- The Gradle Wrapper is authoritative for the project Gradle version.
- Dependency caching is handled by the official Gradle setup action.
- Concurrent superseded runs on the same ref are cancelled to avoid wasting CI time.
- CI configuration should remain understandable and minimal; additional jobs are added only when they provide meaningful assurance.

## Current implementation state
The workflow exists at `.github/workflows/android-ci.yml` but is intentionally path-gated. The repository does not yet contain the Android scaffold, so adding the workflow alone does not trigger a meaningless Android build. The first Android scaffold commit will satisfy the path filters and provide the first real CI verification run.

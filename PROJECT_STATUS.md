# Altrove Project Status

## Phase
**Product Design + Foundation Development**

## Implementation
Android implementation is now authorised as a controlled foundation phase.

No production Android application has been implemented yet.

## Current state
- Project history consolidated.
- Constitution established.
- Vision established.
- Decisions classified into EXPLORE / AGREE / FINAL.
- Seasonal icon system locked as FINAL.
- GitHub repository exists at `Masamunr/altrove`.
- GitHub connector access was successfully restored and write-tested on 2026-08-27.
- The portable baseline documentation has been reconciled into the repository; no newer repository project material existed beyond the original README and `.gitignore`.
- GitHub is now the authoritative source of truth for Altrove project documentation.
- Brand specification is preserved at `docs/brand/BRAND_IDENTITY.md`.
- Asset gap: the supplied baseline ZIP references `docs/brand/altrove_seasonal_icon_system_FINAL.jpg`, but the image binary was not actually included in that ZIP. The exact approved visual reference must still be preserved in the repository.
- On 2026-08-27 the previous implementation gate was reopened: controlled Android foundation development may now proceed while product and architecture design continue.

## Implementation guardrails
- Do not prematurely hard-code unresolved EXPLORE decisions.
- Keep early architecture modular and replaceable where major technical choices remain open.
- Prioritise privacy-first, offline-first and local-first foundations.
- Do not introduce accounts, ads, analytics, telemetry or silent uploads.
- Treat mapping/routing architecture as an investigation boundary until the Organic Maps/OpenStreetMap approach is properly resolved.

## Next priorities
1. Preserve the exact FINAL launcher icon reference asset in GitHub.
2. Define the Android foundation: package/application identity, SDK/toolchain, Compose structure, local persistence boundary, privacy/network rules and CI/build pipeline.
3. Continue product identity beyond the launcher icon: wordmark, typography, in-app visual design language and seasonal theme application.
4. Define the Adventure information model.
5. Define the main UX/navigation model.
6. Investigate map/offline architecture.

## Implementation gate
**OPEN — AGREE**

Altrove may now enter iterative foundation development alongside continued product and architecture design. Unresolved EXPLORE decisions remain open and must not be treated as settled merely because implementation has begun.

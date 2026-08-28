# Launcher branding implementation

The Android launcher icon is generated at build time from `ic_launcher.webp.b64`, which contains the approved no-shield TrailCharter landscape artwork. The source was restored directly from the repository's original launcher-artwork Git blob after a later transfer copy became corrupted. The build produces legacy, round and adaptive launcher resources without redrawing or reinterpreting the approved composition.

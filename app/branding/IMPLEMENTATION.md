# Launcher branding implementation

The Android launcher icon is generated at build time from `ic_launcher.webp`, a binary WebP taken directly from the approved TrailCharter launcher reference supplied for this implementation. Only crop/scale needed to isolate the approved no-shield landscape from the reference board is permitted; the mountains, trees, river and sun composition is not redrawn or reinterpreted.

The build validates the binary source as a complete WebP and checks its SHA-256 before producing the regular, round and adaptive launcher resources. This deliberately avoids the earlier Base64 text-transfer path that corrupted the artwork source.

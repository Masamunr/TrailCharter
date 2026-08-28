# Launcher branding implementation

TrailCharter uses the approved no-shield mountains / trees / river / sun composition unchanged as the launcher artwork source.

Android adaptive icons use a 108 dp layer with an inner safe visual region. The first implementation placed the complete approved artwork across the whole adaptive layer, which caused the launcher mask to crop the composition and made it appear oversized. The next correction inset the approved artwork but mistakenly retained a second full-size copy as the background layer, producing visible duplicate sun, mountain and tree fragments around the inset artwork.

The current implementation uses one copy of the approved composition in the adaptive foreground, inset for the launcher mask, with the approved pale neutral brand background (#F2ECE2) behind it. No second copy of the landscape is present and the artwork itself is not redrawn or reinterpreted.

The build validates the approved WebP bytes before generating launcher resources.

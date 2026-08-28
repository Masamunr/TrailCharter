# Launcher branding implementation

TrailCharter's landscape composition remains FINAL: the approved mountains / trees / river / sun scene must not be redrawn or reinterpreted.

Physical-device testing showed that treating a finished square launcher image as if it were a full 108 dp adaptive foreground allowed Android's launcher mask to crop into the important artwork. Repeated visual insets were therefore the wrong abstraction.

The current implementation follows Android's adaptive-icon geometry directly. Android defines a centred 66 x 66 dp safe zone inside the 108 x 108 dp adaptive-icon canvas that is not clipped by OEM launcher masks. The approved shield launcher artwork is constrained to a centred 66 x 66 dp foreground item, while the adaptive background is the plain TrailCharter dark green (#1F3D2E). The shield itself occupies only part of that 66 dp item, so the mountains / trees / river / sun composition sits comfortably inside the never-clipped region.

This is a geometry constraint rather than another device-specific visual tweak: circle, square and rounded-square launcher masks may crop the surrounding green field, but not the shield composition.

The build validates the approved shield WebP structure and SHA-256 before generating launcher resources.

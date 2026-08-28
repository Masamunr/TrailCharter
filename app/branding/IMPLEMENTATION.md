# Launcher branding implementation

TrailCharter's landscape composition remains FINAL: the approved mountains / trees / river / sun scene must not be redrawn or reinterpreted.

Physical-device testing showed that using that complete scene edge-to-edge as the Android adaptive icon caused the launcher mask to crop into the composition. The first inset correction then exposed duplicate artwork around the edges because a second copy of the scene remained in the adaptive background.

For version 0.1.4-foundation, the launcher-container decision is temporarily reopened for an EXPLORE physical-device test using the previously approved shield treatment. The shield artwork is derived directly from the supplied approved shield launcher reference. The shield is centred on the brand dark-green field (#1F3D2E), with enough surrounding field for Android to apply circle, square and rounded-square masks without cropping the landscape inside the shield.

The adaptive foreground contains one shield composition only. The adaptive background is plain #1F3D2E. No second landscape copy is present.

The build validates the shield WebP structure and SHA-256 before generating regular, round and adaptive launcher resources.

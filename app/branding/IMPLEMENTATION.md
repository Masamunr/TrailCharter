# Launcher branding implementation

TrailCharter uses the approved no-shield mountains / trees / river / sun composition unchanged as the launcher artwork source.

Android adaptive icons use a 108 dp layer but normally expose only the inner 72 dp through the launcher mask; the outer 18 dp on each side is reserved for masking and motion effects. The original implementation placed the full approved artwork across the whole 108 dp layer, which made Android crop and visually enlarge the composition.

The corrected adaptive implementation keeps the original artwork as full-bleed background for launcher effects and overlays the same unchanged artwork inset by 18 dp on each side. The normal masked viewport therefore shows the complete approved composition at the intended scale, while the outer region remains safely filled during adaptive-icon effects.

The build validates the approved WebP bytes before generating launcher resources.

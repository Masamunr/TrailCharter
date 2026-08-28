package com.masamunr.trailcharter.map

import androidx.compose.runtime.Composable
import org.maplibre.compose.map.MaplibreMap

/**
 * Compile-only renderer probe for the architecture spike.
 *
 * It is intentionally not reachable from production UI yet. The next spike step will provide
 * an explicit local style/package instead of MapLibre's network-backed default style.
 */
@Composable
internal fun MapLibreRendererCompileProbe() {
    MaplibreMap()
}

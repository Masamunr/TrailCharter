package com.masamunr.trailcharter.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle

/**
 * Renderer proof for the architecture spike.
 *
 * The map reads a tiny PMTiles fixture from TrailCharter's own app-managed files. It is
 * intentionally not reachable from the production UI yet and has no network-backed style/source.
 */
@Composable
internal fun MapLibreRendererCompileProbe() {
    val context = LocalContext.current
    val localStyle = remember(context) {
        val pmTilesFile = ensureLocalPmTilesProbe(context)
        localPmTilesStyle(pmTilesFile)
    }

    MaplibreMap(baseStyle = BaseStyle.Json(localStyle))
}

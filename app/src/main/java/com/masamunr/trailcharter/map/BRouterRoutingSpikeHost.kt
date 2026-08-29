package com.masamunr.trailcharter.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.maplibre.android.maps.MapLibreMap

/**
 * SPIKE ONLY bridge between the already physically accepted Pass 3 screen and the BRouter test UI.
 *
 * Keeping this in a separate file means the accepted cartography renderer does not need another
 * invasive edit simply to compare routing engines. The renderer explicitly hands over its ready
 * map so package import duration cannot make the routing controls time out. A production-selected
 * engine would replace this with an explicit map-controller/state boundary.
 */
@Composable
internal fun BRouterRoutingSpikeHost(map: MapLibreMap?) {
    val resolvedMap = map ?: return

    LaunchedEffect(resolvedMap) {
        applyPass4PathVisualHierarchy(resolvedMap)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BRouterRoutingSpikeOverlay(
            map = resolvedMap,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 12.dp, top = 118.dp),
        )
    }
}

package com.masamunr.trailcharter.map

import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

/**
 * SPIKE ONLY bridge between the already physically accepted Pass 3 screen and the BRouter test UI.
 *
 * Keeping this in a separate file means the accepted cartography renderer does not need another
 * invasive edit simply to compare routing engines. A production-selected engine would replace this
 * with an explicit map-controller/state boundary rather than view-tree discovery.
 */
@Composable
internal fun BRouterRoutingSpikeHost() {
    val composeView = LocalView.current
    var map by remember { mutableStateOf<MapLibreMap?>(null) }

    LaunchedEffect(composeView) {
        repeat(80) {
            val mapView = findMapView(composeView.rootView)
            if (mapView != null) {
                mapView.getMapAsync { resolved -> map = resolved }
                return@LaunchedEffect
            }
            delay(100L)
        }
    }

    val resolvedMap = map ?: return
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

private fun findMapView(view: View): MapView? {
    if (view is MapView) return view
    if (view !is ViewGroup) return null
    for (index in 0 until view.childCount) {
        findMapView(view.getChildAt(index))?.let { return it }
    }
    return null
}

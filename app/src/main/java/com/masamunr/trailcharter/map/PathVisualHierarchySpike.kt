package com.masamunr.trailcharter.map

import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineDasharray
import org.maplibre.android.style.layers.PropertyFactory.lineOpacity
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.layers.PropertyFactory.textColor
import org.maplibre.android.style.layers.PropertyFactory.textHaloColor

internal const val PASS4_ORDINARY_PATH_COLOR = "#8B79B5"
internal const val PASS4_ESTABLISHED_ROUTE_COLOR = "#6A3FD2"
internal const val PASS4_ORDINARY_PATH_LABEL_COLOR = "#6C608B"
internal const val PASS4_ESTABLISHED_ROUTE_LABEL_COLOR = "#5730A5"
internal const val PASS4_LABEL_HALO_COLOR = "#F3EFE6"
internal const val PASS4_ESTABLISHED_ROUTE_LAYER_ID = "hiking-route-relation-lines"

/**
 * SPIKE ONLY visual hierarchy applied after the offline Pass 4 style is ready.
 *
 * This deliberately changes prominence rather than existence: isolated or short OSM path geometry
 * remains visible. Recognised OSM hiking/foot route relations receive a stronger treatment, while
 * ordinary paths/tracks stay in the same violet family at lower prominence.
 */
internal fun applyPass4PathVisualHierarchy(map: MapLibreMap) {
    val style = map.style ?: return

    style.getLayer("tracks")?.setProperties(
        lineColor(PASS4_ORDINARY_PATH_COLOR),
        lineOpacity(0.72f),
    )
    style.getLayer("paths")?.setProperties(
        lineColor(PASS4_ORDINARY_PATH_COLOR),
        lineOpacity(0.64f),
    )
    style.getLayer("named-walking-path-labels")?.setProperties(
        textColor(PASS4_ORDINARY_PATH_LABEL_COLOR),
        textHaloColor(PASS4_LABEL_HALO_COLOR),
    )

    if (
        style.getSource("hikingRoutes") != null &&
        style.getLayer(PASS4_ESTABLISHED_ROUTE_LAYER_ID) == null
    ) {
        val establishedRouteLayer = LineLayer(
            PASS4_ESTABLISHED_ROUTE_LAYER_ID,
            "hikingRoutes",
        ).withProperties(
            lineColor(PASS4_ESTABLISHED_ROUTE_COLOR),
            lineWidth(2.6f),
            lineDasharray(arrayOf(4.0f, 2.0f)),
            lineOpacity(0.92f),
        )
        establishedRouteLayer.minZoom = 13.0f

        if (style.getLayer("hiking-route-relation-labels") != null) {
            style.addLayerBelow(establishedRouteLayer, "hiking-route-relation-labels")
        } else {
            style.addLayer(establishedRouteLayer)
        }
    }

    style.getLayer("hiking-route-relation-labels")?.setProperties(
        textColor(PASS4_ESTABLISHED_ROUTE_LABEL_COLOR),
        textHaloColor(PASS4_LABEL_HALO_COLOR),
    )
}

package com.masamunr.trailcharter.map

import androidx.compose.runtime.Composable
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle

/**
 * Compile-only renderer probe for the architecture spike.
 *
 * The style and GeoJSON are completely inline, so this proof has no tile/style/network dependency.
 * It is intentionally not reachable from the production UI yet. A later spike step will replace
 * the inline geometry with an app-managed local PMTiles package and terrain data.
 */
@Composable
internal fun MapLibreRendererCompileProbe() {
    MaplibreMap(baseStyle = BaseStyle.Json(localProbeStyle))
}

private val localProbeStyle = """
    {
      "version": 8,
      "name": "TrailCharter local renderer probe",
      "center": [-2.747, 52.709],
      "zoom": 13,
      "sources": {
        "probe-route": {
          "type": "geojson",
          "data": {
            "type": "FeatureCollection",
            "features": [
              {
                "type": "Feature",
                "properties": {},
                "geometry": {
                  "type": "LineString",
                  "coordinates": [
                    [-2.754, 52.707],
                    [-2.749, 52.710],
                    [-2.743, 52.711]
                  ]
                }
              }
            ]
          }
        }
      },
      "layers": [
        {
          "id": "background",
          "type": "background",
          "paint": { "background-color": "#1F3D2E" }
        },
        {
          "id": "probe-route-line",
          "type": "line",
          "source": "probe-route",
          "paint": {
            "line-color": "#F4E7C5",
            "line-width": 5
          }
        }
      ]
    }
""".trimIndent()

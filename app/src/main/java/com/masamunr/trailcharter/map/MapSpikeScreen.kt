package com.masamunr.trailcharter.map

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle

/**
 * Physical-device renderer diagnostic used only on the draft spike branch.
 *
 * No native map surface is created until the user explicitly starts one stage. A synchronous
 * marker is written before each stage starts, so a hard native crash can be identified on the next
 * launch even when MapLibre never reaches an error callback.
 */
@Composable
internal fun MapSpikeScreen() {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences("trailcharter_map_spike_diagnostic", Context.MODE_PRIVATE)
    }
    var activeStage by remember { mutableStateOf<DiagnosticStage?>(null) }
    var lastAttempt by remember { mutableStateOf(prefs.getString("last_attempt", null)) }
    var lastResult by remember { mutableStateOf(prefs.getString("last_result", null)) }
    var status by remember { mutableStateOf("Ready") }

    fun start(stage: DiagnosticStage) {
        prefs.edit()
            .putString("last_attempt", stage.name)
            .putString("last_result", "STARTED")
            .commit()
        lastAttempt = stage.name
        lastResult = "STARTED"
        status = "Running ${stage.label}…"
        activeStage = stage
    }

    val stage = activeStage
    if (stage == null) {
        DiagnosticMenu(
            lastAttempt = lastAttempt,
            lastResult = lastResult,
            onStart = ::start,
        )
        return
    }

    val style = remember(stage, context) {
        when (stage) {
            DiagnosticStage.ENGINE_ONLY -> engineOnlyStyle
            DiagnosticStage.INLINE_VECTOR -> inlineVectorStyle
            DiagnosticStage.PMTILES -> localPmTilesStyle(ensureLocalPmTilesProbe(context))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            baseStyle = BaseStyle.Json(style),
            onMapLoadFinished = {
                prefs.edit()
                    .putString("last_attempt", stage.name)
                    .putString("last_result", "PASSED")
                    .commit()
                lastAttempt = stage.name
                lastResult = "PASSED"
                status = "PASS: ${stage.label}. Close and reopen the spike for the next test."
            },
            onMapLoadFailed = { reason ->
                val result = "FAILED${reason?.let { ": $it" }.orEmpty()}"
                prefs.edit()
                    .putString("last_attempt", stage.name)
                    .putString("last_result", result)
                    .commit()
                lastAttempt = stage.name
                lastResult = result
                status = "Map callback failure: ${reason ?: "unknown reason"}"
            },
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(12.dp),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 4.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "TrailCharter map diagnostic",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(text = status, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun DiagnosticMenu(
    lastAttempt: String?,
    lastResult: String?,
    onStart: (DiagnosticStage) -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("TrailCharter map diagnostic", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Run these in order. If a stage crashes the app, reopen it and read the previous-attempt result before doing anything else.",
                style = MaterialTheme.typography.bodyMedium,
            )

            if (lastAttempt != null) {
                val readableStage = DiagnosticStage.entries
                    .firstOrNull { it.name == lastAttempt }
                    ?.label
                    ?: lastAttempt
                val readableResult = when (lastResult) {
                    "STARTED" -> "STARTED but never reported success. A hard crash during this stage is likely."
                    "PASSED" -> "PASSED"
                    null -> "No result recorded"
                    else -> lastResult
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 2.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("Previous attempt", style = MaterialTheme.typography.titleSmall)
                        Text(readableStage, style = MaterialTheme.typography.bodyMedium)
                        Text(readableResult, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            DiagnosticStage.entries.forEachIndexed { index, stage ->
                Button(
                    onClick = { onStart(stage) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("${index + 1}. ${stage.label}")
                }
            }

            Text(
                "Stage 1 creates only the native map surface and a background. Stage 2 adds inline GeoJSON. Stage 3 is the only test that touches the synthetic PMTiles archive.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private enum class DiagnosticStage(val label: String) {
    ENGINE_ONLY("Map engine only"),
    INLINE_VECTOR("Inline vector route"),
    PMTILES("Local PMTiles route"),
}

private val engineOnlyStyle = """
    {
      "version": 8,
      "name": "TrailCharter engine-only diagnostic",
      "sources": {},
      "layers": [
        {
          "id": "background",
          "type": "background",
          "paint": { "background-color": "#1F3D2E" }
        }
      ]
    }
""".trimIndent()

private val inlineVectorStyle = """
    {
      "version": 8,
      "name": "TrailCharter inline-vector diagnostic",
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

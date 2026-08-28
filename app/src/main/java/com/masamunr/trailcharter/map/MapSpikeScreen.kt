package com.masamunr.trailcharter.map

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import android.os.Build
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView

/**
 * Physical-device native renderer diagnostic used only on the draft spike branch.
 *
 * This deliberately bypasses MapLibre Compose for map-surface tests. The first stage initialises
 * only the MapLibre singleton and creates no MapView. The remaining stages create a native MapView
 * through SurfaceView and TextureView respectively. A synchronous marker is written before each
 * stage so a hard native crash remains identifiable on the next launch.
 */
@Composable
internal fun MapSpikeScreen() {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences("trailcharter_map_spike_diagnostic", Context.MODE_PRIVATE)
    }
    val previousExit = remember(context) { latestExitSummary(context) }
    var activeStage by remember { mutableStateOf<DiagnosticStage?>(null) }
    var lastAttempt by remember { mutableStateOf(prefs.getString("last_attempt", null)) }
    var lastResult by remember { mutableStateOf(prefs.getString("last_result", null)) }
    var status by remember { mutableStateOf("Ready") }

    fun record(stage: DiagnosticStage, result: String) {
        prefs.edit()
            .putString("last_attempt", stage.name)
            .putString("last_result", result)
            .commit()
        lastAttempt = stage.name
        lastResult = result
    }

    fun start(stage: DiagnosticStage) {
        record(stage, "STARTED")
        status = "Running ${stage.label}…"

        if (stage == DiagnosticStage.LIBRARY_INIT_ONLY) {
            try {
                MapLibre.getInstance(context.applicationContext)
                record(stage, "PASSED")
                status = "PASS: ${stage.label}. No MapView was created."
            } catch (error: Throwable) {
                record(stage, "FAILED: ${error::class.java.simpleName}: ${error.message.orEmpty()}")
                status = "FAILED: ${error::class.java.simpleName}: ${error.message.orEmpty()}"
            }
        } else {
            activeStage = stage
        }
    }

    val stage = activeStage
    if (stage == null) {
        DiagnosticMenu(
            lastAttempt = lastAttempt,
            lastResult = lastResult,
            previousExit = previousExit,
            onStart = ::start,
        )
        return
    }

    NativeMapStage(
        stage = stage,
        onPassed = {
            record(stage, "PASSED")
            status = "PASS: ${stage.label}. Close and reopen for the next test."
        },
        onFailed = { reason ->
            record(stage, "FAILED: $reason")
            status = "Map callback failure: $reason"
        },
        status = status,
    )
}

@Composable
private fun NativeMapStage(
    stage: DiagnosticStage,
    onPassed: () -> Unit,
    onFailed: (String) -> Unit,
    status: String,
) {
    val context = LocalContext.current
    val textureMode = stage == DiagnosticStage.DIRECT_TEXTURE_MAP
    val mapView = remember(stage, context) {
        MapLibre.getInstance(context.applicationContext)
        MapView(
            context,
            MapLibreMapOptions.createFromAttributes(context)
                .textureMode(textureMode),
        ).apply {
            onCreate(null)
        }
    }

    DisposableEffect(mapView) {
        mapView.onStart()
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    LaunchedEffect(mapView) {
        mapView.getMapAsync { map ->
            map.setStyle(engineOnlyStyle) {
                onPassed()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
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
                Text("TrailCharter native map diagnostic", style = MaterialTheme.typography.titleSmall)
                Text(status, style = MaterialTheme.typography.bodySmall)
                Text(
                    if (textureMode) "Direct MapLibre TextureView" else "Direct MapLibre SurfaceView",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun DiagnosticMenu(
    lastAttempt: String?,
    lastResult: String?,
    previousExit: String?,
    onStart: (DiagnosticStage) -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("TrailCharter native map diagnostic", style = MaterialTheme.typography.headlineSmall)
            Text(
                "This isolates library loading, SurfaceView rendering and TextureView rendering. Run in order and reopen after any crash.",
                style = MaterialTheme.typography.bodyMedium,
            )

            if (previousExit != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 2.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("Android previous-process record", style = MaterialTheme.typography.titleSmall)
                        Text(previousExit, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            if (lastAttempt != null) {
                val readableStage = DiagnosticStage.entries
                    .firstOrNull { it.name == lastAttempt }
                    ?.label
                    ?: lastAttempt
                val readableResult = when (lastResult) {
                    "STARTED" -> "STARTED but never returned. Hard process death during this stage is likely."
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
                "Stage 1 creates no map surface. Stages 2 and 3 bypass MapLibre Compose and use the native Android MapView directly. No PMTiles or network data is used.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private enum class DiagnosticStage(val label: String) {
    LIBRARY_INIT_ONLY("Library init only"),
    DIRECT_SURFACE_MAP("Direct SurfaceView map"),
    DIRECT_TEXTURE_MAP("Direct TextureView map"),
}

private fun latestExitSummary(context: Context): String? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
    val activityManager = context.getSystemService(ActivityManager::class.java)
    val exit = activityManager
        .getHistoricalProcessExitReasons(context.packageName, 0, 5)
        .firstOrNull()
        ?: return null

    val reason = when (exit.reason) {
        ApplicationExitInfo.REASON_CRASH_NATIVE -> "NATIVE CRASH"
        ApplicationExitInfo.REASON_CRASH -> "JAVA CRASH"
        ApplicationExitInfo.REASON_ANR -> "ANR"
        ApplicationExitInfo.REASON_LOW_MEMORY -> "LOW MEMORY"
        ApplicationExitInfo.REASON_SIGNALED -> "SIGNALED"
        ApplicationExitInfo.REASON_INITIALIZATION_FAILURE -> "INITIALISATION FAILURE"
        ApplicationExitInfo.REASON_USER_REQUESTED -> "USER REQUESTED"
        else -> "reason=${exit.reason}"
    }
    val description = exit.description?.takeIf { it.isNotBlank() }
    return buildString {
        append(reason)
        append("; status/signal=")
        append(exit.status)
        if (description != null) {
            append("; ")
            append(description)
        }
    }
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

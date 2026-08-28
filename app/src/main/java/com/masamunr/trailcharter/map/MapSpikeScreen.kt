package com.masamunr.trailcharter.map

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import android.os.Build
import android.util.Log
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView

/**
 * Physical-device native renderer diagnostic used only on the draft spike branch.
 *
 * This bypasses MapLibre Compose for map-surface tests while deliberately mirroring the lifecycle
 * ordering used by MapLibre Compose itself: the Android view is created first, then a Lifecycle
 * observer forwards ON_CREATE/START/RESUME and their matching teardown events to the MapView.
 * A process-wide uncaught-exception handler persists any Java stack trace before delegating to
 * Android's normal crash handler.
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
    var persistedJavaCrash by remember { mutableStateOf(prefs.getString("last_java_stack", null)) }
    var status by remember { mutableStateOf("Ready") }

    DisposableEffect(prefs) {
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        val diagnosticHandler = Thread.UncaughtExceptionHandler { thread, throwable ->
            runCatching {
                prefs.edit()
                    .putString(
                        "last_java_stack",
                        "Thread: ${thread.name}\n${Log.getStackTraceString(throwable)}",
                    )
                    .commit()
            }
            previousHandler?.uncaughtException(thread, throwable)
        }
        Thread.setDefaultUncaughtExceptionHandler(diagnosticHandler)
        onDispose {
            if (Thread.getDefaultUncaughtExceptionHandler() === diagnosticHandler) {
                Thread.setDefaultUncaughtExceptionHandler(previousHandler)
            }
        }
    }

    fun record(stage: DiagnosticStage, result: String) {
        prefs.edit()
            .putString("last_attempt", stage.name)
            .putString("last_result", result)
            .commit()
        lastAttempt = stage.name
        lastResult = result
    }

    fun start(stage: DiagnosticStage) {
        prefs.edit().remove("last_java_stack").commit()
        persistedJavaCrash = null
        record(stage, "STARTED")
        status = "Running ${stage.label}…"

        if (stage == DiagnosticStage.LIBRARY_INIT_ONLY) {
            try {
                MapLibre.getInstance(context.applicationContext)
                record(stage, "PASSED")
                status = "PASS: ${stage.label}. No MapView was created."
            } catch (error: Throwable) {
                val failure = "FAILED: ${error::class.java.simpleName}: ${error.message.orEmpty()}"
                record(stage, failure)
                status = failure
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
            persistedJavaCrash = persistedJavaCrash,
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
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnPassed by rememberUpdatedState(onPassed)
    val currentOnFailed by rememberUpdatedState(onFailed)
    val textureMode = stage == DiagnosticStage.DIRECT_TEXTURE_MAP
    var currentMapView by remember(stage) { mutableStateOf<MapView?>(null) }

    DisposableEffect(lifecycle, currentMapView) {
        val mapView = currentMapView
        if (mapView == null) {
            return@DisposableEffect onDispose { }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                Lifecycle.Event.ON_ANY -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                MapLibre.getInstance(viewContext.applicationContext)
                MapView(
                    viewContext,
                    MapLibreMapOptions.createFromAttributes(viewContext)
                        .textureMode(textureMode),
                ).also { mapView ->
                    currentMapView = mapView
                    mapView.getMapAsync { map ->
                        runCatching {
                            map.setStyle(engineOnlyStyle) {
                                currentOnPassed()
                            }
                        }.onFailure { error ->
                            currentOnFailed(
                                "${error::class.java.simpleName}: ${error.message.orEmpty()}",
                            )
                        }
                    }
                }
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
    persistedJavaCrash: String?,
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
                "This isolates library loading, SurfaceView rendering and TextureView rendering using MapLibre's documented lifecycle ordering. Reopen after any crash.",
                style = MaterialTheme.typography.bodyMedium,
            )

            if (previousExit != null) {
                DiagnosticCard("Android previous-process record", previousExit)
            }

            if (lastAttempt != null) {
                val readableStage = DiagnosticStage.entries
                    .firstOrNull { it.name == lastAttempt }
                    ?.label
                    ?: lastAttempt
                val readableResult = when (lastResult) {
                    "STARTED" -> "STARTED but never returned. Process death during this stage is likely."
                    "PASSED" -> "PASSED"
                    null -> "No result recorded"
                    else -> lastResult
                }
                DiagnosticCard("Previous attempt", "$readableStage\n$readableResult")
            }

            if (!persistedJavaCrash.isNullOrBlank()) {
                DiagnosticCard(
                    "Persisted Java exception",
                    persistedJavaCrash.take(3500),
                )
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

@Composable
private fun DiagnosticCard(title: String, body: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(body, style = MaterialTheme.typography.bodySmall)
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

package com.masamunr.trailcharter.map

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.masamunr.trailcharter.ui.theme.TrailCharterTheme
import org.maplibre.android.maps.MapLibreMap

/** Activity used only by the isolated map/routing technical spike. */
class MapSpikeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrailCharterTheme {
                var planningOnMap by remember { mutableStateOf(false) }
                var routeDraft by remember { mutableStateOf(StageRoutePlanDraft()) }

                if (!planningOnMap) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .statusBarsPadding()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Text("Stage route planning", style = MaterialTheme.typography.headlineSmall)
                            Text(
                                "Planner ↔ map integration spike. The selected route stays in this test screen for now; Adventure database persistence comes after the interaction is physically accepted.",
                                style = MaterialTheme.typography.bodyMedium,
                            )

                            routeDraft.start?.let {
                                Text(
                                    "Route draft: start + finish selected • ${routeDraft.waypoints.size} waypoint(s)",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            routeDraft.distanceMetres?.let { distance ->
                                Text(
                                    "%.2f km • +%.0f / -%.0f m".format(
                                        distance / 1000.0,
                                        routeDraft.ascentMetres ?: 0.0,
                                        routeDraft.descentMetres ?: 0.0,
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }

                            Button(
                                onClick = { planningOnMap = true },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(if (routeDraft.start == null) "Plan on map" else "Edit route on map")
                            }

                            if (routeDraft.start != null || routeDraft.finish != null) {
                                OutlinedButton(
                                    onClick = { routeDraft = StageRoutePlanDraft() },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("Clear route draft")
                                }
                            }

                            Text(
                                "This slice deliberately does not write prototype route data into saved Adventures.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    var routingMap by remember { mutableStateOf<MapLibreMap?>(null) }
                    Box(modifier = Modifier.fillMaxSize()) {
                        OfflineUkMapPass3Screen(onMapReady = { routingMap = it })
                        routingMap?.let { map ->
                            applyPass4PathVisualHierarchy(map)
                            StageRoutePlanningSpikeOverlay(
                                map = map,
                                initialDraft = routeDraft,
                                onDone = { updated ->
                                    routeDraft = updated
                                    planningOnMap = false
                                },
                                onCancel = { planningOnMap = false },
                            )
                        }
                    }
                }
            }
        }
    }
}

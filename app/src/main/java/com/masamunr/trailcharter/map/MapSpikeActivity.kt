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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.masamunr.trailcharter.data.adventure.AdventureRepository
import com.masamunr.trailcharter.data.adventure.PersistedStageRoute
import com.masamunr.trailcharter.data.adventure.RoutePlanningStageRow
import com.masamunr.trailcharter.data.adventure.StageRouteRepository
import com.masamunr.trailcharter.data.adventure.TrailCharterDatabase
import com.masamunr.trailcharter.geo.RouteGeometry
import com.masamunr.trailcharter.routing.BRouterRoutingEngine
import com.masamunr.trailcharter.routing.RoutePlanningMode
import com.masamunr.trailcharter.routing.RoutingEngineBoundary
import com.masamunr.trailcharter.routing.TravelMode
import com.masamunr.trailcharter.routing.loadInstalledEryriRoutingPackage
import com.masamunr.trailcharter.ui.theme.TrailCharterTheme
import kotlinx.coroutines.launch
import org.maplibre.android.maps.MapLibreMap

/** Activity used only by the isolated map/routing technical spike. */
class MapSpikeActivity : ComponentActivity() {
    private val database by lazy { TrailCharterDatabase.getInstance(this) }
    private val adventureRepository by lazy { AdventureRepository(database.adventureDao()) }
    private val stageRouteRepository by lazy { StageRouteRepository(database.stageRouteDao()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrailCharterTheme {
                PersistedStageRouteSpike(
                    adventureRepository = adventureRepository,
                    stageRouteRepository = stageRouteRepository,
                )
            }
        }
    }
}

@Composable
private fun PersistedStageRouteSpike(
    adventureRepository: AdventureRepository,
    stageRouteRepository: StageRouteRepository,
) {
    val scope = rememberCoroutineScope()
    val stages by adventureRepository.observeRoutePlanningStages().collectAsState(initial = emptyList())
    var selectedStageId by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(stages, selectedStageId) {
        if (selectedStageId !in stages.map { it.stageId }) {
            selectedStageId = stages.firstOrNull()?.stageId
        }
    }

    if (stages.isEmpty()) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("Stage route persistence", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Create a real Adventure and Stage for this persistence check. Existing Adventure data is retained by the additive database migration.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(
                    onClick = {
                        scope.launch {
                            selectedStageId = adventureRepository.createRoutePlanningStage()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Create persistence test stage")
                }
            }
        }
        return
    }

    val selected = stages.firstOrNull { it.stageId == selectedStageId } ?: return
    PersistedStageEditor(
        stages = stages,
        selected = selected,
        onSelectStage = { selectedStageId = it },
        onCreateStage = {
            scope.launch { selectedStageId = adventureRepository.createRoutePlanningStage() }
        },
        repository = stageRouteRepository,
    )
}

@Composable
private fun PersistedStageEditor(
    stages: List<RoutePlanningStageRow>,
    selected: RoutePlanningStageRow,
    onSelectStage: (Long) -> Unit,
    onCreateStage: () -> Unit,
    repository: StageRouteRepository,
) {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    val routingEngine: RoutingEngineBoundary? = remember(context) {
        loadInstalledEryriRoutingPackage(context)?.let(::BRouterRoutingEngine)
    }
    var planningOnMap by rememberSaveable(selected.stageId) { mutableStateOf(false) }
    var routeDraft by remember(selected.stageId) { mutableStateOf(StageRoutePlanDraft()) }
    var savedRoute by remember(selected.stageId) { mutableStateOf<PersistedStageRoute?>(null) }
    var status by remember(selected.stageId) { mutableStateOf("Ready") }
    var routeLoaded by remember(selected.stageId) { mutableStateOf(false) }

    LaunchedEffect(selected.stageId) {
        savedRoute = repository.getStageRoute(selected.stageId)
        routeDraft = savedRoute?.toDraft() ?: StageRoutePlanDraft()
        routeLoaded = true
    }

    if (planningOnMap) {
        var routingMap by remember { mutableStateOf<MapLibreMap?>(null) }
        Box(modifier = Modifier.fillMaxSize()) {
            OfflineUkMapPass3Screen(onMapReady = { routingMap = it })
            routingMap?.let { map ->
                LaunchedEffect(map) { applyPass4PathVisualHierarchy(map) }
                StageRoutePlanningSpikeOverlay(
                    map = map,
                    initialDraft = routeDraft,
                    routingEngine = routingEngine,
                    onDone = { updated ->
                        val route = updated.toPersisted(selected.stageId)
                        scope.launch {
                            repository.saveStageRoute(route)
                            savedRoute = route
                            routeDraft = updated
                            status = "Stage route saved"
                            planningOnMap = false
                        }
                    },
                    onCancel = { planningOnMap = false },
                )
            }
        }
        return
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Stage route planning", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Persistence spike scaffolding only; the production app remains committed to a map-first home.",
                style = MaterialTheme.typography.bodyMedium,
            )

            Text("Choose Stage", style = MaterialTheme.typography.titleMedium)
            stages.forEach { stage ->
                val savedSuffix = if (stage.hasRoute) " • route saved" else ""
                val label = "${stage.adventureTitle} • ${stage.stageTitle}$savedSuffix"
                if (stage.stageId == selected.stageId) {
                    Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text(label) }
                } else {
                    OutlinedButton(
                        onClick = { onSelectStage(stage.stageId) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(label) }
                }
            }
            OutlinedButton(onClick = onCreateStage, modifier = Modifier.fillMaxWidth()) {
                Text("Create another test Stage")
            }

            Text(if (routeLoaded) status else "Loading saved Stage route…", style = MaterialTheme.typography.bodySmall)
            routeDraft.start?.let {
                Text(
                    "Saved route: Start + Finish • ${routeDraft.waypoints.size} waypoint(s) • Snap ${if (routeDraft.snapToNetwork) "ON" else "OFF"}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            routeDraft.distanceMetres?.let { distance ->
                Text(
                    "%.2f km • +%.0f / -%.0f m • %s".format(
                        distance / 1000.0,
                        routeDraft.ascentMetres ?: 0.0,
                        routeDraft.descentMetres ?: 0.0,
                        routeDraft.durationSeconds?.let(::formatRouteDuration) ?: "ETA n/a",
                    ),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Button(
                onClick = { planningOnMap = true },
                enabled = routeLoaded,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (routeDraft.start == null) "Plan on map" else "Reopen / edit route on map")
            }

            if (savedRoute != null) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            repository.removeStageRoute(selected.stageId)
                            savedRoute = null
                            routeDraft = StageRoutePlanDraft()
                            status = "Saved Stage route removed"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Remove saved Stage route")
                }
            }

            Text(
                "Routes are stored against the selected Stage and survive process death. BRouter remains behind the generic routing boundary and in EXPLORE status.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun PersistedStageRoute.toDraft() = StageRoutePlanDraft(
    start = start,
    finish = finish,
    waypoints = waypoints,
    snapToNetwork = snapToNetwork,
    distanceMetres = distanceMetres,
    ascentMetres = ascentMetres,
    descentMetres = descentMetres,
    durationSeconds = durationSeconds,
    routeGeometry = geometry?.points.orEmpty(),
)

private fun StageRoutePlanDraft.toPersisted(stageId: Long): PersistedStageRoute {
    val selectedStart = requireNotNull(start) { "Start is required" }
    val selectedFinish = requireNotNull(finish) { "Finish is required" }
    return PersistedStageRoute(
        stageId = stageId,
        start = selectedStart,
        finish = selectedFinish,
        waypoints = waypoints,
        snapToNetwork = snapToNetwork,
        planningMode = RoutePlanningMode.MAGNETIC,
        travelMode = TravelMode.WALK,
        geometry = routeGeometry.takeIf { it.size >= 2 }?.let(::RouteGeometry),
        distanceMetres = distanceMetres,
        ascentMetres = ascentMetres,
        descentMetres = descentMetres,
        durationSeconds = durationSeconds,
    )
}

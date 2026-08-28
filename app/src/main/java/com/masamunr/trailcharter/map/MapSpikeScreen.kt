package com.masamunr.trailcharter.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle

/** Physical-device renderer proof used only on the draft spike branch. */
@Composable
internal fun MapSpikeScreen() {
    val context = LocalContext.current
    val localStyle = remember(context) {
        localPmTilesStyle(ensureLocalPmTilesProbe(context))
    }
    var status by rememberSaveable { mutableStateOf("Loading local PMTiles…") }

    Box(modifier = Modifier.fillMaxSize()) {
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            baseStyle = BaseStyle.Json(localStyle),
            onMapLoadFinished = { status = "Local PMTiles style loaded" },
            onMapLoadFailed = { reason ->
                status = "Map load failed${reason?.let { ": $it" }.orEmpty()}"
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
                    text = "TrailCharter offline renderer spike",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(text = status, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "Expected: a pale route line across a dark-green map. No network or location permission.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

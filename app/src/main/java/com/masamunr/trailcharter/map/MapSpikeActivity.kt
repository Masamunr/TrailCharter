package com.masamunr.trailcharter.map

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.masamunr.trailcharter.ui.theme.TrailCharterTheme
import org.maplibre.android.maps.MapLibreMap

/** Activity used only by the isolated map/routing technical spike. */
class MapSpikeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrailCharterTheme {
                var routingMap by remember { mutableStateOf<MapLibreMap?>(null) }
                Box(modifier = Modifier.fillMaxSize()) {
                    OfflineUkMapPass3Screen(onMapReady = { routingMap = it })
                    BRouterRoutingSpikeHost(map = routingMap)
                }
            }
        }
    }
}

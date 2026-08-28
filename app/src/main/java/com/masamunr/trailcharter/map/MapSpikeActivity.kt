package com.masamunr.trailcharter.map

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.masamunr.trailcharter.ui.theme.TrailCharterTheme

/** Activity used only by the side-by-side debug renderer spike. */
class MapSpikeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrailCharterTheme {
                OfflineUkMapPass3Screen()
            }
        }
    }
}

package com.masamunr.trailcharter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.masamunr.trailcharter.data.adventure.AdventureRepository
import com.masamunr.trailcharter.data.adventure.TrailCharterDatabase
import com.masamunr.trailcharter.ui.theme.TrailCharterTheme

class MainActivity : ComponentActivity() {
    private val adventureRepository by lazy {
        AdventureRepository(TrailCharterDatabase.getInstance(this).adventureDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrailCharterTheme {
                TrailCharterApp(repository = adventureRepository)
            }
        }
    }
}

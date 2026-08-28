package com.masamunr.trailcharter.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.trailCharterSettings: DataStore<Preferences> by preferencesDataStore(
    name = "trailcharter_settings",
)

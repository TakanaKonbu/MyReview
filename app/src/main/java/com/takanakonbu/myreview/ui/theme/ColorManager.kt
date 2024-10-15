package com.takanakonbu.myreview.ui.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object ColorManager {
    private val MAIN_COLOR_KEY = intPreferencesKey("main_color")

    fun getMainColor(context: Context): Flow<Color> {
        return context.dataStore.data.map { preferences ->
            val colorInt = preferences[MAIN_COLOR_KEY] ?: DefaultMainColor.toArgb()
            Color(colorInt)
        }
    }

    suspend fun setMainColor(context: Context, color: Color) {
        context.dataStore.edit { preferences ->
            preferences[MAIN_COLOR_KEY] = color.toArgb()
        }
        MainColor.value = color
    }

    suspend fun loadSavedColor(context: Context) {
        context.dataStore.data.collect { preferences ->
            val colorInt = preferences[MAIN_COLOR_KEY] ?: DefaultMainColor.toArgb()
            MainColor.value = Color(colorInt)
        }
    }
}
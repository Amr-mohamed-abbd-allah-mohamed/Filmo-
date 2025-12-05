package com.example.filmo.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// اسم الـ DataStore
private const val DATASTORE_NAME = "filmo_settings"

// Extension property على Context
val Context.filmoDataStore by preferencesDataStore(name = DATASTORE_NAME)

class ThemeManager(private val context: Context) {

    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")
    }

    // Flow رجّاع القيمة الحالية (افتراضي false)
    val isDarkMode: Flow<Boolean> = context.filmoDataStore.data
        .map { prefs -> prefs[DARK_MODE_KEY] ?: false }

    // حفظ القيمة
    suspend fun setDarkMode(enabled: Boolean) {
        context.filmoDataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = enabled
        }
    }
}

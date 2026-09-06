package com.expense.tracker.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode(val storageKey: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromKey(key: String?): ThemeMode {
            return entries.find { it.storageKey == key } ?: SYSTEM
        }
    }
}

class UserPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("expense_tracker_prefs", Context.MODE_PRIVATE)

    var isSetupCompleted: Boolean
        get() = prefs.getBoolean(KEY_SETUP_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_SETUP_COMPLETED, value).apply()

    private val _themeModeFlow = MutableStateFlow(
        ThemeMode.fromKey(prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.storageKey))
    )
    val themeModeFlow: StateFlow<ThemeMode> = _themeModeFlow.asStateFlow()

    val currentThemeMode: ThemeMode
        get() = _themeModeFlow.value

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.storageKey).apply()
        _themeModeFlow.value = mode
    }

    companion object {
        private const val KEY_SETUP_COMPLETED = "is_setup_completed"
        private const val KEY_THEME_MODE = "app_theme_mode"
    }
}

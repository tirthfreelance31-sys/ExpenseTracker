package com.expense.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.expense.tracker.data.repository.ThemeMode
import com.expense.tracker.ui.navigation.MainAppNavigation
import com.expense.tracker.ui.theme.AppTheme
import com.expense.tracker.ui.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as ExpenseApp
        val repository = app.repository
        val preferencesRepository = app.preferencesRepository

        setContent {
            val themeMode by preferencesRepository.themeModeFlow.collectAsState()
            val systemInDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> systemInDark
            }

            ExpenseTrackerTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppTheme.colors.background
                ) {
                    MainAppNavigation(
                        repository = repository,
                        preferencesRepository = preferencesRepository
                    )
                }
            }
        }
    }
}

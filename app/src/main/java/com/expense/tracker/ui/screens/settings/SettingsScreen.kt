package com.expense.tracker.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expense.tracker.data.repository.ThemeMode
import com.expense.tracker.data.repository.UserPreferencesRepository
import com.expense.tracker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesRepository: UserPreferencesRepository? = null
) {
    val colors = AppTheme.colors
    val currentThemeMode = preferencesRepository?.themeModeFlow?.collectAsState()?.value ?: ThemeMode.SYSTEM

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp
                        ),
                        color = colors.textPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: APPEARANCE
            item {
                Text(
                    text = "APPEARANCE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    ),
                    color = colors.textSecondary
                )
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        ThemeOptionRow(
                            title = "System default",
                            subtitle = "Follow your device settings",
                            icon = Icons.Default.SettingsBrightness,
                            selected = currentThemeMode == ThemeMode.SYSTEM,
                            onClick = { preferencesRepository?.setThemeMode(ThemeMode.SYSTEM) }
                        )

                        HorizontalDivider(
                            color = colors.border.copy(alpha = 0.5f),
                            thickness = 0.8.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        ThemeOptionRow(
                            title = "Light",
                            subtitle = "Always use light theme",
                            icon = Icons.Default.LightMode,
                            selected = currentThemeMode == ThemeMode.LIGHT,
                            onClick = { preferencesRepository?.setThemeMode(ThemeMode.LIGHT) }
                        )

                        HorizontalDivider(
                            color = colors.border.copy(alpha = 0.5f),
                            thickness = 0.8.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        ThemeOptionRow(
                            title = "Dark",
                            subtitle = "Always use dark theme",
                            icon = Icons.Default.DarkMode,
                            selected = currentThemeMode == ThemeMode.DARK,
                            onClick = { preferencesRepository?.setThemeMode(ThemeMode.DARK) }
                        )
                    }
                }
            }

            // Section 2: PRIVACY & SECURITY
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "DATA & SECURITY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    ),
                    color = colors.textSecondary
                )
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = colors.softTealBg,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "100% Offline & Private",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    ),
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "All financial records are encrypted on-device with SQLCipher AES-256. Zero cloud storage, analytics, or trackers.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = Inter,
                                        fontSize = 12.sp
                                    ),
                                    color = colors.textSecondary
                                )
                            }
                        }

                        HorizontalDivider(color = colors.border.copy(alpha = 0.5f), thickness = 0.8.dp)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = colors.softGreenBg,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = colors.income,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Dynamic Balances",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    ),
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Single source of truth: wallet balances are computed dynamically from logged transactions, ensuring zero data desync.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = Inter,
                                        fontSize = 12.sp
                                    ),
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: APPLICATION INFO
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ABOUT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    ),
                    color = colors.textSecondary
                )
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "App Name",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = Inter),
                                color = colors.textSecondary
                            )
                            Text(
                                "Expense Tracker",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = colors.textPrimary
                            )
                        }

                        HorizontalDivider(color = colors.border.copy(alpha = 0.5f), thickness = 0.8.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Theme",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = Inter),
                                color = colors.textSecondary
                            )
                            Text(
                                "Warm Fintech",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = colors.primary
                            )
                        }

                        HorizontalDivider(color = colors.border.copy(alpha = 0.5f), thickness = 0.8.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Version",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = Inter),
                                color = colors.textSecondary
                            )
                            Text(
                                "1.0.0 (Offline Build)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = CircleShape,
                color = if (selected) colors.primary.copy(alpha = 0.12f) else colors.surfaceSecondary,
                modifier = Modifier.size(36.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (selected) colors.primary else colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = Inter,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        fontSize = 15.sp
                    ),
                    color = if (selected) colors.primary else colors.textPrimary
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = Inter,
                        fontSize = 12.sp
                    ),
                    color = colors.textSecondary
                )
            }
        }

        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = colors.primary,
                unselectedColor = colors.textSecondary.copy(alpha = 0.6f)
            )
        )
    }
}

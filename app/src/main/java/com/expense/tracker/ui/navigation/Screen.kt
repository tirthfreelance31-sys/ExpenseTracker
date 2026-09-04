package com.expense.tracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object AddTransaction : Screen("add_transaction?txId={txId}", "Add Transaction", Icons.Default.AddCircle) {
        fun createRoute(txId: Long? = null) = if (txId != null) "add_transaction?txId=$txId" else "add_transaction"
    }
    object History : Screen("history", "History", Icons.Default.History)
    object Summary : Screen("summary", "Summary", Icons.Default.PieChart)
    object WalletSetup : Screen("wallet_setup", "Wallet Setup", Icons.Default.AccountBalanceWallet)
    object WalletDetail : Screen("wallet_detail/{walletId}", "Wallet Detail", Icons.Default.Wallet) {
        fun createRoute(walletId: Long) = "wallet_detail/$walletId"
    }
    object Categories : Screen("categories", "Categories", Icons.Default.Category)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.History,
    Screen.Summary,
    Screen.Settings
)

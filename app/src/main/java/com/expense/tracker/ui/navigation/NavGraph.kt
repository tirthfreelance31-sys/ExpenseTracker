package com.expense.tracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.expense.tracker.data.repository.ExpenseRepository
import com.expense.tracker.data.repository.UserPreferencesRepository
import com.expense.tracker.ui.screens.addtransaction.AddTransactionScreen
import com.expense.tracker.ui.screens.categories.CategoriesScreen
import com.expense.tracker.ui.screens.history.HistoryScreen
import com.expense.tracker.ui.screens.home.HomeScreen
import com.expense.tracker.ui.screens.settings.SettingsScreen
import com.expense.tracker.ui.screens.summary.SummaryScreen
import com.expense.tracker.ui.screens.walletdetail.WalletDetailScreen
import com.expense.tracker.ui.screens.walletsetup.WalletSetupScreen

@Composable
fun MainAppNavigation(
    repository: ExpenseRepository,
    preferencesRepository: UserPreferencesRepository,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isSetupCompleted = preferencesRepository.isSetupCompleted
    val startDestination = if (isSetupCompleted) Screen.Home.route else Screen.WalletSetup.route

    val wallets by repository.walletsWithBalance.collectAsState(initial = emptyList())
    val categories by repository.allCategories.collectAsState(initial = emptyList())
    val recentTransactions by repository.getRecentTransactions(5).collectAsState(initial = emptyList())

    val sevenDaysAgo = remember { System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L }
    val recentExpenses by repository.getRecentExpenses(sevenDaysAgo).collectAsState(initial = emptyList())

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.History.route,
        Screen.Summary.route,
        Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                screen.icon?.let { icon ->
                                    Icon(icon, contentDescription = screen.title)
                                }
                            },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    wallets = wallets,
                    recentTransactions = recentTransactions,
                    recentExpenses = recentExpenses,
                    categories = categories,
                    onNavigateToAddTransaction = { navController.navigate(Screen.AddTransaction.route) },
                    onNavigateToWalletDetail = { walletId ->
                        navController.navigate(Screen.WalletDetail.createRoute(walletId))
                    },
                    onNavigateToSummary = { navController.navigate(Screen.Summary.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            composable(Screen.WalletSetup.route) {
                WalletSetupScreen(
                    wallets = wallets,
                    onSaveOpeningBalances = { balancesMap ->
                        balancesMap.forEach { (walletId, amount) ->
                            repository.updateOpeningBalance(walletId, amount)
                        }
                        preferencesRepository.isSetupCompleted = true
                    },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.WalletSetup.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.AddTransaction.route) {
                AddTransactionScreen(
                    onTransactionAdded = { navController.navigate(Screen.Home.route) }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen()
            }

            composable(Screen.Summary.route) {
                SummaryScreen()
            }

            composable(
                route = Screen.WalletDetail.route,
                arguments = listOf(navArgument("walletId") { type = NavType.LongType })
            ) { backStackEntry ->
                val walletId = backStackEntry.arguments?.getLong("walletId") ?: 0L
                val wallet = wallets.find { it.id == walletId }
                WalletDetailScreen(
                    wallet = wallet,
                    onReconcileOpeningBalance = { id, newBalance ->
                        repository.updateOpeningBalance(id, newBalance)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Categories.route) {
                CategoriesScreen(
                    categories = categories,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}

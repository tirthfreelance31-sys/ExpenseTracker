package com.expense.tracker.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.expense.tracker.data.local.entity.TransactionEntity
import com.expense.tracker.data.model.TransactionType
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
    val allTransactions by repository.allTransactions.collectAsState(initial = emptyList())

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
                val isDark = com.expense.tracker.ui.theme.AppTheme.colors.isDark
                val navBg = if (isDark) Color(0xF2181B1A) else Color(0xF5FFFDF9)
                val navBorder = if (isDark) Color(0xFF262A28) else Color(0xFFECE8DF)
                val indicatorColor = if (isDark) Color(0xFF244745) else Color(0xFFD8ECEA)
                val selectedItemColor = com.expense.tracker.ui.theme.AppTheme.colors.primary
                val unselectedColor = com.expense.tracker.ui.theme.AppTheme.colors.textSecondary

                Surface(
                    color = navBg,
                    border = BorderStroke(1.dp, navBorder)
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        contentColor = unselectedColor,
                        tonalElevation = 0.dp
                    ) {
                        bottomNavItems.forEach { screen ->
                            val isSelected = currentRoute == screen.route
                            NavigationBarItem(
                                icon = {
                                    screen.icon?.let { icon ->
                                        Icon(
                                            icon,
                                            contentDescription = screen.title,
                                            tint = if (isSelected) selectedItemColor else unselectedColor
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        screen.title,
                                        color = if (isSelected) selectedItemColor else unselectedColor,
                                        fontFamily = com.expense.tracker.ui.theme.Inter,
                                        fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                },
                                selected = isSelected,
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = indicatorColor,
                                    selectedIconColor = selectedItemColor,
                                    unselectedIconColor = unselectedColor,
                                    selectedTextColor = selectedItemColor,
                                    unselectedTextColor = unselectedColor
                                ),
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
                    onNavigateToAddTransaction = { navController.navigate(Screen.AddTransaction.createRoute()) },
                    onEditTransaction = { txId -> navController.navigate(Screen.AddTransaction.createRoute(txId)) },
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

            composable(
                route = Screen.AddTransaction.route,
                arguments = listOf(navArgument("txId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val txIdString = backStackEntry.arguments?.getString("txId")
                val editingTxId = txIdString?.toLongOrNull()

                AddTransactionScreen(
                    editingTransactionId = editingTxId,
                    wallets = wallets,
                    categories = categories,
                    onSaveExpense = { amount, walletId, categoryId, note, timestamp, editingId ->
                        if (editingId == null) {
                            val tx = TransactionEntity(
                                walletId = walletId,
                                categoryId = categoryId,
                                type = TransactionType.EXPENSE,
                                amount = amount,
                                note = note,
                                timestamp = timestamp
                            )
                            repository.insertTransaction(tx)
                        } else {
                            val existing = repository.getTransactionById(editingId)
                            if (existing != null) {
                                val updated = existing.copy(
                                    walletId = walletId,
                                    categoryId = categoryId,
                                    type = TransactionType.EXPENSE,
                                    amount = amount,
                                    note = note,
                                    timestamp = timestamp
                                )
                                repository.updateTransaction(updated)
                            }
                        }
                    },
                    onSaveIncome = { amount, walletId, categoryId, note, timestamp, editingId ->
                        if (editingId == null) {
                            val tx = TransactionEntity(
                                walletId = walletId,
                                categoryId = categoryId,
                                type = TransactionType.INCOME,
                                amount = amount,
                                note = note,
                                timestamp = timestamp
                            )
                            repository.insertTransaction(tx)
                        } else {
                            val existing = repository.getTransactionById(editingId)
                            if (existing != null) {
                                val updated = existing.copy(
                                    walletId = walletId,
                                    categoryId = categoryId,
                                    type = TransactionType.INCOME,
                                    amount = amount,
                                    note = note,
                                    timestamp = timestamp
                                )
                                repository.updateTransaction(updated)
                            }
                        }
                    },
                    onSaveTransfer = { amount, fromWalletId, toWalletId, note, timestamp, editingFromTxId, editingToTxId, linkedId ->
                        if (editingFromTxId == null || editingToTxId == null) {
                            val fromTx = TransactionEntity(
                                walletId = fromWalletId,
                                type = TransactionType.TRANSFER_OUT,
                                amount = amount,
                                note = note,
                                timestamp = timestamp
                            )
                            val toTx = TransactionEntity(
                                walletId = toWalletId,
                                type = TransactionType.TRANSFER_IN,
                                amount = amount,
                                note = note,
                                timestamp = timestamp
                            )
                            repository.saveTransfer(fromTx, toTx)
                        } else {
                            val existingFrom = repository.getTransactionById(editingFromTxId)
                            val existingTo = repository.getTransactionById(editingToTxId)
                            if (existingFrom != null && existingTo != null) {
                                val updatedFrom = existingFrom.copy(
                                    walletId = fromWalletId,
                                    amount = amount,
                                    note = note,
                                    timestamp = timestamp,
                                    linkedTransferId = linkedId ?: existingFrom.linkedTransferId
                                )
                                val updatedTo = existingTo.copy(
                                    walletId = toWalletId,
                                    amount = amount,
                                    note = note,
                                    timestamp = timestamp,
                                    linkedTransferId = linkedId ?: existingFrom.linkedTransferId
                                )
                                repository.updateTransfer(updatedFrom, updatedTo)
                            }
                        }
                    },
                    onDeleteTransaction = { txId, linkedTransferId ->
                        if (linkedTransferId != null) {
                            repository.deleteTransfer(linkedTransferId)
                        } else {
                            val tx = repository.getTransactionById(txId)
                            if (tx != null) {
                                if (tx.linkedTransferId != null) {
                                    repository.deleteTransfer(tx.linkedTransferId)
                                } else {
                                    repository.deleteTransaction(tx)
                                }
                            }
                        }
                    },
                    onFetchTransactionDetails = { txId ->
                        val tx = repository.getTransactionById(txId)
                        if (tx != null) {
                            if (tx.linkedTransferId != null) {
                                val linkedList = repository.getLinkedTransactions(tx.linkedTransferId)
                                val mainTx = linkedList.find { it.id == txId } ?: tx
                                val otherTx = linkedList.find { it.id != mainTx.id }
                                Pair(mainTx, otherTx)
                            } else {
                                Pair(tx, null)
                            }
                        } else null
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    transactions = allTransactions,
                    wallets = wallets,
                    categories = categories,
                    onEditTransaction = { txId -> navController.navigate(Screen.AddTransaction.createRoute(txId)) }
                )
            }

            composable(Screen.Summary.route) {
                SummaryScreen(
                    wallets = wallets,
                    recentExpenses = recentExpenses,
                    categories = categories
                )
            }

            composable(
                route = Screen.WalletDetail.route,
                arguments = listOf(navArgument("walletId") { type = NavType.LongType })
            ) { backStackEntry ->
                val walletId = backStackEntry.arguments?.getLong("walletId") ?: 0L
                val wallet = wallets.find { it.id == walletId }
                val walletTransactions by repository.getTransactionsByWallet(walletId).collectAsState(initial = emptyList())
                WalletDetailScreen(
                    wallet = wallet,
                    transactions = walletTransactions,
                    categories = categories,
                    onReconcileOpeningBalance = { id, newBalance ->
                        repository.updateOpeningBalance(id, newBalance)
                    },
                    onEditTransaction = { txId -> navController.navigate(Screen.AddTransaction.createRoute(txId)) },
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
                SettingsScreen(preferencesRepository = preferencesRepository)
            }
        }
    }
}

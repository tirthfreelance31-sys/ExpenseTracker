package com.expense.tracker.ui.screens.summary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expense.tracker.data.local.entity.CategoryEntity
import com.expense.tracker.data.local.entity.TransactionEntity
import com.expense.tracker.data.model.WalletType
import com.expense.tracker.data.model.WalletWithBalance
import com.expense.tracker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    wallets: List<WalletWithBalance> = emptyList(),
    recentExpenses: List<TransactionEntity> = emptyList(),
    categories: List<CategoryEntity> = emptyList()
) {
    val totalNetBalance = wallets.sumOf { it.currentBalance }
    val totalWeeklyExpenses = recentExpenses.sumOf { it.amount }
    val categoriesMap = categories.associateBy { it.id }

    val expensesByCategory = recentExpenses
        .groupBy { it.categoryId }
        .map { (catId, list) ->
            val catName = categoriesMap[catId]?.name ?: "General"
            val total = list.sumOf { it.amount }
            val count = list.size
            Triple(catName, total, count)
        }
        .sortedByDescending { it.second }

    val colors = AppTheme.colors

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Summary",
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
            // Hero Total Balance Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Total net balance",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = Inter,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = colors.textSecondary
                        )

                        Text(
                            text = "₹${"%.2f".format(totalNetBalance)}",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = SpaceGrotesk,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 32.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = colors.textPrimary
                        )

                        HorizontalDivider(color = colors.border.copy(alpha = 0.6f), thickness = 0.8.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Spent past 7 days",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = Inter,
                                    fontSize = 12.sp
                                ),
                                color = colors.textSecondary
                            )
                            Text(
                                text = "-₹${"%.2f".format(totalWeeklyExpenses)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = SpaceGrotesk,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                ),
                                color = if (totalWeeklyExpenses > 0) colors.expense else colors.textPrimary
                            )
                        }
                    }
                }
            }

            // Section 1: Wallet Breakdown
            item {
                Text(
                    text = "WALLETS",
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
                    Column {
                        wallets.forEachIndexed { index, wallet ->
                            val (accentColor, icon) = when (wallet.name.lowercase()) {
                                "upi" -> Pair(colors.sky, Icons.Default.AccountBalanceWallet)
                                "cash" -> Pair(colors.income, Icons.Default.Payments)
                                "savings" -> Pair(colors.amber, Icons.Default.Savings)
                                else -> Pair(colors.primary, Icons.Default.AccountBalanceWallet)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = accentColor.copy(alpha = 0.12f),
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = wallet.name,
                                                tint = accentColor,
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = wallet.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = Inter,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp
                                            ),
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = if (wallet.type == WalletType.DIGITAL) "Digital" else "Cash",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = Inter,
                                                fontSize = 11.sp
                                            ),
                                            color = colors.textSecondary
                                        )
                                    }
                                }

                                Text(
                                    text = "₹${"%.2f".format(wallet.currentBalance)}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = SpaceGrotesk,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    ),
                                    color = colors.textPrimary
                                )
                            }

                            if (index < wallets.size - 1) {
                                HorizontalDivider(
                                    color = colors.border.copy(alpha = 0.5f),
                                    thickness = 0.8.dp,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: Spending by Category
            item {
                Text(
                    text = "SPENDING BY CATEGORY (7 DAYS)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    ),
                    color = colors.textSecondary
                )
            }

            if (expensesByCategory.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No expenses logged in the past 7 days ✨",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = Inter,
                                    fontSize = 13.sp
                                ),
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            } else {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column {
                            expensesByCategory.forEachIndexed { index, (catName, total, count) ->
                                val percentage = if (totalWeeklyExpenses > 0) (total / totalWeeklyExpenses) else 0.0

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = catName,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = Inter,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 14.sp
                                                ),
                                                color = colors.textPrimary
                                            )
                                            Text(
                                                text = "$count ${if (count == 1) "entry" else "entries"} · ${"%.1f".format(percentage * 100)}%",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = Inter,
                                                    fontSize = 11.sp
                                                ),
                                                color = colors.textSecondary
                                            )
                                        }

                                        Text(
                                            text = "₹${"%.2f".format(total)}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = SpaceGrotesk,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 15.sp
                                            ),
                                            color = colors.expense
                                        )
                                    }

                                    // Category visual spend bar
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(colors.surfaceSecondary)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(percentage.toFloat().coerceIn(0.04f, 1f))
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(colors.primary)
                                        )
                                    }
                                }

                                if (index < expensesByCategory.size - 1) {
                                    HorizontalDivider(
                                        color = colors.border.copy(alpha = 0.5f),
                                        thickness = 0.8.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
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

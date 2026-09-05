package com.expense.tracker.ui.screens.summary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
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

    // Group weekly expenses by category
    val expensesByCategory = recentExpenses
        .groupBy { it.categoryId }
        .map { (catId, list) ->
            val catName = categoriesMap[catId]?.name ?: "General"
            val total = list.sumOf { it.amount }
            val count = list.size
            Triple(catName, total, count)
        }
        .sortedByDescending { it.second }

    Scaffold(
        containerColor = LedgerInk,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "FINANCIAL TALLY & SUMMARY",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RupeeGold,
                        letterSpacing = 1.2.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LedgerInk
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Net Position Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    color = LedgerPaper,
                    border = BorderStroke(1.dp, LedgerDivider)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "CONSOLIDATED LIQUID POSITION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryText,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "₹${"%.2f".format(totalNetBalance)}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = RupeeGold
                        )
                        HorizontalDivider(color = LedgerDivider, thickness = 0.5.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Past 7 Days Debits",
                                style = MaterialTheme.typography.bodySmall,
                                color = SecondaryText
                            )
                            Text(
                                text = "-₹${"%.2f".format(totalWeeklyExpenses)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (totalWeeklyExpenses > 0) SealRed else PrimaryText
                            )
                        }
                    }
                }
            }

            // Section 1: Folio Breakdown
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.width(16.dp), thickness = 1.dp, color = LedgerDivider)
                    Text(
                        text = "FOLIO LIQUIDITY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryText,
                        letterSpacing = 1.sp
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = LedgerDivider)
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(3.dp),
                    color = LedgerPaper,
                    border = BorderStroke(1.dp, LedgerDivider)
                ) {
                    Column {
                        wallets.forEachIndexed { index, wallet ->
                            val accentColor = when (wallet.name.lowercase()) {
                                "upi" -> StampIndigo
                                "cash" -> CurrencyGreen
                                "savings" -> RupeeGold
                                else -> RupeeGold
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(24.dp)
                                            .background(accentColor, RoundedCornerShape(1.dp))
                                    )
                                    Column {
                                        Text(
                                            text = wallet.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryText
                                        )
                                        Text(
                                            text = if (wallet.type == WalletType.DIGITAL) "Digital Folio" else "Physical Cash",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SecondaryText
                                        )
                                    }
                                }

                                Text(
                                    text = "₹${"%.2f".format(wallet.currentBalance)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText
                                )
                            }

                            if (index < wallets.size - 1) {
                                HorizontalDivider(color = LedgerDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 14.dp))
                            }
                        }
                    }
                }
            }

            // Section 2: Outgoings by Classification
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.width(16.dp), thickness = 1.dp, color = LedgerDivider)
                    Text(
                        text = "OUTGOINGS BY CLASSIFICATION (7 DAYS)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryText,
                        letterSpacing = 1.sp
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = LedgerDivider)
                }
            }

            if (expensesByCategory.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(3.dp),
                        color = LedgerPaper,
                        border = BorderStroke(1.dp, LedgerDivider)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No debit entries logged in the past 7 days",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SecondaryText
                            )
                        }
                    }
                }
            } else {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(3.dp),
                        color = LedgerPaper,
                        border = BorderStroke(1.dp, LedgerDivider)
                    ) {
                        Column {
                            expensesByCategory.forEachIndexed { index, (catName, total, count) ->
                                val percentage = if (totalWeeklyExpenses > 0) (total / totalWeeklyExpenses) * 100 else 0.0

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = catName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryText
                                        )
                                        Text(
                                            text = "$count ${if (count == 1) "entry" else "entries"} · ${"%.1f".format(percentage)}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SecondaryText
                                        )
                                    }

                                    Text(
                                        text = "₹${"%.2f".format(total)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SealRed
                                    )
                                }

                                if (index < expensesByCategory.size - 1) {
                                    HorizontalDivider(color = LedgerDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 14.dp))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

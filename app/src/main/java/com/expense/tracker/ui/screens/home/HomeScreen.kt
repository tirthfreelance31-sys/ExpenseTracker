package com.expense.tracker.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expense.tracker.data.local.entity.CategoryEntity
import com.expense.tracker.data.local.entity.TransactionEntity
import com.expense.tracker.data.model.TransactionType
import com.expense.tracker.data.model.WalletType
import com.expense.tracker.data.model.WalletWithBalance
import com.expense.tracker.ui.components.AnimatedRupeeAmount
import com.expense.tracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    wallets: List<WalletWithBalance>,
    recentTransactions: List<TransactionEntity>,
    recentExpenses: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    onNavigateToAddTransaction: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    onNavigateToWalletDetail: (Long) -> Unit,
    onNavigateToSummary: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val totalNetBalance = wallets.sumOf { it.currentBalance }
    val categoriesMap = categories.associateBy { it.id }
    val walletsMap = wallets.associateBy { it.id }

    Scaffold(
        containerColor = LedgerInk,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "PASSBOOK LEDGER",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RupeeGold,
                            letterSpacing = 1.2.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = SecondaryText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LedgerInk
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddTransaction,
                containerColor = RupeeGold,
                contentColor = LedgerInk,
                shape = RoundedCornerShape(8.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null, tint = LedgerInk) },
                text = { Text("Add Entry", fontWeight = FontWeight.Bold, fontFamily = IbmPlexSans) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // TOTAL NET BALANCE HERO CARD (Ledger Paper Surface)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = LedgerPaper),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LedgerDivider))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "TOTAL NET BALANCE",
                            style = MaterialTheme.typography.labelMedium,
                            color = SecondaryText,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AnimatedRupeeAmount(
                            targetAmount = totalNetBalance,
                            style = MaterialTheme.typography.headlineLarge,
                            color = RupeeGold
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = LedgerDivider)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Encrypted Local Ledger",
                                style = MaterialTheme.typography.bodySmall,
                                color = SecondaryText
                            )
                            Text(
                                text = "${wallets.size} Wallets",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                        }
                    }
                }
            }

            // MY WALLETS SECTION (Ruled Ledger Rows)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "ACCOUNTS & WALLETS",
                        style = MaterialTheme.typography.labelMedium,
                        color = SecondaryText,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = LedgerPaper,
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LedgerDivider))
                    ) {
                        Column {
                            wallets.forEachIndexed { index, wallet ->
                                val accentColor = when (wallet.name.lowercase()) {
                                    "upi" -> StampIndigo
                                    "cash" -> CurrencyGreen
                                    "savings" -> RupeeGold
                                    else -> RupeeGold
                                }

                                val walletIcon = when (wallet.name.lowercase()) {
                                    "cash" -> Icons.Default.Payments
                                    "savings" -> Icons.Default.Savings
                                    else -> Icons.Default.AccountBalanceWallet
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToWalletDetail(wallet.id) }
                                        .padding(vertical = 12.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Left Thin Accent Tab
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .height(36.dp)
                                                .background(accentColor, shape = RoundedCornerShape(2.dp))
                                        )

                                        Icon(
                                            imageVector = walletIcon,
                                            contentDescription = null,
                                            tint = accentColor,
                                            modifier = Modifier.size(20.dp)
                                        )

                                        Column {
                                            Text(
                                                text = wallet.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryText
                                            )
                                            Text(
                                                text = if (wallet.type == WalletType.DIGITAL) "Digital" else "Physical",
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
                                    HorizontalDivider(color = LedgerDivider, modifier = Modifier.padding(horizontal = 12.dp))
                                }
                            }
                        }
                    }
                }
            }

            // THIS WEEK'S SPEND SECTION (Ruled Ledger Row)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WEEKLY SUMMARY",
                            style = MaterialTheme.typography.labelMedium,
                            color = SecondaryText,
                            letterSpacing = 1.sp
                        )
                        TextButton(onClick = onNavigateToSummary) {
                            Text("Details", style = MaterialTheme.typography.labelMedium, color = RupeeGold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = RupeeGold, modifier = Modifier.size(14.dp))
                        }
                    }

                    val weeklyTotal = recentExpenses.sumOf { it.amount }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = LedgerPaper,
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LedgerDivider))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Total Spent (Past 7 Days)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SecondaryText
                                )
                                Text(
                                    text = "${recentExpenses.size} debit entries logged",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedText
                                )
                            }
                            Text(
                                text = "₹${"%.2f".format(weeklyTotal)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = SealRed
                            )
                        }
                    }
                }
            }

            // RECENT TRANSACTIONS SECTION (Ruled Ledger Format)
            item {
                Text(
                    text = "PASSBOOK ENTRIES",
                    style = MaterialTheme.typography.labelMedium,
                    color = SecondaryText,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (recentTransactions.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = LedgerPaper,
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LedgerDivider))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No entries logged yet",
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
                        color = LedgerPaper,
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LedgerDivider))
                    ) {
                        Column {
                            val displayedList = recentTransactions.take(5)
                            displayedList.forEachIndexed { index, tx ->
                                val category = categoriesMap[tx.categoryId]
                                val wallet = walletsMap[tx.walletId]
                                val isExpense = tx.type == TransactionType.EXPENSE
                                val isIncome = tx.type == TransactionType.INCOME
                                val isOpening = tx.type == TransactionType.OPENING_BALANCE
                                val isTransfer = tx.type == TransactionType.TRANSFER_OUT || tx.type == TransactionType.TRANSFER_IN
                                val isTransferOut = tx.type == TransactionType.TRANSFER_OUT

                                val amountColor = when {
                                    isExpense || isTransferOut -> SealRed
                                    isIncome || tx.type == TransactionType.TRANSFER_IN -> CurrencyGreen
                                    else -> PrimaryText
                                }

                                val prefix = when {
                                    isExpense || isTransferOut -> "-₹"
                                    isIncome || tx.type == TransactionType.TRANSFER_IN -> "+₹"
                                    else -> "₹"
                                }

                                val titleText = when {
                                    isOpening -> "Opening Balance"
                                    isTransfer -> if (isTransferOut) "Transfer Out" else "Transfer In"
                                    else -> category?.name ?: "General"
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onEditTransaction(tx.id) }
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = when {
                                                isIncome || tx.type == TransactionType.TRANSFER_IN -> Icons.Default.TrendingUp
                                                isExpense || isTransferOut -> Icons.Default.TrendingDown
                                                else -> Icons.Default.SwapHoriz
                                            },
                                            contentDescription = null,
                                            tint = amountColor,
                                            modifier = Modifier.size(20.dp)
                                        )

                                        Column {
                                            Text(
                                                text = titleText,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryText
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                wallet?.name?.let { wName ->
                                                    Text(
                                                        text = wName,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when (wName.lowercase()) {
                                                            "upi" -> StampIndigo
                                                            "cash" -> CurrencyGreen
                                                            "savings" -> RupeeGold
                                                            else -> RupeeGold
                                                        }
                                                    )
                                                }
                                                tx.note?.takeIf { it.isNotBlank() }?.let { note ->
                                                    Text(
                                                        text = "• $note",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = SecondaryText,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "$prefix${"%.2f".format(tx.amount)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = amountColor
                                        )
                                        Text(
                                            text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(tx.timestamp)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MutedText
                                        )
                                    }
                                }

                                if (index < displayedList.size - 1) {
                                    HorizontalDivider(color = LedgerDivider, modifier = Modifier.padding(horizontal = 14.dp))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

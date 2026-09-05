package com.expense.tracker.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                    Text(
                        text = "PASSBOOK LEDGER",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RupeeGold,
                        letterSpacing = 1.2.sp
                    )
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. TOTAL NET BALANCE HERO CARD (Ledger Paper Surface)
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    color = LedgerPaper,
                    border = BorderStroke(1.dp, LedgerDivider)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL NET BALANCE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryText,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "FOLIO 01 · LOCAL ENCRYPTED",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedText,
                                letterSpacing = 0.6.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        AnimatedRupeeAmount(
                            targetAmount = totalNetBalance,
                            style = MaterialTheme.typography.headlineLarge,
                            color = RupeeGold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = LedgerDivider, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "STATUS: RECONCILED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = CurrencyGreen,
                                letterSpacing = 0.5.sp
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

            // 2. ACCOUNTS & WALLETS SECTION (Ruled Ledger Rows)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LedgerSectionHeader(title = "ACCOUNTS & FOLIOS")

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = LedgerPaper,
                        shape = RoundedCornerShape(3.dp),
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

                                val walletIcon = when (wallet.name.lowercase()) {
                                    "cash" -> Icons.Default.Payments
                                    "savings" -> Icons.Default.Savings
                                    else -> Icons.Default.AccountBalanceWallet
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToWalletDetail(wallet.id) }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Left Thin Accent Tab
                                        Box(
                                            modifier = Modifier
                                                .width(3.dp)
                                                .height(28.dp)
                                                .background(accentColor, shape = RoundedCornerShape(1.dp))
                                        )

                                        Icon(
                                            imageVector = walletIcon,
                                            contentDescription = null,
                                            tint = accentColor,
                                            modifier = Modifier.size(18.dp)
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
                                    HorizontalDivider(
                                        color = LedgerDivider,
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. WEEKLY TALLY SECTION (Compact Ledger Tally Strip)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LedgerSectionHeader(title = "WEEKLY TALLY")

                    val weeklyTotal = recentExpenses.sumOf { it.amount }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = LedgerPaper,
                        shape = RoundedCornerShape(3.dp),
                        border = BorderStroke(1.dp, LedgerDivider)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(26.dp)
                                        .background(SealRed, shape = RoundedCornerShape(1.dp))
                                )
                                Column {
                                    Text(
                                        text = "OUTGOINGS (PAST 7 DAYS)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SecondaryText,
                                        letterSpacing = 0.6.sp
                                    )
                                    Text(
                                        text = if (recentExpenses.isEmpty()) "0 debit entries" else "${recentExpenses.size} debit ${if (recentExpenses.size == 1) "entry" else "entries"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedText
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "₹${"%.2f".format(weeklyTotal)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (weeklyTotal > 0.0) SealRed else SecondaryText
                                )
                                IconButton(
                                    onClick = onNavigateToSummary,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Weekly Summary",
                                        tint = RupeeGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. PASSBOOK ENTRIES (Recent Transactions with Typographic Accounting Stamps)
            item {
                LedgerSectionHeader(title = "PASSBOOK ENTRIES")
            }

            if (recentTransactions.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = LedgerPaper,
                        shape = RoundedCornerShape(3.dp),
                        border = BorderStroke(1.dp, LedgerDivider)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No entries logged in passbook",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SecondaryText
                            )
                        }
                    }
                }
            } else {
                item {
                    // Prioritize user transactions over system Opening Balance audit entries
                    val normalTransactions = recentTransactions.filter { it.type != TransactionType.OPENING_BALANCE }
                    val openingTransactions = recentTransactions.filter { it.type == TransactionType.OPENING_BALANCE }
                    val displayedList = if (normalTransactions.isNotEmpty()) {
                        (normalTransactions + openingTransactions).take(5)
                    } else {
                        recentTransactions.take(5)
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = LedgerPaper,
                        shape = RoundedCornerShape(3.dp),
                        border = BorderStroke(1.dp, LedgerDivider)
                    ) {
                        Column {
                            displayedList.forEachIndexed { index, tx ->
                                val category = categoriesMap[tx.categoryId]
                                val wallet = walletsMap[tx.walletId]
                                val isExpense = tx.type == TransactionType.EXPENSE
                                val isIncome = tx.type == TransactionType.INCOME
                                val isOpening = tx.type == TransactionType.OPENING_BALANCE
                                val isTransferOut = tx.type == TransactionType.TRANSFER_OUT
                                val isTransferIn = tx.type == TransactionType.TRANSFER_IN
                                val isTransfer = isTransferOut || isTransferIn

                                val amountColor = when {
                                    isExpense || isTransferOut -> SealRed
                                    isIncome || isTransferIn -> CurrencyGreen
                                    else -> SecondaryText
                                }

                                val prefix = when {
                                    isExpense || isTransferOut -> "-₹"
                                    isIncome || isTransferIn -> "+₹"
                                    else -> "₹"
                                }

                                val titleText = when {
                                    isOpening -> "Opening Balance"
                                    isTransfer -> if (isTransferOut) "Transfer Out" else "Transfer In"
                                    else -> category?.name ?: "General"
                                }

                                val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                    .format(Date(tx.timestamp))
                                    .uppercase()

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onEditTransaction(tx.id) }
                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Typographic Accounting Stamp (DR / CR / TR / OB)
                                        LedgerTransactionStamp(
                                            type = tx.type,
                                            modifier = Modifier.padding(top = 1.dp)
                                        )

                                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                            Text(
                                                text = titleText,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isOpening) SecondaryText else PrimaryText
                                            )
                                            // Metadata line: Wallet · 04 SEP 2026
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                wallet?.name?.let { wName ->
                                                    Text(
                                                        text = wName,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = when (wName.lowercase()) {
                                                            "upi" -> StampIndigo
                                                            "cash" -> CurrencyGreen
                                                            "savings" -> RupeeGold
                                                            else -> RupeeGold
                                                        }
                                                    )
                                                    Text(
                                                        text = "·",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MutedText
                                                    )
                                                }
                                                Text(
                                                    text = formattedDate,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (isOpening) MutedText else SecondaryText
                                                )
                                            }
                                            // Optional note preview
                                            tx.note?.takeIf { it.isNotBlank() }?.let { note ->
                                                Text(
                                                    text = note,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MutedText,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    // Right-aligned financial value in Space Grotesk tabular figures
                                    Text(
                                        text = "$prefix${"%.2f".format(tx.amount)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = amountColor,
                                        modifier = Modifier.padding(top = 1.dp)
                                    )
                                }

                                if (index < displayedList.size - 1) {
                                    HorizontalDivider(
                                        color = LedgerDivider,
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom spacer so FAB doesn't obscure the last entry when scrolling
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

/**
 * Structural ledger section header with hairlines
 */
@Composable
private fun LedgerSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.width(16.dp),
            thickness = 1.dp,
            color = LedgerDivider
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = SecondaryText,
            letterSpacing = 1.sp
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = LedgerDivider
        )
    }
}

/**
 * Compact accounting typographic stamp:
 * DR = Expense / Outgoing (Seal Red)
 * CR = Income / Incoming (Currency Green)
 * TR = Transfer (Stamp Indigo)
 * OB = Opening Balance audit entry (Muted)
 */
@Composable
private fun LedgerTransactionStamp(
    type: TransactionType,
    modifier: Modifier = Modifier
) {
    val (stampText, stampColor) = when (type) {
        TransactionType.EXPENSE -> "DR" to SealRed
        TransactionType.INCOME -> "CR" to CurrencyGreen
        TransactionType.TRANSFER_OUT, TransactionType.TRANSFER_IN -> "TR" to StampIndigo
        TransactionType.OPENING_BALANCE -> "OB" to MutedText
    }

    Surface(
        modifier = modifier,
        color = stampColor.copy(alpha = 0.12f),
        shape = RoundedCornerShape(3.dp),
        border = BorderStroke(1.dp, stampColor.copy(alpha = 0.35f))
    ) {
        Text(
            text = stampText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = stampColor,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
        )
    }
}

package com.expense.tracker.ui.screens.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.expense.tracker.data.model.WalletWithBalance
import com.expense.tracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    transactions: List<TransactionEntity> = emptyList(),
    wallets: List<WalletWithBalance> = emptyList(),
    categories: List<CategoryEntity> = emptyList(),
    onEditTransaction: (Long) -> Unit = {}
) {
    val categoriesMap = categories.associateBy { it.id }
    val walletsMap = wallets.associateBy { it.id }

    val totalDebits = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val totalCredits = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

    Scaffold(
        containerColor = LedgerInk,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "TRANSACTION AUDIT LOG",
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
            // Audit Overview Summary Bar
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    color = LedgerPaper,
                    border = BorderStroke(1.dp, LedgerDivider)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CHRONOLOGICAL REGISTER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryText,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${transactions.size} ENTRIES",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText,
                                letterSpacing = 0.5.sp
                            )
                        }

                        HorizontalDivider(color = LedgerDivider, thickness = 0.5.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Total Inflow (CR)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SecondaryText
                                )
                                Text(
                                    text = "+₹${"%.2f".format(totalCredits)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CurrencyGreen
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Total Outflow (DR)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SecondaryText
                                )
                                Text(
                                    text = "-₹${"%.2f".format(totalDebits)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SealRed
                                )
                            }
                        }
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.width(16.dp), thickness = 1.dp, color = LedgerDivider)
                    Text(
                        text = "LEDGER PASSBOOK ENTRIES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryText,
                        letterSpacing = 1.sp
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = LedgerDivider)
                }
            }

            if (transactions.isEmpty()) {
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
                                .padding(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No ledger entries recorded yet",
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
                        shape = RoundedCornerShape(3.dp),
                        border = BorderStroke(1.dp, LedgerDivider)
                    ) {
                        Column {
                            transactions.forEachIndexed { index, tx ->
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
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Accounting Typographic Stamp
                                        LedgerStamp(type = tx.type, modifier = Modifier.padding(top = 1.dp))

                                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                            Text(
                                                text = titleText,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isOpening) SecondaryText else PrimaryText
                                            )
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
                                                    Text(text = "·", style = MaterialTheme.typography.bodySmall, color = MutedText)
                                                }
                                                Text(
                                                    text = formattedDate,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (isOpening) MutedText else SecondaryText
                                                )
                                            }
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

                                    Text(
                                        text = "$prefix${"%.2f".format(tx.amount)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = amountColor,
                                        modifier = Modifier.padding(top = 1.dp)
                                    )
                                }

                                if (index < transactions.size - 1) {
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

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun LedgerStamp(
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

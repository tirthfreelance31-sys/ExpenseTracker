package com.expense.tracker.ui.screens.walletdetail

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expense.tracker.data.local.entity.CategoryEntity
import com.expense.tracker.data.local.entity.TransactionEntity
import com.expense.tracker.data.model.TransactionType
import com.expense.tracker.data.model.WalletType
import com.expense.tracker.data.model.WalletWithBalance
import com.expense.tracker.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletDetailScreen(
    wallet: WalletWithBalance?,
    transactions: List<TransactionEntity> = emptyList(),
    categories: List<CategoryEntity> = emptyList(),
    onReconcileOpeningBalance: suspend (Long, Double) -> Unit,
    onEditTransaction: (Long) -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showReconcileDialog by remember { mutableStateOf(false) }
    var newBalanceInput by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val accentColor = when (wallet?.name?.lowercase()) {
        "upi" -> StampIndigo
        "cash" -> CurrencyGreen
        "savings" -> RupeeGold
        else -> RupeeGold
    }

    val walletIcon = when (wallet?.name?.lowercase()) {
        "cash" -> Icons.Default.Payments
        "savings" -> Icons.Default.Savings
        else -> Icons.Default.AccountBalanceWallet
    }

    val categoriesMap = categories.associateBy { it.id }

    Scaffold(
        containerColor = LedgerInk,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${wallet?.name?.uppercase() ?: "ACCOUNT"} FOLIO",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RupeeGold,
                        letterSpacing = 1.2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SecondaryText
                        )
                    }
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
            if (wallet != null) {
                // Hero Folio Statement Card
                item {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = LedgerPaper,
                        border = BorderStroke(1.dp, LedgerDivider),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(36.dp)
                                        .background(accentColor, RoundedCornerShape(2.dp))
                                )
                                Icon(
                                    imageVector = walletIcon,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = wallet.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryText
                                    )
                                    Text(
                                        text = if (wallet.type == WalletType.DIGITAL) "Digital Account Folio" else "Physical Currency Folio",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SecondaryText
                                    )
                                }
                            }

                            HorizontalDivider(color = LedgerDivider, thickness = 0.5.dp)

                            // Live Calculated Balance
                            Column {
                                Text(
                                    text = "CALCULATED LIVE BALANCE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryText,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "₹${"%.2f".format(wallet.currentBalance)}",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = RupeeGold
                                )
                            }

                            HorizontalDivider(color = LedgerDivider, thickness = 0.5.dp)

                            // Opening Balance Audit & Reconcile Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "AUDIT OPENING BALANCE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SecondaryText,
                                        letterSpacing = 0.8.sp
                                    )
                                    Text(
                                        text = "₹${"%.2f".format(wallet.openingBalance)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryText
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        newBalanceInput = wallet.openingBalance.toString()
                                        showReconcileDialog = true
                                    },
                                    shape = RoundedCornerShape(3.dp),
                                    border = BorderStroke(1.dp, RupeeGold),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RupeeGold)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("RECONCILE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Section Header: Folio Ledger Entries
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(modifier = Modifier.width(16.dp), thickness = 1.dp, color = LedgerDivider)
                        Text(
                            text = "FOLIO TRANSACTION REGISTER",
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
                                    text = "No transactions recorded for this folio",
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
                                transactions.forEachIndexed { index, tx ->
                                    val category = categoriesMap[tx.categoryId]
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
                                            LedgerDetailStamp(type = tx.type, modifier = Modifier.padding(top = 1.dp))

                                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                                Text(
                                                    text = titleText,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isOpening) SecondaryText else PrimaryText
                                                )
                                                Text(
                                                    text = formattedDate,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (isOpening) MutedText else SecondaryText
                                                )
                                                tx.note?.takeIf { it.isNotBlank() }?.let { note ->
                                                    Text(
                                                        text = note,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MutedText
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
                                        HorizontalDivider(color = LedgerDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 12.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            } else {
                item {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Account folio loading...", color = SecondaryText)
                    }
                }
            }
        }

        // Reconcile Dialog
        if (showReconcileDialog && wallet != null) {
            AlertDialog(
                onDismissRequest = { if (!isSaving) showReconcileDialog = false },
                containerColor = LedgerPaper,
                shape = RoundedCornerShape(4.dp),
                title = {
                    Text(
                        "Reconcile Account Folio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Current Recorded Baseline: ₹${"%.2f".format(wallet.openingBalance)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecondaryText
                        )
                        OutlinedTextField(
                            value = newBalanceInput,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.matches(Regex("""^\d*\.?\d*$"""))) {
                                    newBalanceInput = newValue
                                }
                            },
                            label = { Text("Reconciled Opening Balance", style = MaterialTheme.typography.bodySmall) },
                            prefix = { Text("₹ ", style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, color = RupeeGold)) },
                            textStyle = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, color = PrimaryText),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RupeeGold,
                                unfocusedBorderColor = LedgerDivider,
                                focusedContainerColor = LedgerInk,
                                unfocusedContainerColor = LedgerInk,
                                cursorColor = RupeeGold
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(3.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val parsed = newBalanceInput.trim().toDoubleOrNull()
                            if (parsed == null || parsed < 0) {
                                Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                            } else {
                                isSaving = true
                                coroutineScope.launch {
                                    onReconcileOpeningBalance(wallet.id, parsed)
                                    isSaving = false
                                    showReconcileDialog = false
                                    Toast.makeText(context, "Opening balance reconciled!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = RupeeGold, contentColor = LedgerInk),
                        shape = RoundedCornerShape(3.dp)
                    ) {
                        Text("SAVE ADJUSTMENT", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showReconcileDialog = false },
                        enabled = !isSaving,
                        shape = RoundedCornerShape(3.dp)
                    ) {
                        Text("CANCEL", color = SecondaryText)
                    }
                }
            )
        }
    }
}

@Composable
private fun LedgerDetailStamp(
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

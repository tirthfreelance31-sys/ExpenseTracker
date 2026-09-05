package com.expense.tracker.ui.screens.addtransaction

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expense.tracker.data.local.entity.CategoryEntity
import com.expense.tracker.data.local.entity.TransactionEntity
import com.expense.tracker.data.model.TransactionType
import com.expense.tracker.data.model.WalletWithBalance
import com.expense.tracker.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TransactionTab {
    EXPENSE, INCOME, TRANSFER
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionScreen(
    editingTransactionId: Long? = null,
    wallets: List<WalletWithBalance>,
    categories: List<CategoryEntity>,
    onSaveExpense: suspend (amount: Double, walletId: Long, categoryId: Long, note: String?, timestamp: Long, editingId: Long?) -> Unit,
    onSaveIncome: suspend (amount: Double, walletId: Long, categoryId: Long?, note: String?, timestamp: Long, editingId: Long?) -> Unit,
    onSaveTransfer: suspend (amount: Double, fromWalletId: Long, toWalletId: Long, note: String?, timestamp: Long, editingFromTxId: Long?, editingToTxId: Long?, linkedId: Long?) -> Unit,
    onDeleteTransaction: suspend (txId: Long, linkedTransferId: Long?) -> Unit,
    onFetchTransactionDetails: suspend (txId: Long) -> Pair<TransactionEntity, TransactionEntity?>?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(TransactionTab.EXPENSE) }
    var amountInput by remember { mutableStateOf("") }
    var selectedWalletId by remember { mutableStateOf<Long?>(null) }
    var selectedFromWalletId by remember { mutableStateOf<Long?>(null) }
    var selectedToWalletId by remember { mutableStateOf<Long?>(null) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var noteInput by remember { mutableStateOf("") }
    var timestamp by remember { mutableStateOf(System.currentTimeMillis()) }

    var isEditMode by remember { mutableStateOf(false) }
    var editingMainTx by remember { mutableStateOf<TransactionEntity?>(null) }
    var editingLinkedTx by remember { mutableStateOf<TransactionEntity?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Set default wallet & category selection when wallets/categories load
    LaunchedEffect(wallets) {
        if (selectedWalletId == null && wallets.isNotEmpty()) {
            selectedWalletId = wallets.first().id
            selectedFromWalletId = wallets.first().id
            selectedToWalletId = wallets.getOrNull(1)?.id ?: wallets.first().id
        }
    }

    LaunchedEffect(categories) {
        if (selectedCategoryId == null && categories.isNotEmpty()) {
            selectedCategoryId = categories.first().id
        }
    }

    // Load existing transaction for editing
    LaunchedEffect(editingTransactionId) {
        if (editingTransactionId != null) {
            val pair = onFetchTransactionDetails(editingTransactionId)
            if (pair != null) {
                isEditMode = true
                val mainTx = pair.first
                val linkedTx = pair.second
                editingMainTx = mainTx
                editingLinkedTx = linkedTx

                amountInput = mainTx.amount.toString()
                noteInput = mainTx.note.orEmpty()
                timestamp = mainTx.timestamp

                when (mainTx.type) {
                    TransactionType.EXPENSE -> {
                        selectedTab = TransactionTab.EXPENSE
                        selectedWalletId = mainTx.walletId
                        selectedCategoryId = mainTx.categoryId ?: categories.firstOrNull()?.id
                    }
                    TransactionType.INCOME -> {
                        selectedTab = TransactionTab.INCOME
                        selectedWalletId = mainTx.walletId
                        selectedCategoryId = mainTx.categoryId
                    }
                    TransactionType.TRANSFER_OUT -> {
                        selectedTab = TransactionTab.TRANSFER
                        selectedFromWalletId = mainTx.walletId
                        selectedToWalletId = linkedTx?.walletId ?: wallets.find { it.id != mainTx.walletId }?.id
                    }
                    TransactionType.TRANSFER_IN -> {
                        selectedTab = TransactionTab.TRANSFER
                        selectedFromWalletId = linkedTx?.walletId ?: wallets.find { it.id != mainTx.walletId }?.id
                        selectedToWalletId = mainTx.walletId
                    }
                    TransactionType.OPENING_BALANCE -> {
                        Toast.makeText(context, "Opening Balance transactions should be edited via Wallet Detail Reconcile", Toast.LENGTH_LONG).show()
                        onNavigateBack()
                    }
                }
            }
        }
    }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Scaffold(
        containerColor = LedgerInk,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "EDIT LEDGER ENTRY" else "RECORD ENTRY",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RupeeGold,
                        letterSpacing = 1.2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SecondaryText
                        )
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Entry",
                                tint = SealRed
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LedgerInk
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Accounting Classification Tabs (DR · Expense | CR · Income | TR · Transfer)
            Surface(
                shape = RoundedCornerShape(3.dp),
                color = LedgerPaper,
                border = BorderStroke(1.dp, LedgerDivider),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    val tabs = listOf(
                        Triple(TransactionTab.EXPENSE, "DR · EXPENSE", SealRed),
                        Triple(TransactionTab.INCOME, "CR · INCOME", CurrencyGreen),
                        Triple(TransactionTab.TRANSFER, "TR · TRANSFER", StampIndigo)
                    )

                    tabs.forEach { (tab, title, accentColor) ->
                        val isSelected = selectedTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (!isEditMode) {
                                        selectedTab = tab
                                    } else {
                                        Toast.makeText(context, "Transaction type cannot be changed when editing", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .background(if (isSelected) accentColor.copy(alpha = 0.15f) else Color.Transparent)
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) accentColor else SecondaryText,
                                    letterSpacing = 0.6.sp
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Box(
                                    modifier = Modifier
                                        .width(28.dp)
                                        .height(2.dp)
                                        .background(if (isSelected) accentColor else Color.Transparent, RoundedCornerShape(1.dp))
                                )
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Amount Field (Hero Accounting Entry)
                item {
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.matches(Regex("""^\d*\.?\d*$"""))) {
                                amountInput = newValue
                            }
                        },
                        label = { Text("Financial Amount *", style = MaterialTheme.typography.bodySmall) },
                        prefix = {
                            Text(
                                text = "₹ ",
                                style = TextStyle(
                                    fontFamily = SpaceGrotesk,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = when (selectedTab) {
                                        TransactionTab.EXPENSE -> SealRed
                                        TransactionTab.INCOME -> CurrencyGreen
                                        TransactionTab.TRANSFER -> StampIndigo
                                    }
                                )
                            )
                        },
                        textStyle = TextStyle(
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = PrimaryText,
                            fontFeatureSettings = "tnum"
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RupeeGold,
                            unfocusedBorderColor = LedgerDivider,
                            focusedContainerColor = LedgerPaper,
                            unfocusedContainerColor = LedgerPaper,
                            cursorColor = RupeeGold,
                            focusedLabelColor = RupeeGold,
                            unfocusedLabelColor = SecondaryText
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(3.dp)
                    )
                }

                // Wallet Selection for Expense / Income
                if (selectedTab != TransactionTab.TRANSFER) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                HorizontalDivider(modifier = Modifier.width(16.dp), thickness = 1.dp, color = LedgerDivider)
                                Text(
                                    text = "SELECT ACCOUNT FOLIO",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryText,
                                    letterSpacing = 1.sp
                                )
                                HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = LedgerDivider)
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                wallets.forEach { wallet ->
                                    val isSelected = selectedWalletId == wallet.id
                                    val accentColor = when (wallet.name.lowercase()) {
                                        "upi" -> StampIndigo
                                        "cash" -> CurrencyGreen
                                        "savings" -> RupeeGold
                                        else -> RupeeGold
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedWalletId = wallet.id },
                                        shape = RoundedCornerShape(3.dp),
                                        color = if (isSelected) LedgerPaperVariant else LedgerPaper,
                                        border = BorderStroke(1.dp, if (isSelected) RupeeGold else LedgerDivider)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(3.dp)
                                                    .height(18.dp)
                                                    .background(accentColor, RoundedCornerShape(1.dp))
                                            )
                                            Column {
                                                Text(
                                                    text = wallet.name,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) PrimaryText else SecondaryText
                                                )
                                                Text(
                                                    text = "₹${"%.0f".format(wallet.currentBalance)}",
                                                    style = TextStyle(
                                                        fontFamily = SpaceGrotesk,
                                                        fontSize = 11.sp,
                                                        color = MutedText,
                                                        fontFeatureSettings = "tnum"
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // From Wallet & To Wallet for Transfer
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                HorizontalDivider(modifier = Modifier.width(16.dp), thickness = 1.dp, color = LedgerDivider)
                                Text(
                                    text = "TRANSFER ROUTE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryText,
                                    letterSpacing = 1.sp
                                )
                                HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = LedgerDivider)
                            }

                            // Source Wallet
                            Text(
                                text = "FROM FOLIO (OUTGOING):",
                                style = MaterialTheme.typography.labelSmall,
                                color = SecondaryText,
                                letterSpacing = 0.5.sp
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                wallets.forEach { wallet ->
                                    val isSelected = selectedFromWalletId == wallet.id
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedFromWalletId = wallet.id },
                                        shape = RoundedCornerShape(3.dp),
                                        color = if (isSelected) LedgerPaperVariant else LedgerPaper,
                                        border = BorderStroke(1.dp, if (isSelected) StampIndigo else LedgerDivider)
                                    ) {
                                        Text(
                                            text = wallet.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) PrimaryText else SecondaryText,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }

                            // Destination Wallet
                            Text(
                                text = "TO FOLIO (INCOMING):",
                                style = MaterialTheme.typography.labelSmall,
                                color = SecondaryText,
                                letterSpacing = 0.5.sp
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                wallets.forEach { wallet ->
                                    val isSelected = selectedToWalletId == wallet.id
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedToWalletId = wallet.id },
                                        shape = RoundedCornerShape(3.dp),
                                        color = if (isSelected) LedgerPaperVariant else LedgerPaper,
                                        border = BorderStroke(1.dp, if (isSelected) CurrencyGreen else LedgerDivider)
                                    ) {
                                        Text(
                                            text = wallet.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) PrimaryText else SecondaryText,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Category Selection for Expense (or optional Income)
                if (selectedTab == TransactionTab.EXPENSE) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                HorizontalDivider(modifier = Modifier.width(16.dp), thickness = 1.dp, color = LedgerDivider)
                                Text(
                                    text = "CLASSIFICATION",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryText,
                                    letterSpacing = 1.sp
                                )
                                HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = LedgerDivider)
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                categories.forEach { category ->
                                    val isSelected = selectedCategoryId == category.id
                                    Surface(
                                        modifier = Modifier.clickable { selectedCategoryId = category.id },
                                        shape = RoundedCornerShape(3.dp),
                                        color = if (isSelected) RupeeGold.copy(alpha = 0.15f) else LedgerPaper,
                                        border = BorderStroke(1.dp, if (isSelected) RupeeGold else LedgerDivider)
                                    ) {
                                        Text(
                                            text = category.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) RupeeGold else PrimaryText,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Optional Note Field
                item {
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Particulars / Note (Optional)", style = MaterialTheme.typography.bodySmall) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = PrimaryText),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RupeeGold,
                            unfocusedBorderColor = LedgerDivider,
                            focusedContainerColor = LedgerPaper,
                            unfocusedContainerColor = LedgerPaper,
                            cursorColor = RupeeGold,
                            focusedLabelColor = RupeeGold,
                            unfocusedLabelColor = SecondaryText
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(3.dp)
                    )
                }

                // Date & Time Picker Row
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showDateTimePicker(context, timestamp) { newTimestamp ->
                                    timestamp = newTimestamp
                                }
                            },
                        shape = RoundedCornerShape(3.dp),
                        color = LedgerPaper,
                        border = BorderStroke(1.dp, LedgerDivider)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = RupeeGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = "ENTRY TIMESTAMP",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SecondaryText,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = dateFormatter.format(Date(timestamp)).uppercase(),
                                        style = TextStyle(
                                            fontFamily = SpaceGrotesk,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = PrimaryText,
                                            fontFeatureSettings = "tnum"
                                        )
                                    )
                                }
                            }
                            Text(
                                text = "ADJUST",
                                style = MaterialTheme.typography.labelSmall,
                                color = RupeeGold,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }
            }

            // Action Buttons
            Button(
                onClick = {
                    if (isSaving) return@Button

                    val amount = amountInput.trim().toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        Toast.makeText(context, "Please enter a valid amount greater than ₹0", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    when (selectedTab) {
                        TransactionTab.EXPENSE -> {
                            val walletId = selectedWalletId
                            val categoryId = selectedCategoryId
                            if (walletId == null) {
                                Toast.makeText(context, "Please select a wallet", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (categoryId == null) {
                                Toast.makeText(context, "Please select a category for Expense", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isSaving = true
                            coroutineScope.launch {
                                onSaveExpense(
                                    amount, walletId, categoryId, noteInput.ifBlank { null }, timestamp,
                                    if (isEditMode) editingMainTx?.id else null
                                )
                                isSaving = false
                                Toast.makeText(context, if (isEditMode) "Ledger entry updated!" else "Ledger entry committed!", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }
                        }
                        TransactionTab.INCOME -> {
                            val walletId = selectedWalletId
                            if (walletId == null) {
                                Toast.makeText(context, "Please select a wallet", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isSaving = true
                            coroutineScope.launch {
                                onSaveIncome(
                                    amount, walletId, selectedCategoryId, noteInput.ifBlank { null }, timestamp,
                                    if (isEditMode) editingMainTx?.id else null
                                )
                                isSaving = false
                                Toast.makeText(context, if (isEditMode) "Ledger entry updated!" else "Ledger entry committed!", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }
                        }
                        TransactionTab.TRANSFER -> {
                            val fromId = selectedFromWalletId
                            val toId = selectedToWalletId
                            if (fromId == null || toId == null) {
                                Toast.makeText(context, "Please select both From and To wallets", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (fromId == toId) {
                                Toast.makeText(context, "From Wallet and To Wallet cannot be the same", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isSaving = true
                            coroutineScope.launch {
                                val fromTxId = if (isEditMode) {
                                    if (editingMainTx?.type == TransactionType.TRANSFER_OUT) editingMainTx?.id else editingLinkedTx?.id
                                } else null

                                val toTxId = if (isEditMode) {
                                    if (editingMainTx?.type == TransactionType.TRANSFER_IN) editingMainTx?.id else editingLinkedTx?.id
                                } else null

                                val linkedId = editingMainTx?.linkedTransferId ?: editingMainTx?.id

                                onSaveTransfer(
                                    amount, fromId, toId, noteInput.ifBlank { null }, timestamp,
                                    fromTxId, toTxId, linkedId
                                )
                                isSaving = false
                                Toast.makeText(context, if (isEditMode) "Transfer updated!" else "Transfer committed!", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }
                        }
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RupeeGold,
                    contentColor = LedgerInk
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = LedgerInk, strokeWidth = 2.dp)
                } else {
                    Text(
                        text = if (isEditMode) "UPDATE ENTRY" else "COMMIT ENTRY",
                        fontFamily = IbmPlexSans,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Confirmation Dialog for Deletion
        if (showDeleteConfirmDialog && editingMainTx != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                containerColor = LedgerPaper,
                shape = RoundedCornerShape(4.dp),
                title = {
                    Text(
                        "Delete Ledger Entry?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                },
                text = {
                    Text(
                        if (selectedTab == TransactionTab.TRANSFER)
                            "This will atomically delete both linked transfer rows. Wallet balances will be recalculated."
                        else
                            "Are you sure you want to delete this ledger entry? Live balances will be automatically recalculated.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val txId = editingMainTx!!.id
                                val linkedId = editingMainTx!!.linkedTransferId
                                onDeleteTransaction(txId, linkedId)
                                showDeleteConfirmDialog = false
                                Toast.makeText(context, "Transaction deleted!", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SealRed, contentColor = PrimaryText),
                        shape = RoundedCornerShape(3.dp)
                    ) {
                        Text("Delete", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirmDialog = false },
                        shape = RoundedCornerShape(3.dp)
                    ) {
                        Text("Cancel", color = SecondaryText)
                    }
                }
            )
        }
    }
}

// Native Date/Time Picker Dialog Helper
private fun showDateTimePicker(context: Context, initialTimestamp: Long, onDateTimeSelected: (Long) -> Unit) {
    val calendar = Calendar.getInstance().apply { timeInMillis = initialTimestamp }
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    onDateTimeSelected(calendar.timeInMillis)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

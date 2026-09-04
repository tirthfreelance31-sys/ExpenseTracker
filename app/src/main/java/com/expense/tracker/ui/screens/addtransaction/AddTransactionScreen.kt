package com.expense.tracker.ui.screens.addtransaction

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expense.tracker.data.local.entity.CategoryEntity
import com.expense.tracker.data.local.entity.TransactionEntity
import com.expense.tracker.data.model.TransactionType
import com.expense.tracker.data.model.WalletType
import com.expense.tracker.data.model.WalletWithBalance
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
    var timestamp by remember { mutableStateLongStateOf(System.currentTimeMillis()) }

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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Edit Transaction" else "Add Transaction",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Segmented Control (Expense | Income | Transfer)
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                TransactionTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = {
                            if (!isEditMode) {
                                selectedTab = tab
                            } else {
                                Toast.makeText(context, "Transaction type cannot be changed when editing", Toast.LENGTH_SHORT).show()
                            }
                        },
                        text = {
                            Text(
                                text = tab.name.lowercase().capitalize(Locale.getDefault()),
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 15.sp
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Amount Field
                item {
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.matches(Regex("""^\d*\.?\d*$"""))) {
                                amountInput = newValue
                            }
                        },
                        label = { Text("Amount *") },
                        prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Wallet Selection for Expense / Income
                if (selectedTab != TransactionTab.TRANSFER) {
                    item {
                        Text(
                            text = "Select Wallet *",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            wallets.forEach { wallet ->
                                val isSelected = selectedWalletId == wallet.id
                                val accentColor = when (wallet.name.lowercase()) {
                                    "upi" -> Color(0xFF2196F3)
                                    "cash" -> Color(0xFF4CAF50)
                                    "savings" -> Color(0xFFFF9800)
                                    else -> Color(android.graphics.Color.parseColor(wallet.colorHex.ifEmpty { "#2196F3" }))
                                }

                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedWalletId = wallet.id },
                                    label = { Text(wallet.name, fontWeight = FontWeight.SemiBold) },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(accentColor)
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                } else {
                    // From Wallet & To Wallet for Transfer
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "From Wallet *",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                wallets.forEach { wallet ->
                                    val isSelected = selectedFromWalletId == wallet.id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedFromWalletId = wallet.id },
                                        label = { Text(wallet.name) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Text(
                                text = "To Wallet *",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                wallets.forEach { wallet ->
                                    val isSelected = selectedToWalletId == wallet.id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedToWalletId = wallet.id },
                                        label = { Text(wallet.name) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Category Selection for Expense (or optional Income)
                if (selectedTab == TransactionTab.EXPENSE) {
                    item {
                        Text(
                            text = "Select Category *",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categories.forEach { category ->
                                val isSelected = selectedCategoryId == category.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategoryId = category.id },
                                    label = { Text(category.name) }
                                )
                            }
                        }
                    }
                }

                // Optional Note Field
                item {
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Note (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Date & Time Picker Row
                item {
                    Card(
                        onClick = {
                            showDateTimePicker(context, timestamp) { newTimestamp ->
                                timestamp = newTimestamp
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null)
                                Column {
                                    Text(
                                        text = "Date & Time",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = dateFormatter.format(Date(timestamp)),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Text(
                                text = "Change",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
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
                                Toast.makeText(context, if (isEditMode) "Expense updated!" else "Expense added!", Toast.LENGTH_SHORT).show()
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
                                Toast.makeText(context, if (isEditMode) "Income updated!" else "Income added!", Toast.LENGTH_SHORT).show()
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
                                Toast.makeText(context, if (isEditMode) "Transfer updated!" else "Transfer added!", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(
                        text = if (isEditMode) "Save Changes" else "Save Transaction",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Confirmation Dialog for Deletion
        if (showDeleteConfirmDialog && editingMainTx != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Delete Transaction?", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        if (selectedTab == TransactionTab.TRANSFER)
                            "This will delete both linked transfer rows. Wallet balances will be recalculated."
                        else
                            "Are you sure you want to delete this transaction? Wallet balance will be recalculated."
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
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancel")
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

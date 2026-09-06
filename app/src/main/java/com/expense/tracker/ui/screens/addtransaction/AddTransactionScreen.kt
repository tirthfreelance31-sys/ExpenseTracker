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
import androidx.compose.foundation.shape.CircleShape
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
    val colors = AppTheme.colors

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
                        Toast.makeText(context, "Opening Balance transactions should be edited via Wallet Details", Toast.LENGTH_LONG).show()
                        onNavigateBack()
                    }
                }
            }
        }
    }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    val tabAccentColor = when (selectedTab) {
        TransactionTab.EXPENSE -> colors.expense
        TransactionTab.INCOME -> colors.income
        TransactionTab.TRANSFER -> colors.primary
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Edit Transaction" else "Add Transaction",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp
                        ),
                        color = colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = colors.expense
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Modern Type Selector Tabs
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    val tabs = listOf(
                        Triple(TransactionTab.EXPENSE, "Expense", colors.expense),
                        Triple(TransactionTab.INCOME, "Income", colors.income),
                        Triple(TransactionTab.TRANSFER, "Transfer", colors.primary)
                    )

                    tabs.forEach { (tab, title, accent) ->
                        val isSelected = selectedTab == tab
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (!isEditMode) {
                                        selectedTab = tab
                                    } else {
                                        Toast.makeText(context, "Transaction type cannot be changed when editing", Toast.LENGTH_SHORT).show()
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) accent.copy(alpha = 0.12f) else Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = Manrope,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    ),
                                    color = if (isSelected) accent else colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Large Amount Input Field
                item {
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.matches(Regex("""^\d*\.?\d*$"""))) {
                                amountInput = newValue
                            }
                        },
                        label = { Text("Amount *", style = MaterialTheme.typography.bodySmall.copy(fontFamily = Inter)) },
                        prefix = {
                            Text(
                                text = "₹ ",
                                style = TextStyle(
                                    fontFamily = SpaceGrotesk,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 24.sp,
                                    color = tabAccentColor
                                )
                            )
                        },
                        textStyle = TextStyle(
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp,
                            color = colors.textPrimary,
                            letterSpacing = (-0.3).sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border,
                            cursorColor = colors.primary
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                // Wallet Selection for Expense / Income
                if (selectedTab != TransactionTab.TRANSFER) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "WALLET",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                ),
                                color = colors.textSecondary
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                wallets.forEach { wallet ->
                                    val isSelected = selectedWalletId == wallet.id
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedWalletId = wallet.id },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) colors.primary.copy(alpha = 0.12f) else colors.surface,
                                        border = BorderStroke(1.dp, if (isSelected) colors.primary else colors.border)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = wallet.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = Inter,
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                                    fontSize = 13.sp
                                                ),
                                                color = if (isSelected) colors.primary else colors.textPrimary
                                            )
                                            Text(
                                                text = "₹${"%.0f".format(wallet.currentBalance)}",
                                                style = TextStyle(
                                                    fontFamily = SpaceGrotesk,
                                                    fontSize = 12.sp,
                                                    color = colors.textSecondary
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Transfer: From Wallet & To Wallet
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "FROM WALLET",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                ),
                                color = colors.textSecondary
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
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) colors.expense.copy(alpha = 0.12f) else colors.surface,
                                        border = BorderStroke(1.dp, if (isSelected) colors.expense else colors.border)
                                    ) {
                                        Text(
                                            text = wallet.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = Inter,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                                fontSize = 13.sp
                                            ),
                                            color = if (isSelected) colors.expense else colors.textPrimary,
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "TO WALLET",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                ),
                                color = colors.textSecondary
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
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) colors.income.copy(alpha = 0.12f) else colors.surface,
                                        border = BorderStroke(1.dp, if (isSelected) colors.income else colors.border)
                                    ) {
                                        Text(
                                            text = wallet.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = Inter,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                                fontSize = 13.sp
                                            ),
                                            color = if (isSelected) colors.income else colors.textPrimary,
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Category Selection for Expense
                if (selectedTab == TransactionTab.EXPENSE) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "CATEGORY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                ),
                                color = colors.textSecondary
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                categories.forEach { category ->
                                    val isSelected = selectedCategoryId == category.id
                                    Surface(
                                        modifier = Modifier.clickable { selectedCategoryId = category.id },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) colors.primary.copy(alpha = 0.14f) else colors.surface,
                                        border = BorderStroke(1.dp, if (isSelected) colors.primary else colors.border)
                                    ) {
                                        Text(
                                            text = category.name,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = Inter,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                fontSize = 13.sp
                                            ),
                                            color = if (isSelected) colors.primary else colors.textPrimary,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Note Field
                item {
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Note (Optional)", style = MaterialTheme.typography.bodySmall.copy(fontFamily = Inter)) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = Inter,
                            color = colors.textPrimary
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border,
                            cursorColor = colors.primary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
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
                        shape = RoundedCornerShape(14.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 13.dp),
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
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = "Date & Time",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = Inter,
                                            fontSize = 11.sp
                                        ),
                                        color = colors.textSecondary
                                    )
                                    Text(
                                        text = dateFormatter.format(Date(timestamp)),
                                        style = TextStyle(
                                            fontFamily = SpaceGrotesk,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = colors.textPrimary
                                        )
                                    )
                                }
                            }
                            Text(
                                text = "Change",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = colors.primary
                            )
                        }
                    }
                }
            }

            // Action Button
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
                                Toast.makeText(context, if (isEditMode) "Transaction updated!" else "Expense saved!", Toast.LENGTH_SHORT).show()
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
                                Toast.makeText(context, if (isEditMode) "Transaction updated!" else "Income saved!", Toast.LENGTH_SHORT).show()
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
                                Toast.makeText(context, "From and To wallets cannot be the same", Toast.LENGTH_SHORT).show()
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
                                Toast.makeText(context, if (isEditMode) "Transfer updated!" else "Transfer saved!", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }
                        }
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = if (colors.isDark) Color(0xFF0F2625) else Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = if (colors.isDark) Color(0xFF0F2625) else Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isEditMode) "Save Changes" else "Save Transaction",
                        fontFamily = Manrope,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteConfirmDialog && editingMainTx != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                containerColor = colors.surface,
                shape = RoundedCornerShape(18.dp),
                title = {
                    Text(
                        "Delete Transaction?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.textPrimary
                    )
                },
                text = {
                    Text(
                        if (selectedTab == TransactionTab.TRANSFER)
                            "This will delete both linked transfer entries and recalculate wallet balances."
                        else
                            "Are you sure you want to delete this transaction? Wallet balances will be recalculated.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = Inter),
                        color = colors.textSecondary
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.expense,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Delete", fontWeight = FontWeight.SemiBold, fontFamily = Inter)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirmDialog = false },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = colors.textSecondary, fontFamily = Inter)
                    }
                }
            )
        }
    }
}

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

package com.expense.tracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expense.tracker.data.local.entity.CategoryEntity
import com.expense.tracker.data.model.TransactionType
import com.expense.tracker.data.model.WalletWithBalance
import com.expense.tracker.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddBottomSheet(
    wallets: List<WalletWithBalance>,
    categories: List<CategoryEntity>,
    onSaveExpense: (amount: Double, walletId: Long, categoryId: Long, note: String?) -> Unit,
    onSaveIncome: (amount: Double, walletId: Long, categoryId: Long?, note: String?) -> Unit,
    onSaveTransfer: (amount: Double, fromWalletId: Long, toWalletId: Long, note: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountInput by remember { mutableStateOf("") }
    var selectedWalletId by remember { mutableStateOf<Long?>(null) }
    var selectedFromWalletId by remember { mutableStateOf<Long?>(null) }
    var selectedToWalletId by remember { mutableStateOf<Long?>(null) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var noteInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val focusRequester = remember { FocusRequester() }

    // Pre-select defaults
    LaunchedEffect(wallets) {
        if (wallets.isNotEmpty()) {
            if (selectedWalletId == null) selectedWalletId = wallets.first().id
            if (selectedFromWalletId == null) selectedFromWalletId = wallets.first().id
            if (selectedToWalletId == null) {
                selectedToWalletId = wallets.getOrNull(1)?.id ?: wallets.first().id
            }
        }
    }

    LaunchedEffect(categories) {
        if (selectedCategoryId == null && categories.isNotEmpty()) {
            selectedCategoryId = categories.first().id
        }
    }

    // Auto-focus amount field
    LaunchedEffect(Unit) {
        delay(200)
        try {
            focusRequester.requestFocus()
        } catch (_: Throwable) {}
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = colors.border.copy(alpha = 0.8f)
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = colors.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    Text(
                        text = "Quick Add",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = colors.textPrimary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Transaction Type Segmented Toggle
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.surfaceSecondary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    QuickTypeButton(
                        text = "Expense",
                        isSelected = selectedType == TransactionType.EXPENSE,
                        activeColor = colors.expense,
                        onClick = {
                            selectedType = TransactionType.EXPENSE
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickTypeButton(
                        text = "Income",
                        isSelected = selectedType == TransactionType.INCOME,
                        activeColor = colors.income,
                        onClick = {
                            selectedType = TransactionType.INCOME
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickTypeButton(
                        text = "Transfer",
                        isSelected = selectedType == TransactionType.TRANSFER_OUT,
                        activeColor = colors.primary,
                        onClick = {
                            selectedType = TransactionType.TRANSFER_OUT
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Large Amount Input Field (Auto-focused)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colors.background,
                border = BorderStroke(1.dp, colors.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹",
                        style = TextStyle(
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = when (selectedType) {
                                TransactionType.EXPENSE -> colors.expense
                                TransactionType.INCOME -> colors.income
                                else -> colors.primary
                            }
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = amountInput,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.matches(Regex("""^\d*\.?\d*$"""))) {
                                amountInput = newValue
                                errorMessage = null
                            }
                        },
                        placeholder = {
                            Text(
                                text = "0",
                                style = TextStyle(
                                    fontFamily = SpaceGrotesk,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    color = colors.textMuted
                                )
                            )
                        },
                        textStyle = TextStyle(
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = colors.textPrimary
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                    )
                }
            }

            // Wallet Selection (or Source/Destination for Transfer)
            if (selectedType != TransactionType.TRANSFER_OUT) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (selectedType == TransactionType.EXPENSE) "WALLET" else "DEPOSIT TO WALLET",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = colors.textSecondary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        wallets.forEach { wallet ->
                            val isSelected = selectedWalletId == wallet.id
                            QuickWalletChip(
                                wallet = wallet,
                                isSelected = isSelected,
                                colors = colors,
                                onClick = { selectedWalletId = wallet.id }
                            )
                        }
                    }
                }
            } else {
                // Transfer: From Wallet & To Wallet
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "FROM WALLET",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = colors.textSecondary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        wallets.forEach { wallet ->
                            val isSelected = selectedFromWalletId == wallet.id
                            QuickWalletChip(
                                wallet = wallet,
                                isSelected = isSelected,
                                colors = colors,
                                onClick = {
                                    selectedFromWalletId = wallet.id
                                    errorMessage = null
                                }
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "TO WALLET",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = colors.textSecondary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        wallets.forEach { wallet ->
                            val isSelected = selectedToWalletId == wallet.id
                            QuickWalletChip(
                                wallet = wallet,
                                isSelected = isSelected,
                                colors = colors,
                                onClick = {
                                    selectedToWalletId = wallet.id
                                    errorMessage = null
                                }
                            )
                        }
                    }
                }
            }

            // Category Selection (Shown for Expense)
            if (selectedType == TransactionType.EXPENSE) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "CATEGORY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = colors.textSecondary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            val isSelected = selectedCategoryId == category.id
                            val (icon, tint) = resolveQuickCategoryIcon(category.name, category.iconRes, colors)

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) colors.primary else colors.surfaceSecondary,
                                border = if (isSelected) null else BorderStroke(1.dp, colors.border),
                                modifier = Modifier.clickable { selectedCategoryId = category.id }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = category.name,
                                        tint = if (isSelected) Color.White else tint,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = Inter,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                            fontSize = 12.sp
                                        ),
                                        color = if (isSelected) Color.White else colors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Optional Note Field
            OutlinedTextField(
                value = noteInput,
                onValueChange = { noteInput = it },
                placeholder = {
                    Text(
                        text = "Add note (optional)...",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    )
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = colors.textPrimary
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.border
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Inline Error Message
            errorMessage?.let { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = colors.expense
                )
            }

            // Save Action Button
            Button(
                onClick = {
                    val parsedAmount = amountInput.trim().toDoubleOrNull()
                    if (parsedAmount == null || parsedAmount <= 0) {
                        errorMessage = "Please enter a valid amount greater than 0"
                        return@Button
                    }

                    when (selectedType) {
                        TransactionType.EXPENSE -> {
                            val wId = selectedWalletId
                            val cId = selectedCategoryId
                            if (wId == null) {
                                errorMessage = "Please select a wallet"
                                return@Button
                            }
                            if (cId == null) {
                                errorMessage = "Please select a category"
                                return@Button
                            }
                            onSaveExpense(parsedAmount, wId, cId, noteInput.takeIf { it.isNotBlank() })
                            onDismiss()
                        }
                        TransactionType.INCOME -> {
                            val wId = selectedWalletId
                            if (wId == null) {
                                errorMessage = "Please select a wallet"
                                return@Button
                            }
                            onSaveIncome(parsedAmount, wId, selectedCategoryId, noteInput.takeIf { it.isNotBlank() })
                            onDismiss()
                        }
                        else -> {
                            // Transfer
                            val fromId = selectedFromWalletId
                            val toId = selectedToWalletId
                            if (fromId == null || toId == null) {
                                errorMessage = "Please select source and destination wallets"
                                return@Button
                            }
                            if (fromId == toId) {
                                errorMessage = "Source and destination wallets cannot be the same"
                                return@Button
                            }
                            onSaveTransfer(parsedAmount, fromId, toId, noteInput.takeIf { it.isNotBlank() })
                            onDismiss()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (selectedType) {
                        TransactionType.EXPENSE -> colors.expense
                        TransactionType.INCOME -> colors.income
                        else -> colors.primary
                    },
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = when (selectedType) {
                        TransactionType.EXPENSE -> "Save Expense"
                        TransactionType.INCOME -> "Save Income"
                        else -> "Save Transfer"
                    },
                    fontFamily = Manrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun QuickTypeButton(
    text: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) activeColor else Color.Transparent,
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = Inter,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 13.sp
                ),
                color = if (isSelected) Color.White else AppTheme.colors.textSecondary
            )
        }
    }
}

@Composable
private fun QuickWalletChip(
    wallet: WalletWithBalance,
    isSelected: Boolean,
    colors: AppColors,
    onClick: () -> Unit
) {
    val (accentColor, icon) = when (wallet.name.lowercase(Locale.getDefault())) {
        "upi" -> Pair(colors.sky, Icons.Default.AccountBalanceWallet)
        "cash" -> Pair(colors.mint, Icons.Default.Payments)
        "savings" -> Pair(colors.amber, Icons.Default.Savings)
        else -> Pair(colors.primary, Icons.Default.AccountBalanceWallet)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) colors.primary else colors.surfaceSecondary,
        border = if (isSelected) null else BorderStroke(1.dp, colors.border),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = wallet.name,
                tint = if (isSelected) Color.White else accentColor,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = wallet.name,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = Inter,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = if (isSelected) Color.White else colors.textPrimary
            )
        }
    }
}

private fun resolveQuickCategoryIcon(categoryName: String, iconRes: String?, colors: AppColors): Pair<ImageVector, Color> {
    return when (categoryName.lowercase(Locale.getDefault())) {
        "food", "dining" -> Pair(Icons.Default.Restaurant, colors.expense)
        "travel", "transport" -> Pair(Icons.Default.DirectionsCar, colors.sky)
        "bills", "utilities" -> Pair(Icons.AutoMirrored.Filled.ReceiptLong, colors.amber)
        "shopping" -> Pair(Icons.Default.ShoppingBag, Color(0xFFD6778D))
        "entertainment" -> Pair(Icons.Default.Movie, colors.primary)
        "health", "medical" -> Pair(Icons.Default.MedicalServices, colors.expense)
        "groceries" -> Pair(Icons.Default.ShoppingCart, colors.income)
        else -> {
            val icon = when (iconRes) {
                "Restaurant" -> Icons.Default.Restaurant
                "DirectionsCar" -> Icons.Default.DirectionsCar
                "ReceiptLong" -> Icons.AutoMirrored.Filled.ReceiptLong
                "ShoppingBag" -> Icons.Default.ShoppingBag
                "Movie" -> Icons.Default.Movie
                "MedicalServices" -> Icons.Default.MedicalServices
                "ShoppingCart" -> Icons.Default.ShoppingCart
                else -> Icons.Default.Category
            }
            Pair(icon, colors.sky)
        }
    }
}

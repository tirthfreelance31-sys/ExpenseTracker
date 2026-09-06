package com.expense.tracker.ui.screens.walletdetail

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
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
    val colors = AppTheme.colors

    var showReconcileDialog by remember { mutableStateOf(false) }
    var newBalanceInput by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val accentColor = when (wallet?.name?.lowercase()) {
        "upi" -> colors.sky
        "cash" -> colors.income
        "savings" -> colors.amber
        else -> colors.primary
    }

    val walletIcon = when (wallet?.name?.lowercase()) {
        "cash" -> Icons.Default.Payments
        "savings" -> Icons.Default.Savings
        else -> Icons.Default.AccountBalanceWallet
    }

    val categoriesMap = categories.associateBy { it.id }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${wallet?.name ?: "Wallet"} Details",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp
                        ),
                        color = colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
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
            if (wallet != null) {
                // Hero Wallet Balance Card
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = accentColor.copy(alpha = 0.12f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = walletIcon,
                                            contentDescription = null,
                                            tint = accentColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = wallet.name,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = Manrope,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 20.sp
                                        ),
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = if (wallet.type == WalletType.DIGITAL) "Digital Wallet" else "Cash",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = Inter,
                                            fontSize = 12.sp
                                        ),
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            HorizontalDivider(color = colors.border.copy(alpha = 0.6f), thickness = 0.8.dp)

                            Column {
                                Text(
                                    text = "Current Balance",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    ),
                                    color = colors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "₹${"%.2f".format(wallet.currentBalance)}",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontFamily = SpaceGrotesk,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 32.sp,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = colors.textPrimary
                                )
                            }

                            HorizontalDivider(color = colors.border.copy(alpha = 0.6f), thickness = 0.8.dp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Starting Balance",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = Inter,
                                            fontSize = 11.sp
                                        ),
                                        color = colors.textSecondary
                                    )
                                    Text(
                                        text = "₹${"%.2f".format(wallet.openingBalance)}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = SpaceGrotesk,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp
                                        ),
                                        color = colors.textPrimary
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        newBalanceInput = wallet.openingBalance.toString()
                                        showReconcileDialog = true
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.4f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Edit",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = Inter,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Transactions Section
                item {
                    Text(
                        text = "WALLET ACTIVITY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        ),
                        color = colors.textSecondary
                    )
                }

                if (transactions.isEmpty()) {
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
                                    text = "No transactions recorded for this wallet ✨",
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
                                transactions.forEachIndexed { index, tx ->
                                    val category = categoriesMap[tx.categoryId]
                                    val isExpense = tx.type == TransactionType.EXPENSE
                                    val isIncome = tx.type == TransactionType.INCOME
                                    val isOpening = tx.type == TransactionType.OPENING_BALANCE
                                    val isTransferOut = tx.type == TransactionType.TRANSFER_OUT
                                    val isTransferIn = tx.type == TransactionType.TRANSFER_IN
                                    val isTransfer = isTransferOut || isTransferIn

                                    val amountColor = when {
                                        isExpense || isTransferOut -> colors.expense
                                        isIncome || isTransferIn -> colors.income
                                        else -> colors.textSecondary
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

                                    val visual = resolveCategoryVisual(
                                        categoryName = category?.name,
                                        type = tx.type,
                                        colors = colors
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onEditTransaction(tx.id) }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = visual.bg,
                                                modifier = Modifier.size(38.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = visual.icon,
                                                        contentDescription = titleText,
                                                        tint = visual.tint,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }

                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text(
                                                    text = titleText,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontFamily = Inter,
                                                        fontWeight = FontWeight.Medium,
                                                        fontSize = 14.sp
                                                    ),
                                                    color = if (isOpening) colors.textSecondary else colors.textPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = formattedDate,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontFamily = Inter,
                                                        fontSize = 12.sp
                                                    ),
                                                    color = colors.textSecondary
                                                )
                                                tx.note?.takeIf { it.isNotBlank() }?.let { note ->
                                                    Text(
                                                        text = note,
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontFamily = Inter,
                                                            fontSize = 11.sp
                                                        ),
                                                        color = colors.textMuted,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = "$prefix${"%.2f".format(tx.amount)}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = SpaceGrotesk,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 16.sp
                                            ),
                                            color = amountColor
                                        )
                                    }

                                    if (index < transactions.size - 1) {
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

        // Reconcile Dialog
        if (showReconcileDialog && wallet != null) {
            AlertDialog(
                onDismissRequest = { if (!isSaving) showReconcileDialog = false },
                containerColor = colors.surface,
                shape = RoundedCornerShape(18.dp),
                title = {
                    Text(
                        "Edit Starting Balance",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.textPrimary
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Current: ₹${"%.2f".format(wallet.openingBalance)}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = Inter),
                            color = colors.textSecondary
                        )
                        OutlinedTextField(
                            value = newBalanceInput,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.matches(Regex("""^\d*\.?\d*$"""))) {
                                    newBalanceInput = newValue
                                }
                            },
                            label = { Text("New Starting Balance", style = MaterialTheme.typography.bodySmall.copy(fontFamily = Inter)) },
                            prefix = { Text("₹ ", style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, color = colors.primary)) },
                            textStyle = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, color = colors.textPrimary),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border,
                                cursorColor = colors.primary
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
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
                                    Toast.makeText(context, "Balance updated!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = if (colors.isDark) Color(0xFF0F2625) else Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.SemiBold, fontFamily = Inter)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showReconcileDialog = false },
                        enabled = !isSaving,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = colors.textSecondary, fontFamily = Inter)
                    }
                }
            )
        }
    }
}

private data class CategoryVisual(
    val icon: ImageVector,
    val tint: Color,
    val bg: Color
)

private fun resolveCategoryVisual(categoryName: String?, type: TransactionType, colors: AppColors): CategoryVisual {
    return when (type) {
        TransactionType.TRANSFER_OUT, TransactionType.TRANSFER_IN -> {
            CategoryVisual(
                icon = Icons.Default.SwapHoriz,
                tint = colors.primary,
                bg = colors.primary.copy(alpha = 0.12f)
            )
        }
        TransactionType.OPENING_BALANCE -> {
            CategoryVisual(
                icon = Icons.Default.AccountBalance,
                tint = colors.textSecondary,
                bg = colors.surfaceSecondary
            )
        }
        else -> {
            when (categoryName?.lowercase()) {
                "food", "dining" -> CategoryVisual(
                    icon = Icons.Default.Restaurant,
                    tint = colors.expense,
                    bg = colors.softCoralBg
                )
                "travel", "transport" -> CategoryVisual(
                    icon = Icons.Default.DirectionsCar,
                    tint = colors.sky,
                    bg = colors.sky.copy(alpha = 0.15f)
                )
                "bills", "utilities" -> CategoryVisual(
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    tint = colors.amber,
                    bg = colors.softAmberBg
                )
                "shopping" -> CategoryVisual(
                    icon = Icons.Default.ShoppingBag,
                    tint = Color(0xFFD6778D),
                    bg = Color(0xFFD6778D).copy(alpha = 0.15f)
                )
                "entertainment" -> CategoryVisual(
                    icon = Icons.Default.Movie,
                    tint = colors.primary,
                    bg = colors.softTealBg
                )
                "health", "medical" -> CategoryVisual(
                    icon = Icons.Default.MedicalServices,
                    tint = colors.expense,
                    bg = colors.softCoralBg
                )
                "groceries" -> CategoryVisual(
                    icon = Icons.Default.ShoppingCart,
                    tint = colors.income,
                    bg = colors.softGreenBg
                )
                else -> CategoryVisual(
                    icon = Icons.Default.Category,
                    tint = colors.sky,
                    bg = colors.sky.copy(alpha = 0.15f)
                )
            }
        }
    }
}

package com.expense.tracker.ui.screens.walletdetail

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import kotlin.math.abs

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

    val accentColor = when (wallet?.name?.lowercase(Locale.getDefault())) {
        "upi" -> colors.sky
        "cash" -> colors.mint
        "savings" -> colors.amber
        else -> colors.primary
    }

    val walletIcon = when (wallet?.name?.lowercase(Locale.getDefault())) {
        "cash" -> Icons.Default.Payments
        "savings" -> Icons.Default.Savings
        else -> Icons.Default.AccountBalanceWallet
    }

    val categoriesMap = remember(categories) { categories.associateBy { it.id } }

    // Wallet-specific metrics:
    // Strictly counts only actual Income and Expense. Transfers are NEVER counted as spending.
    val totalIncome = remember(transactions) {
        transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }
    val totalSpending = remember(transactions) {
        transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }
    val netMovement = remember(wallet) {
        if (wallet != null) wallet.currentBalance - wallet.openingBalance else 0.0
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Multi-layered subtle ambient glow behind the hero
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = if (colors.isDark) 0.30f else 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.85f, size.height * 0.30f),
                    radius = size.width * 0.65f
                )
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
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
                        containerColor = Color.Transparent
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
                    // 1. Hero Wallet Balance Card (Premium Glass)
                    item {
                        val isDark = colors.isDark
                        val heroGlassBg = if (isDark) Color(0xCC202321) else Color(0xEDFFFDF9)
                        val heroBorderBrush = Brush.linearGradient(
                            listOf(
                                if (isDark) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.90f),
                                if (isDark) Color.White.copy(alpha = 0.05f) else colors.border
                            )
                        )

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = heroGlassBg,
                            border = BorderStroke(1.2.dp, heroBorderBrush),
                            shadowElevation = 6.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = accentColor.copy(alpha = 0.15f),
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
                                                    fontSize = 18.sp
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

                                    // Edit Starting Balance Button
                                    OutlinedButton(
                                        onClick = {
                                            newBalanceInput = wallet.openingBalance.toString()
                                            showReconcileDialog = true
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.4f)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            "Edit Start",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = Inter,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }

                                HorizontalDivider(color = colors.border.copy(alpha = 0.6f), thickness = 0.8.dp)

                                // Balance Displays: Current & Starting
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text(
                                            text = "Current Balance",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = Inter,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.sp
                                            ),
                                            color = colors.textSecondary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "₹${"%.2f".format(wallet.currentBalance)}",
                                            style = MaterialTheme.typography.headlineLarge.copy(
                                                fontFamily = SpaceGrotesk,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 32.sp,
                                                letterSpacing = (-0.5).sp
                                            ),
                                            color = colors.textPrimary
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Starting Balance",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = Inter,
                                                fontSize = 11.sp
                                            ),
                                            color = colors.textSecondary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
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
                                }
                            }
                        }
                    }

                    // 2. Compact Wallet Analytics Metrics Card
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = colors.surface,
                            border = BorderStroke(1.dp, colors.border)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Income
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(
                                        text = "Total Income",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = Inter,
                                            fontSize = 11.sp
                                        ),
                                        color = colors.textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "+₹${"%.2f".format(totalIncome)}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = SpaceGrotesk,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp
                                        ),
                                        color = colors.income
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(26.dp)
                                        .background(colors.border)
                                )

                                // Spent
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Total Spent",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = Inter,
                                            fontSize = 11.sp
                                        ),
                                        color = colors.textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "-₹${"%.2f".format(totalSpending)}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = SpaceGrotesk,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp
                                        ),
                                        color = if (totalSpending > 0) colors.expense else colors.textPrimary
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(26.dp)
                                        .background(colors.border)
                                )

                                // Net Movement
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Net Movement",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = Inter,
                                            fontSize = 11.sp
                                        ),
                                        color = colors.textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    val movementColor = when {
                                        netMovement > 0.005 -> colors.income
                                        netMovement < -0.005 -> colors.expense
                                        else -> colors.textSecondary
                                    }
                                    val movementPrefix = when {
                                        netMovement > 0.005 -> "+₹"
                                        netMovement < -0.005 -> "-₹"
                                        else -> "₹"
                                    }
                                    Text(
                                        text = "$movementPrefix${"%.2f".format(abs(netMovement))}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = SpaceGrotesk,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp
                                        ),
                                        color = movementColor
                                    )
                                }
                            }
                        }
                    }

                    // 3. Transactions Section Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RECENT ACTIVITY (${transactions.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.8.sp
                                ),
                                color = colors.textSecondary
                            )
                        }
                    }

                    // 4. Wallet Transactions List
                    if (transactions.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = colors.surface,
                                border = BorderStroke(1.dp, colors.border)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = colors.surfaceSecondary,
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Receipt,
                                                contentDescription = null,
                                                tint = colors.textSecondary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "No transactions for ${wallet.name} yet",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = Manrope,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp
                                        ),
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Transactions logged using this wallet will appear here.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = Inter,
                                            fontSize = 12.sp
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
                                            isExpense -> colors.expense
                                            isIncome -> colors.income
                                            isTransfer -> colors.primary
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

                                        val formattedDate = SimpleDateFormat("dd MMM yyyy · h:mm a", Locale.getDefault())
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
                                                .padding(horizontal = 16.dp, vertical = 13.dp),
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
                                                            fontSize = 11.sp
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
        }

        // Reconcile Dialog (Consumer-friendly: Starting Balance)
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
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        ),
                        color = colors.textPrimary
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Set the amount currently available in this wallet.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = Inter,
                                fontSize = 13.sp
                            ),
                            color = colors.textSecondary
                        )
                        OutlinedTextField(
                            value = newBalanceInput,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.matches(Regex("""^\d*\.?\d*$"""))) {
                                    newBalanceInput = newValue
                                }
                            },
                            label = { Text("Starting Balance", style = MaterialTheme.typography.bodySmall.copy(fontFamily = Inter)) },
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
                                    Toast.makeText(context, "Starting balance updated!", Toast.LENGTH_SHORT).show()
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
            when (categoryName?.lowercase(Locale.getDefault())) {
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

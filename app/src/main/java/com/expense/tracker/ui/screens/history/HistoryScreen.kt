package com.expense.tracker.ui.screens.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

    val colors = AppTheme.colors

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp
                        ),
                        color = colors.textPrimary
                    )
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
            // Summary Inflow & Outflow Overview Card
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
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Overview",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = colors.textSecondary
                            )
                            Text(
                                text = "${transactions.size} entries",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = Inter,
                                    fontSize = 12.sp
                                ),
                                color = colors.textSecondary
                            )
                        }

                        HorizontalDivider(color = colors.border.copy(alpha = 0.5f), thickness = 0.8.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Total Inflow",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = Inter,
                                        fontSize = 12.sp
                                    ),
                                    color = colors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "+₹${"%.2f".format(totalCredits)}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = SpaceGrotesk,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 18.sp
                                    ),
                                    color = colors.income
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Total Outflow",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = Inter,
                                        fontSize = 12.sp
                                    ),
                                    color = colors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "-₹${"%.2f".format(totalDebits)}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = SpaceGrotesk,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 18.sp
                                    ),
                                    color = colors.expense
                                )
                            }
                        }
                    }
                }
            }

            // Section Header
            item {
                Text(
                    text = "ALL TRANSACTIONS",
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
                        color = colors.surface,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No transactions recorded yet ✨",
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
                        color = colors.surface,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, colors.border)
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
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = visual.icon,
                                                    contentDescription = titleText,
                                                    tint = visual.tint,
                                                    modifier = Modifier.size(19.dp)
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

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                wallet?.name?.let { wName ->
                                                    Text(
                                                        text = wName,
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontFamily = Inter,
                                                            fontWeight = FontWeight.Medium,
                                                            fontSize = 12.sp
                                                        ),
                                                        color = colors.primary
                                                    )
                                                    Text(
                                                        text = "·",
                                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = Inter),
                                                        color = colors.textSecondary
                                                    )
                                                }
                                                Text(
                                                    text = formattedDate,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontFamily = Inter,
                                                        fontSize = 12.sp
                                                    ),
                                                    color = colors.textSecondary
                                                )
                                            }

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

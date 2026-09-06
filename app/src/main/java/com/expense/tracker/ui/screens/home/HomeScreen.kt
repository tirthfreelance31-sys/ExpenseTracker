package com.expense.tracker.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import java.util.Calendar
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
    val categoriesMap = remember(categories) { categories.associateBy { it.id } }
    val walletsMap = remember(wallets) { wallets.associateBy { it.id } }

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning 👋"
            in 12..16 -> "Good afternoon 👋"
            else -> "Good evening 👋"
        }
    }

    val todayDateFormatted = remember {
        SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
    }

    val colors = AppTheme.colors

    Scaffold(
        containerColor = colors.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddTransaction,
                containerColor = colors.primary,
                contentColor = if (colors.isDark) Color(0xFF0F2625) else Color.White,
                shape = RoundedCornerShape(14.dp),
                icon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = if (colors.isDark) Color(0xFF0F2625) else Color.White
                    )
                },
                text = {
                    Text(
                        "Add Entry",
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Manrope,
                        fontSize = 14.sp
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. TOP HEADER (Friendly greeting + date + settings action)
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = Manrope,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 22.sp
                            ),
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = todayDateFormatted,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = Inter,
                                fontSize = 12.sp
                            ),
                            color = colors.textSecondary
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = colors.surfaceSecondary,
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 2. TOTAL BALANCE HERO CARD (Warm surface, Deep Teal accent, Space Grotesk)
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total balance",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                ),
                                color = colors.textSecondary
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colors.primary.copy(alpha = 0.12f),
                                border = BorderStroke(0.6.dp, colors.primary.copy(alpha = 0.25f))
                            ) {
                                Text(
                                    text = "Across ${wallets.size} wallets",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    ),
                                    color = colors.primary,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        AnimatedRupeeAmount(
                            targetAmount = totalNetBalance,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = SpaceGrotesk,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 36.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = colors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        HorizontalDivider(color = colors.border, thickness = 0.8.dp)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToSummary() },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(colors.income, shape = CircleShape)
                                )
                                Text(
                                    text = "Active & Encrypted",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 12.sp
                                    ),
                                    color = colors.textSecondary
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Spending overview",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    ),
                                    color = colors.primary
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. WALLET SECTION ("YOUR MONEY") - Horizontal carousel with intentional 3rd card peek
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your money",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = Manrope,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            ),
                            color = colors.textPrimary
                        )

                        Text(
                            text = "${wallets.size} wallets",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = Inter,
                                fontSize = 12.sp
                            ),
                            color = colors.textSecondary
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(end = 8.dp)
                    ) {
                        items(wallets) { wallet ->
                            val identity = resolveWalletIdentity(
                                name = wallet.name,
                                type = wallet.type,
                                colors = colors
                            )

                            Surface(
                                modifier = Modifier
                                    .width(148.dp)
                                    .clickable { onNavigateToWalletDetail(wallet.id) },
                                shape = RoundedCornerShape(16.dp),
                                color = identity.tint,
                                border = BorderStroke(1.dp, identity.border)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(14.dp)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (colors.isDark) colors.surfaceElevated else Color.White,
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = identity.icon,
                                                    contentDescription = wallet.name,
                                                    tint = identity.accent,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (colors.isDark) colors.surface.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.85f)
                                        ) {
                                            Text(
                                                text = identity.badge,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = Inter,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 10.sp
                                                ),
                                                color = identity.accent,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Column {
                                        Text(
                                            text = wallet.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = Inter,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp
                                            ),
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = "₹${"%.2f".format(wallet.currentBalance)}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = SpaceGrotesk,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 16.sp
                                            ),
                                            color = colors.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. SPENDING INSIGHT ("THIS WEEK" with lightweight 7-day visualization)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "This week",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        ),
                        color = colors.textPrimary
                    )

                    val weeklyTotal = recentExpenses.sumOf { it.amount }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToSummary() },
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
                                Column {
                                    Text(
                                        text = "Spent past 7 days",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = Inter,
                                            fontSize = 12.sp
                                        ),
                                        color = colors.textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "₹${"%.2f".format(weeklyTotal)}",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = SpaceGrotesk,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 22.sp
                                        ),
                                        color = if (weeklyTotal > 0.0) colors.expense else colors.textPrimary
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = colors.surfaceSecondary,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Summary",
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }

                            if (recentExpenses.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = colors.surfaceSecondary.copy(alpha = 0.6f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "✨",
                                            fontSize = 18.sp
                                        )
                                        Column {
                                            Text(
                                                text = "No spending yet ✨",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = Inter,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 13.sp
                                                ),
                                                color = colors.textPrimary
                                            )
                                            Text(
                                                text = "You're having a quiet week.",
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
                                // 7-Day lightweight daily expenditure bar chart
                                val dayBuckets = remember(recentExpenses) {
                                    (6 downTo 0).map { daysBack ->
                                        val cal = Calendar.getInstance().apply {
                                            add(Calendar.DAY_OF_YEAR, -daysBack)
                                        }
                                        val dayStart = cal.apply {
                                            set(Calendar.HOUR_OF_DAY, 0)
                                            set(Calendar.MINUTE, 0)
                                            set(Calendar.SECOND, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }.timeInMillis
                                        val dayEnd = dayStart + 24 * 60 * 60 * 1000L - 1
                                        val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(Date(dayStart)).take(3)
                                        val dayExpenses = recentExpenses.filter { it.timestamp in dayStart..dayEnd }.sumOf { it.amount }
                                        DaySpend(dayName = dayName, amount = dayExpenses, isToday = (daysBack == 0))
                                    }
                                }

                                val maxExpense = remember(dayBuckets) {
                                    dayBuckets.maxOfOrNull { it.amount }?.takeIf { it > 0.0 } ?: 1.0
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(64.dp)
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    dayBuckets.forEach { bucket ->
                                        val heightFraction = if (bucket.amount > 0.0) {
                                            (bucket.amount / maxExpense).toFloat().coerceIn(0.15f, 1.0f)
                                        } else {
                                            0.06f
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(18.dp)
                                                    .height((38 * heightFraction).dp)
                                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                                                    .background(
                                                        if (bucket.isToday) colors.primary
                                                        else if (bucket.amount > 0.0) colors.primary.copy(alpha = 0.35f)
                                                        else colors.surfaceSecondary
                                                    )
                                            )
                                            Text(
                                                text = bucket.dayName,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = Inter,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (bucket.isToday) FontWeight.SemiBold else FontWeight.Normal
                                                ),
                                                color = if (bucket.isToday) colors.primary else colors.textSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. RECENT ACTIVITY (Clean transaction rows with modern category icons)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent activity",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        ),
                        color = colors.textPrimary
                    )

                    if (recentTransactions.isNotEmpty()) {
                        Text(
                            text = "Latest",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = Inter,
                                fontSize = 12.sp
                            ),
                            color = colors.textSecondary
                        )
                    }
                }
            }

            if (recentTransactions.isEmpty()) {
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
                                .padding(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No activity yet",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    ),
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Tap + Add Entry below to track an expense",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = Inter,
                                        fontSize = 12.sp
                                    ),
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
            } else {
                item {
                    val normalTransactions = recentTransactions.filter { it.type != TransactionType.OPENING_BALANCE }
                    val openingTransactions = recentTransactions.filter { it.type == TransactionType.OPENING_BALANCE }
                    val displayedList = if (normalTransactions.isNotEmpty()) {
                        (normalTransactions + openingTransactions).take(5)
                    } else {
                        recentTransactions.take(5)
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.border)
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

                                val timeString = formatRelativeDate(tx.timestamp)
                                val subtitleText = if (wallet != null) "${wallet.name} · $timeString" else timeString

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
                                            modifier = Modifier.size(42.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = visual.icon,
                                                    contentDescription = titleText,
                                                    tint = visual.tint,
                                                    modifier = Modifier.size(20.dp)
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
                                                text = subtitleText,
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

                                if (index < displayedList.size - 1) {
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

            // Bottom spacer so FAB doesn't obscure content
            item {
                Spacer(modifier = Modifier.height(88.dp))
            }
        }
    }
}

private data class WalletIdentity(
    val tint: Color,
    val border: Color,
    val accent: Color,
    val badge: String,
    val icon: ImageVector
)

private fun resolveWalletIdentity(name: String, type: WalletType, colors: AppColors): WalletIdentity {
    return if (colors.isDark) {
        when (name.lowercase()) {
            "upi" -> WalletIdentity(
                tint = colors.softTealBg,
                border = Color(0xFF234B49),
                accent = colors.primary,
                badge = "Digital",
                icon = Icons.Default.AccountBalanceWallet
            )
            "cash" -> WalletIdentity(
                tint = colors.softGreenBg,
                border = Color(0xFF234735),
                accent = colors.income,
                badge = "Physical",
                icon = Icons.Default.Payments
            )
            "savings" -> WalletIdentity(
                tint = colors.softAmberBg,
                border = Color(0xFF4C3C24),
                accent = colors.amber,
                badge = "Reserve",
                icon = Icons.Default.Savings
            )
            else -> WalletIdentity(
                tint = colors.surfaceSecondary,
                border = colors.border,
                accent = colors.primary,
                badge = if (type == WalletType.DIGITAL) "Digital" else "Cash",
                icon = Icons.Default.AccountBalanceWallet
            )
        }
    } else {
        when (name.lowercase()) {
            "upi" -> WalletIdentity(
                tint = SoftTealBg,
                border = Color(0xFFC7E2DF),
                accent = DeepTeal,
                badge = "Digital",
                icon = Icons.Default.AccountBalanceWallet
            )
            "cash" -> WalletIdentity(
                tint = SoftMintBg,
                border = Color(0xFFC3E8D3),
                accent = IncomeGreen,
                badge = "Physical",
                icon = Icons.Default.Payments
            )
            "savings" -> WalletIdentity(
                tint = SoftAmberBg,
                border = Color(0xFFF3DEB8),
                accent = WarmAmber,
                badge = "Reserve",
                icon = Icons.Default.Savings
            )
            else -> WalletIdentity(
                tint = SurfaceSecondaryLight,
                border = SurfaceBorderLight,
                accent = DeepTeal,
                badge = if (type == WalletType.DIGITAL) "Digital" else "Cash",
                icon = Icons.Default.AccountBalanceWallet
            )
        }
    }
}

private data class DaySpend(
    val dayName: String,
    val amount: Double,
    val isToday: Boolean
)

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

private fun formatRelativeDate(timestamp: Long): String {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { timeInMillis = timestamp }

    val isSameYear = now.get(Calendar.YEAR) == then.get(Calendar.YEAR)
    val isSameDay = isSameYear && now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)

    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val isYesterday = isSameYear && yesterday.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)

    return when {
        isSameDay -> "Today"
        isYesterday -> "Yesterday"
        isSameYear -> SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(timestamp))
        else -> SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}

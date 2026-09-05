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
import androidx.compose.ui.graphics.Brush
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

    Scaffold(
        containerColor = BrandBackground,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddTransaction,
                containerColor = BrandViolet,
                contentColor = Color.White,
                shape = RoundedCornerShape(14.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) },
                text = { Text("Add Entry", fontWeight = FontWeight.Bold, fontFamily = IbmPlexSans) }
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
            // 1. TOP HEADER (Friendly greeting + date + subtle settings action)
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = SpaceGrotesk,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = todayDateFormatted,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = IbmPlexSans,
                                fontSize = 12.sp
                            ),
                            color = TextSecondary
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = SurfaceSecondary,
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 2. TOTAL BALANCE HERO CARD (Soft gradient, large Space Grotesk typography)
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = SurfacePrimary,
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFFFFFFFF),
                                        Color(0xFFF7F9FD)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
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
                                        fontFamily = IbmPlexSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    ),
                                    color = TextSecondary
                                )

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = BrandViolet.copy(alpha = 0.08f),
                                    border = BorderStroke(0.5.dp, BrandViolet.copy(alpha = 0.2f))
                                ) {
                                    Text(
                                        text = "Across ${wallets.size} wallets",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = IbmPlexSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        ),
                                        color = BrandViolet,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            AnimatedRupeeAmount(
                                targetAmount = totalNetBalance,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = SpaceGrotesk,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 36.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            HorizontalDivider(color = SurfaceBorder, thickness = 0.8.dp)

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
                                            .background(FreshGreen, shape = CircleShape)
                                    )
                                    Text(
                                        text = "Active & Encrypted",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = IbmPlexSans,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp
                                        ),
                                        color = TextSecondary
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Spending overview",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = IbmPlexSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        ),
                                        color = BrandViolet
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = BrandViolet,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. WALLET SECTION ("YOUR MONEY") - Distinct soft-tinted cards
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
                                fontFamily = SpaceGrotesk,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = TextPrimary
                        )

                        Text(
                            text = "${wallets.size} accounts",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = IbmPlexSans,
                                fontSize = 12.sp
                            ),
                            color = TextSecondary
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(end = 4.dp)
                    ) {
                        items(wallets) { wallet ->
                            val (tintBg, borderColor, accentColor, walletBadge, walletIcon) = when (wallet.name.lowercase()) {
                                "upi" -> WalletIdentity(
                                    tint = UpiTint,
                                    border = Color(0xFFD3E4FE),
                                    accent = SkyBlue,
                                    badge = "Digital",
                                    icon = Icons.Default.AccountBalanceWallet
                                )
                                "cash" -> WalletIdentity(
                                    tint = CashTint,
                                    border = Color(0xFFC7EED8),
                                    accent = FreshGreen,
                                    badge = "Physical",
                                    icon = Icons.Default.Payments
                                )
                                "savings" -> WalletIdentity(
                                    tint = SavingsTint,
                                    border = Color(0xFFFDE7B8),
                                    accent = WarmAmber,
                                    badge = "Reserve",
                                    icon = Icons.Default.Savings
                                )
                                else -> WalletIdentity(
                                    tint = SurfaceSecondary,
                                    border = SurfaceBorder,
                                    accent = BrandViolet,
                                    badge = if (wallet.type == WalletType.DIGITAL) "Digital" else "Cash",
                                    icon = Icons.Default.AccountBalanceWallet
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .width(148.dp)
                                    .clickable { onNavigateToWalletDetail(wallet.id) },
                                shape = RoundedCornerShape(16.dp),
                                color = tintBg,
                                border = BorderStroke(1.dp, borderColor)
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
                                            color = Color.White,
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = walletIcon,
                                                    contentDescription = wallet.name,
                                                    tint = accentColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.White.copy(alpha = 0.85f)
                                        ) {
                                            Text(
                                                text = walletBadge,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = IbmPlexSans,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 10.sp
                                                ),
                                                color = accentColor,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Column {
                                        Text(
                                            text = wallet.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = IbmPlexSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            ),
                                            color = TextPrimary
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = "₹${"%.2f".format(wallet.currentBalance)}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = SpaceGrotesk,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            ),
                                            color = TextPrimary
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
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = TextPrimary
                    )

                    val weeklyTotal = recentExpenses.sumOf { it.amount }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToSummary() },
                        shape = RoundedCornerShape(16.dp),
                        color = SurfacePrimary,
                        border = BorderStroke(1.dp, SurfaceBorder)
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
                                            fontFamily = IbmPlexSans,
                                            fontSize = 12.sp
                                        ),
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "₹${"%.2f".format(weeklyTotal)}",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = SpaceGrotesk,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp
                                        ),
                                        color = if (weeklyTotal > 0.0) CoralRed else TextPrimary
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = SurfaceSecondary,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Summary",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }

                            if (recentExpenses.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SurfaceSecondary.copy(alpha = 0.6f),
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
                                                    fontFamily = IbmPlexSans,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp
                                                ),
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "You're having a quiet week.",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = IbmPlexSans,
                                                    fontSize = 12.sp
                                                ),
                                                color = TextSecondary
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
                                                        if (bucket.isToday) BrandViolet
                                                        else if (bucket.amount > 0.0) BrandViolet.copy(alpha = 0.28f)
                                                        else SurfaceSecondary
                                                    )
                                            )
                                            Text(
                                                text = bucket.dayName,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = IbmPlexSans,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (bucket.isToday) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = if (bucket.isToday) BrandViolet else TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. RECENT ACTIVITY (Modern transaction rows with clean category icons)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent activity",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = TextPrimary
                    )

                    if (recentTransactions.isNotEmpty()) {
                        Text(
                            text = "Latest",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = IbmPlexSans,
                                fontSize = 12.sp
                            ),
                            color = TextSecondary
                        )
                    }
                }
            }

            if (recentTransactions.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = SurfacePrimary,
                        border = BorderStroke(1.dp, SurfaceBorder)
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
                                        fontFamily = IbmPlexSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    ),
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Tap + Add Entry below to track an expense",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = IbmPlexSans,
                                        fontSize = 12.sp
                                    ),
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            } else {
                item {
                    // Prioritize user transactions over system Opening Balance audit entries
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
                        color = SurfacePrimary,
                        border = BorderStroke(1.dp, SurfaceBorder)
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
                                    isExpense || isTransferOut -> CoralRed
                                    isIncome || isTransferIn -> FreshGreen
                                    else -> TextSecondary
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

                                val (categoryIcon, iconTint, iconBg) = resolveCategoryVisual(
                                    categoryName = category?.name,
                                    type = tx.type
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
                                        // Category icon in soft pastel rounded container
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = iconBg,
                                            modifier = Modifier.size(42.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = categoryIcon,
                                                    contentDescription = titleText,
                                                    tint = iconTint,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = titleText,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = IbmPlexSans,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 14.sp
                                                ),
                                                color = if (isOpening) TextSecondary else TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Text(
                                                text = subtitleText,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = IbmPlexSans,
                                                    fontSize = 12.sp
                                                ),
                                                color = TextSecondary
                                            )

                                            tx.note?.takeIf { it.isNotBlank() }?.let { note ->
                                                Text(
                                                    text = note,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontFamily = IbmPlexSans,
                                                        fontSize = 11.sp
                                                    ),
                                                    color = TextMuted,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    // Right-aligned tabular amount in Space Grotesk Bold
                                    Text(
                                        text = "$prefix${"%.2f".format(tx.amount)}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = SpaceGrotesk,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        ),
                                        color = amountColor
                                    )
                                }

                                if (index < displayedList.size - 1) {
                                    HorizontalDivider(
                                        color = Color(0xFFF2F4F8),
                                        thickness = 0.8.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom spacer so FAB doesn't obscure recent activity
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

private fun resolveCategoryVisual(categoryName: String?, type: TransactionType): CategoryVisual {
    return when (type) {
        TransactionType.TRANSFER_OUT, TransactionType.TRANSFER_IN -> {
            CategoryVisual(
                icon = Icons.Default.SwapHoriz,
                tint = BrandViolet,
                bg = BrandViolet.copy(alpha = 0.12f)
            )
        }
        TransactionType.OPENING_BALANCE -> {
            CategoryVisual(
                icon = Icons.Default.AccountBalance,
                tint = TextSecondary,
                bg = SurfaceSecondary
            )
        }
        else -> {
            when (categoryName?.lowercase()) {
                "food", "dining" -> CategoryVisual(
                    icon = Icons.Default.Restaurant,
                    tint = CoralRed,
                    bg = CoralRed.copy(alpha = 0.12f)
                )
                "travel", "transport" -> CategoryVisual(
                    icon = Icons.Default.DirectionsCar,
                    tint = SkyBlue,
                    bg = SkyBlue.copy(alpha = 0.12f)
                )
                "bills", "utilities" -> CategoryVisual(
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    tint = WarmAmber,
                    bg = WarmAmber.copy(alpha = 0.12f)
                )
                "shopping" -> CategoryVisual(
                    icon = Icons.Default.ShoppingBag,
                    tint = SoftPink,
                    bg = SoftPink.copy(alpha = 0.12f)
                )
                "entertainment" -> CategoryVisual(
                    icon = Icons.Default.Movie,
                    tint = BrandViolet,
                    bg = BrandViolet.copy(alpha = 0.12f)
                )
                "health", "medical" -> CategoryVisual(
                    icon = Icons.Default.MedicalServices,
                    tint = CoralRed,
                    bg = CoralRed.copy(alpha = 0.12f)
                )
                "groceries" -> CategoryVisual(
                    icon = Icons.Default.ShoppingCart,
                    tint = FreshGreen,
                    bg = FreshGreen.copy(alpha = 0.12f)
                )
                else -> CategoryVisual(
                    icon = Icons.Default.Category,
                    tint = SkyBlue,
                    bg = SkyBlue.copy(alpha = 0.12f)
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

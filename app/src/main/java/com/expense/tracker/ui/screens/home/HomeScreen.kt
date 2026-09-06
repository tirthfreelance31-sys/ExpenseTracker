package com.expense.tracker.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
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

    // Filter out system OPENING_BALANCE entries on Home screen so actual user activity is prioritized
    val userTransactions = remember(recentTransactions) {
        recentTransactions.filter { it.type != TransactionType.OPENING_BALANCE }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Multi-layered organic ambient lighting behind the glass elements
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp)
        ) {
            // 1. Top-right Soft Mint/Emerald Glow (illuminates Hero & Header)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (colors.isDark) Color(0xFF1E4844).copy(alpha = 0.48f) else Color(0xFF8AF8BE).copy(alpha = 0.42f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.86f, size.height * 0.22f),
                    radius = size.width * 0.72f
                )
            )

            // 2. Center-left Deep Teal Glow (shimmers through Hero & Wallet Carousel)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (colors.isDark) Color(0xFF15383A).copy(alpha = 0.42f) else Color(0xFF167C80).copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.12f, size.height * 0.50f),
                    radius = size.width * 0.65f
                )
            )

            // 3. Bottom-right Warm Amber Glow (revealed through Savings wallet card)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (colors.isDark) Color(0xFF382914).copy(alpha = 0.38f) else Color(0xFFFDE68A).copy(alpha = 0.45f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.94f, size.height * 0.72f),
                    radius = size.width * 0.52f
                )
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToAddTransaction,
                    modifier = Modifier
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(18.dp),
                            ambientColor = Color(0x38167C80),
                            spotColor = Color(0x38167C80)
                        )
                        .border(
                            width = 1.2.dp,
                            brush = Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.60f),
                                    Color.White.copy(alpha = 0.20f)
                                )
                            ),
                            shape = RoundedCornerShape(18.dp)
                        ),
                    containerColor = if (colors.isDark) Color(0xFF1E7072) else colors.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 2.dp
                    ),
                    icon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    text = {
                        Text(
                            "Add Entry",
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = Manrope,
                            fontSize = 14.sp,
                            color = Color.White
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
                // 1. TOP HEADER (Greeting + date + glass settings action)
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
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = todayDateFormatted,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 13.sp
                                ),
                                color = colors.textSecondary
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = if (colors.isDark) Color(0xFF262927) else Color(0xCCFFFDF9),
                            border = BorderStroke(
                                1.dp,
                                Brush.linearGradient(
                                    listOf(
                                        if (colors.isDark) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.85f),
                                        if (colors.isDark) Color(0xFF383C3A) else Color(0xFFEAE5DC)
                                    )
                                )
                            ),
                            shadowElevation = if (colors.isDark) 0.dp else 2.dp,
                            modifier = Modifier.size(42.dp)
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

                // 2. TOTAL BALANCE HERO GLASS CARD (Stitch Premium Glass variant)
                item {
                    val heroBgGradient = if (colors.isDark) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xB3202321), // ~70% opacity
                                Color(0x99181B1A)  // ~60% opacity
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xB8FFFDF9), // ~72% opacity warm white
                                Color(0x8CFAF6EE)  // ~55% opacity warm surface
                            )
                        )
                    }

                    val heroBorderBrush = if (colors.isDark) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.45f),
                                Color(0xFF58B9B7).copy(alpha = 0.35f),
                                Color(0xFF2F3230).copy(alpha = 0.20f)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.95f),
                                Color.White.copy(alpha = 0.50f),
                                Color(0xFF167C80).copy(alpha = 0.25f),
                                Color.White.copy(alpha = 0.25f)
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 10.dp,
                                shape = RoundedCornerShape(22.dp),
                                ambientColor = if (colors.isDark) Color(0x33000000) else Color(0x1F167C80),
                                spotColor = if (colors.isDark) Color(0x2E58B9B7) else Color(0x1A167C80)
                            )
                            .clip(RoundedCornerShape(22.dp))
                            .background(heroBgGradient)
                            .border(1.dp, heroBorderBrush, RoundedCornerShape(22.dp))
                    ) {
                        // Stitch Premium Glass inner specular glow & inset top highlight
                        Canvas(modifier = Modifier.matchParentSize()) {
                            // Specular top-left glass highlight
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        if (colors.isDark) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.55f),
                                        Color.Transparent
                                    ),
                                    center = Offset(0f, 0f),
                                    radius = size.width * 0.65f
                                )
                            )

                            // Top-right subtle brand teal aura
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        if (colors.isDark) Color(0xFF244745).copy(alpha = 0.50f) else Color(0xFFD8ECEA).copy(alpha = 0.65f),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width * 0.92f, size.height * 0.15f),
                                    radius = size.width * 0.55f
                                )
                            )

                            // Inset top specular edge highlight line (simulating beveled glass edge)
                            drawLine(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        if (colors.isDark) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.75f),
                                        Color.Transparent
                                    )
                                ),
                                start = Offset(size.width * 0.1f, 1f),
                                end = Offset(size.width * 0.9f, 1f),
                                strokeWidth = 1.5f
                            )
                        }

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
                                    color = if (colors.isDark) Color(0xCC244745) else Color(0xCCD8ECEA),
                                    border = BorderStroke(
                                        0.8.dp,
                                        if (colors.isDark) Color.White.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.65f)
                                    )
                                ) {
                                    Text(
                                        text = "Across ${wallets.size} wallets",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = Inter,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        ),
                                        color = colors.primary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            AnimatedRupeeAmount(
                                targetAmount = totalNetBalance,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = SpaceGrotesk,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 38.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = colors.textPrimary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            HorizontalDivider(
                                color = colors.border.copy(alpha = 0.40f),
                                thickness = 0.8.dp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
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
                                    modifier = Modifier.clickable { onNavigateToSummary() },
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

                // 3. WALLET SECTION ("YOUR MONEY") - Stitch Premium Glass Cards with Intentional Peek
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
                            contentPadding = PaddingValues(end = 12.dp)
                        ) {
                            items(wallets) { wallet ->
                                val identity = resolveWalletIdentity(
                                    name = wallet.name,
                                    type = wallet.type,
                                    colors = colors
                                )

                                Box(
                                    modifier = Modifier
                                        .width(114.dp)
                                        .height(138.dp)
                                        .shadow(
                                            elevation = 8.dp,
                                            shape = RoundedCornerShape(20.dp),
                                            ambientColor = identity.shadowTint,
                                            spotColor = identity.shadowTint
                                        )
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(identity.backgroundBrush)
                                        .border(1.dp, identity.borderBrush, RoundedCornerShape(20.dp))
                                        .clickable { onNavigateToWalletDetail(wallet.id) }
                                        .padding(12.dp)
                                ) {
                                    // Glass specular highlight and inset sheen on wallet cards
                                    Canvas(modifier = Modifier.matchParentSize()) {
                                        drawCircle(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    if (colors.isDark) Color.White.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.48f),
                                                    Color.Transparent
                                                ),
                                                center = Offset(0f, 0f),
                                                radius = size.width * 0.75f
                                            )
                                        )
                                        drawLine(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    if (colors.isDark) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.65f),
                                                    Color.Transparent
                                                )
                                            ),
                                            start = Offset(size.width * 0.15f, 1f),
                                            end = Offset(size.width * 0.85f, 1f),
                                            strokeWidth = 1.2f
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Squircle icon container
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(identity.iconContainerColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = identity.icon,
                                                contentDescription = wallet.name,
                                                tint = identity.iconTint,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // Wallet texts: Name, Badge, Balance
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = wallet.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = Inter,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 14.sp
                                                ),
                                                color = colors.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Text(
                                                text = identity.badge,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = Inter,
                                                    fontWeight = FontWeight.Normal,
                                                    fontSize = 11.sp
                                                ),
                                                color = if (colors.isDark) colors.textSecondary else identity.badgeColor,
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
                                                color = colors.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. SPENDING INSIGHT ("THIS WEEK" - Kept more solid for contrast)
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
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = colors.surface,
                            border = BorderStroke(1.dp, colors.border.copy(alpha = 0.70f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                                fontWeight = FontWeight.Medium,
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
                                            color = if (weeklyTotal > 0.0) colors.expense else colors.primary
                                        )
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = colors.surfaceSecondary,
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        IconButton(
                                            onClick = onNavigateToSummary,
                                            modifier = Modifier.fillMaxSize()
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

                                // 7-Day clean pill expenditure bar chart
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
                                        .height(56.dp)
                                        .padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    dayBuckets.forEach { bucket ->
                                        val hasSpending = bucket.amount > 0.0

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (hasSpending) {
                                                val heightFraction = (bucket.amount / maxExpense).toFloat().coerceIn(0.35f, 1.0f)
                                                Box(
                                                    modifier = Modifier
                                                        .width(14.dp)
                                                        .height((34 * heightFraction).dp)
                                                        .clip(RoundedCornerShape(7.dp))
                                                        .background(
                                                            if (bucket.isToday) colors.primary
                                                            else colors.primary.copy(alpha = 0.40f)
                                                        )
                                                )
                                            } else if (bucket.isToday) {
                                                // Today highlight pill when no spending yet
                                                Box(
                                                    modifier = Modifier
                                                        .width(14.dp)
                                                        .height(26.dp)
                                                        .clip(RoundedCornerShape(7.dp))
                                                        .background(colors.primary)
                                                )
                                            } else {
                                                // Inactive day dash
                                                Box(
                                                    modifier = Modifier
                                                        .width(14.dp)
                                                        .height(3.dp)
                                                        .clip(RoundedCornerShape(2.dp))
                                                        .background(colors.textSecondary.copy(alpha = 0.30f))
                                                )
                                            }

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

                                // Bottom quiet week / summary banner
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = colors.surfaceSecondary.copy(alpha = 0.50f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToSummary() }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(
                                                text = "✨",
                                                fontSize = 18.sp
                                            )
                                            Column {
                                                Text(
                                                    text = if (weeklyTotal == 0.0) "No spending yet" else "${recentExpenses.size} expenses recorded",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontFamily = Inter,
                                                        fontWeight = FontWeight.Medium,
                                                        fontSize = 13.sp
                                                    ),
                                                    color = colors.textPrimary
                                                )
                                                Text(
                                                    text = if (weeklyTotal == 0.0) "You're having a quiet week." else "Tap to view spending breakdown",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontFamily = Inter,
                                                        fontSize = 12.sp
                                                    ),
                                                    color = colors.textSecondary
                                                )
                                            }
                                        }

                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = colors.textSecondary.copy(alpha = 0.70f),
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. RECENT ACTIVITY (Solid contrast container, user transactions prioritized)
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

                        if (userTransactions.isNotEmpty()) {
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

                if (userTransactions.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = colors.surface,
                            border = BorderStroke(1.dp, colors.border.copy(alpha = 0.70f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp, horizontal = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "No recent transactions",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = Inter,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp
                                        ),
                                        color = colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
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
                        val displayedList = userTransactions.take(5)

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = colors.surface,
                            border = BorderStroke(1.dp, colors.border.copy(alpha = 0.70f))
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
                                                        fontWeight = FontWeight.SemiBold,
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
                                            color = colors.border.copy(alpha = 0.40f),
                                            thickness = 0.8.dp,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Ample bottom spacer ensuring FAB and bottom bar never obscure activity content
                item {
                    Spacer(modifier = Modifier.height(116.dp))
                }
            }
        }
    }
}

private data class WalletIdentity(
    val backgroundBrush: Brush,
    val borderBrush: Brush,
    val shadowTint: Color,
    val iconContainerColor: Color,
    val iconTint: Color,
    val badgeColor: Color,
    val badge: String,
    val icon: ImageVector
)

private fun resolveWalletIdentity(name: String, type: WalletType, colors: AppColors): WalletIdentity {
    return if (colors.isDark) {
        when (name.lowercase()) {
            "upi" -> WalletIdentity(
                backgroundBrush = Brush.linearGradient(
                    listOf(Color(0xB3182E33), Color(0x80102125))
                ),
                borderBrush = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.35f), Color(0xFF264C56))
                ),
                shadowTint = Color(0x28000000),
                iconContainerColor = Color(0xFF234B5A),
                iconTint = Color(0xFF86BCD9),
                badgeColor = Color(0xFF86BCD9),
                badge = "Digital",
                icon = Icons.Default.AccountBalanceWallet
            )
            "cash" -> WalletIdentity(
                backgroundBrush = Brush.linearGradient(
                    listOf(Color(0xB3183326), Color(0x8010251C))
                ),
                borderBrush = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.35f), Color(0xFF244B36))
                ),
                shadowTint = Color(0x28000000),
                iconContainerColor = Color(0xFF1F4832),
                iconTint = Color(0xFF55C28A),
                badgeColor = Color(0xFF55C28A),
                badge = "Physical",
                icon = Icons.Default.Payments
            )
            "savings" -> WalletIdentity(
                backgroundBrush = Brush.linearGradient(
                    listOf(Color(0xB333291B), Color(0x80241C12))
                ),
                borderBrush = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.35f), Color(0xFF4C3C24))
                ),
                shadowTint = Color(0x28000000),
                iconContainerColor = Color(0xFF4A381F),
                iconTint = Color(0xFFE8B65B),
                badgeColor = Color(0xFFE8B65B),
                badge = "Reserve",
                icon = Icons.Default.Savings
            )
            else -> WalletIdentity(
                backgroundBrush = Brush.linearGradient(
                    listOf(colors.surfaceElevated.copy(alpha = 0.70f), colors.surface.copy(alpha = 0.50f))
                ),
                borderBrush = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.30f), colors.border)
                ),
                shadowTint = Color(0x28000000),
                iconContainerColor = colors.softTealBg,
                iconTint = colors.primary,
                badgeColor = colors.textSecondary,
                badge = if (type == WalletType.DIGITAL) "Digital" else "Cash",
                icon = Icons.Default.AccountBalanceWallet
            )
        }
    } else {
        when (name.lowercase()) {
            "upi" -> WalletIdentity(
                backgroundBrush = Brush.linearGradient(
                    listOf(Color(0xB8E0F2FE), Color(0x85BAE6FD))
                ),
                borderBrush = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.90f), Color(0xFF6FA8C9).copy(alpha = 0.40f))
                ),
                shadowTint = Color(0x1F6FA8C9),
                iconContainerColor = Color(0xFF6FA8C9),
                iconTint = Color.White,
                badgeColor = Color(0xFF0369A1),
                badge = "Digital",
                icon = Icons.Default.AccountBalanceWallet
            )
            "cash" -> WalletIdentity(
                backgroundBrush = Brush.linearGradient(
                    listOf(Color(0xB8DCFCE7), Color(0x85BBF7D0))
                ),
                borderBrush = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.90f), Color(0xFF35A875).copy(alpha = 0.40f))
                ),
                shadowTint = Color(0x1F35A875),
                iconContainerColor = Color(0xFF35A875),
                iconTint = Color.White,
                badgeColor = Color(0xFF15803D),
                badge = "Physical",
                icon = Icons.Default.Payments
            )
            "savings" -> WalletIdentity(
                backgroundBrush = Brush.linearGradient(
                    listOf(Color(0xB8FEF3C7), Color(0x85FDE68A))
                ),
                borderBrush = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.90f), Color(0xFFE8A83E).copy(alpha = 0.40f))
                ),
                shadowTint = Color(0x1FE8A83E),
                iconContainerColor = Color(0xFFE8A83E),
                iconTint = Color.White,
                badgeColor = Color(0xFFB45309),
                badge = "Reserve",
                icon = Icons.Default.Savings
            )
            else -> WalletIdentity(
                backgroundBrush = Brush.linearGradient(
                    listOf(Color(0xB8F7F5F0), Color(0x85EAE6DC))
                ),
                borderBrush = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.80f), SurfaceBorderLight)
                ),
                shadowTint = Color(0x12167C80),
                iconContainerColor = DeepTeal,
                iconTint = Color.White,
                badgeColor = MutedWarmGray,
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


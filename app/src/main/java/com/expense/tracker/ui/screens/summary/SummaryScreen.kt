package com.expense.tracker.ui.screens.summary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expense.tracker.data.local.entity.CategoryEntity
import com.expense.tracker.data.local.entity.TransactionEntity
import com.expense.tracker.data.model.TransactionType
import com.expense.tracker.data.model.WalletWithBalance
import com.expense.tracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

enum class SummaryPeriodType {
    WEEKLY,
    MONTHLY
}

private data class ChartBarData(
    val label: String,
    val amount: Double,
    val isHighlighted: Boolean
)

private data class CategorySpendingData(
    val id: Long?,
    val name: String,
    val amount: Double,
    val percentage: Double,
    val count: Int,
    val color: Color,
    val icon: ImageVector
)

private data class WalletSpendingData(
    val wallet: WalletWithBalance,
    val amount: Double,
    val percentage: Double,
    val color: Color,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    wallets: List<WalletWithBalance> = emptyList(),
    allTransactions: List<TransactionEntity> = emptyList(),
    categories: List<CategoryEntity> = emptyList()
) {
    val colors = AppTheme.colors
    var selectedPeriod by remember { mutableStateOf(SummaryPeriodType.WEEKLY) }

    val categoriesMap = remember(categories) { categories.associateBy { it.id } }

    // STRICT DATA INTEGRITY:
    // Only actual EXPENSE transactions count towards spending totals!
    // OPENING_BALANCE, TRANSFER_IN, TRANSFER_OUT, and INCOME are completely excluded from spending.
    val expenseTransactions = remember(allTransactions) {
        allTransactions.filter { it.type == TransactionType.EXPENSE }
    }

    // Date range calculations
    val (startOfCurrent, endOfCurrent, startOfPrev, endOfPrev, periodLabel) = remember(selectedPeriod) {
        val now = Calendar.getInstance()
        when (selectedPeriod) {
            SummaryPeriodType.WEEKLY -> {
                val endCur = (now.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis

                val startCur = (now.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, -6)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val startP = (now.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, -13)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val endP = (now.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, -7)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis

                Tuple5(startCur, endCur, startP, endP, "last week")
            }
            SummaryPeriodType.MONTHLY -> {
                val endCur = (now.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis

                val startCur = (now.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val startP = (now.clone() as Calendar).apply {
                    add(Calendar.MONTH, -1)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val endP = (startCur - 1L)

                Tuple5(startCur, endCur, startP, endP, "last month")
            }
        }
    }

    // Filter expenses into current and previous periods
    val currentPeriodExpenses = remember(expenseTransactions, startOfCurrent, endOfCurrent) {
        expenseTransactions.filter { it.timestamp in startOfCurrent..endOfCurrent }
    }
    val prevPeriodExpenses = remember(expenseTransactions, startOfPrev, endOfPrev) {
        expenseTransactions.filter { it.timestamp in startOfPrev..endOfPrev }
    }

    val currentTotalSpending = remember(currentPeriodExpenses) {
        currentPeriodExpenses.sumOf { it.amount }
    }
    val prevTotalSpending = remember(prevPeriodExpenses) {
        prevPeriodExpenses.sumOf { it.amount }
    }

    // Inflow during current period for reference
    val currentInflow = remember(allTransactions, startOfCurrent, endOfCurrent) {
        allTransactions.filter { it.type == TransactionType.INCOME && it.timestamp in startOfCurrent..endOfCurrent }
            .sumOf { it.amount }
    }

    // Period comparison badge calculation
    val comparisonInfo = remember(currentTotalSpending, prevTotalSpending, periodLabel, colors) {
        if (prevTotalSpending > 0.0) {
            val changePercent = ((currentTotalSpending - prevTotalSpending) / prevTotalSpending) * 100.0
            when {
                changePercent > 0.05 -> ComparisonBadge(
                    text = "↑${"%.1f".format(changePercent)}% vs $periodLabel",
                    textColor = colors.expense,
                    bgColor = colors.softCoralBg,
                    icon = Icons.AutoMirrored.Filled.TrendingUp
                )
                changePercent < -0.05 -> ComparisonBadge(
                    text = "↓${"%.1f".format(abs(changePercent))}% vs $periodLabel",
                    textColor = colors.income,
                    bgColor = colors.softGreenBg,
                    icon = Icons.AutoMirrored.Filled.TrendingDown
                )
                else -> ComparisonBadge(
                    text = "No change vs $periodLabel",
                    textColor = colors.textSecondary,
                    bgColor = colors.surfaceSecondary,
                    icon = Icons.AutoMirrored.Filled.TrendingFlat
                )
            }
        } else {
            if (currentTotalSpending > 0.0) {
                ComparisonBadge(
                    text = "First period on record",
                    textColor = colors.primary,
                    bgColor = colors.softTealBg,
                    icon = Icons.Default.FiberNew
                )
            } else {
                ComparisonBadge(
                    text = "No spending in this period",
                    textColor = colors.textSecondary,
                    bgColor = colors.surfaceSecondary,
                    icon = null
                )
            }
        }
    }

    // Chart bars calculation
    val chartBars = remember(selectedPeriod, currentPeriodExpenses) {
        val now = Calendar.getInstance()
        when (selectedPeriod) {
            SummaryPeriodType.WEEKLY -> {
                // 7 days: Day -6 to Day 0 (today)
                val bars = mutableListOf<ChartBarData>()
                for (i in 6 downTo 0) {
                    val cal = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -i) }
                    val dayStart = (cal.clone() as Calendar).apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    val dayEnd = (cal.clone() as Calendar).apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.timeInMillis

                    val dayAmount = currentPeriodExpenses
                        .filter { it.timestamp in dayStart..dayEnd }
                        .sumOf { it.amount }

                    val label = if (i == 0) "Today" else SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time)
                    bars.add(ChartBarData(label = label, amount = dayAmount, isHighlighted = (i == 0)))
                }
                bars
            }
            SummaryPeriodType.MONTHLY -> {
                // Weeks of current month
                val cal = (now.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                val weekBuckets = mutableListOf<Pair<String, Pair<Int, Int>>>()
                weekBuckets.add("W1" to (1 to 7))
                weekBuckets.add("W2" to (8 to 14))
                weekBuckets.add("W3" to (15 to 21))
                weekBuckets.add("W4" to (22 to 28))
                if (daysInMonth > 28) {
                    weekBuckets.add("W5" to (29 to daysInMonth))
                }

                val currentDay = now.get(Calendar.DAY_OF_MONTH)

                weekBuckets.map { (wLabel, range) ->
                    val (startDay, endDay) = range
                    val wStart = (now.clone() as Calendar).apply {
                        set(Calendar.DAY_OF_MONTH, startDay)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    val wEnd = (now.clone() as Calendar).apply {
                        set(Calendar.DAY_OF_MONTH, endDay)
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.timeInMillis

                    val wAmount = currentPeriodExpenses
                        .filter { it.timestamp in wStart..wEnd }
                        .sumOf { it.amount }

                    val isCurrentWeek = currentDay in startDay..endDay
                    ChartBarData(label = wLabel, amount = wAmount, isHighlighted = isCurrentWeek)
                }
            }
        }
    }

    // Category breakdown calculation
    val categorySpendings = remember(currentPeriodExpenses, categoriesMap, currentTotalSpending, colors) {
        val grouped = currentPeriodExpenses.groupBy { it.categoryId }
        grouped.map { (catId, txList) ->
            val catName = categoriesMap[catId]?.name ?: "General"
            val total = txList.sumOf { it.amount }
            val count = txList.size
            val pct = if (currentTotalSpending > 0.0) (total / currentTotalSpending) * 100.0 else 0.0
            val visual = resolveCategoryVisual(catName, colors)
            CategorySpendingData(
                id = catId,
                name = catName,
                amount = total,
                percentage = pct,
                count = count,
                color = visual.tint,
                icon = visual.icon
            )
        }.sortedByDescending { it.amount }
    }

    // Wallet breakdown calculation
    val walletSpendings = remember(currentPeriodExpenses, wallets, currentTotalSpending, colors) {
        wallets.map { w ->
            val wAmount = currentPeriodExpenses.filter { it.walletId == w.id }.sumOf { it.amount }
            val pct = if (currentTotalSpending > 0.0) (wAmount / currentTotalSpending) * 100.0 else 0.0
            val (wColor, wIcon) = when (w.name.lowercase(Locale.getDefault())) {
                "upi" -> Pair(colors.sky, Icons.Default.AccountBalanceWallet)
                "cash" -> Pair(colors.mint, Icons.Default.Payments)
                "savings" -> Pair(colors.amber, Icons.Default.Savings)
                else -> Pair(colors.primary, Icons.Default.AccountBalanceWallet)
            }
            WalletSpendingData(
                wallet = w,
                amount = wAmount,
                percentage = pct,
                color = wColor,
                icon = wIcon
            )
        }.sortedByDescending { it.amount }
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
                        if (colors.isDark) Color(0xFF163E3A).copy(alpha = 0.40f) else Color(0xFF8AF8BE).copy(alpha = 0.32f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.85f, size.height * 0.25f),
                    radius = size.width * 0.70f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (colors.isDark) Color(0xFF15383A).copy(alpha = 0.35f) else Color(0xFF167C80).copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.15f, size.height * 0.55f),
                    radius = size.width * 0.60f
                )
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Spending Summary",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = Manrope,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp
                            ),
                            color = colors.textPrimary
                        )
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
                // 1. Weekly / Monthly Segmented Toggle
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            PeriodToggleButton(
                                text = "Weekly",
                                isSelected = selectedPeriod == SummaryPeriodType.WEEKLY,
                                onClick = { selectedPeriod = SummaryPeriodType.WEEKLY },
                                colors = colors,
                                modifier = Modifier.weight(1f)
                            )
                            PeriodToggleButton(
                                text = "Monthly",
                                isSelected = selectedPeriod == SummaryPeriodType.MONTHLY,
                                onClick = { selectedPeriod = SummaryPeriodType.MONTHLY },
                                colors = colors,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 2. Total Spending Hero Card (Premium Glass)
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
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = heroGlassBg,
                        border = BorderStroke(1.2.dp, heroBorderBrush),
                        shadowElevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (selectedPeriod == SummaryPeriodType.WEEKLY) "TOTAL SPENT (7 DAYS)" else "TOTAL SPENT (THIS MONTH)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = Manrope,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.8.sp
                                    ),
                                    color = colors.textSecondary
                                )

                                // Previous period comparison pill badge
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = comparisonInfo.bgColor
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        comparisonInfo.icon?.let { icon ->
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = comparisonInfo.textColor,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                        Text(
                                            text = comparisonInfo.text,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = Inter,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp
                                            ),
                                            color = comparisonInfo.textColor
                                        )
                                    }
                                }
                            }

                            // Large Financial Value
                            Text(
                                text = "₹${"%.2f".format(currentTotalSpending)}",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontFamily = SpaceGrotesk,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 36.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = if (currentTotalSpending > 0.0) colors.expense else colors.textPrimary
                            )

                            HorizontalDivider(
                                color = colors.border.copy(alpha = 0.6f),
                                thickness = 0.8.dp
                            )

                            // Quick Inflow & Outflow Counter Row
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
                                            .clip(CircleShape)
                                            .background(colors.income)
                                    )
                                    Text(
                                        text = "Inflow: +₹${"%.2f".format(currentInflow)}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = Inter,
                                            fontSize = 12.sp
                                        ),
                                        color = colors.textSecondary
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(colors.expense)
                                    )
                                    Text(
                                        text = "Outflow: -₹${"%.2f".format(currentTotalSpending)}",
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
                }

                // 3. Spending Over Time Chart
                item {
                    Text(
                        text = if (selectedPeriod == SummaryPeriodType.WEEKLY) "SPENDING OVER TIME (DAILY)" else "SPENDING OVER TIME (WEEKLY)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = colors.textSecondary,
                        modifier = Modifier.padding(start = 2.dp, top = 4.dp)
                    )
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        if (currentTotalSpending == 0.0) {
                            EmptyStateNotice(
                                message = "No spending data yet",
                                subtitle = "Expenses you record will show here over time.",
                                colors = colors
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val maxBarSpend = remember(chartBars) {
                                    maxOf(chartBars.maxOfOrNull { it.amount } ?: 0.0, 1.0)
                                }

                                // Bar chart view
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .padding(top = 10.dp, bottom = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    chartBars.forEach { bar ->
                                        val barHeightRatio = (bar.amount / maxBarSpend).toFloat().coerceIn(0.04f, 1f)
                                        val isPeak = bar.amount > 0 && bar.amount == maxBarSpend
                                        val barColor = if (isPeak) colors.expense else colors.primary

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Bottom,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            // Optional small top value label if bar is peak
                                            if (bar.amount > 0) {
                                                Text(
                                                    text = if (bar.amount >= 1000) "₹${(bar.amount / 1000).toInt()}k" else "₹${bar.amount.toInt()}",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontFamily = SpaceGrotesk,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    ),
                                                    color = if (isPeak) colors.expense else colors.textSecondary,
                                                    maxLines = 1
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                            } else {
                                                Spacer(modifier = Modifier.height(18.dp))
                                            }

                                            // The Bar
                                            Box(
                                                modifier = Modifier
                                                    .width(22.dp)
                                                    .fillMaxHeight(barHeightRatio * 0.75f)
                                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
                                                    .background(
                                                        if (bar.amount > 0)
                                                            Brush.verticalGradient(
                                                                listOf(
                                                                    barColor,
                                                                    barColor.copy(alpha = 0.75f)
                                                                )
                                                            )
                                                        else
                                                            Brush.verticalGradient(
                                                                listOf(
                                                                    colors.surfaceSecondary,
                                                                    colors.surfaceSecondary
                                                                )
                                                            )
                                                    )
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Label below
                                            Text(
                                                text = bar.label,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = Inter,
                                                    fontWeight = if (bar.isHighlighted) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 11.sp
                                                ),
                                                color = if (bar.isHighlighted) colors.textPrimary else colors.textSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Category Spending Breakdown
                item {
                    Text(
                        text = "SPENDING BY CATEGORY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = colors.textSecondary,
                        modifier = Modifier.padding(start = 2.dp, top = 4.dp)
                    )
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        if (categorySpendings.isEmpty() || currentTotalSpending == 0.0) {
                            EmptyStateNotice(
                                message = "No category data yet",
                                subtitle = "Expenses categorized by Food, Travel, etc. will appear here.",
                                colors = colors
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Donut visualization
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.size(150.dp)) {
                                        var currentAngle = -90f
                                        val strokeWidth = 18.dp.toPx()
                                        val inset = strokeWidth / 2f
                                        val arcSize = Size(size.width - inset * 2, size.height - inset * 2)

                                        categorySpendings.forEach { cat ->
                                            val sweep = (cat.percentage / 100.0 * 360.0).toFloat()
                                            if (sweep > 0f) {
                                                drawArc(
                                                    color = cat.color,
                                                    startAngle = currentAngle,
                                                    sweepAngle = (sweep - 2f).coerceAtLeast(1f),
                                                    useCenter = false,
                                                    topLeft = Offset(inset, inset),
                                                    size = arcSize,
                                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                                )
                                                currentAngle += sweep
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${categorySpendings.size} ${if (categorySpendings.size == 1) "Category" else "Categories"}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = Inter,
                                                fontSize = 10.sp
                                            ),
                                            color = colors.textSecondary
                                        )
                                        Text(
                                            text = "₹${"%.0f".format(currentTotalSpending)}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = SpaceGrotesk,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            ),
                                            color = colors.textPrimary
                                        )
                                    }
                                }

                                HorizontalDivider(color = colors.border.copy(alpha = 0.6f), thickness = 0.8.dp)

                                // Itemized category spending list
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    categorySpendings.forEachIndexed { index, cat ->
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(10.dp),
                                                        color = cat.color.copy(alpha = 0.15f),
                                                        modifier = Modifier.size(34.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = cat.icon,
                                                                contentDescription = cat.name,
                                                                tint = cat.color,
                                                                modifier = Modifier.size(17.dp)
                                                            )
                                                        }
                                                    }

                                                    Column {
                                                        Text(
                                                            text = cat.name,
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                fontFamily = Inter,
                                                                fontWeight = FontWeight.Medium,
                                                                fontSize = 14.sp
                                                            ),
                                                            color = colors.textPrimary
                                                        )
                                                        Text(
                                                            text = "${cat.count} ${if (cat.count == 1) "entry" else "entries"} · ${"%.1f".format(cat.percentage)}%",
                                                            style = MaterialTheme.typography.bodySmall.copy(
                                                                fontFamily = Inter,
                                                                fontSize = 11.sp
                                                            ),
                                                            color = colors.textSecondary
                                                        )
                                                    }
                                                }

                                                Text(
                                                    text = "₹${"%.2f".format(cat.amount)}",
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontFamily = SpaceGrotesk,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 15.sp
                                                    ),
                                                    color = colors.expense
                                                )
                                            }

                                            // Progress Bar
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(5.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(colors.surfaceSecondary)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth((cat.percentage / 100f).toFloat().coerceIn(0.04f, 1f))
                                                        .fillMaxHeight()
                                                        .clip(RoundedCornerShape(3.dp))
                                                        .background(cat.color)
                                                )
                                            }
                                        }

                                        if (index < categorySpendings.size - 1) {
                                            HorizontalDivider(
                                                color = colors.border.copy(alpha = 0.4f),
                                                thickness = 0.6.dp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Wallet Breakdown
                item {
                    Text(
                        text = "SPENDING BY WALLET",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = colors.textSecondary,
                        modifier = Modifier.padding(start = 2.dp, top = 4.dp)
                    )
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        if (walletSpendings.isEmpty() || currentTotalSpending == 0.0) {
                            EmptyStateNotice(
                                message = "No wallet spending data yet",
                                subtitle = "Expenses logged from your wallets will be distributed here.",
                                colors = colors
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                walletSpendings.forEachIndexed { index, wData ->
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = wData.color.copy(alpha = 0.15f),
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = wData.icon,
                                                            contentDescription = wData.wallet.name,
                                                            tint = wData.color,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }

                                                Text(
                                                    text = wData.wallet.name,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontFamily = Inter,
                                                        fontWeight = FontWeight.Medium,
                                                        fontSize = 14.sp
                                                    ),
                                                    color = colors.textPrimary
                                                )
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "${"%.1f".format(wData.percentage)}%",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontFamily = SpaceGrotesk,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 12.sp
                                                    ),
                                                    color = colors.textSecondary
                                                )

                                                Text(
                                                    text = "₹${"%.2f".format(wData.amount)}",
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontFamily = SpaceGrotesk,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 15.sp
                                                    ),
                                                    color = colors.textPrimary
                                                )
                                            }
                                        }

                                        // Horizontal bar
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(colors.surfaceSecondary)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth((wData.percentage / 100f).toFloat().coerceIn(0.02f, 1f))
                                                    .fillMaxHeight()
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(wData.color)
                                            )
                                        }
                                    }

                                    if (index < walletSpendings.size - 1) {
                                        HorizontalDivider(
                                            color = colors.border.copy(alpha = 0.4f),
                                            thickness = 0.6.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }
}

@Composable
private fun PeriodToggleButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: AppColors,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) colors.primary else Color.Transparent,
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
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = Inter,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 13.sp
                ),
                color = if (isSelected) Color.White else colors.textSecondary
            )
        }
    }
}

@Composable
private fun EmptyStateNotice(
    message: String,
    subtitle: String,
    colors: AppColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 20.dp),
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
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = Manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            ),
            color = colors.textPrimary
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = Inter,
                fontSize = 12.sp
            ),
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

private data class ComparisonBadge(
    val text: String,
    val textColor: Color,
    val bgColor: Color,
    val icon: ImageVector?
)

private data class Tuple5<A, B, C, D, E>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E
)

private data class CatVisual(
    val icon: ImageVector,
    val tint: Color
)

private fun resolveCategoryVisual(categoryName: String?, colors: AppColors): CatVisual {
    return when (categoryName?.lowercase(Locale.getDefault())) {
        "food", "dining" -> CatVisual(Icons.Default.Restaurant, colors.expense)
        "travel", "transport" -> CatVisual(Icons.Default.DirectionsCar, colors.sky)
        "bills", "utilities" -> CatVisual(Icons.AutoMirrored.Filled.ReceiptLong, colors.amber)
        "shopping" -> CatVisual(Icons.Default.ShoppingBag, Color(0xFFD6778D))
        "entertainment" -> CatVisual(Icons.Default.Movie, colors.primary)
        "health", "medical" -> CatVisual(Icons.Default.MedicalServices, colors.expense)
        "groceries" -> CatVisual(Icons.Default.ShoppingCart, colors.income)
        else -> CatVisual(Icons.Default.Category, colors.sky)
    }
}

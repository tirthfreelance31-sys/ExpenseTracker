package com.expense.tracker.ui.screens.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class HistoryDateFilter(val label: String) {
    ALL("All Dates"),
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month")
}

enum class HistoryTypeFilter(val label: String) {
    ALL("All Types"),
    EXPENSE("Expense"),
    INCOME("Income"),
    TRANSFER("Transfer")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    transactions: List<TransactionEntity> = emptyList(),
    wallets: List<WalletWithBalance> = emptyList(),
    categories: List<CategoryEntity> = emptyList(),
    onEditTransaction: (Long) -> Unit = {},
    onDeleteTransaction: (TransactionEntity) -> Unit = {},
    onRestoreTransaction: (List<TransactionEntity>) -> Unit = {}
) {
    val colors = AppTheme.colors
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Search and filter states
    var searchQuery by remember { mutableStateOf("") }
    var selectedWalletId by remember { mutableStateOf<Long?>(null) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedDateFilter by remember { mutableStateOf(HistoryDateFilter.ALL) }
    var selectedTypeFilter by remember { mutableStateOf(HistoryTypeFilter.ALL) }

    // Dropdown expanded states
    var showWalletMenu by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showDateMenu by remember { mutableStateOf(false) }
    var showTypeMenu by remember { mutableStateOf(false) }

    val categoriesMap = remember(categories) { categories.associateBy { it.id } }
    val walletsMap = remember(wallets) { wallets.associateBy { it.id } }

    val isFilterActive = remember(searchQuery, selectedWalletId, selectedCategoryId, selectedDateFilter, selectedTypeFilter) {
        searchQuery.isNotBlank() ||
                selectedWalletId != null ||
                selectedCategoryId != null ||
                selectedDateFilter != HistoryDateFilter.ALL ||
                selectedTypeFilter != HistoryTypeFilter.ALL
    }

    // Filter transactions immediately
    val filteredTransactions = remember(
        transactions,
        searchQuery,
        selectedWalletId,
        selectedCategoryId,
        selectedDateFilter,
        selectedTypeFilter,
        categoriesMap,
        walletsMap
    ) {
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val startOfWeek = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        }.timeInMillis

        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis

        transactions.filter { tx ->
            // Search query filter
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase(Locale.getDefault())
                val catName = categoriesMap[tx.categoryId]?.name?.lowercase(Locale.getDefault()) ?: ""
                val walletName = walletsMap[tx.walletId]?.name?.lowercase(Locale.getDefault()) ?: ""
                val note = tx.note?.lowercase(Locale.getDefault()) ?: ""
                val typeStr = tx.type.name.lowercase(Locale.getDefault())
                val amountStr = tx.amount.toString()

                catName.contains(q) || walletName.contains(q) || note.contains(q) || typeStr.contains(q) || amountStr.contains(q)
            }

            // Wallet filter
            val matchesWallet = selectedWalletId == null || tx.walletId == selectedWalletId

            // Category filter
            val matchesCategory = selectedCategoryId == null || tx.categoryId == selectedCategoryId

            // Type filter
            val matchesType = when (selectedTypeFilter) {
                HistoryTypeFilter.ALL -> true
                HistoryTypeFilter.EXPENSE -> tx.type == TransactionType.EXPENSE
                HistoryTypeFilter.INCOME -> tx.type == TransactionType.INCOME
                HistoryTypeFilter.TRANSFER -> tx.type == TransactionType.TRANSFER_OUT || tx.type == TransactionType.TRANSFER_IN
            }

            // Date filter
            val matchesDate = when (selectedDateFilter) {
                HistoryDateFilter.ALL -> true
                HistoryDateFilter.TODAY -> tx.timestamp >= startOfToday
                HistoryDateFilter.THIS_WEEK -> tx.timestamp >= startOfWeek
                HistoryDateFilter.THIS_MONTH -> tx.timestamp >= startOfMonth
            }

            matchesSearch && matchesWallet && matchesCategory && matchesType && matchesDate
        }
    }

    // Inflow & Outflow for current filtered set
    val totalInflow = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }
    val totalOutflow = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }

    // Group transactions by date
    val groupedTransactions = remember(filteredTransactions) {
        val groupMap = linkedMapOf<String, MutableList<TransactionEntity>>()
        val calNow = Calendar.getInstance()
        val calTx = Calendar.getInstance()

        filteredTransactions.forEach { tx ->
            calTx.timeInMillis = tx.timestamp

            val isSameYear = calNow.get(Calendar.YEAR) == calTx.get(Calendar.YEAR)
            val isToday = isSameYear && calNow.get(Calendar.DAY_OF_YEAR) == calTx.get(Calendar.DAY_OF_YEAR)

            val calYesterday = (calNow.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
            val isYesterday = calYesterday.get(Calendar.YEAR) == calTx.get(Calendar.YEAR) &&
                    calYesterday.get(Calendar.DAY_OF_YEAR) == calTx.get(Calendar.DAY_OF_YEAR)

            val groupHeader = when {
                isToday -> "TODAY"
                isYesterday -> "YESTERDAY"
                else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(tx.timestamp)).uppercase(Locale.getDefault())
            }

            groupMap.getOrPut(groupHeader) { mutableListOf() }.add(tx)
        }
        groupMap
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Subtle ambient glow at top
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (colors.isDark) Color(0xFF163E3A).copy(alpha = 0.35f) else Color(0xFF8AF8BE).copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.85f, size.height * 0.25f),
                    radius = size.width * 0.65f
                )
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = if (colors.isDark) Color(0xFF2E3230) else Color(0xFF262B28),
                        contentColor = Color.White,
                        actionColor = colors.primary,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "History",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 20.sp
                                ),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "${filteredTransactions.size} ${if (filteredTransactions.size == 1) "entry" else "entries"}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = Inter,
                                    fontSize = 11.sp
                                ),
                                color = colors.textSecondary
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Search Bar & Filter Controls Container (Glass treatment)
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Search Field
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = colors.surface,
                            border = BorderStroke(1.dp, colors.border),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = {
                                        Text(
                                            text = "Search note, category, wallet...",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = Inter,
                                                fontSize = 13.sp
                                            ),
                                            color = colors.textMuted
                                        )
                                    },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = Inter,
                                        fontSize = 14.sp,
                                        color = colors.textPrimary
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear search",
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Compact Horizontal Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Date Filter Chip
                            Box {
                                FilterChipPill(
                                    label = selectedDateFilter.label,
                                    isActive = selectedDateFilter != HistoryDateFilter.ALL,
                                    icon = Icons.Default.CalendarToday,
                                    onClick = { showDateMenu = true },
                                    colors = colors
                                )
                                DropdownMenu(
                                    expanded = showDateMenu,
                                    onDismissRequest = { showDateMenu = false },
                                    modifier = Modifier.background(colors.surface)
                                ) {
                                    HistoryDateFilter.values().forEach { option ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    option.label,
                                                    fontFamily = Inter,
                                                    fontWeight = if (selectedDateFilter == option) FontWeight.SemiBold else FontWeight.Normal,
                                                    color = if (selectedDateFilter == option) colors.primary else colors.textPrimary
                                                )
                                            },
                                            onClick = {
                                                selectedDateFilter = option
                                                showDateMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Type Filter Chip
                            Box {
                                FilterChipPill(
                                    label = selectedTypeFilter.label,
                                    isActive = selectedTypeFilter != HistoryTypeFilter.ALL,
                                    icon = Icons.Default.FilterList,
                                    onClick = { showTypeMenu = true },
                                    colors = colors
                                )
                                DropdownMenu(
                                    expanded = showTypeMenu,
                                    onDismissRequest = { showTypeMenu = false },
                                    modifier = Modifier.background(colors.surface)
                                ) {
                                    HistoryTypeFilter.values().forEach { option ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    option.label,
                                                    fontFamily = Inter,
                                                    fontWeight = if (selectedTypeFilter == option) FontWeight.SemiBold else FontWeight.Normal,
                                                    color = if (selectedTypeFilter == option) colors.primary else colors.textPrimary
                                                )
                                            },
                                            onClick = {
                                                selectedTypeFilter = option
                                                showTypeMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Wallet Filter Chip
                            Box {
                                val activeWalletName = selectedWalletId?.let { walletsMap[it]?.name } ?: "All Wallets"
                                FilterChipPill(
                                    label = activeWalletName,
                                    isActive = selectedWalletId != null,
                                    icon = Icons.Default.AccountBalanceWallet,
                                    onClick = { showWalletMenu = true },
                                    colors = colors
                                )
                                DropdownMenu(
                                    expanded = showWalletMenu,
                                    onDismissRequest = { showWalletMenu = false },
                                    modifier = Modifier.background(colors.surface)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "All Wallets",
                                                fontFamily = Inter,
                                                fontWeight = if (selectedWalletId == null) FontWeight.SemiBold else FontWeight.Normal,
                                                color = if (selectedWalletId == null) colors.primary else colors.textPrimary
                                            )
                                        },
                                        onClick = {
                                            selectedWalletId = null
                                            showWalletMenu = false
                                        }
                                    )
                                    wallets.forEach { w ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    w.name,
                                                    fontFamily = Inter,
                                                    fontWeight = if (selectedWalletId == w.id) FontWeight.SemiBold else FontWeight.Normal,
                                                    color = if (selectedWalletId == w.id) colors.primary else colors.textPrimary
                                                )
                                            },
                                            onClick = {
                                                selectedWalletId = w.id
                                                showWalletMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Category Filter Chip
                            Box {
                                val activeCatName = selectedCategoryId?.let { categoriesMap[it]?.name } ?: "All Categories"
                                FilterChipPill(
                                    label = activeCatName,
                                    isActive = selectedCategoryId != null,
                                    icon = Icons.Default.Category,
                                    onClick = { showCategoryMenu = true },
                                    colors = colors
                                )
                                DropdownMenu(
                                    expanded = showCategoryMenu,
                                    onDismissRequest = { showCategoryMenu = false },
                                    modifier = Modifier.background(colors.surface)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "All Categories",
                                                fontFamily = Inter,
                                                fontWeight = if (selectedCategoryId == null) FontWeight.SemiBold else FontWeight.Normal,
                                                color = if (selectedCategoryId == null) colors.primary else colors.textPrimary
                                            )
                                        },
                                        onClick = {
                                            selectedCategoryId = null
                                            showCategoryMenu = false
                                        }
                                    )
                                    categories.forEach { c ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    c.name,
                                                    fontFamily = Inter,
                                                    fontWeight = if (selectedCategoryId == c.id) FontWeight.SemiBold else FontWeight.Normal,
                                                    color = if (selectedCategoryId == c.id) colors.primary else colors.textPrimary
                                                )
                                            },
                                            onClick = {
                                                selectedCategoryId = c.id
                                                showCategoryMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Clear Filters Button
                            if (isFilterActive) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = colors.softCoralBg,
                                    modifier = Modifier.clickable {
                                        searchQuery = ""
                                        selectedWalletId = null
                                        selectedCategoryId = null
                                        selectedDateFilter = HistoryDateFilter.ALL
                                        selectedTypeFilter = HistoryTypeFilter.ALL
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Clear",
                                            tint = colors.expense,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "Clear",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = Inter,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp
                                            ),
                                            color = colors.expense
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Inflow / Outflow Summary Header Card
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
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Inflow",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = Inter,
                                        fontSize = 11.sp
                                    ),
                                    color = colors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "+₹${"%.2f".format(totalInflow)}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = SpaceGrotesk,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
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

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Total Outflow",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = Inter,
                                        fontSize = 11.sp
                                    ),
                                    color = colors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "-₹${"%.2f".format(totalOutflow)}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = SpaceGrotesk,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    ),
                                    color = colors.expense
                                )
                            }
                        }
                    }
                }

                // 3. Date Grouped Transactions or Empty State
                if (filteredTransactions.isEmpty()) {
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
                                    .padding(vertical = 40.dp, horizontal = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = colors.surfaceSecondary,
                                    modifier = Modifier.size(54.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isFilterActive) Icons.Default.SearchOff else Icons.Default.Receipt,
                                            contentDescription = null,
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (isFilterActive) "No matching transactions" else "No transactions yet",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = Manrope,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    ),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = if (isFilterActive)
                                        "Try changing your search keywords or resetting the active filters."
                                    else
                                        "Transactions you log will appear here grouped by day. Tap + to add your first expense or income.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    ),
                                    color = colors.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    groupedTransactions.forEach { (dateHeader, txList) ->
                        // Date Heading
                        item(key = "header_$dateHeader") {
                            Text(
                                text = dateHeader,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.8.sp
                                ),
                                color = colors.textSecondary,
                                modifier = Modifier.padding(start = 2.dp, top = 6.dp)
                            )
                        }

                        // Container Card for the day's transactions
                        item(key = "group_$dateHeader") {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = colors.surface,
                                border = BorderStroke(1.dp, colors.border)
                            ) {
                                Column {
                                    txList.forEachIndexed { index, tx ->
                                        val category = categoriesMap[tx.categoryId]
                                        val wallet = walletsMap[tx.walletId]
                                        val timeFormatted = SimpleDateFormat("h:mm a", Locale.getDefault())
                                            .format(Date(tx.timestamp))

                                        SwipeableTransactionItem(
                                            tx = tx,
                                            category = category,
                                            wallet = wallet,
                                            timeFormatted = timeFormatted,
                                            colors = colors,
                                            onEdit = { onEditTransaction(tx.id) },
                                            onDelete = {
                                                // Handle linked transfer restoration atomically
                                                val toRestore = if (tx.linkedTransferId != null) {
                                                    transactions.filter {
                                                        it.linkedTransferId == tx.linkedTransferId || it.id == tx.linkedTransferId
                                                    }
                                                } else {
                                                    listOf(tx)
                                                }

                                                onDeleteTransaction(tx)

                                                coroutineScope.launch {
                                                    val result = snackbarHostState.showSnackbar(
                                                        message = if (tx.linkedTransferId != null) "Transfer deleted" else "Transaction deleted",
                                                        actionLabel = "Undo",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                    if (result == SnackbarResult.ActionPerformed) {
                                                        onRestoreTransaction(toRestore)
                                                    }
                                                }
                                            }
                                        )

                                        if (index < txList.size - 1) {
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
                }

                item {
                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTransactionItem(
    tx: TransactionEntity,
    category: CategoryEntity?,
    wallet: WalletWithBalance?,
    timeFormatted: String,
    colors: AppColors,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val currentOnDelete by rememberUpdatedState(onDelete)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart || dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                currentOnDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val alignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.expense)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Delete",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }
    ) {
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
            isExpense -> "-₹"
            isIncome -> "+₹"
            isTransfer -> "⇄ ₹"
            else -> "₹"
        }

        val titleText = when {
            isOpening -> "Opening Balance"
            isTransfer -> if (isTransferOut) "Transfer Out" else "Transfer In"
            else -> category?.name ?: "General"
        }

        val visual = resolveCategoryVisual(
            categoryName = category?.name,
            type = tx.type,
            colors = colors
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEdit() },
            color = colors.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
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

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
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
                                text = timeFormatted,
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

                Spacer(modifier = Modifier.width(8.dp))

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
        }
    }
}

@Composable
private fun FilterChipPill(
    label: String,
    isActive: Boolean,
    icon: ImageVector,
    onClick: () -> Unit,
    colors: AppColors
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) colors.primary else colors.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (isActive) colors.primary else colors.border
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) Color.White else colors.textSecondary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = Inter,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = if (isActive) Color.White else colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = if (isActive) Color.White.copy(alpha = 0.8f) else colors.textSecondary,
                modifier = Modifier.size(14.dp)
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

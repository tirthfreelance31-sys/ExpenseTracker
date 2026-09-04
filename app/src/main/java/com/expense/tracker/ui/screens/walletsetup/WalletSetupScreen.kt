package com.expense.tracker.ui.screens.walletsetup

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expense.tracker.data.model.WalletWithBalance
import com.expense.tracker.data.model.WalletType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletSetupScreen(
    wallets: List<WalletWithBalance>,
    onSaveOpeningBalances: suspend (Map<Long, Double>) -> Unit,
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Map walletId -> input text string
    val balanceInputs = remember { mutableStateMapOf<Long, String>() }
    var isSaving by remember { mutableStateOf(false) }

    // Pre-populate input fields with existing opening balances or default empty/zero string
    LaunchedEffect(wallets) {
        wallets.forEach { wallet ->
            if (!balanceInputs.containsKey(wallet.id)) {
                val initialText = if (wallet.openingBalance > 0) wallet.openingBalance.toString() else "0"
                balanceInputs[wallet.id] = initialText
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Set up your wallets",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Set Initial Balances",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enter starting funds for each wallet. You can set 0 or any positive amount.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(wallets) { wallet ->
                    val accentColor = when (wallet.name.lowercase()) {
                        "upi" -> Color(0xFF2196F3)
                        "cash" -> Color(0xFF4CAF50)
                        "savings" -> Color(0xFFFF9800)
                        else -> Color(android.graphics.Color.parseColor(wallet.colorHex.ifEmpty { "#2196F3" }))
                    }

                    val icon = when (wallet.name.lowercase()) {
                        "cash" -> Icons.Default.Payments
                        "savings" -> Icons.Default.Savings
                        else -> Icons.Default.AccountBalanceWallet
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(accentColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                                Column {
                                    Text(
                                        text = wallet.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (wallet.type == WalletType.DIGITAL) "Digital Wallet" else "Physical Wallet",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = balanceInputs[wallet.id] ?: "0",
                                onValueChange = { newValue ->
                                    // Allow numbers and single decimal point
                                    if (newValue.isEmpty() || newValue.matches(Regex("""^\d*\.?\d*$"""))) {
                                        balanceInputs[wallet.id] = newValue
                                    }
                                },
                                label = { Text("Initial Balance") },
                                prefix = { Text("₹ ") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Custom wallets feature coming soon!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("+ Add another wallet", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Button(
                onClick = {
                    if (isSaving) return@Button

                    // Validate balance inputs
                    val finalBalances = mutableMapOf<Long, Double>()
                    var hasError = false

                    wallets.forEach { wallet ->
                        val input = balanceInputs[wallet.id]?.trim() ?: "0"
                        val parsed = input.toDoubleOrNull()
                        if (parsed == null || parsed < 0) {
                            hasError = true
                            Toast.makeText(context, "Please enter a valid balance for ${wallet.name}", Toast.LENGTH_SHORT).show()
                        } else {
                            finalBalances[wallet.id] = parsed
                        }
                    }

                    if (!hasError) {
                        isSaving = true
                        coroutineScope.launch {
                            onSaveOpeningBalances(finalBalances)
                            onNavigateToHome()
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

package com.expense.tracker.ui.screens.walletsetup

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expense.tracker.data.model.WalletType
import com.expense.tracker.data.model.WalletWithBalance
import com.expense.tracker.ui.theme.*
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
    val colors = AppTheme.colors

    val balanceInputs = remember { mutableStateMapOf<Long, String>() }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(wallets) {
        wallets.forEach { wallet ->
            if (!balanceInputs.containsKey(wallet.id)) {
                val initialText = if (wallet.openingBalance > 0) wallet.openingBalance.toString() else "0"
                balanceInputs[wallet.id] = initialText
            }
        }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Wallet Setup",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Instructions Card
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
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Set Starting Balances",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Enter current funds for each wallet to establish your starting balance. You can edit this anytime.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = Inter,
                            fontSize = 13.sp
                        ),
                        color = colors.textSecondary
                    )
                }
            }

            Text(
                text = "YOUR WALLETS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                ),
                color = colors.textSecondary
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(wallets) { wallet ->
                    val (accentColor, icon) = when (wallet.name.lowercase()) {
                        "upi" -> Pair(colors.sky, Icons.Default.AccountBalanceWallet)
                        "cash" -> Pair(colors.income, Icons.Default.Payments)
                        "savings" -> Pair(colors.amber, Icons.Default.Savings)
                        else -> Pair(colors.primary, Icons.Default.AccountBalanceWallet)
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.border),
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
                                Surface(
                                    shape = CircleShape,
                                    color = accentColor.copy(alpha = 0.12f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = accentColor,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = wallet.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = Inter,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp
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

                            OutlinedTextField(
                                value = balanceInputs[wallet.id] ?: "0",
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty() || newValue.matches(Regex("""^\d*\.?\d*$"""))) {
                                        balanceInputs[wallet.id] = newValue
                                    }
                                },
                                label = { Text("Starting Balance", style = MaterialTheme.typography.bodySmall.copy(fontFamily = Inter)) },
                                prefix = {
                                    Text(
                                        text = "₹ ",
                                        style = TextStyle(
                                            fontFamily = SpaceGrotesk,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp,
                                            color = colors.primary
                                        )
                                    )
                                },
                                textStyle = TextStyle(
                                    fontFamily = SpaceGrotesk,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = colors.textPrimary
                                ),
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
                    }
                }
            }

            // Primary Action Button
            Button(
                onClick = {
                    if (isSaving) return@Button

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
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = if (colors.isDark) Color(0xFF0F2625) else Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = if (colors.isDark) Color(0xFF0F2625) else Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Get Started",
                        fontFamily = Manrope,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

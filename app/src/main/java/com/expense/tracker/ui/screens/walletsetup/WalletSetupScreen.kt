package com.expense.tracker.ui.screens.walletsetup

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

    // Map walletId -> input text string
    val balanceInputs = remember { mutableStateMapOf<Long, String>() }
    var isSaving by remember { mutableStateOf(false) }

    // Pre-populate input fields with existing opening balances or default zero string
    LaunchedEffect(wallets) {
        wallets.forEach { wallet ->
            if (!balanceInputs.containsKey(wallet.id)) {
                val initialText = if (wallet.openingBalance > 0) wallet.openingBalance.toString() else "0"
                balanceInputs[wallet.id] = initialText
            }
        }
    }

    Scaffold(
        containerColor = LedgerInk,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "INITIAL LEDGER SETUP",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RupeeGold,
                        letterSpacing = 1.2.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LedgerInk
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Instructions Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                color = LedgerPaper,
                border = BorderStroke(1.dp, LedgerDivider)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "FOLIO OPENING BALANCES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryText,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Specify starting funds for each account folio. This records your initial ledger audit baseline. You can adjust this anytime via account reconciliation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryText
                    )
                }
            }

            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalDivider(modifier = Modifier.width(16.dp), thickness = 1.dp, color = LedgerDivider)
                Text(
                    text = "ACCOUNTS & FOLIOS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryText,
                    letterSpacing = 1.sp
                )
                HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = LedgerDivider)
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(wallets) { wallet ->
                    val accentColor = when (wallet.name.lowercase()) {
                        "upi" -> StampIndigo
                        "cash" -> CurrencyGreen
                        "savings" -> RupeeGold
                        else -> RupeeGold
                    }

                    val icon = when (wallet.name.lowercase()) {
                        "cash" -> Icons.Default.Payments
                        "savings" -> Icons.Default.Savings
                        else -> Icons.Default.AccountBalanceWallet
                    }

                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = LedgerPaper,
                        border = BorderStroke(1.dp, LedgerDivider),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(30.dp)
                                        .background(accentColor, RoundedCornerShape(1.dp))
                                )
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = wallet.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryText
                                    )
                                    Text(
                                        text = if (wallet.type == WalletType.DIGITAL) "Digital Folio" else "Physical Cash",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SecondaryText
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
                                label = { Text("Opening Balance", style = MaterialTheme.typography.bodySmall) },
                                prefix = {
                                    Text(
                                        text = "₹ ",
                                        style = TextStyle(
                                            fontFamily = SpaceGrotesk,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = RupeeGold
                                        )
                                    )
                                },
                                textStyle = TextStyle(
                                    fontFamily = SpaceGrotesk,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = PrimaryText,
                                    fontFeatureSettings = "tnum"
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RupeeGold,
                                    unfocusedBorderColor = LedgerDivider,
                                    focusedContainerColor = LedgerInk,
                                    unfocusedContainerColor = LedgerInk,
                                    cursorColor = RupeeGold,
                                    focusedLabelColor = RupeeGold,
                                    unfocusedLabelColor = SecondaryText
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(3.dp)
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
                    containerColor = RupeeGold,
                    contentColor = LedgerInk
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = LedgerInk,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "OPEN PASSBOOK LEDGER",
                        fontFamily = IbmPlexSans,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

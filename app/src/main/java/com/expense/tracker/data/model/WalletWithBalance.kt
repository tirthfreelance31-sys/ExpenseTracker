package com.expense.tracker.data.model

data class WalletWithBalance(
    val id: Long,
    val name: String,
    val type: WalletType,
    val colorHex: String,
    val iconRes: String,
    val openingBalance: Double,
    val createdAt: Long,
    val currentBalance: Double
)

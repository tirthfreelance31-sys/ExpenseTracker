package com.expense.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.expense.tracker.data.model.WalletType

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: WalletType,
    val colorHex: String,
    val iconRes: String,
    val openingBalance: Double,
    val createdAt: Long = System.currentTimeMillis()
)

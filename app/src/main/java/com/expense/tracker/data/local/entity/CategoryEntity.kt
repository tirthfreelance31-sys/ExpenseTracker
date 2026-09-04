package com.expense.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconRes: String,
    val monthlyBudgetLimit: Double? = null,
    val isDefault: Boolean = false
)

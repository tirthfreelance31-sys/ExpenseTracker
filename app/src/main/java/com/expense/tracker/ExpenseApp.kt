package com.expense.tracker

import android.app.Application
import com.expense.tracker.data.local.ExpenseDatabase
import com.expense.tracker.data.repository.ExpenseRepository
import com.expense.tracker.data.repository.UserPreferencesRepository
import com.expense.tracker.ui.theme.initSafeFonts

class ExpenseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initSafeFonts(this)
    }

    val database by lazy { ExpenseDatabase.getDatabase(this) }
    val repository by lazy {
        ExpenseRepository(
            walletDao = database.walletDao(),
            categoryDao = database.categoryDao(),
            transactionDao = database.transactionDao()
        )
    }
    val preferencesRepository by lazy { UserPreferencesRepository(this) }
}

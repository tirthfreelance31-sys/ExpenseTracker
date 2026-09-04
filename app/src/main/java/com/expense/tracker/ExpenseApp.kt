package com.expense.tracker

import android.app.Application
import com.expense.tracker.data.local.ExpenseDatabase
import com.expense.tracker.data.repository.ExpenseRepository

class ExpenseApp : Application() {
    val database by lazy { ExpenseDatabase.getDatabase(this) }
    val repository by lazy {
        ExpenseRepository(
            walletDao = database.walletDao(),
            categoryDao = database.categoryDao(),
            transactionDao = database.transactionDao()
        )
    }
}

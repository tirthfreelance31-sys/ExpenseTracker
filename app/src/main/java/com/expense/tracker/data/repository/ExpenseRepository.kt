package com.expense.tracker.data.repository

import com.expense.tracker.data.local.dao.CategoryDao
import com.expense.tracker.data.local.dao.TransactionDao
import com.expense.tracker.data.local.dao.WalletDao
import com.expense.tracker.data.local.entity.CategoryEntity
import com.expense.tracker.data.local.entity.TransactionEntity
import com.expense.tracker.data.local.entity.WalletEntity
import com.expense.tracker.data.model.WalletWithBalance
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val walletDao: WalletDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao
) {
    // Wallets
    val walletsWithBalance: Flow<List<WalletWithBalance>> = walletDao.getWalletsWithBalance()

    fun getWalletWithBalanceById(walletId: Long): Flow<WalletWithBalance?> {
        return walletDao.getWalletWithBalanceById(walletId)
    }

    suspend fun getWalletById(walletId: Long): WalletEntity? {
        return walletDao.getWalletById(walletId)
    }

    suspend fun insertWallet(wallet: WalletEntity): Long {
        return walletDao.insertWallet(wallet)
    }

    suspend fun updateWallet(wallet: WalletEntity) {
        walletDao.updateWallet(wallet)
    }

    suspend fun deleteWallet(wallet: WalletEntity) {
        walletDao.deleteWallet(wallet)
    }

    // Categories
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun getCategoryById(id: Long): CategoryEntity? {
        return categoryDao.getCategoryById(id)
    }

    suspend fun insertCategory(category: CategoryEntity): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
    }

    // Transactions
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun getRecentTransactions(limit: Int = 5): Flow<List<TransactionEntity>> {
        return transactionDao.getRecentTransactions(limit)
    }

    fun getRecentExpenses(startTime: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getRecentExpenses(startTime)
    }

    fun getTransactionsByWallet(walletId: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByWallet(walletId)
    }

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun updateOpeningBalance(walletId: Long, newOpeningBalance: Double) {
        val existingWallet = walletDao.getWalletById(walletId) ?: return
        val updatedWallet = existingWallet.copy(openingBalance = newOpeningBalance)
        walletDao.updateWallet(updatedWallet)

        val existingOpeningTx = transactionDao.getOpeningBalanceTransactionForWallet(walletId)
        if (existingOpeningTx != null) {
            val updatedTx = existingOpeningTx.copy(amount = newOpeningBalance, timestamp = System.currentTimeMillis())
            transactionDao.updateTransaction(updatedTx)
        } else {
            val newTx = TransactionEntity(
                walletId = walletId,
                categoryId = null,
                type = com.expense.tracker.data.model.TransactionType.OPENING_BALANCE,
                amount = newOpeningBalance,
                note = "Opening Balance",
                timestamp = System.currentTimeMillis()
            )
            transactionDao.insertTransaction(newTx)
        }
    }
}

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
}

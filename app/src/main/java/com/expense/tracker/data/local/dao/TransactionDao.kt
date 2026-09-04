package com.expense.tracker.data.local.dao

import androidx.room.*
import com.expense.tracker.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE walletId = :walletId ORDER BY timestamp DESC")
    fun getTransactionsByWallet(walletId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = 'EXPENSE' AND timestamp >= :startTime ORDER BY timestamp ASC")
    fun getRecentExpenses(startTime: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE walletId = :walletId AND type = 'OPENING_BALANCE' LIMIT 1")
    suspend fun getOpeningBalanceTransactionForWallet(walletId: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE linkedTransferId = :linkedTransferId")
    suspend fun getLinkedTransactions(linkedTransferId: Long): List<TransactionEntity>

    @Query("DELETE FROM transactions WHERE linkedTransferId = :linkedTransferId OR id = :linkedTransferId")
    suspend fun deleteLinkedTransfer(linkedTransferId: Long)

    @Transaction
    suspend fun insertTransfer(fromTx: TransactionEntity, toTx: TransactionEntity) {
        val fromId = insertTransaction(fromTx)
        val linkedId = fromTx.linkedTransferId ?: fromId
        val updatedFromTx = fromTx.copy(id = fromId, linkedTransferId = linkedId)
        updateTransaction(updatedFromTx)
        val updatedToTx = toTx.copy(linkedTransferId = linkedId)
        insertTransaction(updatedToTx)
    }

    @Transaction
    suspend fun updateTransfer(fromTx: TransactionEntity, toTx: TransactionEntity) {
        updateTransaction(fromTx)
        updateTransaction(toTx)
    }

    @Transaction
    suspend fun deleteTransferByLinkedId(linkedTransferId: Long) {
        deleteLinkedTransfer(linkedTransferId)
    }

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int
}

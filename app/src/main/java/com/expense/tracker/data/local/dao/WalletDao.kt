package com.expense.tracker.data.local.dao

import androidx.room.*
import com.expense.tracker.data.local.entity.WalletEntity
import com.expense.tracker.data.model.WalletWithBalance
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("""
        SELECT 
            w.id AS id, 
            w.name AS name, 
            w.type AS type, 
            w.colorHex AS colorHex, 
            w.iconRes AS iconRes, 
            w.openingBalance AS openingBalance, 
            w.createdAt AS createdAt,
            COALESCE(w.openingBalance, 0.0) + COALESCE(SUM(
                CASE 
                    WHEN t.type IN ('INCOME', 'TRANSFER_IN') THEN t.amount
                    WHEN t.type IN ('EXPENSE', 'TRANSFER_OUT') THEN -t.amount
                    WHEN t.type = 'OPENING_BALANCE' THEN t.amount
                    ELSE 0.0 
                END
            ), 0.0) AS currentBalance
        FROM wallets w
        LEFT JOIN transactions t ON w.id = t.walletId
        GROUP BY w.id
        ORDER BY w.createdAt ASC
    """)
    fun getWalletsWithBalance(): Flow<List<WalletWithBalance>>

    @Query("""
        SELECT 
            w.id AS id, 
            w.name AS name, 
            w.type AS type, 
            w.colorHex AS colorHex, 
            w.iconRes AS iconRes, 
            w.openingBalance AS openingBalance, 
            w.createdAt AS createdAt,
            COALESCE(w.openingBalance, 0.0) + COALESCE(SUM(
                CASE 
                    WHEN t.type IN ('INCOME', 'TRANSFER_IN') THEN t.amount
                    WHEN t.type IN ('EXPENSE', 'TRANSFER_OUT') THEN -t.amount
                    WHEN t.type = 'OPENING_BALANCE' THEN t.amount
                    ELSE 0.0 
                END
            ), 0.0) AS currentBalance
        FROM wallets w
        LEFT JOIN transactions t ON w.id = t.walletId
        WHERE w.id = :walletId
        GROUP BY w.id
    """)
    fun getWalletWithBalanceById(walletId: Long): Flow<WalletWithBalance?>

    @Query("SELECT * FROM wallets WHERE id = :id")
    suspend fun getWalletById(id: Long): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity): Long

    @Update
    suspend fun updateWallet(wallet: WalletEntity)

    @Delete
    suspend fun deleteWallet(wallet: WalletEntity)

    @Query("SELECT COUNT(*) FROM wallets")
    suspend fun getWalletCount(): Int
}

package com.expense.tracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.expense.tracker.data.local.dao.CategoryDao
import com.expense.tracker.data.local.dao.TransactionDao
import com.expense.tracker.data.local.dao.WalletDao
import com.expense.tracker.data.local.entity.CategoryEntity
import com.expense.tracker.data.local.entity.TransactionEntity
import com.expense.tracker.data.local.entity.WalletEntity
import com.expense.tracker.data.model.WalletType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        WalletEntity::class,
        CategoryEntity::class,
        TransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ExpenseDatabase : RoomDatabase() {

    abstract fun walletDao(): WalletDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: ExpenseDatabase? = null

        fun getDatabase(context: Context, passphrase: ByteArray? = "expense_tracker_secure_key_2026".toByteArray()): ExpenseDatabase {
            return INSTANCE ?: synchronized(this) {
                SQLiteDatabase.loadLibs(context)

                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDatabase::class.java,
                    "expense_tracker_db"
                ).addCallback(DatabaseCallback(context))

                if (passphrase != null) {
                    val factory = SupportFactory(passphrase)
                    builder.openHelperFactory(factory)
                }

                val instance = builder.build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDefaultData(database)
                    }
                }
            }

            private suspend fun populateDefaultData(db: ExpenseDatabase) {
                val defaultCategories = listOf(
                    CategoryEntity(name = "Food", iconRes = "restaurant", isDefault = true),
                    CategoryEntity(name = "Travel", iconRes = "directions_car", isDefault = true),
                    CategoryEntity(name = "Bills", iconRes = "receipt_long", isDefault = true),
                    CategoryEntity(name = "Shopping", iconRes = "shopping_bag", isDefault = true),
                    CategoryEntity(name = "Entertainment", iconRes = "movie", isDefault = true),
                    CategoryEntity(name = "Health", iconRes = "medical_services", isDefault = true),
                    CategoryEntity(name = "Groceries", iconRes = "shopping_cart", isDefault = true),
                    CategoryEntity(name = "Other", iconRes = "more_horiz", isDefault = true)
                )
                db.categoryDao().insertCategories(defaultCategories)

                if (db.walletDao().getWalletCount() == 0) {
                    db.walletDao().insertWallet(
                        WalletEntity(
                            name = "UPI",
                            type = WalletType.DIGITAL,
                            colorHex = "#2196F3",
                            iconRes = "account_balance_wallet",
                            openingBalance = 0.0
                        )
                    )
                    db.walletDao().insertWallet(
                        WalletEntity(
                            name = "Cash",
                            type = WalletType.PHYSICAL,
                            colorHex = "#4CAF50",
                            iconRes = "payments",
                            openingBalance = 0.0
                        )
                    )
                    db.walletDao().insertWallet(
                        WalletEntity(
                            name = "Savings",
                            type = WalletType.PHYSICAL,
                            colorHex = "#FF9800",
                            iconRes = "savings",
                            openingBalance = 0.0
                        )
                    )
                }
            }
        }
    }
}

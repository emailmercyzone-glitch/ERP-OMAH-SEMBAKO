package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.*
import com.example.data.model.*

@Database(
    entities = [
        ProductEntity::class,
        BatchEntity::class,
        CustomerEntity::class,
        SupplierEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        PurchaseEntity::class,
        PurchaseItemEntity::class,
        DebtEntity::class,
        DebtPaymentEntity::class,
        KnsSettlementEntity::class,
        CashAccountEntity::class,
        CashMutationEntity::class,
        ExpenseEntity::class,
        JournalEntryEntity::class,
        JournalLineEntity::class,
        ShiftRecordEntity::class,
        OpnameRecordEntity::class,
        OpnameLineEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun batchDao(): BatchDao
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun saleDao(): SaleDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun debtDao(): DebtDao
    abstract fun knsSettlementDao(): KnsSettlementDao
    abstract fun cashDao(): CashDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun accountingDao(): AccountingDao
    abstract fun shiftDao(): ShiftDao
    abstract fun opnameDao(): OpnameDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "omah_erp_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

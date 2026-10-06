package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Query("UPDATE products SET stockQty = :newStock WHERE id = :productId")
    suspend fun updateStockQty(productId: String, newStock: Double)

    @Query("DELETE FROM products")
    suspend fun deleteAll()
}

@Dao
interface BatchDao {
    @Query("SELECT * FROM batches WHERE productId = :productId AND qtyLeft > 0 ORDER BY timestamp ASC")
    suspend fun getActiveBatchesFifo(productId: String): List<BatchEntity>

    @Query("SELECT * FROM batches WHERE productId = :productId ORDER BY timestamp DESC")
    fun getBatchesForProduct(productId: String): Flow<List<BatchEntity>>

    @Query("SELECT * FROM batches ORDER BY timestamp DESC")
    fun getAllBatches(): Flow<List<BatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: BatchEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatches(batches: List<BatchEntity>)

    @Update
    suspend fun updateBatch(batch: BatchEntity)

    @Query("DELETE FROM batches")
    suspend fun deleteAll()
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCustomer(customer: CustomerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Query("UPDATE customers SET outstandingDebt = outstandingDebt + :amount WHERE id = :customerId")
    suspend fun adjustOutstandingDebt(customerId: String, amount: Double)

    @Query("DELETE FROM customers")
    suspend fun deleteAll()
}

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE id = :id LIMIT 1")
    suspend fun getSupplierById(id: String): SupplierEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuppliers(suppliers: List<SupplierEntity>)

    @Query("DELETE FROM suppliers")
    suspend fun deleteAll()
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    suspend fun getSaleById(id: String): SaleEntity?

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getSaleItems(saleId: String): List<SaleItemEntity>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    fun getSaleItemsFlow(saleId: String): Flow<List<SaleItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Query("DELETE FROM sales")
    suspend fun deleteAllSales()

    @Query("DELETE FROM sale_items")
    suspend fun deleteAllSaleItems()
}

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases ORDER BY timestamp DESC")
    fun getAllPurchases(): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun getPurchaseItems(purchaseId: String): List<PurchaseItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItems(items: List<PurchaseItemEntity>)

    @Query("DELETE FROM purchases")
    suspend fun deleteAllPurchases()

    @Query("DELETE FROM purchase_items")
    suspend fun deleteAllPurchaseItems()
}

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts WHERE type = 'AR' ORDER BY createdAt DESC")
    fun getAllReceivables(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE type = 'AP' ORDER BY createdAt DESC")
    fun getAllPayables(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    suspend fun getDebtById(id: String): DebtEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity)

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtPayment(payment: DebtPaymentEntity)

    @Query("SELECT * FROM debt_payments WHERE debtId = :debtId ORDER BY timestamp DESC")
    fun getPaymentsForDebt(debtId: String): Flow<List<DebtPaymentEntity>>

    @Query("DELETE FROM debts")
    suspend fun deleteAllDebts()

    @Query("DELETE FROM debt_payments")
    suspend fun deleteAllPayments()
}

@Dao
interface KnsSettlementDao {
    @Query("SELECT * FROM kns_settlements ORDER BY id DESC")
    fun getAllSettlements(): Flow<List<KnsSettlementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettlement(settlement: KnsSettlementEntity)

    @Update
    suspend fun updateSettlement(settlement: KnsSettlementEntity)

    @Query("DELETE FROM kns_settlements")
    suspend fun deleteAll()
}

@Dao
interface CashDao {
    @Query("SELECT * FROM cash_accounts ORDER BY id ASC")
    fun getAllAccounts(): Flow<List<CashAccountEntity>>

    @Query("SELECT * FROM cash_accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: String): CashAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<CashAccountEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: CashAccountEntity)

    @Query("UPDATE cash_accounts SET balance = balance + :amount WHERE id = :accountId")
    suspend fun adjustBalance(accountId: String, amount: Double)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMutation(mutation: CashMutationEntity)

    @Query("SELECT * FROM cash_mutations ORDER BY timestamp DESC LIMIT 100")
    fun getRecentMutations(): Flow<List<CashMutationEntity>>

    @Query("DELETE FROM cash_accounts")
    suspend fun deleteAllAccounts()

    @Query("DELETE FROM cash_mutations")
    suspend fun deleteAllMutations()
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses")
    suspend fun deleteAll()
}

@Dao
interface AccountingDao {
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllJournals(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_lines WHERE journalId = :journalId")
    suspend fun getLinesForJournal(journalId: String): List<JournalLineEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(entry: JournalEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalLines(lines: List<JournalLineEntity>)

    @Query("DELETE FROM journal_entries")
    suspend fun deleteAllJournals()

    @Query("DELETE FROM journal_lines")
    suspend fun deleteAllLines()
}

@Dao
interface ShiftDao {
    @Query("SELECT * FROM shift_records WHERE status = 'OPEN' ORDER BY openedAt DESC LIMIT 1")
    suspend fun getCurrentOpenShift(): ShiftRecordEntity?

    @Query("SELECT * FROM shift_records WHERE status = 'OPEN' ORDER BY openedAt DESC LIMIT 1")
    fun getCurrentOpenShiftFlow(): Flow<ShiftRecordEntity?>

    @Query("SELECT * FROM shift_records ORDER BY openedAt DESC")
    fun getAllShifts(): Flow<List<ShiftRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: ShiftRecordEntity): Long

    @Update
    suspend fun updateShift(shift: ShiftRecordEntity)

    @Query("DELETE FROM shift_records")
    suspend fun deleteAll()
}

@Dao
interface OpnameDao {
    @Query("SELECT * FROM opname_records ORDER BY timestamp DESC")
    fun getAllOpnames(): Flow<List<OpnameRecordEntity>>

    @Query("SELECT * FROM opname_lines WHERE opnameId = :opnameId")
    suspend fun getOpnameLines(opnameId: Long): List<OpnameLineEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpname(opname: OpnameRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpnameLines(lines: List<OpnameLineEntity>)

    @Update
    suspend fun updateOpname(opname: OpnameRecordEntity)

    @Query("DELETE FROM opname_records")
    suspend fun deleteAllOpnames()

    @Query("DELETE FROM opname_lines")
    suspend fun deleteAllOpnameLines()
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 150")
    fun getRecentLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity)

    @Query("DELETE FROM audit_logs")
    suspend fun deleteAll()
}

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Inventory ownership:
 * - EQT: Owned by Omah (milik sendiri, physical, FIFO valuation & COGS)
 * - KNS: Consignment (milik supplier, titipan fisik, commission Z=X*rate, supplier share Y=X-Z)
 * - AFL: Affiliate / Virtual (virtual, tanpa fisik, earned commission)
 */
enum class OwnershipType {
    EQT, KNS, AFL
}

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String, // e.g. "P-BERAS-EQT"
    val name: String,
    val category: String, // Sembako, Minuman, Titipan, Jasa-AFL
    val brand: String = "Lokal",
    val barcode: String,
    val uomBase: String = "pcs", // kg, pcs, dus, etc.
    val buyPrice: Double, // Baseline acquisition cost
    val sellPrice: Double, // Normal selling price
    val ownership: OwnershipType = OwnershipType.EQT,
    val knsRate: Double = 0.10, // 10% commission default for KNS
    val aflRate: Double = 0.15, // 15% commission default for AFL
    val stockQty: Double = 0.0, // Cached aggregated stock
    val minStockAlert: Double = 5.0,
    val isActive: Boolean = true
)

@Entity(tableName = "batches")
data class BatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: String,
    val batchNumber: String,
    val qtyIn: Double,
    val qtyLeft: Double, // Can be temporarily negative if stock is sold before receipt entered
    val buyPrice: Double,
    val expiryDate: String = "",
    val ownership: OwnershipType = OwnershipType.EQT,
    val receiptRef: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val negativeFlag: Boolean = false
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String, // e.g. "C-WARUNG"
    val name: String,
    val phone: String = "",
    val address: String = "",
    val creditLimit: Double = 0.0,
    val outstandingDebt: Double = 0.0,
    val tempoDays: Int = 0,
    val isBlacklisted: Boolean = false
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey val id: String, // e.g. "S-DIST"
    val name: String,
    val phone: String = "",
    val address: String = "",
    val defaultTermDays: Int = 7
)

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey val id: String, // INV-YYYYMMDD-SEQ
    val customerId: String = "C-TUNAI",
    val customerName: String = "Pelanggan Tunai",
    val totalAmount: Double,
    val discountAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val grandTotal: Double,
    val paidAmount: Double,
    val changeAmount: Double = 0.0,
    val paymentMethod: String = "CASH", // CASH, BANK, CREDIT, SPLIT
    val paymentStatus: String = "PAID", // PAID, PARTIAL, UNPAID
    val cashierName: String = "Kasir 1",
    val roleMode: String = "CASHIER", // CASHIER or OWNER
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "sale_items")
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: String,
    val productId: String,
    val productName: String,
    val ownership: OwnershipType,
    val qty: Double,
    val unitPrice: Double,
    val subtotal: Double,
    val costPriceSnapshot: Double = 0.0, // COGS per unit from FIFO
    val knsSupplierShare: Double = 0.0, // Y share for consignment
    val knsOmahCommission: Double = 0.0, // Z commission for Omah
    val aflCommission: Double = 0.0 // AFL commission
)

@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey val id: String, // PO-YYYYMMDD-SEQ or REC-YYYYMMDD-SEQ
    val invoiceNumber: String,
    val supplierId: String,
    val supplierName: String,
    val totalAmount: Double,
    val paidAmount: Double,
    val paymentMethod: String = "CASH", // CASH, BANK, AP (Tempo)
    val status: String = "PAID", // PAID, AP
    val dueDate: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "purchase_items")
data class PurchaseItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseId: String,
    val productId: String,
    val productName: String,
    val ownership: OwnershipType,
    val qty: Double,
    val unitCost: Double,
    val subtotal: Double,
    val expiryDate: String = "",
    val batchNumber: String = ""
)

/**
 * Accounts Receivable (AR) from Customers and Accounts Payable (AP) to Suppliers
 */
@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey val id: String, // AR-XXX or AP-XXX
    val type: String, // "AR" (Piutang) or "AP" (Hutang)
    val refId: String, // saleId or purchaseId
    val partnerId: String, // customerId or supplierId
    val partnerName: String,
    val totalAmount: Double,
    val paidAmount: Double,
    val remainingAmount: Double,
    val dueDate: String = "",
    val status: String = "CURRENT", // CURRENT, OVERDUE, PAID, WRITTEN_OFF
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "debt_payments")
data class DebtPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val debtId: String,
    val amount: Double,
    val paymentMethod: String = "CASH", // CASH, BANK
    val accountUsed: String = "KAS_UTAMA",
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "kns_settlements")
data class KnsSettlementEntity(
    @PrimaryKey val id: String, // SET-YYYYMMDD-SEQ
    val supplierId: String,
    val supplierName: String,
    val periodLabel: String,
    val totalSalesX: Double,
    val supplierShareY: Double,
    val omahCommissionZ: Double,
    val status: String = "CALCULATED", // CALCULATED, APPROVED, SETTLED
    val settledAt: Long = 0
)

@Entity(tableName = "cash_accounts")
data class CashAccountEntity(
    @PrimaryKey val id: String, // "KAS_UTAMA", "BANK_BCA", "E_WALLET"
    val name: String,
    val balance: Double,
    val accountType: String = "CASH" // CASH, BANK, EWALLET
)

@Entity(tableName = "cash_mutations")
data class CashMutationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: String,
    val type: String, // "IN" or "OUT"
    val amount: Double,
    val refType: String, // "SALE", "PURCHASE", "EXPENSE", "DEBT_PAYMENT", "SETTLEMENT", "SHIFT_DIFF", "TRANSFER", "OPENING"
    val refId: String = "",
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // Listrik, Air, Sewa, Internet, Transportasi, ATK, Perbaikan, Lain-lain
    val amount: Double,
    val paymentAccount: String = "KAS_UTAMA",
    val isPaid: Boolean = true,
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey val id: String, // JRN-YYYYMMDD-SEQ
    val txRef: String,
    val txType: String, // SALE, PURCHASE, RETURN, EXPENSE, DEBT, SETTLEMENT, OPNAME, OPENING
    val date: String,
    val description: String,
    val debitTotal: Double,
    val creditTotal: Double, // Must equal debitTotal (D=K)
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "journal_lines")
data class JournalLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val journalId: String,
    val accountName: String, // e.g. "Kas", "Persediaan EQT", "Piutang Usaha", "Pendapatan Penjualan", "HPP (COGS)"
    val isDebit: Boolean,
    val amount: Double
)

@Entity(tableName = "shift_records")
data class ShiftRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cashierName: String,
    val openedAt: Long = System.currentTimeMillis(),
    val closedAt: Long = 0,
    val startCash: Double,
    val expectedCash: Double = 0.0,
    val actualCash: Double = 0.0,
    val difference: Double = 0.0,
    val notes: String = "",
    val status: String = "OPEN" // OPEN, CLOSED
)

@Entity(tableName = "opname_records")
data class OpnameRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val conductedBy: String,
    val status: String = "DRAFT", // DRAFT, APPLIED
    val totalDifferenceCost: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "opname_lines")
data class OpnameLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val opnameId: Long,
    val productId: String,
    val productName: String,
    val systemQty: Double,
    val physicalQty: Double,
    val differenceQty: Double,
    val unitCost: Double,
    val differenceAmount: Double,
    val reason: String = ""
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String,
    val actor: String,
    val roleMode: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

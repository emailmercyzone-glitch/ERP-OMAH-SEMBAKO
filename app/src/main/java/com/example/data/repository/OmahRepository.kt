package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class OmahRepository(private val db: AppDatabase) {

    private val productDao = db.productDao()
    private val batchDao = db.batchDao()
    private val customerDao = db.customerDao()
    private val supplierDao = db.supplierDao()
    private val saleDao = db.saleDao()
    private val purchaseDao = db.purchaseDao()
    private val debtDao = db.debtDao()
    private val knsSettlementDao = db.knsSettlementDao()
    private val cashDao = db.cashDao()
    private val expenseDao = db.expenseDao()
    private val accountingDao = db.accountingDao()
    private val shiftDao = db.shiftDao()
    private val opnameDao = db.opnameDao()
    private val auditLogDao = db.auditLogDao()

    // Reactive Flows for UI
    val productsFlow: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val customersFlow: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    val suppliersFlow: Flow<List<SupplierEntity>> = supplierDao.getAllSuppliers()
    val salesFlow: Flow<List<SaleEntity>> = saleDao.getAllSales()
    val purchasesFlow: Flow<List<PurchaseEntity>> = purchaseDao.getAllPurchases()
    val receivablesFlow: Flow<List<DebtEntity>> = debtDao.getAllReceivables()
    val payablesFlow: Flow<List<DebtEntity>> = debtDao.getAllPayables()
    val settlementsFlow: Flow<List<KnsSettlementEntity>> = knsSettlementDao.getAllSettlements()
    val accountsFlow: Flow<List<CashAccountEntity>> = cashDao.getAllAccounts()
    val mutationsFlow: Flow<List<CashMutationEntity>> = cashDao.getRecentMutations()
    val expensesFlow: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val journalsFlow: Flow<List<JournalEntryEntity>> = accountingDao.getAllJournals()
    val shiftsFlow: Flow<List<ShiftRecordEntity>> = shiftDao.getAllShifts()
    val currentOpenShiftFlow: Flow<ShiftRecordEntity?> = shiftDao.getCurrentOpenShiftFlow()
    val auditLogsFlow: Flow<List<AuditLogEntity>> = auditLogDao.getRecentLogs()

    suspend fun getSaleItems(saleId: String): List<SaleItemEntity> = withContext(Dispatchers.IO) {
        saleDao.getSaleItems(saleId)
    }

    suspend fun getJournalLines(journalId: String): List<JournalLineEntity> = withContext(Dispatchers.IO) {
        accountingDao.getLinesForJournal(journalId)
    }

    suspend fun getBatchesForProduct(productId: String): Flow<List<BatchEntity>> = withContext(Dispatchers.IO) {
        batchDao.getBatchesForProduct(productId)
    }

    /**
     * Seeds canonical "Toko Maju Jaya" data if empty.
     */
    suspend fun seedCanonicalDataIfEmpty() = withContext(Dispatchers.IO) {
        val existingAccounts = cashDao.getAccountById("KAS_UTAMA")
        if (existingAccounts == null) {
            resetToCanonicalData()
        }
    }

    /**
     * Resets database to Canonical Synthetic Dataset "Toko Maju Jaya" (Briefing / 53)
     */
    suspend fun resetToCanonicalData() = withContext(Dispatchers.IO) {
        // Clear all tables
        productDao.deleteAll()
        batchDao.deleteAll()
        customerDao.deleteAll()
        supplierDao.deleteAll()
        saleDao.deleteAllSales()
        saleDao.deleteAllSaleItems()
        purchaseDao.deleteAllPurchases()
        purchaseDao.deleteAllPurchaseItems()
        debtDao.deleteAllDebts()
        debtDao.deleteAllPayments()
        knsSettlementDao.deleteAll()
        cashDao.deleteAllAccounts()
        cashDao.deleteAllMutations()
        expenseDao.deleteAll()
        accountingDao.deleteAllJournals()
        accountingDao.deleteAllLines()
        shiftDao.deleteAll()
        opnameDao.deleteAllOpnames()
        opnameDao.deleteAllOpnameLines()
        auditLogDao.deleteAll()

        // 1. Initial Cash & Bank Accounts (M3 Opening: Kas 5jt, Bank 10jt)
        val initialAccounts = listOf(
            CashAccountEntity("KAS_UTAMA", "Kas Toko (Laci)", 5_000_000.0, "CASH"),
            CashAccountEntity("BANK_BCA", "Bank BCA", 10_000_000.0, "BANK"),
            CashAccountEntity("E_WALLET", "E-Wallet QRIS", 1_500_000.0, "EWALLET")
        )
        cashDao.insertAccounts(initialAccounts)

        cashDao.insertMutation(CashMutationEntity(
            accountId = "KAS_UTAMA",
            type = "IN",
            amount = 5_000_000.0,
            refType = "OPENING",
            description = "Saldo Awal Kas Toko Maju Jaya"
        ))
        cashDao.insertMutation(CashMutationEntity(
            accountId = "BANK_BCA",
            type = "IN",
            amount = 10_000_000.0,
            refType = "OPENING",
            description = "Saldo Awal Rekening Bank BCA"
        ))

        // 2. Initial Suppliers
        val initialSuppliers = listOf(
            SupplierEntity("S-DIST", "Distributor Sembako Nusantara", "08123456789", "Pergudangan Jaya Blok B-12", 14),
            SupplierEntity("S1-KNS", "Juragan Gula Pak Haji (Konsinyasi)", "08139876543", "Pasar Induk Kios 45", 7),
            SupplierEntity("P1-AFL", "Kopi Nusantara Mitra (Afiliasi)", "08155554444", "Digital Partner hub", 30)
        )
        supplierDao.insertSuppliers(initialSuppliers)

        // 3. Initial Customers
        val initialCustomers = listOf(
            CustomerEntity("C-TUNAI", "Pelanggan Tunai (Umum)", "-", "-", 0.0, 0.0, 0, false),
            CustomerEntity("C-WARUNG", "Warung Bu Siti", "081711223344", "Jl. Mawar No. 12", 1_500_000.0, 0.0, 7, false),
            CustomerEntity("C-RESTO", "Rumah Makan Sedap Rasa", "081822334455", "Jl. Sudirman No. 88", 3_000_000.0, 0.0, 14, false),
            CustomerEntity("C-PAK-BUDI", "Toko Kelontong Pak Budi", "081933445566", "Dusun Krajan RT 02", 2_000_000.0, 0.0, 7, false)
        )
        customerDao.insertCustomers(initialCustomers)

        // 4. Products (M2 Master: Beras EQT, Minyak EQT, Mie EQT, Gula KNS, Kopi AFL, etc.)
        val initialProducts = listOf(
            ProductEntity(
                id = "P-BERAS-EQT",
                name = "Beras Ramos Super 1kg",
                category = "Sembako",
                brand = "Ramos Jaya",
                barcode = "BR001",
                uomBase = "kg",
                buyPrice = 12_000.0,
                sellPrice = 15_000.0,
                ownership = OwnershipType.EQT,
                stockQty = 100.0,
                minStockAlert = 10.0
            ),
            ProductEntity(
                id = "P-MINYAK-EQT",
                name = "Minyak Goreng Sawit 2L",
                category = "Sembako",
                brand = "Sania",
                barcode = "MY002",
                uomBase = "pcs",
                buyPrice = 30_000.0,
                sellPrice = 35_000.0,
                ownership = OwnershipType.EQT,
                stockQty = 24.0,
                minStockAlert = 6.0
            ),
            ProductEntity(
                id = "P-MIE-EQT",
                name = "Mie Instan Goreng Spesial",
                category = "Sembako",
                brand = "Indofood",
                barcode = "MI003",
                uomBase = "pcs",
                buyPrice = 3_000.0,
                sellPrice = 3_500.0,
                ownership = OwnershipType.EQT,
                stockQty = 200.0,
                minStockAlert = 20.0
            ),
            ProductEntity(
                id = "P-GULA-KNS",
                name = "Gula Pasir Kristal 1kg (Konsinyasi)",
                category = "Titipan",
                brand = "Pak Haji",
                barcode = "GL004",
                uomBase = "kg",
                buyPrice = 14_400.0, // Supplier entitlement Y
                sellPrice = 16_000.0,
                ownership = OwnershipType.KNS,
                knsRate = 0.10, // 10% Komisi Z = 1.600, Hak Supplier Y = 14.400
                stockQty = 50.0,
                minStockAlert = 10.0
            ),
            ProductEntity(
                id = "P-KOPI-AFL",
                name = "Kopi Robusta Kemasan 250g (Virtual)",
                category = "Jasa-AFL",
                brand = "Kopi Nusantara",
                barcode = "KP005",
                uomBase = "pack",
                buyPrice = 17_000.0,
                sellPrice = 20_000.0,
                ownership = OwnershipType.AFL,
                aflRate = 0.15, // 15% Komisi = 3.000
                stockQty = 999.0, // Virtual
                minStockAlert = 0.0
            ),
            ProductEntity(
                id = "P-TELUR-EQT",
                name = "Telur Ayam Negeri 1kg",
                category = "Sembako",
                brand = "Peternak Lokal",
                barcode = "TL006",
                uomBase = "kg",
                buyPrice = 26_000.0,
                sellPrice = 29_000.0,
                ownership = OwnershipType.EQT,
                stockQty = 40.0,
                minStockAlert = 10.0
            ),
            ProductEntity(
                id = "P-TERIGU-EQT",
                name = "Tepung Terigu Segitiga 1kg",
                category = "Sembako",
                brand = "Bogasari",
                barcode = "TG007",
                uomBase = "pcs",
                buyPrice = 11_000.0,
                sellPrice = 13_500.0,
                ownership = OwnershipType.EQT,
                stockQty = 60.0,
                minStockAlert = 10.0
            ),
            ProductEntity(
                id = "P-SABUN-EQT",
                name = "Sabun Cuci Piring Cair 750ml",
                category = "Kebersihan",
                brand = "Sunlight",
                barcode = "SB008",
                uomBase = "pcs",
                buyPrice = 14_000.0,
                sellPrice = 16_500.0,
                ownership = OwnershipType.EQT,
                stockQty = 35.0,
                minStockAlert = 5.0
            )
        )
        productDao.insertProducts(initialProducts)

        // 5. Initial Batches (FIFO foundation)
        val initialBatches = listOf(
            BatchEntity(
                productId = "P-BERAS-EQT",
                batchNumber = "B-BR-01",
                qtyIn = 100.0,
                qtyLeft = 100.0,
                buyPrice = 12_000.0,
                expiryDate = "2027-01-01",
                ownership = OwnershipType.EQT,
                receiptRef = "OPENING-STOCK"
            ),
            BatchEntity(
                productId = "P-MINYAK-EQT",
                batchNumber = "B-MY-01",
                qtyIn = 24.0,
                qtyLeft = 24.0,
                buyPrice = 30_000.0,
                expiryDate = "2027-06-01",
                ownership = OwnershipType.EQT,
                receiptRef = "OPENING-STOCK"
            ),
            BatchEntity(
                productId = "P-MIE-EQT",
                batchNumber = "B-MI-01",
                qtyIn = 200.0,
                qtyLeft = 200.0,
                buyPrice = 3_000.0,
                expiryDate = "2026-12-31",
                ownership = OwnershipType.EQT,
                receiptRef = "OPENING-STOCK"
            ),
            BatchEntity(
                productId = "P-GULA-KNS",
                batchNumber = "B-GL-KNS-01",
                qtyIn = 50.0,
                qtyLeft = 50.0,
                buyPrice = 14_400.0,
                expiryDate = "2027-03-01",
                ownership = OwnershipType.KNS,
                receiptRef = "OPENING-KNS"
            ),
            BatchEntity(
                productId = "P-TELUR-EQT",
                batchNumber = "B-TL-01",
                qtyIn = 40.0,
                qtyLeft = 40.0,
                buyPrice = 26_000.0,
                expiryDate = "2026-10-30",
                ownership = OwnershipType.EQT,
                receiptRef = "OPENING-STOCK"
            ),
            BatchEntity(
                productId = "P-TERIGU-EQT",
                batchNumber = "B-TG-01",
                qtyIn = 60.0,
                qtyLeft = 60.0,
                buyPrice = 11_000.0,
                expiryDate = "2027-05-15",
                ownership = OwnershipType.EQT,
                receiptRef = "OPENING-STOCK"
            ),
            BatchEntity(
                productId = "P-SABUN-EQT",
                batchNumber = "B-SB-01",
                qtyIn = 35.0,
                qtyLeft = 35.0,
                buyPrice = 14_000.0,
                expiryDate = "2028-01-01",
                ownership = OwnershipType.EQT,
                receiptRef = "OPENING-STOCK"
            )
        )
        batchDao.insertBatches(initialBatches)

        // 6. Double Entry Opening Balance Journal (D=K)
        // Persediaan EQT: (100*12k + 24*30k + 200*3k + 40*26k + 60*11k + 35*14k) = 1.2M + 720k + 600k + 1.04M + 660k + 490k = 4.710.000
        // Kas: 5.000.000, Bank: 10.000.000, E-Wallet: 1.500.000
        // Total Debit: 21.210.000 -> Total Kredit: Modal Awal Pemilik 21.210.000
        val openingJournalId = "JRN-OPENING-001"
        val totalAssets = 21_210_000.0
        accountingDao.insertJournal(
            JournalEntryEntity(
                id = openingJournalId,
                txRef = "OPENING-BALANCE",
                txType = "OPENING",
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                description = "Saldo Awal Usaha Toko Maju Jaya",
                debitTotal = totalAssets,
                creditTotal = totalAssets
            )
        )
        accountingDao.insertJournalLines(
            listOf(
                JournalLineEntity(journalId = openingJournalId, accountName = "Kas Toko (Laci)", isDebit = true, amount = 5_000_000.0),
                JournalLineEntity(journalId = openingJournalId, accountName = "Bank BCA", isDebit = true, amount = 10_000_000.0),
                JournalLineEntity(journalId = openingJournalId, accountName = "E-Wallet QRIS", isDebit = true, amount = 1_500_000.0),
                JournalLineEntity(journalId = openingJournalId, accountName = "Persediaan Barang Dagang (EQT)", isDebit = true, amount = 4_710_000.0),
                JournalLineEntity(journalId = openingJournalId, accountName = "Modal Awal Pemilik (Equity)", isDebit = false, amount = totalAssets)
            )
        )

        // 7. Open initial shift for Kasir
        shiftDao.insertShift(
            ShiftRecordEntity(
                cashierName = "Kasir 1",
                startCash = 500_000.0, // Kas kecil kembalian awal
                status = "OPEN",
                notes = "Shift Pagi Toko Maju Jaya"
            )
        )

        auditLogDao.insertLog(
            AuditLogEntity(
                action = "INITIAL_SEED",
                actor = "Owner",
                roleMode = "OWNER",
                details = "Initialized canonical synthetic dataset Toko Maju Jaya"
            )
        )
    }

    /**
     * Executes atomic sale checkout with strict FIFO inventory deduction,
     * KNS/AFL commission accounting, debt updates if credit, and double-entry journal.
     */
    suspend fun executeSale(
        sale: SaleEntity,
        items: List<SaleItemEntity>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            var totalCogsEqt = 0.0
            var totalKnsSales = 0.0
            var totalKnsSupplierShare = 0.0
            var totalKnsCommission = 0.0
            var totalAflSales = 0.0
            var totalAflCommission = 0.0

            val processedItems = mutableListOf<SaleItemEntity>()

            // 1. Process each sale item according to its ownership
            for (item in items) {
                var itemCogs = 0.0
                var knsSuppShare = 0.0
                var knsOmahComm = 0.0
                var aflComm = 0.0

                when (item.ownership) {
                    OwnershipType.EQT -> {
                        // Strict FIFO deduction from batches
                        val batches = batchDao.getActiveBatchesFifo(item.productId)
                        var remainingQtyToDeduct = item.qty

                        for (batch in batches) {
                            if (remainingQtyToDeduct <= 0) break

                            val deductFromThisBatch = minOf(batch.qtyLeft, remainingQtyToDeduct)
                            val newQtyLeft = batch.qtyLeft - deductFromThisBatch
                            batchDao.updateBatch(batch.copy(qtyLeft = newQtyLeft))

                            itemCogs += deductFromThisBatch * batch.buyPrice
                            remainingQtyToDeduct -= deductFromThisBatch
                        }

                        // If remainingQtyToDeduct > 0 (negative stock edge case authorized by Q-05):
                        if (remainingQtyToDeduct > 0) {
                            // Find product buy price as provisional COGS
                            val product = productDao.getProductById(item.productId)
                            val provisionalPrice = product?.buyPrice ?: 0.0
                            itemCogs += remainingQtyToDeduct * provisionalPrice

                            // Record provisional negative batch
                            batchDao.insertBatch(
                                BatchEntity(
                                    productId = item.productId,
                                    batchNumber = "NEG-${System.currentTimeMillis() % 10000}",
                                    qtyIn = 0.0,
                                    qtyLeft = -remainingQtyToDeduct,
                                    buyPrice = provisionalPrice,
                                    ownership = OwnershipType.EQT,
                                    negativeFlag = true
                                )
                            )
                        }

                        // Deduct product cached stock
                        val prod = productDao.getProductById(item.productId)
                        if (prod != null) {
                            productDao.updateStockQty(item.productId, prod.stockQty - item.qty)
                        }

                        totalCogsEqt += itemCogs
                    }

                    OwnershipType.KNS -> {
                        // Consignment: deduct physical custody stock
                        val prod = productDao.getProductById(item.productId)
                        val rate = prod?.knsRate ?: 0.10
                        knsOmahComm = item.subtotal * rate
                        knsSuppShare = item.subtotal - knsOmahComm

                        totalKnsSales += item.subtotal
                        totalKnsSupplierShare += knsSuppShare
                        totalKnsCommission += knsOmahComm

                        if (prod != null) {
                            productDao.updateStockQty(item.productId, prod.stockQty - item.qty)
                        }

                        // Record into KnsSettlement queue
                        knsSettlementDao.insertSettlement(
                            KnsSettlementEntity(
                                id = "SET-${System.currentTimeMillis()}-${(100..999).random()}",
                                supplierId = "S1-KNS",
                                supplierName = "Juragan Gula Pak Haji",
                                periodLabel = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                                totalSalesX = item.subtotal,
                                supplierShareY = knsSuppShare,
                                omahCommissionZ = knsOmahComm,
                                status = "CALCULATED"
                            )
                        )
                    }

                    OwnershipType.AFL -> {
                        // Virtual Affiliate: no physical stock deduction
                        val prod = productDao.getProductById(item.productId)
                        val rate = prod?.aflRate ?: 0.15
                        aflComm = item.subtotal * rate

                        totalAflSales += item.subtotal
                        totalAflCommission += aflComm
                    }
                }

                processedItems.add(
                    item.copy(
                        saleId = sale.id,
                        costPriceSnapshot = itemCogs,
                        knsSupplierShare = knsSuppShare,
                        knsOmahCommission = knsOmahComm,
                        aflCommission = aflComm
                    )
                )
            }

            // 2. Save Sale & Sale Items
            saleDao.insertSale(sale)
            saleDao.insertSaleItems(processedItems)

            // 3. Payment Handling (Cash vs Credit / Tempo)
            val paymentTargetAccount = if (sale.paymentMethod == "BANK") "BANK_BCA" else "KAS_UTAMA"

            if (sale.paymentMethod == "CREDIT" || sale.paidAmount < sale.grandTotal) {
                // Sisa masuk Piutang (AR)
                val remainingDebt = sale.grandTotal - sale.paidAmount
                if (remainingDebt > 0) {
                    val customer = customerDao.getCustomerById(sale.customerId)
                    val tempoDays = customer?.tempoDays?.takeIf { it > 0 } ?: 7
                    val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, tempoDays) }
                    val dueDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

                    debtDao.insertDebt(
                        DebtEntity(
                            id = "AR-${sale.id}",
                            type = "AR",
                            refId = sale.id,
                            partnerId = sale.customerId,
                            partnerName = sale.customerName,
                            totalAmount = remainingDebt,
                            paidAmount = 0.0,
                            remainingAmount = remainingDebt,
                            dueDate = dueDateStr,
                            status = "CURRENT"
                        )
                    )
                    customerDao.adjustOutstandingDebt(sale.customerId, remainingDebt)
                }

                // If customer made a down payment / DP
                if (sale.paidAmount > 0) {
                    cashDao.adjustBalance(paymentTargetAccount, sale.paidAmount)
                    cashDao.insertMutation(
                        CashMutationEntity(
                            accountId = paymentTargetAccount,
                            type = "IN",
                            amount = sale.paidAmount,
                            refType = "SALE",
                            refId = sale.id,
                            description = "DP Penjualan ${sale.id} (${sale.customerName})"
                        )
                    )
                }
            } else {
                // Fully Paid Cash / Bank
                cashDao.adjustBalance(paymentTargetAccount, sale.paidAmount - sale.changeAmount)
                cashDao.insertMutation(
                    CashMutationEntity(
                        accountId = paymentTargetAccount,
                        type = "IN",
                        amount = sale.paidAmount - sale.changeAmount,
                        refType = "SALE",
                        refId = sale.id,
                        description = "Penjualan Tunai ${sale.id}"
                    )
                )
            }

            // 4. Double-Entry Balanced Accounting Journal (D = K)
            val journalId = "JRN-${sale.id}"
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(sale.timestamp))
            val journalLines = mutableListOf<JournalLineEntity>()

            // Debit side: Kas + Piutang
            val cashIn = if (sale.paymentMethod == "CREDIT") sale.paidAmount else (sale.paidAmount - sale.changeAmount)
            if (cashIn > 0) {
                journalLines.add(JournalLineEntity(journalId = journalId, accountName = if (sale.paymentMethod == "BANK") "Bank BCA" else "Kas Toko (Laci)", isDebit = true, amount = cashIn))
            }

            val arAmount = sale.grandTotal - cashIn
            if (arAmount > 0) {
                journalLines.add(JournalLineEntity(journalId = journalId, accountName = "Piutang Usaha (${sale.customerName})", isDebit = true, amount = arAmount))
            }

            // Credit side:
            // EQT revenue:
            val eqtTotalSales = items.filter { it.ownership == OwnershipType.EQT }.sumOf { it.subtotal }
            if (eqtTotalSales > 0) {
                journalLines.add(JournalLineEntity(journalId = journalId, accountName = "Pendapatan Penjualan EQT", isDebit = false, amount = eqtTotalSales))
            }

            // KNS: Z to Omah Commission, Y to Supplier Liability
            if (totalKnsCommission > 0) {
                journalLines.add(JournalLineEntity(journalId = journalId, accountName = "Pendapatan Komisi KNS", isDebit = false, amount = totalKnsCommission))
            }
            if (totalKnsSupplierShare > 0) {
                journalLines.add(JournalLineEntity(journalId = journalId, accountName = "Kewajiban Supplier KNS", isDebit = false, amount = totalKnsSupplierShare))
            }

            // AFL: Z to Commission, X-Z to Partner Liability
            if (totalAflCommission > 0) {
                journalLines.add(JournalLineEntity(journalId = journalId, accountName = "Pendapatan Komisi AFL", isDebit = false, amount = totalAflCommission))
            }
            val aflPartnerShare = totalAflSales - totalAflCommission
            if (aflPartnerShare > 0) {
                journalLines.add(JournalLineEntity(journalId = journalId, accountName = "Kewajiban Mitra Afiliasi", isDebit = false, amount = aflPartnerShare))
            }

            // Tax if enabled
            if (sale.taxAmount > 0) {
                journalLines.add(JournalLineEntity(journalId = journalId, accountName = "Utang PPN Keluaran", isDebit = false, amount = sale.taxAmount))
            }

            // COGS & Inventory Asset movement for EQT
            if (totalCogsEqt > 0) {
                journalLines.add(JournalLineEntity(journalId = journalId, accountName = "Harga Pokok Penjualan (HPP)", isDebit = true, amount = totalCogsEqt))
                journalLines.add(JournalLineEntity(journalId = journalId, accountName = "Persediaan Barang Dagang (EQT)", isDebit = false, amount = totalCogsEqt))
            }

            val debitSum = journalLines.filter { it.isDebit }.sumOf { it.amount }
            val creditSum = journalLines.filter { !it.isDebit }.sumOf { it.amount }

            accountingDao.insertJournal(
                JournalEntryEntity(
                    id = journalId,
                    txRef = sale.id,
                    txType = "SALE",
                    date = dateStr,
                    description = "Penjualan Struk #${sale.id}",
                    debitTotal = debitSum,
                    creditTotal = creditSum
                )
            )
            accountingDao.insertJournalLines(journalLines)

            // 5. Audit Log
            auditLogDao.insertLog(
                AuditLogEntity(
                    action = "SALE_COMMITTED",
                    actor = sale.cashierName,
                    roleMode = sale.roleMode,
                    details = "Invoice ${sale.id}, Total Rp ${sale.grandTotal}, Method ${sale.paymentMethod}"
                )
            )

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Executes Restock / Purchasing (Kulakan)
     */
    suspend fun executePurchase(
        purchase: PurchaseEntity,
        items: List<PurchaseItemEntity>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            purchaseDao.insertPurchase(purchase)
            purchaseDao.insertPurchaseItems(items)

            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(purchase.timestamp))

            for (item in items) {
                // Create new batch for FIFO
                val batchNumber = if (item.batchNumber.isNotBlank()) item.batchNumber else "B-${System.currentTimeMillis() % 100000}"
                batchDao.insertBatch(
                    BatchEntity(
                        productId = item.productId,
                        batchNumber = batchNumber,
                        qtyIn = item.qty,
                        qtyLeft = item.qty,
                        buyPrice = item.unitCost,
                        expiryDate = item.expiryDate,
                        ownership = item.ownership,
                        receiptRef = purchase.id
                    )
                )

                // Update product stock and buy price
                val prod = productDao.getProductById(item.productId)
                if (prod != null) {
                    productDao.updateStockQty(item.productId, prod.stockQty + item.qty)
                }
            }

            // Cash or AP
            val targetAccount = if (purchase.paymentMethod == "BANK") "BANK_BCA" else "KAS_UTAMA"
            if (purchase.status == "AP" || purchase.paidAmount < purchase.totalAmount) {
                val remainingPayable = purchase.totalAmount - purchase.paidAmount
                debtDao.insertDebt(
                    DebtEntity(
                        id = "AP-${purchase.id}",
                        type = "AP",
                        refId = purchase.id,
                        partnerId = purchase.supplierId,
                        partnerName = purchase.supplierName,
                        totalAmount = remainingPayable,
                        paidAmount = 0.0,
                        remainingAmount = remainingPayable,
                        dueDate = purchase.dueDate,
                        status = "CURRENT"
                    )
                )

                if (purchase.paidAmount > 0) {
                    cashDao.adjustBalance(targetAccount, -purchase.paidAmount)
                    cashDao.insertMutation(
                        CashMutationEntity(
                            accountId = targetAccount,
                            type = "OUT",
                            amount = purchase.paidAmount,
                            refType = "PURCHASE",
                            refId = purchase.id,
                            description = "Uang Muka Beli ${purchase.id} (${purchase.supplierName})"
                        )
                    )
                }
            } else {
                cashDao.adjustBalance(targetAccount, -purchase.totalAmount)
                cashDao.insertMutation(
                    CashMutationEntity(
                        accountId = targetAccount,
                        type = "OUT",
                        amount = purchase.totalAmount,
                        refType = "PURCHASE",
                        refId = purchase.id,
                        description = "Pembelian Tunai ${purchase.id} (${purchase.supplierName})"
                    )
                )
            }

            // Journal Entry
            val journalId = "JRN-${purchase.id}"
            val journalLines = mutableListOf<JournalLineEntity>()

            journalLines.add(
                JournalLineEntity(
                    journalId = journalId,
                    accountName = "Persediaan Barang Dagang (EQT)",
                    isDebit = true,
                    amount = purchase.totalAmount
                )
            )

            if (purchase.paidAmount > 0) {
                journalLines.add(
                    JournalLineEntity(
                        journalId = journalId,
                        accountName = if (purchase.paymentMethod == "BANK") "Bank BCA" else "Kas Toko (Laci)",
                        isDebit = false,
                        amount = purchase.paidAmount
                    )
                )
            }

            val apAmount = purchase.totalAmount - purchase.paidAmount
            if (apAmount > 0) {
                journalLines.add(
                    JournalLineEntity(
                        journalId = journalId,
                        accountName = "Utang Usaha (${purchase.supplierName})",
                        isDebit = false,
                        amount = apAmount
                    )
                )
            }

            val dSum = journalLines.filter { it.isDebit }.sumOf { it.amount }
            val cSum = journalLines.filter { !it.isDebit }.sumOf { it.amount }

            accountingDao.insertJournal(
                JournalEntryEntity(
                    id = journalId,
                    txRef = purchase.id,
                    txType = "PURCHASE",
                    date = dateStr,
                    description = "Pembelian Kulakan #${purchase.id}",
                    debitTotal = dSum,
                    creditTotal = cSum
                )
            )
            accountingDao.insertJournalLines(journalLines)

            auditLogDao.insertLog(
                AuditLogEntity(
                    action = "PURCHASE_COMMITTED",
                    actor = "Owner",
                    roleMode = "OWNER",
                    details = "Purchase ${purchase.id}, Supplier ${purchase.supplierName}, Total Rp ${purchase.totalAmount}"
                )
            )

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Customer pays debt (Piutang AR) or Omah pays Supplier (Hutang AP)
     */
    suspend fun payDebt(
        debtId: String,
        amount: Double,
        paymentMethod: String,
        accountUsed: String
    ): Boolean = withContext(Dispatchers.IO) {
        val debt = debtDao.getDebtById(debtId) ?: return@withContext false
        val newPaid = debt.paidAmount + amount
        val newRemaining = debt.totalAmount - newPaid
        val newStatus = if (newRemaining <= 0) "PAID" else "CURRENT"

        debtDao.updateDebt(debt.copy(paidAmount = newPaid, remainingAmount = maxOf(0.0, newRemaining), status = newStatus))
        debtDao.insertDebtPayment(
            DebtPaymentEntity(
                debtId = debtId,
                amount = amount,
                paymentMethod = paymentMethod,
                accountUsed = accountUsed,
                notes = "Pelunasan $debtId"
            )
        )

        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val journalId = "JRN-PAY-${System.currentTimeMillis()}"

        if (debt.type == "AR") {
            // Customer pays Piutang: Kas In, Piutang Down
            customerDao.adjustOutstandingDebt(debt.partnerId, -amount)
            cashDao.adjustBalance(accountUsed, amount)
            cashDao.insertMutation(
                CashMutationEntity(
                    accountId = accountUsed,
                    type = "IN",
                    amount = amount,
                    refType = "DEBT_PAYMENT",
                    refId = debtId,
                    description = "Terima Pembayaran Piutang ${debt.partnerName}"
                )
            )

            accountingDao.insertJournal(
                JournalEntryEntity(
                    id = journalId,
                    txRef = debtId,
                    txType = "DEBT",
                    date = dateStr,
                    description = "Pelunasan Piutang ${debt.partnerName}",
                    debitTotal = amount,
                    creditTotal = amount
                )
            )
            accountingDao.insertJournalLines(
                listOf(
                    JournalLineEntity(journalId = journalId, accountName = if (accountUsed == "BANK_BCA") "Bank BCA" else "Kas Toko (Laci)", isDebit = true, amount = amount),
                    JournalLineEntity(journalId = journalId, accountName = "Piutang Usaha (${debt.partnerName})", isDebit = false, amount = amount)
                )
            )
        } else {
            // Omah pays AP to Supplier: Kas Out, Hutang Down
            cashDao.adjustBalance(accountUsed, -amount)
            cashDao.insertMutation(
                CashMutationEntity(
                    accountId = accountUsed,
                    type = "OUT",
                    amount = amount,
                    refType = "DEBT_PAYMENT",
                    refId = debtId,
                    description = "Bayar Hutang Supplier ${debt.partnerName}"
                )
            )

            accountingDao.insertJournal(
                JournalEntryEntity(
                    id = journalId,
                    txRef = debtId,
                    txType = "DEBT",
                    date = dateStr,
                    description = "Bayar Hutang Supplier ${debt.partnerName}",
                    debitTotal = amount,
                    creditTotal = amount
                )
            )
            accountingDao.insertJournalLines(
                listOf(
                    JournalLineEntity(journalId = journalId, accountName = "Utang Usaha (${debt.partnerName})", isDebit = true, amount = amount),
                    JournalLineEntity(journalId = journalId, accountName = if (accountUsed == "BANK_BCA") "Bank BCA" else "Kas Toko (Laci)", isDebit = false, amount = amount)
                )
            )
        }

        auditLogDao.insertLog(
            AuditLogEntity(
                action = "DEBT_PAID",
                actor = "Kasir/Owner",
                roleMode = "OWNER",
                details = "Pembayaran $debtId senilai Rp $amount via $accountUsed"
            )
        )
        true
    }

    /**
     * Settles Consignment (KNS) payout to supplier
     */
    suspend fun settleKns(settlement: KnsSettlementEntity, accountUsed: String = "KAS_UTAMA"): Boolean = withContext(Dispatchers.IO) {
        knsSettlementDao.updateSettlement(settlement.copy(status = "SETTLED", settledAt = System.currentTimeMillis()))

        cashDao.adjustBalance(accountUsed, -settlement.supplierShareY)
        cashDao.insertMutation(
            CashMutationEntity(
                accountId = accountUsed,
                type = "OUT",
                amount = settlement.supplierShareY,
                refType = "SETTLEMENT",
                refId = settlement.id,
                description = "Settlement Bagi Hasil KNS ${settlement.supplierName}"
            )
        )

        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val journalId = "JRN-SET-${settlement.id}"
        accountingDao.insertJournal(
            JournalEntryEntity(
                id = journalId,
                txRef = settlement.id,
                txType = "SETTLEMENT",
                date = dateStr,
                description = "Pelunasan Bagi Hasil KNS ${settlement.supplierName}",
                debitTotal = settlement.supplierShareY,
                creditTotal = settlement.supplierShareY
            )
        )
        accountingDao.insertJournalLines(
            listOf(
                JournalLineEntity(journalId = journalId, accountName = "Kewajiban Supplier KNS", isDebit = true, amount = settlement.supplierShareY),
                JournalLineEntity(journalId = journalId, accountName = if (accountUsed == "BANK_BCA") "Bank BCA" else "Kas Toko (Laci)", isDebit = false, amount = settlement.supplierShareY)
            )
        )

        auditLogDao.insertLog(
            AuditLogEntity(
                action = "KNS_SETTLED",
                actor = "Owner",
                roleMode = "OWNER",
                details = "Settlement KNS ${settlement.id} ke ${settlement.supplierName} sebesar Rp ${settlement.supplierShareY}"
            )
        )
        true
    }

    /**
     * Records Operational Expense (8 Standard Categories)
     */
    suspend fun recordExpense(
        category: String,
        amount: Double,
        accountUsed: String,
        description: String
    ): Boolean = withContext(Dispatchers.IO) {
        expenseDao.insertExpense(
            ExpenseEntity(
                category = category,
                amount = amount,
                paymentAccount = accountUsed,
                isPaid = true,
                description = description
            )
        )

        cashDao.adjustBalance(accountUsed, -amount)
        cashDao.insertMutation(
            CashMutationEntity(
                accountId = accountUsed,
                type = "OUT",
                amount = amount,
                refType = "EXPENSE",
                description = "Biaya $category: $description"
            )
        )

        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val journalId = "JRN-EXP-${System.currentTimeMillis()}"
        accountingDao.insertJournal(
            JournalEntryEntity(
                id = journalId,
                txRef = "EXP-$category",
                txType = "EXPENSE",
                date = dateStr,
                description = "Beban Operasional: $category ($description)",
                debitTotal = amount,
                creditTotal = amount
            )
        )
        accountingDao.insertJournalLines(
            listOf(
                JournalLineEntity(journalId = journalId, accountName = "Beban Operasional - $category", isDebit = true, amount = amount),
                JournalLineEntity(journalId = journalId, accountName = if (accountUsed == "BANK_BCA") "Bank BCA" else "Kas Toko (Laci)", isDebit = false, amount = amount)
            )
        )

        auditLogDao.insertLog(
            AuditLogEntity(
                action = "EXPENSE_RECORDED",
                actor = "Owner",
                roleMode = "OWNER",
                details = "Catat Beban $category sebesar Rp $amount ($description)"
            )
        )
        true
    }

    /**
     * Executes Blind Stock Opname adjustment
     */
    suspend fun applyStockOpname(
        conductedBy: String,
        lines: List<OpnameLineEntity>
    ): Boolean = withContext(Dispatchers.IO) {
        val totalDiffCost = lines.sumOf { it.differenceAmount }
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val opnameId = opnameDao.insertOpname(
            OpnameRecordEntity(
                date = dateStr,
                conductedBy = conductedBy,
                status = "APPLIED",
                totalDifferenceCost = totalDiffCost
            )
        )

        val linesWithId = lines.map { it.copy(opnameId = opnameId) }
        opnameDao.insertOpnameLines(linesWithId)

        // Apply differences to product stock and batches
        for (line in linesWithId) {
            productDao.updateStockQty(line.productId, line.physicalQty)
            // Adjust batch
            val batches = batchDao.getActiveBatchesFifo(line.productId)
            if (batches.isNotEmpty()) {
                val latestBatch = batches.first()
                batchDao.updateBatch(latestBatch.copy(qtyLeft = maxOf(0.0, latestBatch.qtyLeft + line.differenceQty)))
            }
        }

        // Accounting entry for stock variance
        if (totalDiffCost != 0.0) {
            val journalId = "JRN-OPN-$opnameId"
            val absCost = kotlin.math.abs(totalDiffCost)

            if (totalDiffCost < 0) {
                // Shortage: loss
                accountingDao.insertJournal(
                    JournalEntryEntity(
                        id = journalId,
                        txRef = "OPN-$opnameId",
                        txType = "OPNAME",
                        date = dateStr,
                        description = "Penyesuaian Selisih Kurang Stock Opname #$opnameId",
                        debitTotal = absCost,
                        creditTotal = absCost
                    )
                )
                accountingDao.insertJournalLines(
                    listOf(
                        JournalLineEntity(journalId = journalId, accountName = "Beban Selisih Stok (Opname Loss)", isDebit = true, amount = absCost),
                        JournalLineEntity(journalId = journalId, accountName = "Persediaan Barang Dagang (EQT)", isDebit = false, amount = absCost)
                    )
                )
            } else {
                // Overage: gain
                accountingDao.insertJournal(
                    JournalEntryEntity(
                        id = journalId,
                        txRef = "OPN-$opnameId",
                        txType = "OPNAME",
                        date = dateStr,
                        description = "Penyesuaian Selisih Lebih Stock Opname #$opnameId",
                        debitTotal = absCost,
                        creditTotal = absCost
                    )
                )
                accountingDao.insertJournalLines(
                    listOf(
                        JournalLineEntity(journalId = journalId, accountName = "Persediaan Barang Dagang (EQT)", isDebit = true, amount = absCost),
                        JournalLineEntity(journalId = journalId, accountName = "Pendapatan Penyesuaian Stok (Opname Gain)", isDebit = false, amount = absCost)
                    )
                )
            }
        }

        auditLogDao.insertLog(
            AuditLogEntity(
                action = "OPNAME_APPLIED",
                actor = conductedBy,
                roleMode = "OWNER",
                details = "Stock Opname #$opnameId, Total Selisih Rp $totalDiffCost"
            )
        )
        true
    }

    /**
     * Opens or Closes a Shift
     */
    suspend fun openShift(cashierName: String, startCash: Double): Boolean = withContext(Dispatchers.IO) {
        val currentOpen = shiftDao.getCurrentOpenShift()
        if (currentOpen != null) return@withContext false

        shiftDao.insertShift(
            ShiftRecordEntity(
                cashierName = cashierName,
                startCash = startCash,
                status = "OPEN"
            )
        )
        auditLogDao.insertLog(
            AuditLogEntity(
                action = "SHIFT_OPENED",
                actor = cashierName,
                roleMode = "CASHIER",
                details = "Buka Shift dengan modal kas Rp $startCash"
            )
        )
        true
    }

    suspend fun closeShift(shiftId: Long, actualCash: Double, notes: String): Boolean = withContext(Dispatchers.IO) {
        val currentOpen = shiftDao.getCurrentOpenShift() ?: return@withContext false
        val kasAccount = cashDao.getAccountById("KAS_UTAMA")
        val expectedCash = kasAccount?.balance ?: 0.0
        val diff = actualCash - expectedCash

        shiftDao.updateShift(
            currentOpen.copy(
                closedAt = System.currentTimeMillis(),
                expectedCash = expectedCash,
                actualCash = actualCash,
                difference = diff,
                notes = notes,
                status = "CLOSED"
            )
        )

        auditLogDao.insertLog(
            AuditLogEntity(
                action = "SHIFT_CLOSED",
                actor = currentOpen.cashierName,
                roleMode = "CASHIER",
                details = "Tutup Shift: Fisik Rp $actualCash, Sistem Rp $expectedCash, Selisih Rp $diff"
            )
        )
        true
    }

    suspend fun addCustomer(customer: CustomerEntity): Boolean = withContext(Dispatchers.IO) {
        customerDao.insertOrUpdateCustomer(customer)
        auditLogDao.insertLog(
            AuditLogEntity(
                action = "CUSTOMER_ADDED",
                actor = "Kasir/Owner",
                roleMode = "OWNER",
                details = "Tambah Pelanggan Baru: ${customer.name} (${customer.id})"
            )
        )
        true
    }

    suspend fun addProduct(product: ProductEntity, initialStock: Double): Boolean = withContext(Dispatchers.IO) {
        productDao.insertOrUpdateProduct(product.copy(stockQty = initialStock))
        if (initialStock > 0 && product.ownership != OwnershipType.AFL) {
            batchDao.insertBatch(
                BatchEntity(
                    productId = product.id,
                    batchNumber = "B-INIT-${System.currentTimeMillis() % 10000}",
                    qtyIn = initialStock,
                    qtyLeft = initialStock,
                    buyPrice = product.buyPrice,
                    expiryDate = "2027-12-31",
                    ownership = product.ownership,
                    receiptRef = "NEW-PRODUCT-INIT"
                )
            )
        }
        auditLogDao.insertLog(
            AuditLogEntity(
                action = "PRODUCT_ADDED",
                actor = "Owner",
                roleMode = "OWNER",
                details = "Tambah Master Barang: ${product.name} (${product.id}), Harga Jual Rp ${product.sellPrice}"
            )
        )
        true
    }

    suspend fun updateProductPrice(productId: String, buyPrice: Double, sellPrice: Double): Boolean = withContext(Dispatchers.IO) {
        val prod = productDao.getProductById(productId) ?: return@withContext false
        productDao.insertOrUpdateProduct(prod.copy(buyPrice = buyPrice, sellPrice = sellPrice))
        auditLogDao.insertLog(
            AuditLogEntity(
                action = "PRICE_UPDATED",
                actor = "Owner",
                roleMode = "OWNER",
                details = "Ubah Harga ${prod.name}: Beli Rp $buyPrice, Jual Rp $sellPrice"
            )
        )
        true
    }

    suspend fun executeSalesReturn(saleId: String, reason: String): Boolean = withContext(Dispatchers.IO) {
        val sale = saleDao.getSaleById(saleId) ?: return@withContext false
        val items = saleDao.getSaleItems(saleId)
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Restock products and batches for EQT & KNS
        for (item in items) {
            if (item.ownership != OwnershipType.AFL) {
                val prod = productDao.getProductById(item.productId)
                if (prod != null) {
                    productDao.updateStockQty(item.productId, prod.stockQty + item.qty)
                }
                batchDao.insertBatch(
                    BatchEntity(
                        productId = item.productId,
                        batchNumber = "RET-${System.currentTimeMillis() % 10000}",
                        qtyIn = item.qty,
                        qtyLeft = item.qty,
                        buyPrice = item.costPriceSnapshot / maxOf(1.0, item.qty),
                        expiryDate = "2027-12-31",
                        ownership = item.ownership,
                        receiptRef = "RETURN-$saleId"
                    )
                )
            }
        }

        // Refund money or reverse AR
        if (sale.paymentMethod == "CREDIT") {
            customerDao.adjustOutstandingDebt(sale.customerId, -(sale.grandTotal - sale.paidAmount))
            debtDao.getDebtById("AR-$saleId")?.let { ar ->
                debtDao.updateDebt(ar.copy(status = "WRITTEN_OFF", remainingAmount = 0.0))
            }
            if (sale.paidAmount > 0) {
                cashDao.adjustBalance("KAS_UTAMA", -sale.paidAmount)
                cashDao.insertMutation(
                    CashMutationEntity(
                        accountId = "KAS_UTAMA",
                        type = "OUT",
                        amount = sale.paidAmount,
                        refType = "RETURN",
                        refId = saleId,
                        description = "Pengembalian Dana Retur Penjualan #$saleId"
                    )
                )
            }
        } else {
            val account = if (sale.paymentMethod == "BANK") "BANK_BCA" else "KAS_UTAMA"
            cashDao.adjustBalance(account, -sale.grandTotal)
            cashDao.insertMutation(
                CashMutationEntity(
                    accountId = account,
                    type = "OUT",
                    amount = sale.grandTotal,
                    refType = "RETURN",
                    refId = saleId,
                    description = "Pengembalian Dana Retur Penjualan #$saleId ($reason)"
                )
            )
        }

        // Double-entry reversal journal D=K
        val journalId = "JRN-RET-$saleId"
        val totalRefund = sale.grandTotal
        accountingDao.insertJournal(
            JournalEntryEntity(
                id = journalId,
                txRef = "RETURN-$saleId",
                txType = "RETURN",
                date = dateStr,
                description = "Retur Penjualan Struk #$saleId ($reason)",
                debitTotal = totalRefund,
                creditTotal = totalRefund
            )
        )
        accountingDao.insertJournalLines(
            listOf(
                JournalLineEntity(journalId = journalId, accountName = "Retur Penjualan & Pengurangan Pendapatan", isDebit = true, amount = totalRefund),
                JournalLineEntity(journalId = journalId, accountName = if (sale.paymentMethod == "BANK") "Bank BCA" else "Kas Toko (Laci)", isDebit = false, amount = totalRefund)
            )
        )

        auditLogDao.insertLog(
            AuditLogEntity(
                action = "SALE_RETURNED",
                actor = "Owner",
                roleMode = "OWNER",
                details = "Retur Struk #$saleId senilai Rp ${sale.grandTotal}. Alasan: $reason"
            )
        )
        true
    }
}

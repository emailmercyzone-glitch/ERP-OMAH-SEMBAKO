package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.OmahRepository
import com.example.domain.AuthManager
import com.example.domain.RoleMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class NavTab {
    DASHBOARD, JUAL, BELI, BAYAR, CETAK, STOK, KAS, SETTINGS
}

data class CartItem(
    val product: ProductEntity,
    val qty: Double,
    val unitPrice: Double = product.sellPrice,
    val discountPercent: Double = 0.0
) {
    val subtotal: Double
        get() = (qty * unitPrice) * (1.0 - (discountPercent / 100.0))
}

class OmahViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val repository = OmahRepository(db)
    val authManager = AuthManager(application)

    // Current Navigation Tab
    private val _currentTab = MutableStateFlow(NavTab.DASHBOARD)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    // Role Mode: CASHIER vs OWNER
    val roleMode: StateFlow<RoleMode> = authManager.currentMode
    val activeCashierName: StateFlow<String> = authManager.activeCashierName

    // Data Flows from Repository
    val products = repository.productsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val customers = repository.customersFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val suppliers = repository.suppliersFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val sales = repository.salesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val purchases = repository.purchasesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val receivables = repository.receivablesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val payables = repository.payablesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val settlements = repository.settlementsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val cashAccounts = repository.accountsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val cashMutations = repository.mutationsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val expenses = repository.expensesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val journals = repository.journalsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val shifts = repository.shiftsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val currentOpenShift = repository.currentOpenShiftFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val auditLogs = repository.auditLogsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // POS Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val selectedCustomer: StateFlow<CustomerEntity?> = _selectedCustomer.asStateFlow()

    private val _taxEnabled = MutableStateFlow(false)
    val taxEnabled: StateFlow<Boolean> = _taxEnabled.asStateFlow()

    private val _globalDiscount = MutableStateFlow(0.0)
    val globalDiscount: StateFlow<Double> = _globalDiscount.asStateFlow()

    // Owner PIN Dialog State
    private val _pinDialogVisible = MutableStateFlow(false)
    val pinDialogVisible: StateFlow<Boolean> = _pinDialogVisible.asStateFlow()

    private val _pinErrorMessage = MutableStateFlow<String?>(null)
    val pinErrorMessage: StateFlow<String?> = _pinErrorMessage.asStateFlow()

    private var onPinSuccessCallback: (() -> Unit)? = null

    // UI Feedback (Snackbar message)
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    // Last completed sale for receipt preview
    private val _lastCompletedSale = MutableStateFlow<Pair<SaleEntity, List<SaleItemEntity>>?>(null)
    val lastCompletedSale: StateFlow<Pair<SaleEntity, List<SaleItemEntity>>?> = _lastCompletedSale.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedCanonicalDataIfEmpty()
        }
    }

    fun selectTab(tab: NavTab) {
        _currentTab.value = tab
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun showUiMessage(message: String) {
        _uiMessage.value = message
    }

    fun setCustomer(customer: CustomerEntity?) {
        _selectedCustomer.value = customer
    }

    fun toggleTax(enabled: Boolean) {
        _taxEnabled.value = enabled
    }

    fun setGlobalDiscount(discount: Double) {
        _globalDiscount.value = discount
    }

    // --- POS Cart Operations ---
    fun addToCart(product: ProductEntity) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(qty = existing.qty + 1.0)
        } else {
            current.add(CartItem(product = product, qty = 1.0))
        }
        _cartItems.value = current
    }

    fun updateCartItemQty(productId: String, newQty: Double) {
        if (newQty <= 0) {
            removeFromCart(productId)
            return
        }
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            current[index] = current[index].copy(qty = newQty)
            _cartItems.value = current
        }
    }

    fun removeFromCart(productId: String) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _selectedCustomer.value = null
        _globalDiscount.value = 0.0
    }

    fun getCartSubtotal(): Double = _cartItems.value.sumOf { it.subtotal }

    fun getCartTax(): Double {
        val sub = getCartSubtotal() - _globalDiscount.value
        return if (_taxEnabled.value && sub > 0) sub * 0.11 else 0.0
    }

    fun getCartGrandTotal(): Double {
        val sub = getCartSubtotal() - _globalDiscount.value
        val tax = getCartTax()
        return maxOf(0.0, sub + tax)
    }

    /**
     * Completes POS Checkout
     */
    fun checkout(
        paymentMethod: String, // CASH, BANK, CREDIT
        paidAmount: Double,
        notes: String = ""
    ) {
        val items = _cartItems.value
        if (items.isEmpty()) return

        val grandTotal = getCartGrandTotal()
        val customer = _selectedCustomer.value

        // Validate credit policy
        if (paymentMethod == "CREDIT") {
            if (customer == null || customer.id == "C-TUNAI") {
                _uiMessage.value = "Penjualan Tempo/Kredit WAJIB memilih Pelanggan Terdaftar!"
                return
            }
            if (customer.isBlacklisted) {
                _uiMessage.value = "Pelanggan di-blacklist! Kredit ditolak."
                return
            }
            val remainingAfterDp = grandTotal - paidAmount
            if (customer.creditLimit > 0 && (customer.outstandingDebt + remainingAfterDp) > customer.creditLimit) {
                _uiMessage.value = "Peringatan: Melebihi Limit Kredit (Maks: Rp ${customer.creditLimit.toLong()})!"
            }
        }

        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val seq = (1000..9999).random()
            val saleId = "INV-$dateStr-$seq"

            val change = if (paymentMethod != "CREDIT" && paidAmount > grandTotal) paidAmount - grandTotal else 0.0

            val saleEntity = SaleEntity(
                id = saleId,
                customerId = customer?.id ?: "C-TUNAI",
                customerName = customer?.name ?: "Pelanggan Tunai",
                totalAmount = getCartSubtotal(),
                discountAmount = _globalDiscount.value,
                taxAmount = getCartTax(),
                grandTotal = grandTotal,
                paidAmount = paidAmount,
                changeAmount = change,
                paymentMethod = paymentMethod,
                paymentStatus = if (paymentMethod == "CREDIT" && paidAmount < grandTotal) {
                    if (paidAmount > 0) "PARTIAL" else "UNPAID"
                } else "PAID",
                cashierName = activeCashierName.value,
                roleMode = roleMode.value.name,
                notes = notes
            )

            val saleItemEntities = items.map { item ->
                SaleItemEntity(
                    saleId = saleId,
                    productId = item.product.id,
                    productName = item.product.name,
                    ownership = item.product.ownership,
                    qty = item.qty,
                    unitPrice = item.unitPrice,
                    subtotal = item.subtotal
                )
            }

            val success = repository.executeSale(saleEntity, saleItemEntities)
            if (success) {
                _lastCompletedSale.value = Pair(saleEntity, saleItemEntities)
                clearCart()
                _uiMessage.value = "Transaksi #$saleId berhasil disimpan!"
            } else {
                _uiMessage.value = "Gagal memproses transaksi!"
            }
        }
    }

    fun dismissReceipt() {
        _lastCompletedSale.value = null
    }

    // --- Owner Mode Security & PIN Verification ---
    fun requestOwnerAction(action: () -> Unit) {
        if (roleMode.value == RoleMode.OWNER) {
            action()
        } else {
            onPinSuccessCallback = action
            _pinErrorMessage.value = null
            _pinDialogVisible.value = true
        }
    }

    fun verifyOwnerPin(pin: String) {
        if (authManager.verifyAndUnlockOwner(pin)) {
            _pinDialogVisible.value = false
            _pinErrorMessage.value = null
            _uiMessage.value = "Mode Owner Terbuka!"
            onPinSuccessCallback?.invoke()
            onPinSuccessCallback = null
        } else {
            _pinErrorMessage.value = "PIN Salah! Masukkan 6 digit PIN Owner yang benar."
        }
    }

    fun dismissPinDialog() {
        _pinDialogVisible.value = false
        _pinErrorMessage.value = null
        onPinSuccessCallback = null
    }

    fun lockToCashier() {
        authManager.lockToCashier()
        _uiMessage.value = "Kembali ke Mode Kasir."
    }

    fun changePin(oldPin: String, newPin: String) {
        if (authManager.changeOwnerPin(oldPin, newPin)) {
            _uiMessage.value = "PIN Owner berhasil diperbarui!"
        } else {
            _uiMessage.value = "Gagal memperbarui PIN (PIN lama salah atau kurang dari 6 digit)."
        }
    }

    // --- Purchasing / Kulakan ---
    fun createPurchase(
        supplier: SupplierEntity,
        items: List<PurchaseItemEntity>,
        paymentMethod: String,
        paidAmount: Double,
        dueDate: String = ""
    ) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val seq = (1000..9999).random()
            val purchaseId = "PO-$dateStr-$seq"
            val total = items.sumOf { it.subtotal }

            val purchase = PurchaseEntity(
                id = purchaseId,
                invoiceNumber = "INV-SUP-$seq",
                supplierId = supplier.id,
                supplierName = supplier.name,
                totalAmount = total,
                paidAmount = paidAmount,
                paymentMethod = paymentMethod,
                status = if (paidAmount >= total) "PAID" else "AP",
                dueDate = dueDate
            )

            val success = repository.executePurchase(purchase, items)
            if (success) {
                _uiMessage.value = "Kulakan #$purchaseId dari ${supplier.name} berhasil disimpan!"
            } else {
                _uiMessage.value = "Gagal menyimpan pembelian!"
            }
        }
    }

    // --- Debt Management (Pay Piutang / Hutang) ---
    fun payDebt(debtId: String, amount: Double, method: String, accountUsed: String) {
        viewModelScope.launch {
            val success = repository.payDebt(debtId, amount, method, accountUsed)
            if (success) {
                _uiMessage.value = "Pembayaran hutang/piutang Rp $amount berhasil!"
            } else {
                _uiMessage.value = "Gagal memproses pembayaran hutang/piutang."
            }
        }
    }

    // --- Settle Consignment (KNS) ---
    fun settleKns(settlement: KnsSettlementEntity, accountUsed: String = "KAS_UTAMA") {
        viewModelScope.launch {
            val success = repository.settleKns(settlement, accountUsed)
            if (success) {
                _uiMessage.value = "Settlement bagi hasil Rp ${settlement.supplierShareY} ke ${settlement.supplierName} selesai!"
            } else {
                _uiMessage.value = "Gagal memproses settlement konsinyasi."
            }
        }
    }

    // --- Record Operational Expense ---
    fun recordExpense(category: String, amount: Double, accountUsed: String, description: String) {
        viewModelScope.launch {
            val success = repository.recordExpense(category, amount, accountUsed, description)
            if (success) {
                _uiMessage.value = "Biaya $category Rp $amount tercatat!"
            } else {
                _uiMessage.value = "Gagal mencatat pengeluaran."
            }
        }
    }

    // --- Stock Opname ---
    fun applyOpname(lines: List<OpnameLineEntity>) {
        viewModelScope.launch {
            val conductedBy = if (roleMode.value == RoleMode.OWNER) "Owner" else activeCashierName.value
            val success = repository.applyStockOpname(conductedBy, lines)
            if (success) {
                _uiMessage.value = "Stock Opname berhasil diterapkan dan jurnal penyesuaian dibukukan!"
            } else {
                _uiMessage.value = "Gagal menerapkan Stock Opname."
            }
        }
    }

    // --- Shift Management ---
    fun openShift(cashierName: String, startCash: Double) {
        viewModelScope.launch {
            val success = repository.openShift(cashierName, startCash)
            if (success) {
                authManager.setCashierName(cashierName)
                _uiMessage.value = "Shift dibuka untuk $cashierName dengan modal Rp $startCash"
            } else {
                _uiMessage.value = "Gagal membuka shift (sudah ada shift terbuka)."
            }
        }
    }

    fun closeShift(shiftId: Long, actualCash: Double, notes: String) {
        viewModelScope.launch {
            val success = repository.closeShift(shiftId, actualCash, notes)
            if (success) {
                _uiMessage.value = "Shift ditutup dan rekonsiliasi kas tercatat!"
            } else {
                _uiMessage.value = "Gagal menutup shift."
            }
        }
    }

    // --- Reset / Seed Canonical Data ---
    fun resetToDemoData() {
        viewModelScope.launch {
            repository.resetToCanonicalData()
            clearCart()
            _uiMessage.value = "Data Toko Maju Jaya berhasil dimuat ulang!"
        }
    }

    fun addCustomer(name: String, phone: String, address: String, creditLimit: Double, tempoDays: Int) {
        viewModelScope.launch {
            val id = "C-${name.uppercase().replace(" ", "-").take(10)}-${(100..999).random()}"
            val customer = CustomerEntity(
                id = id,
                name = name,
                phone = phone,
                address = address,
                creditLimit = creditLimit,
                outstandingDebt = 0.0,
                tempoDays = tempoDays
            )
            repository.addCustomer(customer)
            _selectedCustomer.value = customer
            _uiMessage.value = "Pelanggan $name berhasil ditambahkan!"
        }
    }

    fun addProduct(
        name: String,
        category: String,
        brand: String,
        barcode: String,
        uom: String,
        buyPrice: Double,
        sellPrice: Double,
        ownership: OwnershipType,
        initialStock: Double
    ) {
        viewModelScope.launch {
            val id = "P-${name.uppercase().replace(" ", "-").take(10)}-${(100..999).random()}"
            val product = ProductEntity(
                id = id,
                name = name,
                category = category,
                brand = brand,
                barcode = barcode,
                uomBase = uom,
                buyPrice = buyPrice,
                sellPrice = sellPrice,
                ownership = ownership,
                stockQty = initialStock
            )
            repository.addProduct(product, initialStock)
            _uiMessage.value = "Barang $name berhasil ditambahkan ke katalog!"
        }
    }

    fun updateProductPrice(productId: String, buyPrice: Double, sellPrice: Double) {
        viewModelScope.launch {
            repository.updateProductPrice(productId, buyPrice, sellPrice)
            _uiMessage.value = "Harga barang berhasil diperbarui!"
        }
    }

    fun processSalesReturn(saleId: String, reason: String) {
        viewModelScope.launch {
            val success = repository.executeSalesReturn(saleId, reason)
            if (success) {
                _uiMessage.value = "Retur penjualan #$saleId berhasil dibukukan!"
            } else {
                _uiMessage.value = "Gagal memproses retur penjualan."
            }
        }
    }
}

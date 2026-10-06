package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OwnershipType
import com.example.data.model.PurchaseItemEntity
import com.example.data.model.SupplierEntity
import com.example.ui.OmahViewModel
import com.example.ui.components.formatDateTime
import com.example.ui.components.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasingScreen(
    viewModel: OmahViewModel,
    modifier: Modifier = Modifier
) {
    val suppliers by viewModel.suppliers.collectAsState()
    val products by viewModel.products.collectAsState()
    val purchases by viewModel.purchases.collectAsState()

    var showNewPurchaseDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.requestOwnerAction {
                        showNewPurchaseDialog = true
                    }
                },
                icon = { Icon(Icons.Filled.AddShoppingCart, contentDescription = null) },
                text = { Text("Catat Kulakan Baru") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                "Pembelian & Restock Barang (Kulakan)",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Mencatat penerimaan barang dagang EQT & KNS ke dalam batch FIFO.",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Total Transaksi Beli", fontSize = 11.sp, color = Color.DarkGray)
                        Text("${purchases.size} Kali", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Total Nilai Kulakan", fontSize = 11.sp, color = Color.DarkGray)
                        Text(formatRupiah(purchases.sumOf { it.totalAmount }), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Riwayat Kulakan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            if (purchases.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada riwayat kulakan. Klik tombol di bawah untuk menambah.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(purchases) { purchase ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(purchase.id, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Supplier: ${purchase.supplierName}", fontSize = 12.sp)
                                    Text(formatDateTime(purchase.timestamp), fontSize = 11.sp, color = Color.Gray)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = if (purchase.status == "PAID") Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = if (purchase.status == "PAID") "Lunas (${purchase.paymentMethod})" else "Hutang AP (${purchase.dueDate})",
                                                color = if (purchase.status == "PAID") Color(0xFF1B5E20) else Color(0xFFC62828),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    formatRupiah(purchase.totalAmount),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewPurchaseDialog) {
        NewPurchaseDialog(
            suppliers = suppliers,
            products = products.filter { it.ownership != OwnershipType.AFL }, // AFL is virtual
            onDismiss = { showNewPurchaseDialog = false },
            onConfirmPurchase = { supplier, items, method, paid, due ->
                showNewPurchaseDialog = false
                viewModel.createPurchase(supplier, items, method, paid, due)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPurchaseDialog(
    suppliers: List<SupplierEntity>,
    products: List<com.example.data.model.ProductEntity>,
    onDismiss: () -> Unit,
    onConfirmPurchase: (SupplierEntity, List<PurchaseItemEntity>, String, Double, String) -> Unit
) {
    var selectedSupplier by remember { mutableStateOf(suppliers.firstOrNull()) }
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var qtyText by remember { mutableStateOf("10") }
    var costPriceText by remember { mutableStateOf(selectedProduct?.buyPrice?.toLong()?.toString() ?: "10000") }
    var expiryText by remember { mutableStateOf("2027-12-31") }
    var isTempo by remember { mutableStateOf(false) }

    val qty = qtyText.toDoubleOrNull() ?: 0.0
    val costPrice = costPriceText.toDoubleOrNull() ?: 0.0
    val totalAmount = qty * costPrice

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Form Kulakan Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Supplier Selector
                Text("Pilih Supplier:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                var expandedSupplier by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedSupplier,
                    onExpandedChange = { expandedSupplier = !expandedSupplier }
                ) {
                    OutlinedTextField(
                        value = selectedSupplier?.name ?: "Pilih Supplier",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSupplier) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSupplier,
                        onDismissRequest = { expandedSupplier = false }
                    ) {
                        suppliers.forEach { sup ->
                            DropdownMenuItem(
                                text = { Text(sup.name) },
                                onClick = {
                                    selectedSupplier = sup
                                    expandedSupplier = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Product Selector
                Text("Pilih Produk:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                var expandedProduct by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedProduct,
                    onExpandedChange = { expandedProduct = !expandedProduct }
                ) {
                    OutlinedTextField(
                        value = selectedProduct?.name ?: "Pilih Produk",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProduct) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedProduct,
                        onDismissRequest = { expandedProduct = false }
                    ) {
                        products.forEach { prod ->
                            DropdownMenuItem(
                                text = { Text("${prod.name} [${prod.ownership}]") },
                                onClick = {
                                    selectedProduct = prod
                                    costPriceText = prod.buyPrice.toLong().toString()
                                    expandedProduct = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) qtyText = it },
                        label = { Text("Jumlah (${selectedProduct?.uomBase ?: "pcs"})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = costPriceText,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) costPriceText = it },
                        label = { Text("Harga Beli (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = expiryText,
                    onValueChange = { expiryText = it },
                    label = { Text("Tgl Kadaluarsa (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Bayar Tempo (Hutang Supplier / AP)")
                    Switch(checked = isTempo, onCheckedChange = { isTempo = it })
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Total Kulakan:", fontSize = 11.sp)
                        Text(formatRupiah(totalAmount), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sup = selectedSupplier
                    val prod = selectedProduct
                    if (sup != null && prod != null && qty > 0 && costPrice > 0) {
                        val item = PurchaseItemEntity(
                            purchaseId = "",
                            productId = prod.id,
                            productName = prod.name,
                            ownership = prod.ownership,
                            qty = qty,
                            unitCost = costPrice,
                            subtotal = totalAmount,
                            expiryDate = expiryText,
                            batchNumber = "B-${System.currentTimeMillis() % 100000}"
                        )
                        val method = if (isTempo) "AP" else "CASH"
                        val paid = if (isTempo) 0.0 else totalAmount
                        val due = if (isTempo) "2026-11-15" else ""
                        onConfirmPurchase(sup, listOf(item), method, paid, due)
                    }
                },
                enabled = selectedSupplier != null && selectedProduct != null && qty > 0 && costPrice > 0
            ) {
                Text("Simpan Kulakan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

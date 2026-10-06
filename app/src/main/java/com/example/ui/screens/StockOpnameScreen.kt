package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import com.example.data.model.OpnameLineEntity
import com.example.data.model.OwnershipType
import com.example.data.model.ProductEntity
import com.example.ui.OmahViewModel
import com.example.ui.components.OwnershipBadge
import com.example.ui.components.formatRupiah

@Composable
fun StockOpnameScreen(
    viewModel: OmahViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()

    var filterOwnership by remember { mutableStateOf<OwnershipType?>(null) }
    var showOpnameDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Manajemen Stok & Opname",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Pemisahan tegas EQT (Milik), KNS (Titipan), dan AFL (Virtual).",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        viewModel.requestOwnerAction {
                            showAddProductDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        viewModel.requestOwnerAction {
                            showOpnameDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Opname", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Ownership Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterOwnership == null,
                onClick = { filterOwnership = null },
                label = { Text("Semua (${products.size})") }
            )
            FilterChip(
                selected = filterOwnership == OwnershipType.EQT,
                onClick = { filterOwnership = OwnershipType.EQT },
                label = { Text("EQT Milik") }
            )
            FilterChip(
                selected = filterOwnership == OwnershipType.KNS,
                onClick = { filterOwnership = OwnershipType.KNS },
                label = { Text("KNS Titipan") }
            )
            FilterChip(
                selected = filterOwnership == OwnershipType.AFL,
                onClick = { filterOwnership = OwnershipType.AFL },
                label = { Text("AFL Virtual") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        val displayProducts = products.filter {
            filterOwnership == null || it.ownership == filterOwnership
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(displayProducts) { prod ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.requestOwnerAction {
                                productToEdit = prod
                            }
                        }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    OwnershipBadge(prod.ownership)
                                }
                                Text(
                                    "Barcode: ${prod.barcode} • Kategori: ${prod.category} • Merek: ${prod.brand}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (prod.ownership == OwnershipType.AFL) "Virtual" else "${prod.stockQty.toInt()} ${prod.uomBase}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = if (prod.stockQty <= prod.minStockAlert && prod.ownership != OwnershipType.AFL) Color(0xFFC62828) else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Beli: ${formatRupiah(prod.buyPrice)}",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }

                        if (prod.ownership == OwnershipType.EQT) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Estimasi Nilai FIFO:", fontSize = 11.sp, color = Color.DarkGray)
                                Text(formatRupiah(prod.stockQty * prod.buyPrice), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else if (prod.ownership == OwnershipType.KNS) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Komisi Toko Omah (Z):", fontSize = 11.sp, color = Color.DarkGray)
                                Text("${(prod.knsRate * 100).toInt()}% per unit terjual", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog Blind Opname
    if (showOpnameDialog) {
        BlindOpnameDialog(
            products = products.filter { it.ownership != OwnershipType.AFL },
            onDismiss = { showOpnameDialog = false },
            onApplyOpname = { lines ->
                showOpnameDialog = false
                viewModel.applyOpname(lines)
            }
        )
    }

    // Dialog Tambah Produk Baru
    if (showAddProductDialog) {
        var nameText by remember { mutableStateOf("") }
        var categoryText by remember { mutableStateOf("Sembako") }
        var barcodeText by remember { mutableStateOf("BR-${(100..999).random()}") }
        var uomText by remember { mutableStateOf("pcs") }
        var buyPriceText by remember { mutableStateOf("10000") }
        var sellPriceText by remember { mutableStateOf("12000") }
        var stockText by remember { mutableStateOf("50") }
        var selectedOwnership by remember { mutableStateOf(OwnershipType.EQT) }

        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            title = { Text("Tambah Master Barang Baru", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text("Nama Barang") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = categoryText,
                            onValueChange = { categoryText = it },
                            label = { Text("Kategori") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = uomText,
                            onValueChange = { uomText = it },
                            label = { Text("Satuan (kg/pcs)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = barcodeText,
                        onValueChange = { barcodeText = it },
                        label = { Text("Barcode / Kode SKU") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = buyPriceText,
                            onValueChange = { if (it.all { ch -> ch.isDigit() }) buyPriceText = it },
                            label = { Text("Harga Beli (Rp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = sellPriceText,
                            onValueChange = { if (it.all { ch -> ch.isDigit() }) sellPriceText = it },
                            label = { Text("Harga Jual (Rp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) stockText = it },
                        label = { Text("Stok Awal Fisik") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Kepemilikan:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = selectedOwnership == OwnershipType.EQT,
                            onClick = { selectedOwnership = OwnershipType.EQT },
                            label = { Text("EQT (Milik)", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedOwnership == OwnershipType.KNS,
                            onClick = { selectedOwnership = OwnershipType.KNS },
                            label = { Text("KNS (Titipan)", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedOwnership == OwnershipType.AFL,
                            onClick = { selectedOwnership = OwnershipType.AFL },
                            label = { Text("AFL (Virtual)", fontSize = 11.sp) }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val buy = buyPriceText.toDoubleOrNull() ?: 0.0
                        val sell = sellPriceText.toDoubleOrNull() ?: 0.0
                        val stock = stockText.toDoubleOrNull() ?: 0.0
                        if (nameText.isNotBlank()) {
                            viewModel.addProduct(nameText, categoryText, "Lokal", barcodeText, uomText, buy, sell, selectedOwnership, stock)
                            showAddProductDialog = false
                        }
                    },
                    enabled = nameText.isNotBlank()
                ) {
                    Text("Simpan Barang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) { Text("Batal") }
            }
        )
    }

    // Dialog Ubah Harga Master
    productToEdit?.let { prod ->
        var buyText by remember { mutableStateOf(prod.buyPrice.toLong().toString()) }
        var sellText by remember { mutableStateOf(prod.sellPrice.toLong().toString()) }

        AlertDialog(
            onDismissRequest = { productToEdit = null },
            title = { Text("Ubah Harga ${prod.name}") },
            text = {
                Column {
                    Text("Kepemilikan: ${prod.ownership} • Stok Saat Ini: ${prod.stockQty.toInt()} ${prod.uomBase}")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = buyText,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) buyText = it },
                        label = { Text("Harga Pokok / Beli (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sellText,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) sellText = it },
                        label = { Text("Harga Jual (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val buy = buyText.toDoubleOrNull() ?: prod.buyPrice
                        val sell = sellText.toDoubleOrNull() ?: prod.sellPrice
                        viewModel.updateProductPrice(prod.id, buy, sell)
                        productToEdit = null
                    }
                ) {
                    Text("Perbarui Harga")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToEdit = null }) { Text("Batal") }
            }
        )
    }
}

@Composable
fun BlindOpnameDialog(
    products: List<ProductEntity>,
    onDismiss: () -> Unit,
    onApplyOpname: (List<OpnameLineEntity>) -> Unit
) {
    val counts = remember { mutableStateMapOf<String, String>() }
    val reasons = remember { mutableStateMapOf<String, String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Stock Opname (Hitung Fisik)", fontWeight = FontWeight.Bold)
                Text("Sistem Blind Count: Kasir mencatat fisik aktual, sistem otomatis membukukan selisih.", fontSize = 11.sp, color = Color.Gray)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(products) { prod ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Sistem: ${prod.stockQty.toInt()} ${prod.uomBase}", fontSize = 11.sp, color = Color.DarkGray)

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = counts[prod.id] ?: prod.stockQty.toInt().toString(),
                                    onValueChange = { counts[prod.id] = it },
                                    label = { Text("Fisik (${prod.uomBase})") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = reasons[prod.id] ?: "",
                                    onValueChange = { reasons[prod.id] = it },
                                    label = { Text("Keterangan") },
                                    placeholder = { Text("e.g. Rusak") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lines = products.map { prod ->
                        val physical = (counts[prod.id] ?: prod.stockQty.toInt().toString()).toDoubleOrNull() ?: prod.stockQty
                        val diffQty = physical - prod.stockQty
                        val diffAmount = diffQty * prod.buyPrice

                        OpnameLineEntity(
                            opnameId = 0,
                            productId = prod.id,
                            productName = prod.name,
                            systemQty = prod.stockQty,
                            physicalQty = physical,
                            differenceQty = diffQty,
                            unitCost = prod.buyPrice,
                            differenceAmount = diffAmount,
                            reason = reasons[prod.id] ?: "Penyesuaian Fisik"
                        )
                    }
                    onApplyOpname(lines)
                }
            ) {
                Text("Terapkan & Sesuaikan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

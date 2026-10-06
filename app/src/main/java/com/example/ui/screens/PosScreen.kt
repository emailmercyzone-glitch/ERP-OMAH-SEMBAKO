package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.model.CustomerEntity
import com.example.data.model.OwnershipType
import com.example.data.model.ProductEntity
import com.example.ui.OmahViewModel
import com.example.ui.components.OwnershipBadge
import com.example.ui.components.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: OmahViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val taxEnabled by viewModel.taxEnabled.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showCustomerPicker by remember { mutableStateOf(false) }
    var showNewCustomerDialog by remember { mutableStateOf(false) }

    val categories = listOf("Semua", "Sembako", "Titipan", "Jasa-AFL", "Kebersihan")

    val filteredProducts = products.filter { product ->
        val matchesCategory = selectedCategory == "Semua" || product.category.contains(selectedCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                product.name.contains(searchQuery, ignoreCase = true) ||
                product.barcode.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    val totalCartQty = cartItems.sumOf { it.qty }
    val grandTotal = viewModel.getCartGrandTotal()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "${totalCartQty.toInt()} Barang di Keranjang",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                formatRupiah(grandTotal),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = { showCheckoutDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Icon(Icons.Filled.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("BAYAR", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar & Customer Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari barang atau scan barcode...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Customer Selector Button
                OutlinedButton(
                    onClick = { showCustomerPicker = true },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = selectedCustomer?.name?.take(10) ?: "Tunai",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cart Summary Panel (expandable if items in cart)
            if (cartItems.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Keranjang Belanja",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("PPN 11%", fontSize = 11.sp, modifier = Modifier.padding(end = 4.dp))
                                Switch(
                                    checked = taxEnabled,
                                    onCheckedChange = { viewModel.toggleTax(it) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(onClick = { viewModel.clearCart() }) {
                                    Text("Kosongkan", color = Color.Red, fontSize = 11.sp)
                                }
                            }
                        }

                        cartItems.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.product.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${formatRupiah(item.unitPrice)} / ${item.product.uomBase}",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.updateCartItemQty(item.product.id, item.qty - 1) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Filled.RemoveCircleOutline, contentDescription = "Kurang")
                                    }

                                    Text(
                                        text = "${item.qty.toInt()}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    IconButton(
                                        onClick = { viewModel.updateCartItemQty(item.product.id, item.qty + 1) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Filled.AddCircleOutline, contentDescription = "Tambah")
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = formatRupiah(item.subtotal),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Products Catalog Grid/List
            Text(
                "Katalog Produk (${filteredProducts.size} item)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredProducts) { product ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.addToCart(product) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = product.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    OwnershipBadge(product.ownership)
                                }

                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Barcode: ${product.barcode} • Kategori: ${product.category}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = formatRupiah(product.sellPrice),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = " / ${product.uomBase}",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (product.ownership == OwnershipType.AFL) "Stok: Virtual" else "Stok: ${product.stockQty.toInt()} ${product.uomBase}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (product.stockQty <= product.minStockAlert && product.ownership != OwnershipType.AFL) Color(0xFFC62828) else Color(0xFF2E7D32)
                                    )
                                }
                            }

                            FilledIconButton(
                                onClick = { viewModel.addToCart(product) },
                                shape = CircleShape,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Tambah")
                            }
                        }
                    }
                }
            }
        }
    }

    // Customer Picker Dialog
    if (showCustomerPicker) {
        AlertDialog(
            onDismissRequest = { showCustomerPicker = false },
            title = { Text("Pilih Pelanggan") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(customers) { cust ->
                        ListItem(
                            headlineContent = { Text(cust.name, fontWeight = FontWeight.Bold) },
                            supportingContent = {
                                Text("Telp: ${cust.phone} • Limit Piutang: ${formatRupiah(cust.creditLimit)} • Sisa Hutang: ${formatRupiah(cust.outstandingDebt)}")
                            },
                            trailingContent = {
                                if (cust.isBlacklisted) {
                                    Surface(color = Color.Red, shape = RoundedCornerShape(4.dp)) {
                                        Text("Blacklist", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(4.dp))
                                    }
                                }
                            },
                            modifier = Modifier.clickable {
                                viewModel.setCustomer(cust)
                                showCustomerPicker = false
                            }
                        )
                        HorizontalDivider()
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCustomerPicker = false
                        showNewCustomerDialog = true
                    }
                ) {
                    Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pelanggan Baru")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomerPicker = false }) {
                    Text("Tutup")
                }
            }
        )
    }

    if (showNewCustomerDialog) {
        var nameText by remember { mutableStateOf("") }
        var phoneText by remember { mutableStateOf("") }
        var addressText by remember { mutableStateOf("") }
        var limitText by remember { mutableStateOf("1000000") }
        var tempoText by remember { mutableStateOf("7") }

        AlertDialog(
            onDismissRequest = { showNewCustomerDialog = false },
            title = { Text("Daftar Pelanggan Baru", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text("Nama Pelanggan / Warung") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = phoneText,
                        onValueChange = { phoneText = it },
                        label = { Text("No. HP / WhatsApp") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = addressText,
                        onValueChange = { addressText = it },
                        label = { Text("Alamat") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = limitText,
                            onValueChange = { if (it.all { ch -> ch.isDigit() }) limitText = it },
                            label = { Text("Limit Piutang (Rp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = tempoText,
                            onValueChange = { if (it.all { ch -> ch.isDigit() }) tempoText = it },
                            label = { Text("Tempo (Hari)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = limitText.toDoubleOrNull() ?: 0.0
                        val tempo = tempoText.toIntOrNull() ?: 7
                        if (nameText.isNotBlank()) {
                            viewModel.addCustomer(nameText, phoneText, addressText, limit, tempo)
                            showNewCustomerDialog = false
                        }
                    },
                    enabled = nameText.isNotBlank()
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewCustomerDialog = false }) { Text("Batal") }
            }
        )
    }

    // Checkout Payment Modal
    if (showCheckoutDialog) {
        CheckoutDialog(
            grandTotal = grandTotal,
            selectedCustomer = selectedCustomer,
            onDismiss = { showCheckoutDialog = false },
            onConfirmCheckout = { method, paidAmount, notes ->
                showCheckoutDialog = false
                viewModel.checkout(method, paidAmount, notes)
            }
        )
    }
}

@Composable
fun CheckoutDialog(
    grandTotal: Double,
    selectedCustomer: CustomerEntity?,
    onDismiss: () -> Unit,
    onConfirmCheckout: (paymentMethod: String, paidAmount: Double, notes: String) -> Unit
) {
    var paymentMethod by remember { mutableStateOf("CASH") } // CASH, BANK, CREDIT
    var paidText by remember { mutableStateOf(grandTotal.toLong().toString()) }
    var notesText by remember { mutableStateOf("") }

    val paidAmount = paidText.toDoubleOrNull() ?: 0.0
    val changeAmount = if (paymentMethod != "CREDIT" && paidAmount > grandTotal) paidAmount - grandTotal else 0.0
    val remainingDebt = if (paymentMethod == "CREDIT" || paidAmount < grandTotal) maxOf(0.0, grandTotal - paidAmount) else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Pembayaran POS", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Total Tagihan", fontSize = 12.sp, color = Color.DarkGray)
                        Text(
                            formatRupiah(grandTotal),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (selectedCustomer != null) {
                            Text("Pelanggan: ${selectedCustomer.name}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Method Selector
                Text("Metode Pembayaran:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        Triple("CASH", "Tunai", Icons.Filled.Money),
                        Triple("BANK", "Bank BCA", Icons.Filled.CreditCard),
                        Triple("CREDIT", "Tempo (Kredit)", Icons.Filled.Schedule)
                    ).forEach { (id, label, icon) ->
                        val isSelected = paymentMethod == id
                        OutlinedButton(
                            onClick = {
                                paymentMethod = id
                                if (id == "CREDIT") {
                                    paidText = "0" // Default DP 0 for credit
                                } else {
                                    paidText = grandTotal.toLong().toString()
                                }
                            },
                            colors = if (isSelected) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ButtonDefaults.outlinedButtonColors(),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick nominal buttons for cash
                if (paymentMethod == "CASH") {
                    Text("Nominal Cepat:", fontSize = 11.sp, color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(grandTotal, 20_000.0, 50_000.0, 100_000.0).forEach { amount ->
                            if (amount >= grandTotal || amount == grandTotal) {
                                SuggestionChip(
                                    onClick = { paidText = amount.toLong().toString() },
                                    label = { Text(if (amount == grandTotal) "Uang Pas" else "${amount.toLong() / 1000}k", fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = paidText,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) paidText = it },
                    label = { Text(if (paymentMethod == "CREDIT") "Uang Muka (DP) jika ada" else "Jumlah Dibayar") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (paymentMethod != "CREDIT" && changeAmount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Kembalian:", fontWeight = FontWeight.Bold)
                        Text(formatRupiah(changeAmount), fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                }

                if (paymentMethod == "CREDIT") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Masuk Piutang (Tempo):", fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                        Text(formatRupiah(remainingDebt), fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmCheckout(paymentMethod, paidAmount, notesText) },
                enabled = paidAmount >= 0
            ) {
                Text("Konfirmasi Bayar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}


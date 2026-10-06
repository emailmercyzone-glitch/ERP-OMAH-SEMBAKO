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
import com.example.data.model.CashAccountEntity
import com.example.ui.OmahViewModel
import com.example.ui.components.formatDateTime
import com.example.ui.components.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashShiftScreen(
    viewModel: OmahViewModel,
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.cashAccounts.collectAsState()
    val mutations by viewModel.cashMutations.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val currentShift by viewModel.currentOpenShift.collectAsState()

    var showOpenShiftDialog by remember { mutableStateOf(false) }
    var showCloseShiftDialog by remember { mutableStateOf(false) }
    var showExpenseDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Laci Kas & Sesi Shift Toko",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Pengelolaan kas fisik laci toko, mutasi bank, dan biaya operasional.",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        // --- SHIFT CARD ---
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (currentShift != null) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (currentShift != null) Icons.Filled.LockOpen else Icons.Filled.Lock,
                                contentDescription = null,
                                tint = if (currentShift != null) Color(0xFF1B5E20) else Color(0xFFC62828)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (currentShift != null) "Shift Sedang Berjalan" else "Shift Tertutup",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (currentShift != null) Color(0xFF1B5E20) else Color(0xFFC62828)
                            )
                        }

                        if (currentShift != null) {
                            Button(
                                onClick = { showCloseShiftDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Tutup Shift", fontSize = 12.sp)
                            }
                        } else {
                            Button(
                                onClick = { showOpenShiftDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Buka Shift", fontSize = 12.sp)
                            }
                        }
                    }

                    if (currentShift != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Kasir Bertugas: ${currentShift?.cashierName}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("Dibuka Sejak: ${formatDateTime(currentShift?.openedAt ?: 0)}", fontSize = 11.sp, color = Color.DarkGray)
                        Text("Modal Awal Laci: ${formatRupiah(currentShift?.startCash ?: 0.0)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- CASH & BANK BALANCES ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Saldo Akun Keuangan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Button(
                    onClick = { showExpenseDialog = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Catat Biaya Operasional", fontSize = 12.sp)
                }
            }
        }

        items(accounts) { acc ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (acc.id) {
                                "KAS_UTAMA" -> Icons.Filled.PointOfSale
                                "BANK_BCA" -> Icons.Filled.AccountBalance
                                else -> Icons.Filled.QrCodeScanner
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(acc.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Kode: ${acc.id} • ${acc.accountType}", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Text(
                        formatRupiah(acc.balance),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // --- RECENT MUTATIONS ---
        item {
            Text("Mutasi Kas & Bank Terakhir", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        if (mutations.isEmpty()) {
            item {
                Text("Belum ada mutasi tercatat.", color = Color.Gray, fontSize = 12.sp)
            }
        } else {
            items(mutations.take(15)) { mut ->
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(mut.description, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("${mut.accountId} • ${formatDateTime(mut.timestamp)}", fontSize = 10.sp, color = Color.Gray)
                        }
                        Text(
                            text = (if (mut.type == "IN") "+ " else "- ") + formatRupiah(mut.amount),
                            color = if (mut.type == "IN") Color(0xFF1B5E20) else Color(0xFFC62828),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    // Open Shift Dialog
    if (showOpenShiftDialog) {
        var cashierText by remember { mutableStateOf("Kasir 1") }
        var cashText by remember { mutableStateOf("500000") }

        AlertDialog(
            onDismissRequest = { showOpenShiftDialog = false },
            title = { Text("Buka Shift Kasir Baru") },
            text = {
                Column {
                    OutlinedTextField(
                        value = cashierText,
                        onValueChange = { cashierText = it },
                        label = { Text("Nama Kasir") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cashText,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) cashText = it },
                        label = { Text("Modal Kas Kecil Awal Laci (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cash = cashText.toDoubleOrNull() ?: 0.0
                        viewModel.openShift(cashierText, cash)
                        showOpenShiftDialog = false
                    }
                ) {
                    Text("Buka Shift")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOpenShiftDialog = false }) { Text("Batal") }
            }
        )
    }

    // Close Shift Dialog
    if (showCloseShiftDialog) {
        val shift = currentShift
        val kasAccount = accounts.firstOrNull { it.id == "KAS_UTAMA" }
        val expected = kasAccount?.balance ?: 0.0
        var actualCashText by remember { mutableStateOf(expected.toLong().toString()) }
        var notesText by remember { mutableStateOf("") }

        val actual = actualCashText.toDoubleOrNull() ?: 0.0
        val diff = actual - expected

        AlertDialog(
            onDismissRequest = { showCloseShiftDialog = false },
            title = { Text("Tutup Shift & Rekonsiliasi Laci") },
            text = {
                Column {
                    Text("Kasir: ${shift?.cashierName}")
                    Text("Estimasi Kas Menurut Sistem: ${formatRupiah(expected)}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = actualCashText,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) actualCashText = it },
                        label = { Text("Uang Fisik Dihitung Nyata (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Selisih Kas:", fontWeight = FontWeight.Bold)
                        Text(
                            text = (if (diff >= 0) "+ " else "") + formatRupiah(diff),
                            fontWeight = FontWeight.Bold,
                            color = if (diff == 0.0) Color(0xFF1B5E20) else if (diff > 0) Color(0xFF1565C0) else Color(0xFFC62828)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("Catatan Serah Terima (Handover)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sId = shift?.id ?: 0L
                        viewModel.closeShift(sId, actual, notesText)
                        showCloseShiftDialog = false
                    }
                ) {
                    Text("Tutup & Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseShiftDialog = false }) { Text("Batal") }
            }
        )
    }

    // Expense Dialog
    if (showExpenseDialog) {
        val categories = listOf("Listrik", "Air", "Sewa", "Internet", "Transportasi", "ATK", "Perbaikan", "Lain-lain")
        var selectedCat by remember { mutableStateOf(categories.first()) }
        var amountText by remember { mutableStateOf("") }
        var descText by remember { mutableStateOf("") }
        var selectedAccount by remember { mutableStateOf("KAS_UTAMA") }

        AlertDialog(
            onDismissRequest = { showExpenseDialog = false },
            title = { Text("Catat Pengeluaran Operasional") },
            text = {
                Column {
                    Text("Kategori Beban (8 Standar):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCat,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            categories.forEach { cat ->
                                DropdownMenuItem(text = { Text(cat) }, onClick = { selectedCat = cat; expanded = false })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) amountText = it },
                        label = { Text("Nominal Pengeluaran (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = descText,
                        onValueChange = { descText = it },
                        label = { Text("Keterangan Biaya") },
                        placeholder = { Text("e.g. Beli lakban & nota") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Bayar Menggunakan:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { selectedAccount = "KAS_UTAMA" },
                            colors = if (selectedAccount == "KAS_UTAMA") ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ButtonDefaults.outlinedButtonColors(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Kas Laci")
                        }
                        OutlinedButton(
                            onClick = { selectedAccount = "BANK_BCA" },
                            colors = if (selectedAccount == "BANK_BCA") ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ButtonDefaults.outlinedButtonColors(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Bank BCA")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.recordExpense(selectedCat, amt, selectedAccount, descText)
                            showExpenseDialog = false
                        }
                    },
                    enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Text("Simpan Biaya")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExpenseDialog = false }) { Text("Batal") }
            }
        )
    }
}

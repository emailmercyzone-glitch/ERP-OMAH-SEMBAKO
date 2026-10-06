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
import com.example.data.model.DebtEntity
import com.example.data.model.KnsSettlementEntity
import com.example.ui.OmahViewModel
import com.example.ui.components.formatDateTime
import com.example.ui.components.formatRupiah

@Composable
fun PaymentScreen(
    viewModel: OmahViewModel,
    modifier: Modifier = Modifier
) {
    val receivables by viewModel.receivables.collectAsState()
    val payables by viewModel.payables.collectAsState()
    val settlements by viewModel.settlements.collectAsState()

    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Piutang AR, 1: Hutang AP, 2: Konsinyasi KNS
    var debtToPay by remember { mutableStateOf<DebtEntity?>(null) }
    var knsToSettle by remember { mutableStateOf<KnsSettlementEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Bayar & Manajemen Tagihan",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            "Pelunasan Piutang Pelanggan, Hutang Kulakan Supplier, dan Settlement Konsinyasi.",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subtabs
        TabRow(selectedTabIndex = selectedSubTab) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("Piutang (${receivables.count { it.status != "PAID" }})", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("Hutang AP (${payables.count { it.status != "PAID" }})", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSubTab == 2,
                onClick = { selectedSubTab = 2 },
                text = { Text("Bagi Hasil KNS", fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedSubTab) {
            0 -> {
                // Piutang Pelanggan (AR)
                val activeAr = receivables.filter { it.status != "PAID" }
                val totalAr = activeAr.sumOf { it.remainingAmount }

                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(10.dp),
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
                            Text("Total Piutang Belum Tertagih", fontSize = 11.sp, color = Color.DarkGray)
                            Text(formatRupiah(totalAr), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFFE65100))
                        }
                        Text("${activeAr.size} Pelanggan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (activeAr.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Semua piutang pelanggan lunas!", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(activeAr) { ar ->
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
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ar.partnerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Ref: ${ar.refId} • Jatuh Tempo: ${ar.dueDate}", fontSize = 11.sp, color = Color.Gray)
                                        Text("Total: ${formatRupiah(ar.totalAmount)} • Sisa: ${formatRupiah(ar.remainingAmount)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE65100))
                                    }

                                    Button(
                                        onClick = { debtToPay = ar },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Terima Bayar", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Hutang Supplier (AP)
                val activeAp = payables.filter { it.status != "PAID" }
                val totalAp = activeAp.sumOf { it.remainingAmount }

                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(10.dp),
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
                            Text("Total Hutang Kulakan ke Supplier", fontSize = 11.sp, color = Color.DarkGray)
                            Text(formatRupiah(totalAp), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFFC62828))
                        }
                        Text("${activeAp.size} Tagihan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (activeAp.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Tidak ada hutang supplier yang jatuh tempo.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(activeAp) { ap ->
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
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ap.partnerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Ref: ${ap.refId} • Jatuh Tempo: ${ap.dueDate}", fontSize = 11.sp, color = Color.Gray)
                                        Text("Sisa Hutang: ${formatRupiah(ap.remainingAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.requestOwnerAction {
                                                debtToPay = ap
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Bayar", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // KNS Settlement
                val activeSettlements = settlements.filter { it.status != "SETTLED" }
                val totalShare = activeSettlements.sumOf { it.supplierShareY }
                val totalComm = activeSettlements.sumOf { it.omahCommissionZ }

                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Hak Supplier Titipan (Y)", fontSize = 11.sp, color = Color.DarkGray)
                                Text(formatRupiah(totalShare), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF1B5E20))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Komisi Omah (Z)", fontSize = 11.sp, color = Color.DarkGray)
                                Text(formatRupiah(totalComm), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2E7D32))
                            }
                        }
                        Text(
                            "Formula locked: Penjualan X = Hak Supplier Y + Komisi Omah Z (10%)",
                            fontSize = 10.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (settlements.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Belum ada transaksi konsinyasi terjual.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(settlements) { item ->
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
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${item.supplierName} (${item.periodLabel})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Penjualan X: ${formatRupiah(item.totalSalesX)}", fontSize = 11.sp, color = Color.DarkGray)
                                        Text("Komisi Omah Z: ${formatRupiah(item.omahCommissionZ)}", fontSize = 11.sp, color = Color(0xFF2E7D32))
                                        Text("Wajib Setor Y: ${formatRupiah(item.supplierShareY)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                                    }

                                    if (item.status == "SETTLED") {
                                        Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp)) {
                                            Text("Sudah Disetor", color = Color(0xFF1B5E20), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(6.dp))
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                viewModel.requestOwnerAction {
                                                    knsToSettle = item
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("Setor Y", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Debt Payment Dialog (AR or AP)
    debtToPay?.let { debt ->
        PayDebtDialog(
            debt = debt,
            onDismiss = { debtToPay = null },
            onConfirmPay = { amount, method, account ->
                debtToPay = null
                viewModel.payDebt(debt.id, amount, method, account)
            }
        )
    }

    // KNS Settlement Dialog
    knsToSettle?.let { kns ->
        AlertDialog(
            onDismissRequest = { knsToSettle = null },
            title = { Text("Settlement Bagi Hasil Konsinyasi") },
            text = {
                Column {
                    Text("Supplier: ${kns.supplierName}")
                    Text("Periode: ${kns.periodLabel}")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Total Penjualan Konsinyasi X: ${formatRupiah(kns.totalSalesX)}")
                    Text("Pendapatan Komisi Omah Z (10%): ${formatRupiah(kns.omahCommissionZ)}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    Text("Wajib Setor ke Supplier Y: ${formatRupiah(kns.supplierShareY)}", color = Color(0xFF1B5E20), fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Saldo Kas Toko akan dipotong sebesar ${formatRupiah(kns.supplierShareY)} dan jurnal penutup liabilitas konsinyasi akan dibukukan secara otomatis.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val item = knsToSettle
                        knsToSettle = null
                        if (item != null) viewModel.settleKns(item)
                    }
                ) {
                    Text("Setor Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { knsToSettle = null }) { Text("Batal") }
            }
        )
    }
}

@Composable
fun PayDebtDialog(
    debt: DebtEntity,
    onDismiss: () -> Unit,
    onConfirmPay: (Double, String, String) -> Unit
) {
    var amountText by remember { mutableStateOf(debt.remainingAmount.toLong().toString()) }
    var selectedAccount by remember { mutableStateOf("KAS_UTAMA") } // KAS_UTAMA or BANK_BCA

    val amount = amountText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (debt.type == "AR") "Terima Pelunasan Piutang" else "Pembayaran Hutang Supplier") },
        text = {
            Column {
                Text("Pihak: ${debt.partnerName}", fontWeight = FontWeight.Bold)
                Text("Referensi: ${debt.refId}")
                Text("Sisa Tagihan: ${formatRupiah(debt.remainingAmount)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) amountText = it },
                    label = { Text("Jumlah Pembayaran") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Pilih Akun Kas / Bank:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { selectedAccount = "KAS_UTAMA" },
                        colors = if (selectedAccount == "KAS_UTAMA") ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ButtonDefaults.outlinedButtonColors(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Kas Toko (Laci)")
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
                onClick = { onConfirmPay(amount, if (selectedAccount == "BANK_BCA") "BANK" else "CASH", selectedAccount) },
                enabled = amount > 0 && amount <= debt.remainingAmount
            ) {
                Text("Proses Pembayaran")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

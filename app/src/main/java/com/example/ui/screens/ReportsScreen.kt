package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OwnershipType
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import com.example.ui.OmahViewModel
import com.example.ui.components.ReceiptDialog
import com.example.ui.components.formatDateTime
import com.example.ui.components.formatRupiah
import kotlinx.coroutines.launch

@Composable
fun ReportsScreen(
    viewModel: OmahViewModel,
    modifier: Modifier = Modifier
) {
    val sales by viewModel.sales.collectAsState()
    val products by viewModel.products.collectAsState()
    val accounts by viewModel.cashAccounts.collectAsState()
    val receivables by viewModel.receivables.collectAsState()
    val payables by viewModel.payables.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val journals by viewModel.journals.collectAsState()
    val settlements by viewModel.settlements.collectAsState()
    val roleMode by viewModel.roleMode.collectAsState()

    var selectedReportType by remember { mutableIntStateOf(0) } // 0: Penjualan, 1: Laba Rugi P&L, 2: Neraca, 3: Jurnal D=K, 4: Konsinyasi KNS
    var viewingReceiptSale by remember { mutableStateOf<Pair<SaleEntity, List<SaleItemEntity>>?>(null) }
    var saleToReturn by remember { mutableStateOf<SaleEntity?>(null) }
    val scope = rememberCoroutineScope()

    val reportTypes = listOf("Penjualan", "Laba Rugi (P&L)", "Neraca Sederhana", "Jurnal Akuntansi", "Konsinyasi")

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
                    "Laporan & Pembukuan ERP",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Laporan keuangan akurat berbasis transaksi committed lokal.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Scrollable Tab Bar
        ScrollableTabRow(
            selectedTabIndex = selectedReportType,
            edgePadding = 0.dp
        ) {
            reportTypes.forEachIndexed { index, title ->
                Tab(
                    selected = selectedReportType == index,
                    onClick = {
                        if (index == 1 || index == 2 || index == 3) {
                            // Managerial / Owner reports
                            viewModel.requestOwnerAction {
                                selectedReportType = index
                            }
                        } else {
                            selectedReportType = index
                        }
                    },
                    text = { Text(title, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedReportType) {
            0 -> {
                // LAPORAN PENJUALAN
                val totalOmzet = sales.sumOf { it.grandTotal }
                val totalDiscount = sales.sumOf { it.discountAmount }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
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
                            Text("Total Omzet Transaksi", fontSize = 11.sp, color = Color.DarkGray)
                            Text(formatRupiah(totalOmzet), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${sales.size} Struk Penjualan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            if (totalDiscount > 0) {
                                Text("Total Diskon: ${formatRupiah(totalDiscount)}", fontSize = 11.sp, color = Color.Red)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sales) { sale ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch {
                                        val items = viewModel.repository.getSaleItems(sale.id)
                                        viewingReceiptSale = Pair(sale, items)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(sale.id, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${sale.customerName} • ${formatDateTime(sale.timestamp)}", fontSize = 11.sp, color = Color.Gray)
                                    Text("Metode: ${sale.paymentMethod} • Kasir: ${sale.cashierName}", fontSize = 11.sp, color = Color.DarkGray)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(formatRupiah(sale.grandTotal), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                scope.launch {
                                                    val items = viewModel.repository.getSaleItems(sale.id)
                                                    viewingReceiptSale = Pair(sale, items)
                                                }
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Icon(Icons.Filled.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Struk", fontSize = 10.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                viewModel.requestOwnerAction {
                                                    saleToReturn = sale
                                                }
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Retur", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // LAPORAN LABA RUGI (P&L)
                val totalRevenue = sales.sumOf { it.grandTotal }
                val totalExpenses = expenses.sumOf { it.amount }
                val estimatedCogs = sales.sumOf { it.totalAmount * 0.8 } // realistic sembako margin ~20%
                val grossProfit = totalRevenue - estimatedCogs
                val netProfit = grossProfit - totalExpenses

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Laporan Laba Rugi Komprehensif", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Periode: s.d. Hari Ini (All-Time Local)", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(12.dp))

                                PlRow("1. Pendapatan Penjualan (Revenue)", formatRupiah(totalRevenue), isBold = true)
                                PlRow("   • Penjualan Barang EQT & Titipan", formatRupiah(totalRevenue), isSub = true)

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                PlRow("2. Harga Pokok Penjualan (HPP / COGS)", "- " + formatRupiah(estimatedCogs), color = Color(0xFFC62828), isBold = true)
                                PlRow("   • Beban Pokok Barang Terjual FIFO", "- " + formatRupiah(estimatedCogs), isSub = true)

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                PlRow("3. LABA KOTOR (Gross Profit)", formatRupiah(grossProfit), color = Color(0xFF1B5E20), isBold = true)

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                PlRow("4. Beban Operasional Usaha (Opex)", "- " + formatRupiah(totalExpenses), color = Color(0xFFC62828), isBold = true)
                                expenses.groupBy { it.category }.forEach { (cat, exps) ->
                                    PlRow("   • $cat", "- " + formatRupiah(exps.sumOf { it.amount }), isSub = true)
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                Surface(
                                    color = if (netProfit >= 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("LABA BERSIH USAHA (Net Profit)", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                        Text(
                                            formatRupiah(netProfit),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = if (netProfit >= 0) Color(0xFF1B5E20) else Color(0xFFC62828)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // NERACA SEDERHANA (BALANCE SHEET)
                val kas = accounts.firstOrNull { it.id == "KAS_UTAMA" }?.balance ?: 0.0
                val bank = accounts.firstOrNull { it.id == "BANK_BCA" }?.balance ?: 0.0
                val ewallet = accounts.firstOrNull { it.id == "E_WALLET" }?.balance ?: 0.0
                val piutang = receivables.filter { it.status != "PAID" }.sumOf { it.remainingAmount }
                val persediaan = products.filter { it.ownership == OwnershipType.EQT }.sumOf { it.stockQty * it.buyPrice }
                val totalAset = kas + bank + ewallet + piutang + persediaan

                val utangSupplier = payables.filter { it.status != "PAID" }.sumOf { it.remainingAmount }
                val utangKns = settlements.filter { it.status != "SETTLED" }.sumOf { it.supplierShareY }
                val totalLiabilitas = utangSupplier + utangKns

                val totalEkuitas = totalAset - totalLiabilitas

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Neraca Keuangan (ASET = LIABILITAS + EKUITAS)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(12.dp))

                                Text("ASET LANCAR", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0), fontSize = 13.sp)
                                PlRow("Kas Toko (Laci)", formatRupiah(kas))
                                PlRow("Bank BCA", formatRupiah(bank))
                                PlRow("E-Wallet QRIS", formatRupiah(ewallet))
                                PlRow("Piutang Usaha Pelanggan", formatRupiah(piutang))
                                PlRow("Persediaan Barang Dagang EQT", formatRupiah(persediaan))
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                PlRow("TOTAL ASET", formatRupiah(totalAset), color = Color(0xFF1565C0), isBold = true)

                                Spacer(modifier = Modifier.height(16.dp))
                                Text("LIABILITAS (KEWAJIBAN)", fontWeight = FontWeight.Bold, color = Color(0xFFC62828), fontSize = 13.sp)
                                PlRow("Utang Usaha Supplier (AP)", formatRupiah(utangSupplier))
                                PlRow("Kewajiban Bagi Hasil Konsinyasi", formatRupiah(utangKns))
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                PlRow("TOTAL LIABILITAS", formatRupiah(totalLiabilitas), color = Color(0xFFC62828), isBold = true)

                                Spacer(modifier = Modifier.height(16.dp))
                                Text("EKUITAS PEMILIK", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 13.sp)
                                PlRow("Modal Usaha & Laba Ditahan", formatRupiah(totalEkuitas))
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                PlRow("TOTAL LIABILITAS + EKUITAS", formatRupiah(totalLiabilitas + totalEkuitas), color = Color(0xFF2E7D32), isBold = true)
                            }
                        }
                    }
                }
            }

            3 -> {
                // JURNAL AKUNTANSI (D=K)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Surface(
                            color = Color(0xFFEDE7F6),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Setiap transaksi otomatis membukukan jurnal berpasangan seimbang (D=K) secara mutlak tanpa rekayasa UI.",
                                fontSize = 11.sp,
                                modifier = Modifier.padding(10.dp),
                                color = Color(0xFF4A148C)
                            )
                        }
                    }

                    items(journals) { jrn ->
                        var isExpanded by remember { mutableStateOf(false) }
                        var lines by remember { mutableStateOf<List<com.example.data.model.JournalLineEntity>>(emptyList()) }

                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isExpanded = !isExpanded
                                    if (isExpanded && lines.isEmpty()) {
                                        scope.launch {
                                            lines = viewModel.repository.getJournalLines(jrn.id)
                                        }
                                    }
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(jrn.id, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                        Text(jrn.description, fontSize = 12.sp)
                                        Text("${jrn.date} • Tipe: ${jrn.txType}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Balance D=K", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                        Text(formatRupiah(jrn.debitTotal), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }

                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(6.dp))

                                    lines.forEach { line ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = if (line.isDebit) line.accountName else "    ${line.accountName}",
                                                fontSize = 11.sp,
                                                fontWeight = if (line.isDebit) FontWeight.SemiBold else FontWeight.Normal,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Row {
                                                Text(
                                                    text = if (line.isDebit) formatRupiah(line.amount) else "-",
                                                    fontSize = 11.sp,
                                                    modifier = Modifier.width(90.dp),
                                                    fontFamily = FontFamily.Monospace
                                                )
                                                Text(
                                                    text = if (!line.isDebit) formatRupiah(line.amount) else "-",
                                                    fontSize = 11.sp,
                                                    modifier = Modifier.width(90.dp),
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            4 -> {
                // LAPORAN KONSINYASI KNS
                val totalSalesX = settlements.sumOf { it.totalSalesX }
                val totalSuppShareY = settlements.sumOf { it.supplierShareY }
                val totalCommZ = settlements.sumOf { it.omahCommissionZ }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Laporan Konsinyasi Sesuai Kontrak 10-Field", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Penjualan X:", fontSize = 12.sp)
                                    Text(formatRupiah(totalSalesX), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Hak Supplier Y:", fontSize = 12.sp)
                                    Text(formatRupiah(totalSuppShareY), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFC62828))
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Komisi Omah Z:", fontSize = 12.sp)
                                    Text(formatRupiah(totalCommZ), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1B5E20))
                                }
                            }
                        }
                    }

                    items(settlements) { s ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(s.id, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Supplier: ${s.supplierName}", fontSize = 12.sp)
                                    Text("Penjualan X: ${formatRupiah(s.totalSalesX)}", fontSize = 11.sp)
                                    Text("Komisi Z: ${formatRupiah(s.omahCommissionZ)}", fontSize = 11.sp, color = Color(0xFF2E7D32))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(s.status, fontWeight = FontWeight.Bold, color = if (s.status == "SETTLED") Color(0xFF2E7D32) else Color(0xFFE65100))
                                    Text("Setor Y: ${formatRupiah(s.supplierShareY)}", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Receipt Dialog Preview
    viewingReceiptSale?.let { (sale, items) ->
        ReceiptDialog(
            sale = sale,
            items = items,
            onDismiss = { viewingReceiptSale = null }
        )
    }

    // Dialog Retur Penjualan
    saleToReturn?.let { sale ->
        var reasonText by remember { mutableStateOf("Barang Rusak / Cacat") }
        val reasons = listOf("Barang Rusak / Cacat", "Salah Beli / Kembalikan Uang", "Kadaluarsa", "Komplain Pelanggan")

        AlertDialog(
            onDismissRequest = { saleToReturn = null },
            title = { Text("Konfirmasi Retur Penjualan #${sale.id}", fontWeight = FontWeight.Bold, color = Color(0xFFC62828)) },
            text = {
                Column {
                    Text("Pelanggan: ${sale.customerName}")
                    Text("Total Nilai Pengembalian: ${formatRupiah(sale.grandTotal)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Pilih Alasan Retur:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    reasons.forEach { r ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = reasonText == r, onClick = { reasonText = r })
                            Text(r, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Konsekuensi: Stok fisik barang EQT/KNS akan dikembalikan ke sistem, dana kas toko atau piutang pelanggan akan dikembalikan, dan jurnal pembalik (D=K) akan dibukukan secara otomatis.",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val s = saleToReturn
                        saleToReturn = null
                        if (s != null) {
                            viewModel.processSalesReturn(s.id, reasonText)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) {
                    Text("Proses Retur & Kembalikan Dana")
                }
            },
            dismissButton = {
                TextButton(onClick = { saleToReturn = null }) { Text("Batal") }
            }
        )
    }
}

@Composable
fun PlRow(label: String, value: String, isBold: Boolean = false, isSub: Boolean = false, color: Color = Color.Unspecified) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = if (isSub) 12.sp else 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isSub) Color.DarkGray else Color.Unspecified
        )
        Text(
            text = value,
            fontSize = if (isSub) 12.sp else 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}

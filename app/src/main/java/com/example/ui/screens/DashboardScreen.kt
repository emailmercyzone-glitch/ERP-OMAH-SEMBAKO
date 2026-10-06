package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.OwnershipType
import com.example.ui.NavTab
import com.example.ui.OmahViewModel
import com.example.ui.components.OwnershipBadge
import com.example.ui.components.formatDateTime
import com.example.ui.components.formatRupiah

@Composable
fun DashboardScreen(
    viewModel: OmahViewModel,
    modifier: Modifier = Modifier
) {
    val sales by viewModel.sales.collectAsState()
    val products by viewModel.products.collectAsState()
    val receivables by viewModel.receivables.collectAsState()
    val payables by viewModel.payables.collectAsState()
    val accounts by viewModel.cashAccounts.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val currentShift by viewModel.currentOpenShift.collectAsState()

    // Financial Metrics Calculation
    val totalOmzetBruto = sales.sumOf { it.grandTotal }
    val totalCashAndBank = accounts.sumOf { it.balance }
    val totalPiutang = receivables.filter { it.status != "PAID" }.sumOf { it.remainingAmount }
    val totalHutang = payables.filter { it.status != "PAID" }.sumOf { it.remainingAmount }

    // Inventory Value (EQT only)
    val eqtProducts = products.filter { it.ownership == OwnershipType.EQT }
    val totalInventoryValue = eqtProducts.sumOf { it.stockQty * it.buyPrice }

    // Estimated Gross Profit & Net Profit
    val totalExpenses = expenses.sumOf { it.amount }
    val estimatedCogs = sales.sumOf { sale ->
        // rough estimate ~80% of sales or based on product margin
        sale.totalAmount * 0.8
    }
    val grossProfit = maxOf(0.0, totalOmzetBruto - estimatedCogs)
    val netProfit = grossProfit - totalExpenses

    // Low stock items
    val lowStockItems = products.filter { it.stockQty <= it.minStockAlert && it.ownership != OwnershipType.AFL }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Store Hero Card with Generated Illustration Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.omah_store_banner_1791270005309),
                            contentDescription = "Omah Sembako Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color(0xCC1B5E20)),
                                        startY = 50f
                                    )
                                )
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFF57F17),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Storefront,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Toko Maju Jaya (Omah Sembako)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    "Status: ${if (currentShift != null) "Shift Dibuka (${currentShift?.cashierName})" else "Shift Ditutup"}",
                                    color = Color(0xFFC8E6C9),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- HERO METRIC: OMZET & LABA ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ringkasan Finansial Toko",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "${sales.size} Penjualan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Total Omzet Penjualan", fontSize = 11.sp, color = Color.DarkGray)
                            Text(
                                formatRupiah(totalOmzetBruto),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Estimasi Laba Bersih", fontSize = 11.sp, color = Color.DarkGray)
                            Text(
                                formatRupiah(netProfit),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (netProfit >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }
                }
            }
        }

        // --- PRIMARY COCKPIT METRICS (Kas, Piutang, Hutang, Stok) ---
        item {
            Text(
                "Kondisi Kas & Tagihan",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Kas & Bank
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kas & Bank", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(formatRupiah(totalCashAndBank), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Piutang (AR)
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CallReceived, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Piutang (AR)", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(formatRupiah(totalPiutang), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Hutang Supplier (AP)
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CallMade, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hutang (AP)", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(formatRupiah(totalHutang), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                    }
                }

                // Nilai Persediaan EQT
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Inventory, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Nilai Stok EQT", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(formatRupiah(totalInventoryValue), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- QUICK ACTION BUTTONS ---
        item {
            Text(
                "Aksi Cepat Kasir & Toko",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionButton(
                    icon = Icons.Filled.ShoppingCart,
                    label = "Jual (POS)",
                    color = Color(0xFF1B5E20),
                    onClick = { viewModel.selectTab(NavTab.JUAL) }
                )
                QuickActionButton(
                    icon = Icons.Filled.LocalShipping,
                    label = "Kulakan",
                    color = Color(0xFF1565C0),
                    onClick = { viewModel.selectTab(NavTab.BELI) }
                )
                QuickActionButton(
                    icon = Icons.Filled.AccountBalanceWallet,
                    label = "Bayar Tagihan",
                    color = Color(0xFFF57F17),
                    onClick = { viewModel.selectTab(NavTab.BAYAR) }
                )
                QuickActionButton(
                    icon = Icons.Filled.FactCheck,
                    label = "Opname Stok",
                    color = Color(0xFF6A1B9A),
                    onClick = { viewModel.selectTab(NavTab.STOK) }
                )
            }
        }

        // --- LOW STOCK ALERT ---
        if (lowStockItems.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = Color(0xFFE65100))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Perhatian: ${lowStockItems.size} Barang Stok Menipis",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFFE65100)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        lowStockItems.take(3).forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("Sisa: ${item.stockQty} ${item.uomBase}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                            }
                        }
                    }
                }
            }
        }

        // --- RECENT SALES TRANSACTIONS ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Transaksi Penjualan Terakhir",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { viewModel.selectTab(NavTab.CETAK) }) {
                    Text("Semua Laporan")
                }
            }
        }

        if (sales.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada penjualan. Buka tab Jual untuk mulai transaksi.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            items(sales.take(5)) { sale ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(sale.id, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                "${sale.customerName} • ${formatDateTime(sale.timestamp)}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (sale.paymentMethod == "CREDIT") Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = sale.paymentMethod,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (sale.paymentMethod == "CREDIT") Color(0xFFC62828) else Color(0xFF2E7D32),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            formatRupiah(sale.grandTotal),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(76.dp)
    ) {
        FilledIconButton(
            onClick = onClick,
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = color.copy(alpha = 0.12f)),
            modifier = Modifier.size(52.dp)
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 13.sp
        )
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OwnershipType
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import com.example.domain.RoleMode
import com.example.ui.NavTab
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    format.maximumFractionDigits = 0
    return format.format(amount)
}

fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    return sdf.format(Date(timestamp))
}

@Composable
fun OwnershipBadge(ownership: OwnershipType) {
    val (label, bg, fg) = when (ownership) {
        OwnershipType.EQT -> Triple("EQT (Milik)", Color(0xFFE8F5E9), Color(0xFF1B5E20))
        OwnershipType.KNS -> Triple("KNS (Titipan)", Color(0xFFFFF3E0), Color(0xFFE65100))
        OwnershipType.AFL -> Triple("AFL (Virtual)", Color(0xFFE8EAF6), Color(0xFF1A237E))
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(2.dp)
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmahTopBar(
    roleMode: RoleMode,
    activeCashier: String,
    currentTab: NavTab,
    onRoleClick: () -> Unit,
    onOpenDrawerOrMenu: (NavTab) -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "OMAH SEMBAKO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = if (roleMode == RoleMode.OWNER) Color(0xFFFFB300) else Color(0xFF81C784),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (roleMode == RoleMode.OWNER) "OWNER" else "KASIR",
                            color = Color(0xFF1B5E20),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Operator: $activeCashier • Offline First Local ERP",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        },
        actions = {
            // Quick switcher to other modules
            IconButton(onClick = { onOpenDrawerOrMenu(NavTab.STOK) }) {
                Icon(Icons.Filled.Inventory2, contentDescription = "Stok", tint = Color.White)
            }
            IconButton(onClick = { onOpenDrawerOrMenu(NavTab.KAS) }) {
                Icon(Icons.Filled.PointOfSale, contentDescription = "Kas", tint = Color.White)
            }
            IconButton(onClick = { onOpenDrawerOrMenu(NavTab.SETTINGS) }) {
                Icon(Icons.Filled.Settings, contentDescription = "Pengaturan", tint = Color.White)
            }

            // Mode Toggle button
            FilledTonalButton(
                onClick = onRoleClick,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (roleMode == RoleMode.OWNER) Color(0xFFFFCC80) else Color.White.copy(alpha = 0.2f),
                    contentColor = if (roleMode == RoleMode.OWNER) Color(0xFFE65100) else Color.White
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = if (roleMode == RoleMode.OWNER) Icons.Filled.LockOpen else Icons.Filled.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (roleMode == RoleMode.OWNER) "Kunci" else "PIN Owner",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
fun OmahBottomNavBar(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        val items = listOf(
            Triple(NavTab.DASHBOARD, "Dashboard", Icons.Filled.Dashboard),
            Triple(NavTab.JUAL, "Jual (POS)", Icons.Filled.ShoppingCart),
            Triple(NavTab.BELI, "Beli (Kulak)", Icons.Filled.LocalShipping),
            Triple(NavTab.BAYAR, "Bayar & Piutang", Icons.Filled.AccountBalanceWallet),
            Triple(NavTab.CETAK, "Laporan", Icons.Filled.Assessment)
        )

        items.forEach { (tab, label, icon) ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = { Icon(icon, contentDescription = label) },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
fun OwnerPinDialog(
    isVisible: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmitPin: (String) -> Unit
) {
    if (!isVisible) return

    var pinText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Otorisasi Mode Owner")
            }
        },
        text = {
            Column {
                Text(
                    "Masukkan 6-digit PIN Owner untuk membuka fitur manajerial, laporan laba, atau perubahan master harga (Default PIN: 123456).",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = pinText,
                    onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) pinText = it },
                    label = { Text("6-Digit PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmitPin(pinText) },
                enabled = pinText.length >= 6
            ) {
                Text("Buka Kunci")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun ReceiptDialog(
    sale: SaleEntity,
    items: List<SaleItemEntity>,
    onDismiss: () -> Unit
) {
    var copyCount by remember { mutableIntStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Struk Pembayaran",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "TOKO OMAH SEMBAKO",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Pasar Tradisional & Grosir Sembako",
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Telp: 0812-3456-7890 • Nota Sideload Sesuai Standar",
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color.Gray, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("No: ${sale.id}", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text(if (copyCount > 1) "COPY #$copyCount" else "ORIGINAL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("Tgl: ${formatDateTime(sale.timestamp)}", fontSize = 11.sp, color = Color.DarkGray)
                    Text("Kasir: ${sale.cashierName} | Pelanggan: ${sale.customerName}", fontSize = 11.sp, color = Color.DarkGray)

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color.LightGray)
                    Spacer(modifier = Modifier.height(6.dp))

                    items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.productName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("${item.qty} x ${formatRupiah(item.unitPrice)} [${item.ownership}]", fontSize = 11.sp, color = Color.DarkGray)
                            }
                            Text(formatRupiah(item.subtotal), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = Color.Gray)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", fontSize = 11.sp)
                        Text(formatRupiah(sale.totalAmount), fontSize = 11.sp)
                    }
                    if (sale.discountAmount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Diskon", fontSize = 11.sp, color = Color.Red)
                            Text("- ${formatRupiah(sale.discountAmount)}", fontSize = 11.sp, color = Color.Red)
                        }
                    }
                    if (sale.taxAmount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("PPN (11%)", fontSize = 11.sp)
                            Text(formatRupiah(sale.taxAmount), fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TOTAL", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                        Text(formatRupiah(sale.grandTotal), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Metode Bayar", fontSize = 11.sp)
                        Text(sale.paymentMethod, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Bayar / Tunai", fontSize = 11.sp)
                        Text(formatRupiah(sale.paidAmount), fontSize = 11.sp)
                    }
                    if (sale.changeAmount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Kembalian", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(formatRupiah(sale.changeAmount), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }
                    if (sale.grandTotal > sale.paidAmount) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sisa Piutang (Tempo)", fontSize = 11.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                            Text(formatRupiah(sale.grandTotal - sale.paidAmount), fontSize = 11.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "— Terima Kasih Atas Kunjungan Anda —",
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { copyCount++ },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cetak / Salin Struk")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.RoleMode
import com.example.ui.OmahViewModel
import com.example.ui.components.formatDateTime

@Composable
fun SettingsScreen(
    viewModel: OmahViewModel,
    modifier: Modifier = Modifier
) {
    val roleMode by viewModel.roleMode.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    var showChangePinDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Pengaturan & Keamanan Toko",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Konfigurasi profil toko, keamanan PIN Owner, audit trail, dan data lokal.",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        // Store Profile Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Profil Bisnis", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Nama Toko: Toko Maju Jaya", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Jenis Usaha: Retail UMKM Sembako & Titipan Konsinyasi", fontSize = 12.sp, color = Color.DarkGray)
                    Text("Alamat: Jl. Raya Pasar Tradisional No. 10", fontSize = 12.sp, color = Color.DarkGray)
                    Text("Pajak: PPh Final UMKM PP 55/2022 (Tarif 0,5% Berperingkat)", fontSize = 12.sp, color = Color.DarkGray)
                }
            }
        }

        // Security & PIN Section
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Security, contentDescription = null, tint = Color(0xFFF57F17))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Keamanan & Akses Ganda", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Surface(
                            color = if (roleMode == RoleMode.OWNER) Color(0xFFFFB300) else Color(0xFF81C784),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (roleMode == RoleMode.OWNER) "MODE OWNER AKTIF" else "MODE KASIR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1B5E20),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Mode Kasir digunakan untuk transaksi POS harian. Mode Owner memerlukan 6-digit PIN untuk melihat laba, mengubah master harga, dan opname stok.",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                viewModel.requestOwnerAction {
                                    showChangePinDialog = true
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ubah PIN Owner", fontSize = 12.sp)
                        }

                        if (roleMode == RoleMode.OWNER) {
                            OutlinedButton(
                                onClick = { viewModel.lockToCashier() },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Kunci ke Kasir", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Database & Canonical Dataset Management
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Storage, contentDescription = null, tint = Color(0xFF1565C0))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Database Lokal & Dataset", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Aplikasi berjalan 100% offline dengan Room SQLite (ACID & WAL mode). Seluruh transaksi langsung sah di perangkat tanpa memerlukan server.",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.requestOwnerAction {
                                showResetConfirmDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset ke Data Awal Toko Maju Jaya", fontSize = 12.sp)
                    }
                }
            }
        }

        // Audit Trail Explorer
        item {
            Text("Jejak Audit Sistem (Audit Log Explorer)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Seluruh aksi manajerial dan transaksi tercatat secara permanen.", fontSize = 11.sp, color = Color.Gray)
        }

        if (auditLogs.isEmpty()) {
            item {
                Text("Belum ada log tercatat.", fontSize = 12.sp, color = Color.Gray)
            }
        } else {
            items(auditLogs.take(20)) { log ->
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                            Text(formatDateTime(log.timestamp), fontSize = 10.sp, color = Color.Gray)
                        }
                        Text("${log.actor} (${log.roleMode}): ${log.details}", fontSize = 11.sp, color = Color.DarkGray)
                    }
                }
            }
        }
    }

    // Change PIN Dialog
    if (showChangePinDialog) {
        var oldPin by remember { mutableStateOf("") }
        var newPin by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("Ubah PIN Owner") },
            text = {
                Column {
                    OutlinedTextField(
                        value = oldPin,
                        onValueChange = { if (it.length <= 6 && it.all { ch -> ch.isDigit() }) oldPin = it },
                        label = { Text("PIN Lama (Default: 123456)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 6 && it.all { ch -> ch.isDigit() }) newPin = it },
                        label = { Text("PIN Baru (Minimal 6 digit)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.changePin(oldPin, newPin)
                        showChangePinDialog = false
                    },
                    enabled = oldPin.isNotEmpty() && newPin.length >= 6
                ) {
                    Text("Perbarui PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) { Text("Batal") }
            }
        )
    }

    // Reset Confirm Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Konfirmasi Reset Data", color = Color(0xFFC62828)) },
            text = {
                Text(
                    "Semua transaksi saat ini akan dibersihkan dan dataset sintetis kanonikal 'Toko Maju Jaya' (Beras EQT, Minyak EQT, Gula KNS, Kopi AFL, saldo awal 15jt) akan dimuat ulang. Lanjutkan?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetToDemoData()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) {
                    Text("Ya, Muat Ulang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) { Text("Batal") }
            }
        )
    }
}

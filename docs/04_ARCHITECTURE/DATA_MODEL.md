# DATA_MODEL — Architecture (Tipe Data Moneter, Kuantitas, & Skema)

Status: FINAL / DECIDED (Dipatch per Audit Forensik 6 Oktober 2026).
Otoritas: Spesifikasi tipe data dan struktur entitas database lokal Room.

## 1. Aturan Tipe Data Uang & Kuantitas (Keputusan Audit)

### A. Mata Uang (Currency & Nilai Uang)
- **Tipe Data**: `Long` (Rupiah integer murni) di seluruh perhitungan transaksi final dan database.
- **Tanpa Pecahan Sen**: Mata uang Indonesia (IDR) dalam ritel UMKM tidak mengenal uang pecahan sen (misal: `15000` = Rp 15.000).
- **Pembulatan (Rounding)**:
  - Diskon persen, kalkulasi pajak PPN, dan komisi dihitung dengan presisi desimal internal lalu dibulatkan ke Rupiah bulat terdekat menggunakan `Math.round(...)`.
  - Akun Penampung Selisih: Setiap selisih pembulatan dibukukan ke akun tunggal `Beban/Pendapatan Selisih Pembulatan` agar jurnal Debit = Kredit selalu seimbang.

### B. Kuantitas Barang (Quantity)
- **Tipe Data**: Skala 3 desimal (`Double` / scaled integer milli-units `0.001`).
- **Tujuan**: Mendukung timbangan fisik komoditas sembako secara presisi:
  - Beras: `0.500 kg`, `1.250 kg`, `25.000 kg` (karung).
  - Telur: `0.750 kg`.
  - Minyak: `2.000 pcs / L`.
  - Mie instan: `1.000 pcs`, `40.000 pcs` (dus).

## 2. Entitas Database Inti (Room Entities)

```kotlin
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,           // P-BERAS-EQT
    val name: String,
    val category: String,
    val brand: String,
    val barcode: String,
    val uomBase: String,                  // kg, pcs, pack
    val buyPrice: Double,                 // Baseline cost (Rupiah)
    val sellPrice: Double,                // Selling price (Rupiah)
    val ownership: OwnershipType,         // EQT, KNS, AFL
    val knsRate: Double,                  // Komisi KNS (e.g. 0.10)
    val aflRate: Double,                  // Komisi AFL (e.g. 0.15)
    val stockQty: Double,                 // Presisi 3 desimal
    val minStockAlert: Double,
    val isActive: Boolean
)

@Entity(tableName = "batches")
data class BatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val productId: String,
    val batchNumber: String,
    val qtyIn: Double,                    // 3 desimal
    val qtyLeft: Double,                  // 3 desimal (FIFO deduction)
    val buyPrice: Double,                 // Cost batch (Rupiah)
    val expiryDate: String,
    val ownership: OwnershipType,
    val receiptRef: String,
    val timestamp: Long,
    val negativeFlag: Boolean             // Flag stok minus sementara (BIZ-Q05)
)

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey val id: String,           // INV-YYYYMMDD-SEQ
    val originDeviceId: String,           // Device pencatat (multi-device)
    val customerId: String,
    val customerName: String,
    val totalAmount: Double,              // Subtotal (Rupiah)
    val discountAmount: Double,
    val taxAmount: Double,
    val grandTotal: Double,
    val paidAmount: Double,
    val changeAmount: Double,
    val paymentMethod: String,            // CASH, BANK, CREDIT
    val paymentStatus: String,            // PAID, PARTIAL, UNPAID
    val cashierName: String,
    val roleMode: String,                 // CASHIER, OWNER
    val timestamp: Long,
    val syncStatus: String = "LOCAL"      // LOCAL, SYNCED
)
```

## 3. Identitas Global & Penomoran
- **Transaction ID**: `INV-{YYYYMMDD}-{DEVICE_CODE}-{SEQ}` (contoh: `INV-20261006-KASIR1-1024`).
- **Jurnal ID**: `JRN-{TX_REF}`.
- Penomoran bersifat unik lokal per perangkat dan deterministik global ketika digabungkan via Google Drive sync.

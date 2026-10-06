# DESIGN_SYSTEM — Omah Android UX Contract

Status: FINAL / DECIDED (Dipatch per Audit Forensik 6 Oktober 2026).
Karakter Visual: Modern Business Utility + Premium Retail Toko Sembako Indonesia.

## 1. Token Warna (Semantic Palette Tokens)

### Warna Utama (Branded Emerald & Earthy Gold)
- **Primary (Forest Green)**: `#1B5E20` — Digunakan untuk header utama, tombol aksi primer (BAYAR, SIMPAN), dan saldo positif.
- **Primary Variant (Emerald Green)**: `#2E7D32` — Digunakan untuk ikon aktif, status lunas, dan indikator laba.
- **Primary Container (Sage Container)**: `#D7E8D5` — Digunakan untuk kartu ringkasan omzet dan kartu aktif.
- **On-Primary Container**: `#0F3812` — Teks di atas primary container.
- **Secondary (Harvest Amber / Gold)**: `#F57F17` — Aksen visual, lencana Owner mode, tombol peringatan.
- **Secondary Container (Warm Amber)**: `#FFF3CD` — Kontainer peringatan dan kartu piutang.

### Warna Status & Peringatan
- **Error / Negatif (Brick Red)**: `#C62828` — Hutang jatuh tempo, stok menipis, kas keluar, selisih kurang.
- **Error Container**: `#FFEBEE` — Latar belakang status hutang / tempo.
- **Success (Green)**: `#2E7D32` — Pembayaran lunas, opname seimbang.
- **Success Container**: `#E8F5E9` — Latar belakang status lunas.
- **Info / Afiliasi (Navy Blue)**: `#1565C0` — Transaksi bank, akun bank BCA, produk virtual AFL.
- **Info Container**: `#E3F2FD` — Latar belakang kartu perbankan.

### Warna Netral & Surface
- **Surface Light**: `#F8F9FA` — Latar belakang layar umum (bersih, tidak silau).
- **Surface Card**: `#FFFFFF` — Permukaan kartu produk, struk, dan dialog.
- **Surface Variant**: `#EFF2F0` — Latar belakang input text field dan filter chip.
- **Outline / Divider**: `#CBD5CD` — Garis pemisah antar item nota dan list.

## 2. Tipografi & Hierarki Skala (Typography Scale)
- **Display / Hero KPI**: 20sp - 24sp, Font-Weight ExtraBold (Total Omzet, Grand Total Bayar).
- **Heading Layar**: 18sp, Font-Weight Bold (Judul Halaman).
- **Subheading / Kartu**: 14sp - 15sp, Font-Weight SemiBold (Nama Produk, Nama Pelanggan).
- **Body Regular**: 13sp, Font-Weight Normal (Deskripsi transaksi).
- **Caption & Helper**: 11sp - 12sp, Font-Weight Medium (Barcode, Kategori, Timestamp).
- **Monospace Code**: 11sp - 12sp, FontFamily Monospace (Nomor Invoice, Jurnal Akuntansi, Struk).

## 3. Format Angka & Mata Uang
- **Rupiah (IDR)**: Format bilangan bulat tanpa desimal sen (contoh: `Rp 15.000`, `Rp 2.500.000`).
- **Kuantitas (Qty)**: Ditampilkan dengan format desimal fleksibel (contoh: `10 pcs`, `1.5 kg`, `0.25 kg`).
- **Persentase**: Ditampilkan dengan simbol persen (contoh: `10%`, `11%`).

## 4. Spacing & Grid System
- Grid kelipatan 4dp & 8dp:
  - Spasi Mikro: `4.dp` (Jarak ikon ke teks label)
  - Spasi Standar: `8.dp` (Jarak antar elemen kartu)
  - Spasi Komponen: `12.dp` - `16.dp` (Padding horizontal layar dan isi kartu)
  - Spasi Seksi: `24.dp` (Jarak antar seksi halaman)
- Minimum Touch Target: 48.dp x 48.dp pada seluruh tombol kasir interaktif untuk akurasi tap satu tangan di layar sentuh.

## 5. Komponen Kunci
1. **Lencana Kepemilikan (Ownership Badge)**:
   - `EQT (Milik)`: Latar Hijau Muda `#E8F5E9`, Teks `#1B5E20`.
   - `KNS (Titipan)`: Latar Oranye Muda `#FFF3E0`, Teks `#E65100`.
   - `AFL (Virtual)`: Latar Biru Muda `#E8EAF6`, Teks `#1A237E`.
2. **Kartu Hero Dashboard**: Kartu kontainer hijau emerald dengan banner ilustrasi toko Omah Sembako.
3. **Struk Thermal**: Pratinjau nota berlatar kuning struk lembut `#FFFDE7` dengan tipografi monospace rapi dan counter `COPY`.

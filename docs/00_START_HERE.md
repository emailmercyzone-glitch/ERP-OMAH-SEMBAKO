# 00_START_HERE — Omah Android (Boot Document)

Status: FINAL / DECIDED (Dipatch per Audit Forensik 6 Oktober 2026).
Otoritas: Dokumen Induk Seluruh Spesifikasi Proyek Omah ERP.

## 1. Identitas & Tujuan Proyek
- **Nama Aplikasi**: Omah ERP (ERP Omah Sembako)
- **Kategori**: Retail ERP UMKM Sembako & Kelontong
- **Platform**: Android Native (Kotlin + Jetpack Compose + Room SQLite)
- **Model Distribusi & Operasional**: Multi-Device Operasional via 1 Akun Google Drive Bersama, Offline-First, Local-First.
- **Prinsip Utama**: Setiap perangkat (HP Kasir / Tablet Owner) memiliki database lokal Room yang mandiri dan beroperasi 100% offline. Sinkronisasi event transaksi dan backup antar-perangkat menggunakan folder penyimpanan terenkripsi pada 1 Akun Google Drive toko yang sama tanpa memerlukan server backend terpisah.

## 2. Keputusan Kunci Audit Forensik (Final Locks)
1. **Multi-Device Sync**: Beroperasi dengan **1 Akun Google Drive Bersama**. File event outbox dan snapshot backup diunggah/diunduh secara idempotent ke Drive folder toko (`OmahERP_Sync`).
2. **Tipe Uang & Kuantitas**:
   - **Mata Uang (IDR)**: Integer Rupiah murni (`Long`), tanpa pecahan sen (Rp 15.000).
   - **Kuantitas (Qty)**: Skala 3 desimal (`Double` / scaled integer 0.001) untuk mendukung timbangan fisik sembako (misal: `0.500 kg`, `1.250 kg`) dan konversi UOM.
   - **COGS, Komisi, & Pembulatan**: Pembulatan matematis ke Rupiah terdekat (`Math.round`), selisih pembulatan dibukukan ke satu akun operasional: `Beban/Pendapatan Selisih Pembulatan`.
3. **Scope "Personal Core" (Dual-Role)**:
   - Hanya 2 mode akses: **KASIR (Cashier)** dan **OWNER**.
   - Semua role lama (Supervisor, Manager, Admin, 5-Role matrix) resmi dihapus.
   - Kasir hanya dapat mengakses POS, scan barang, cetak struk, terima cicilan piutang, dan serah terima shift laci kas.
   - Owner memiliki akses penuh Kasir + seluruh wewenang manajerial (laba rugi, neraca, ubah harga, kulakan, bayar hutang, settlement konsinyasi, stock opname, tutup buku, dan ganti PIN). Tampilan Owner dilindungi 6-digit PIN.
4. **Enkripsi At-Rest**:
   - Menggunakan SQLCipher / Android Keystore master key untuk database Room lokal dan enkripsi passphrase AES-256 pada file backup `.omahbak`.
5. **Posting Akuntansi Tunggal**:
   - Satu tabel kanonikal di `POSTING_MODEL.md` (tabel kedua obsolete dihapus). Setiap transaksi wajib menghasilkan jurnal seimbang Debit = Kredit (D=K).

## 3. Struktur Dokumen
```
docs/
├── 00_START_HERE.md                  (Boot document & otoritas utama)
├── 01_OVERVIEW/                      (Konteks bisnis, ruang lingkup, glosarium)
├── 02_SPEC/                          (Spesifikasi bisnis per modul)
├── 03_UX/                            (Design system, alur navigasi, POS flow)
├── 04_ARCHITECTURE/                  (Arsitektur sistem, data model, sync model, posting)
├── 05_DECISIONS/                     (Register keputusan locked ADR)
└── 07_VERIFICATION/                  (Laporan audit forensik & matriks verifikasi)
```

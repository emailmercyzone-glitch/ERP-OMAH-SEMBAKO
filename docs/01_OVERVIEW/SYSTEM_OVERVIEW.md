# SYSTEM_OVERVIEW — Omah Android

Status: FINAL / DECIDED (Dipatch per Audit Forensik 6 Oktober 2026).

## 1. Apa itu Omah ERP?
Omah ERP adalah sistem Enterprise Resource Planning ritel untuk toko sembako dan kelontong (UMKM) berbasis Android Native. Sistem dirancang dengan prinsip **Local-First & Offline-First**: seluruh transaksi penjualan, pencatatan kulakan, kartu stok FIFO, mutasi kas, dan jurnal akuntansi dieksekusi secara instan dan atomik di database lokal perangkat tanpa ketergantungan koneksi internet.

## 2. Arsitektur Multi-Device via 1 Akun Google Drive Toko
- **Perangkat Terlibat**: HP Kasir (untuk checkout kasir cepat) dan Tablet/HP Owner (untuk pemantauan stok, kulakan, dan laporan laba).
- **Mekanisme Sinkronisasi**: Menggunakan **1 Akun Google Drive Toko** yang terpasang di perangkat.
  - Setiap perangkat menghasilkan event lokal bertanda unik deterministik (`UUID` + `deviceID` + `timestamp` + `sequence`).
  - Event outbox disinkronkan ke folder shared app data Google Drive (`OmahERP_Sync`).
  - Perangkat lawan membaca event baru dan mereplay-nya ke Room DB lokal secara idempotent.
  - Backup berkala dalam format file `.omahbak` terenkripsi otomatis diunggah ke Google Drive sebagai arsip pemulihan bencana (disaster recovery).

## 3. Dual-Role Access Control
- **Mode Kasir (Cashier)**: Mode kerja default. Kasir dapat melakukan penjualan POS, pencarian barang, scan barcode, cetak struk, serah terima shift kas, dan menerima pelunasan piutang pelanggan.
- **Mode Owner**: Diaktifkan melalui PIN 6-digit (Default: `123456`). Memiliki seluruh hak Kasir ditambah:
  - Melihat laporan laba kotor & laba bersih, neraca keuangan, dan jurnal akuntansi.
  - Mengubah harga jual master dan data master barang.
  - Melakukan kulakan pembelian barang ke supplier dan pelunasan hutang AP.
  - Menyetujui dan membukukan penyesuaian selisih stock opname.
  - Menyetor bagi hasil konsinyasi KNS ke supplier.
  - Mengatur konfigurasi toko, ganti PIN, dan ekspor/reset database.

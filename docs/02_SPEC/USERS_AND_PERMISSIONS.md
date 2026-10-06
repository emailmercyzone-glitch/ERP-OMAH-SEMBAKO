# USERS_AND_PERMISSIONS — Spec (Dual-Role Access Control)

Status: FINAL / DECIDED (Dipatch per Audit Forensik 6 Oktober 2026).
Otoritas: Pengaturan hak akses pengguna dan otorisasi toko.

## 1. Definisi Dual-Role (Hanya 2 Mode Akses)
Seluruh konsep lama 5-role (Supervisor, Manager, Admin, dll.) dinyatakan **OBSOLETE & SUPERSEDED**.
Sistem Omah ERP menerapkan kontrol akses ganda yang sederhana dan kokoh untuk UMKM:

### A. Mode KASIR (Cashier Mode)
- **Status**: Mode default saat aplikasi dibuka setelah kunci layar perangkat aktif.
- **Wewenang**:
  - Melakukan transaksi checkout POS penjualan (tunai dan tempo/kredit).
  - Scan barcode dan pencarian produk.
  - Cetak struk dan cetak salinan (COPY) nota.
  - Menerima pelunasan piutang dari pelanggan (AR).
  - Membuka dan menutup shift kasir dengan hitung kas laci fisik.
  - Melihat ringkasan penjualan shift kasir yang sedang bertugas.
- **Batasan Kasir**:
  - **TIDAK BISA** melihat laporan laba kotor/bersih, neraca toko, dan jurnal akuntansi.
  - **TIDAK BISA** mengubah harga jual atau data master barang dagang.
  - **TIDAK BISA** melakukan kulakan pembelian ke supplier atau membayar hutang supplier.
  - **TIDAK BISA** melakukan penyesuaian selisih stock opname.
  - **TIDAK BISA** melakukan reset database atau ganti konfigurasi sistem.

### B. Mode OWNER (Owner Mode)
- **Status**: Diaktifkan melalui otorisasi **6-Digit PIN Owner** (Default: `123456`).
- **Wewenang**:
  - Memiliki seluruh hak akses Mode Kasir.
  - Melihat Dashboard Finansial, Laporan Laba Rugi (P&L), Neraca Keuangan, dan Buku Jurnal Akuntansi.
  - Menambah, mengedit, dan menonaktifkan master barang, harga jual, dan harga beli.
  - Mencatat kulakan barang baru (EQT dan KNS) dan membayar hutang supplier (AP).
  - Menyetor bagi hasil konsinyasi KNS ke supplier.
  - Menyetujui dan membukukan selisih stock opname (Opname Loss / Gain).
  - Mengelola akun kas dan bank (Kas Laci, Bank BCA, E-Wallet).
  - Mengubah 6-digit PIN Owner.
  - Melakukan pencadangan (backup) dan pemulihan data (restore) ke Google Drive.

## 2. Kebijakan Keamanan PIN & Sesi
- **Panjang PIN**: Minimal 6 digit numerik.
- **Lockout Proteksi**: 5 kali kesalahan input PIN berturut-turut akan mengunci input sementara selama 5 menit dan mencatat audit log peringatan.
- **Auto-Lock Timeout**: Jika aplikasi berada dalam mode Owner dan tidak ada aktivitas (idle) selama 5 menit, aplikasi otomatis mengunci kembali ke Mode Kasir demi keamanan laci kas toko.

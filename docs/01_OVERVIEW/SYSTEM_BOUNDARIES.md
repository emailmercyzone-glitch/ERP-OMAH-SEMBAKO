# SYSTEM_BOUNDARIES — Omah Android

Status: FINAL / DECIDED (Dipatch per Audit Forensik 6 Oktober 2026).

## In Scope (Core Operasional Lokal & Offline)
1. **Master Data**: Produk (EQT, KNS, AFL), Kategori, Pelanggan, Supplier, Akun Kas & Bank, Pengaturan Pajak.
2. **Penjualan (POS)**: Scan barcode, pencarian cepat, diskon promo, pajak toggle (PPN 11%), pembayaran tunai (kembalian otomatis), transfer bank, dan kredit (tempo).
3. **Persediaan (Inventory)**:
   - **EQT (Milik)**: Fisik milik toko, valuasi aset, metode pengeluaran strict FIFO per batch, pembentukan HPP (COGS).
   - **KNS (Konsinyasi)**: Fisik titipan supplier, pembagian komisi toko $Z = X \times \text{rate}$ dan hak supplier $Y = X - Z$, pipeline settlement.
   - **AFL (Afiliasi Virtual)**: Produk virtual tanpa stok fisik di toko, pengakuan komisi instan saat transaksi commit.
4. **Hutang & Piutang (AR / AP)**:
   - Piutang Pelanggan (AR): Limit kredit, jatuh tempo, bucket aging (Current, 1-30, 31-60, >60 hari), cicilan & pelunasan.
   - Hutang Supplier (AP): Pencatatan faktur kulakan tempo, jadwal jatuh tempo, pembayaran via kas/bank.
5. **Kas, Bank, & Laci Kas**:
   - Multi-akun: Kas Laci Toko, Rekening Bank BCA, E-Wallet QRIS.
   - Manajemen Shift Kasir: Buka modal laci, tutup shift dengan hitung fisik nyata, pencatatan selisih kas (drawer variance).
   - Beban Operasional (Expenses): 8 kategori standar UMKM (Listrik, Air, Sewa, Internet, Transportasi, ATK, Perbaikan, Lain-lain).
6. **Akuntansi & Laporan Transaksional**:
   - Jurnal ganda otomatis seimbang (Debit = Kredit / D=K) tanpa input jurnal manual oleh pengguna.
   - Laporan Penjualan, Laba Rugi (P&L), Neraca Sederhana, Laporan Konsinyasi KNS, Mutasi Kas/Bank.
7. **Keamanan & Otorisasi**:
   - Dual-Role: Mode KASIR dan Mode OWNER (dilindungi 6-digit PIN).
8. **Multi-Device Sync & Backup**:
   - Berjalan pada beberapa perangkat (misal: HP Kasir dan Tablet Owner) menggunakan **1 Akun Google Drive Toko Bersama**.
   - Setiap transaksi commit lokal langsung sah di perangkat tersebut. Sinkronisasi event outbox bertukar via folder Drive secara idempotent.

## Out of Scope
- Server/cloud backend proprietary (tidak diperlukan, menggunakan Google Drive toko untuk sinkronisasi dan backup).
- Manufaktur pabrikasi, payroll/penggajian multi-tingkat HRD rumit.
- E-commerce online consumer checkout di luar toko fisik.
- AI otomatis menulis atau mengubah data database secara langsung (AI hanya advisory/analisis laporan via ekspor XLSX).

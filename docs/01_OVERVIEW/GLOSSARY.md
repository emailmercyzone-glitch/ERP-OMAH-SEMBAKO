# GLOSSARY — Omah Android

Status: FINAL / DECIDED (Dipatch per Audit Forensik 6 Oktober 2026).

| Istilah | Definisi Kanonikal |
|---|---|
| Omah ERP | Sistem ERP ritel sembako UMKM offline-first pada platform Android. |
| EQT | Barang dagang milik toko sendiri, stok fisik, dinilai dalam aset persediaan, pengeluaran FIFO strict, membentuk HPP saat terjual. |
| KNS | Barang konsinyasi titipan supplier di toko. Penjualan menghasilkan bagi hasil: Komisi Toko $Z = X \times \text{rate}$ dan Hak Supplier $Y = X - Z$. |
| AFL | Produk virtual / afiliasi mitra tanpa stok fisik toko. Menghasilkan pendapatan komisi instan. |
| Mode Kasir | Mode operasional default bebas PIN. Melakukan penjualan POS, cetak struk, dan buka/tutup laci kasir. |
| Mode Owner | Mode otoritas penuh yang dilindungi 6-digit PIN. Mengelola laba, neraca, master harga, kulakan, dan opname. |
| Double-Entry | Prinsip pembukuan akuntansi otomatis di mana setiap transaksi menghasilkan Total Debit = Total Kredit (D=K). |
| Blind Opname | Proses hitung fisik stok di mana petugas menghitung tanpa melihat angka stok sistem, untuk mencegah bias. |
| FIFO Strict | First-In First-Out per lot/batch penerimaan barang dagang untuk akurasi HPP (COGS). |
| Outbox Sync | Antrean event lokal yang disinkronkan secara idempotent melalui 1 Akun Google Drive bersama toko. |
| Selisih Pembulatan | Akun penampung operasional untuk menyeimbangkan pembulatan desimal ke Rupiah integer bulat. |

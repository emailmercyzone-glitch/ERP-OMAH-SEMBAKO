# POSTING_MODEL — Architecture (Satu Sumber Kebenaran Akuntansi)

Status: FINAL / DECIDED (Dipatch per Audit Forensik 6 Oktober 2026 — Tabel Ganda Dihapus).
Otoritas: Satu-satunya acuan mapping transaksi ke jurnal berpasangan seimbang (D=K).

Setiap transaksi bisnis otomatis menghasilkan jurnal seimbang Debit = Kredit tanpa input manual.

## Tabel Kanonikal Tunggal Posting Akuntansi

| Event Bisnis | Sisi Debit | Sisi Kredit | Penjelasan & Subledger |
|---|---|---|---|
| **Penjualan EQT (Tunai)** | D Kas Toko / Bank BCA | K Pendapatan Penjualan EQT | Kas masuk sebesar grand total |
| *(COGS EQT bersamaan)* | D Harga Pokok Penjualan (HPP) | K Persediaan Barang Dagang (EQT) | Pengurangan aset persediaan via batch FIFO |
| **Penjualan EQT (Kredit/Tempo)** | D Piutang Usaha Pelanggan | K Pendapatan Penjualan EQT | Piutang pelanggan bertambah sesuai tempo |
| *(COGS EQT kredit)* | D Harga Pokok Penjualan (HPP) | K Persediaan Barang Dagang (EQT) | Nilai HPP batch FIFO terpakai |
| **Penjualan KNS (Konsinyasi)** | D Kas Toko / Bank BCA | K Kewajiban Supplier KNS (Y)<br>K Pendapatan Komisi KNS (Z) | Net Method: $X = Y + Z$. Hak supplier $Y$, komisi toko $Z$. |
| **Penjualan AFL (Virtual)** | D Kas Toko / Bank BCA | K Kewajiban Mitra Afiliasi ($X-Z$)<br>K Pendapatan Komisi AFL (Z) | Tanpa mutasi stok fisik. Komisi $Z$ diakui saat commit. |
| **Pajak Penjualan PPN (11%)** | D Kas Toko / Piutang | K Utang PPN Keluaran | Dibukukan terpisah dari PPh bila toggle pajak ON |
| **Penerimaan Kulakan EQT (Tunai)** | D Persediaan Barang Dagang (EQT) | K Kas Toko / Bank BCA | Nilai batch pembelian tunai bertambah |
| **Penerimaan Kulakan EQT (Tempo/AP)** | D Persediaan Barang Dagang (EQT) | K Utang Usaha Supplier (AP) | Lahir kewajiban faktur pembelian tempo |
| **Pelunasan Piutang Pelanggan (AR)** | D Kas Toko / Bank BCA | K Piutang Usaha Pelanggan | Saldo piutang berkurang sesuai nominal bayar |
| **Pelunasan Hutang Supplier (AP)** | D Utang Usaha Supplier (AP) | K Kas Toko / Bank BCA | Saldo hutang berkurang sesuai nominal bayar |
| **Setor Bagi Hasil KNS ke Supplier** | D Kewajiban Supplier KNS | K Kas Toko / Bank BCA | Pelunasan hak supplier $Y$ setelah periode closing |
| **Beban Operasional (Expense)** | D Beban Operasional - [Kategori] | K Kas Toko / Bank BCA | 8 kategori beban (Listrik, Sewa, dll) |
| **Stock Opname Selisih Kurang (Loss)**| D Beban Selisih Stok (Opname Loss) | K Persediaan Barang Dagang (EQT) | Fisik < Sistem, pengurangan aset persediaan |
| **Stock Opname Selisih Lebih (Gain)** | D Persediaan Barang Dagang (EQT) | K Pendapatan Penyesuaian Stok (Gain)| Fisik > Sistem, penambahan aset persediaan |
| **Selisih Pembulatan (Rounding Diff)** | D Beban Selisih Pembulatan *(jika rugi)*<br>atau Kas Toko | K Kas Toko<br>atau K Pendapatan Selisih Pembulatan *(jika untung)* | Penampung pembulatan desimal ke Rupiah integer |
| **Saldo Awal Usaha (Opening Balance)**| D Kas Toko / Bank / Persediaan EQT | K Modal Awal Pemilik (Equity) | Keseimbangan awal neraca hari ke-1 |

## Aturan Integritas
1. Setiap transaksi wajib membukukan `debitTotal == creditTotal`. Jika terjadi ketidakseimbangan, transaksi dibatalkan sepenuhnya (atomic rollback).
2. Data jurnal bersifat *immutable* (tidak dapat diedit atau dihapus). Koreksi kesalahan dilakukan melalui transaksi balik (reversal) atau transaksi penyesuaian (adjustment).

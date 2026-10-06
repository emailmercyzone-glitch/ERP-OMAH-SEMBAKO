# SYNC_MODEL — Architecture (Multi-Device via 1 Akun Google Drive Toko)

Status: FINAL / DECIDED (Dipatch per Audit Forensik 6 Oktober 2026).
Otoritas: Mekanisme sinkronisasi multi-perangkat toko UMKM.

## 1. Arsitektur Transport & Infrastruktur
- Toko UMKM Omah Sembako menggunakan **1 Akun Google Drive Bersama** yang login pada seluruh perangkat toko (misal: HP Kasir dan Tablet/HP Owner).
- Tidak memerlukan server/cloud backend kustom yang mahal atau rumit.
- Google Drive digunakan sebagai media pertukaran event (*Shared Storage Transport*) dan repositori cadangan (*Backup Store*):
  - Folder Sinkronisasi: `Google Drive / OmahERP_Sync / events /`
  - Folder Backup: `Google Drive / OmahERP_Sync / backups /`

## 2. Event Format & Determinisme
Setiap kejadian bisnis committed di perangkat lokal menghasilkan event payload beridentitas deterministik:
```json
{
  "eventId": "EVT_HP1_1728172800000_0001",
  "originDeviceId": "DEV_HP_KASIR_1",
  "businessDate": "2026-10-06",
  "timestamp": 1728172800000,
  "eventType": "SALE_COMMITTED",
  "payload": { ... },
  "idempotencyKey": "INV-20261006-1024"
}
```

## 3. Alur Sinkronisasi 2-Arah (HP Kasir ↔ Tablet Owner)
1. **Local Commit First**: Transaksi langsung disimpan di database Room lokal perangkat dan langsung sah (Local-First).
2. **Outbox Enqueue**: Event disimpan ke tabel `outbox_events` di Room lokal.
3. **Drive Upload Worker**: Background worker secara berkala mengunggah file event baru ke folder Google Drive toko `events/{eventId}.json`.
4. **Drive Ingest Worker**: Perangkat lain membaca file event baru di folder Google Drive, memverifikasi `idempotencyKey`, dan mereplay transaksi tersebut ke Room lokal secara idempotent (mencegah duplikasi).
5. **Konvergensi Data**: Setelah event dari kedua belah pihak dipertukarkan, kedua perangkat mencapai kondisi saldo stok dan keuangan yang konvergen.

## 4. Penanganan Konflik & Offline Resilience
- **Offline Penuh**: Jika koneksi internet mati atau tidak stabil, kedua perangkat tetap dapat melayani penjualan dan mencatat transaksi tanpa hambatan.
- **Konflik Stok**: Jika dua kasir menjual stok yang sama saat offline, stok sementara ditandai dengan flag negatif (sesuai aturan BIZ-Q05), dan diselesaikan secara damai saat batch kulakan baru tiba atau via stock opname.
- **Idempotency**: Jika sebuah event diterima lebih dari satu kali, database lokal hanya menerapkan perubahan tepat satu kali.

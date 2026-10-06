# SECURITY — Architecture (Enkripsi At-Rest & Keamanan Data)

Status: FINAL / DECIDED (Dipatch per Audit Forensik 6 Oktober 2026).
Otoritas: Standar keamanan data lokal dan cadangan toko UMKM.

## 1. Enkripsi At-Rest (Database Lokal)
- **Rekomendasi Terpilih**: Enkripsi basis data SQLite Room menggunakan kunci master perangkat (`Android Keystore System` + `SQLCipher / Encrypted File Storage`).
- **Penyimpanan Kredensial**:
  - PIN Owner disimpan dalam bentuk hash terenkripsi (PBKDF2WithHmacSHA256 / SHA-256 ber-salt).
  - PIN tidak pernah disimpan dalam bentuk teks biasa (plaintext) di SharedPreferences atau log sistem.
- **Proteksi Log**: Seluruh query, data transaksi, dan log audit tidak memuat password, PIN, atau data rahasia perbankan.

## 2. Enkripsi Berkas Cadangan (Backup Security)
- Berkas cadangan database `.omahbak` dienkripsi menggunakan algoritma standar industri **AES-256-GCM** sebelum disimpan di memori perangkat atau diunggah ke Google Drive.
- Kata sandi cadangan (*Backup Passphrase*) diatur oleh Owner saat inisialisasi awal toko.
- Berkas backup korup atau berkas dengan modifikasi tanpa kunci yang sah otomatis ditolak oleh modul verifikasi pemulihan (*Restore Guard*).

## 3. Disaster Recovery & Succession
- **Kartu Pemulihan Tertulis (Emergency Recovery Card)**: Owner mencatat PIN Master dan Passphrase Backup pada media fisik di luar perangkat.
- **Penggantian Perangkat (Device Replacement)**: Jika HP kasir hilang atau rusak, perangkat baru mengunduh file cadangan terenkripsi terakhir dari Google Drive toko, memverifikasi kata sandi, dan memulihkan seluruh data secara utuh.

# DECISION_REGISTER — Omah ERP

Status: FINAL / DECIDED (Dipatch per Audit Forensik 6 Oktober 2026).

| ID | Topik Keputusan | Status | Dampak Arsitektur & Bisnis |
|---|---|---|---|
| **PRINSIP-001** | Documentation-first, spec sebelum build | DECIDED | Standar rekayasa perangkat lunak |
| **PRINSIP-002** | Local Committed != Synced | DECIDED | Transaksi lokal langsung sah tanpa menunggu online |
| **PRINSIP-003** | AI Advisory Only | DECIDED | AI hanya membaca ekspor dataset XLSX, dilarang menulis DB |
| **PRINSIP-004** | EQT/KNS/AFL Ownership Classification | DECIDED | EQT milik toko (FIFO), KNS konsinyasi titipan, AFL virtual |
| **PRINSIP-005** | Double-Entry Accounting D=K | DECIDED | Setiap transaksi menghasilkan Debit = Kredit seimbang |
| **ADR-001** | Offline-First Local-First Architecture | DECIDED | Inti sistem beroperasi 100% tanpa internet |
| **ADR-002** | Local Database: Room SQLite + Encryption | DECIDED | Room persistence, ACID, WAL, dan enkripsi at-rest |
| **ADR-003** | Strict FIFO Batch & Negative Control | DECIDED | FIFO konsumsi lot pembelian; stok minus sementara diizinkan (BIZ-Q05) |
| **ADR-004** | Single Canonical Posting Model | DECIDED | Satu tabel posting kanonikal, hapus tabel ganda lama |
| **ADR-005** | Backup AES-256 to Local & Google Drive | DECIDED | Snapshot database terenkripsi AES-256 ke Google Drive toko |
| **ADR-006** | Multi-Device via 1 Shared Google Drive Account | DECIDED | Sinkronisasi multi-device berbasis folder Google Drive toko bersama |
| **ADR-007** | Dual-Role Access: Kasir & Owner (PIN 6-Digit) | DECIDED | Hapus 5-role lama; hanya Kasir (operasional) & Owner (manajerial) |
| **ADR-008** | AI Advisory Read-Only Boundary | DECIDED | AI tidak memiliki endpoint atau hak tulis ke database ERP |
| **ADR-010** | Tipe Uang Long IDR & Qty 3-Desimal | DECIDED | Uang integer Rupiah bulat (Long), kuantitas 3 desimal (Double), selisih pembulatan dibukukan |

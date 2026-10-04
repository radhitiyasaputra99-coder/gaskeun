# Dokumentasi Pengujian

Pengujian dilakukan secara manual dengan menjalankan `run.bat`:

1. **Alur uji berurutan** (bagian 1) dijalankan dalam satu sesi, mencakup semua fitur pada instruksi.
2. **Bukti** (bagian 2): transkrip sesi nyata di [docs/test-session.txt](docs/test-session.txt) dan [docs/sample-session.txt](docs/sample-session.txt).

## 1. Alur uji manual berurutan

Jalankan `run.bat`, lalu ikuti tahap A sampai J **berurutan dalam satu sesi** (data hanya hidup selama aplikasi terbuka).

**Penulisan tanggal** (format `yyyy-MM-dd`). Hari ini pada saat pengujian: **2026-10-04**.

- `TGL1` = hari ini + 3 hari -> `2026-10-07`
- `TGL2` = hari ini + 5 hari -> `2026-10-09`

Jika diuji di hari lain, ganti sesuai tanggalnya. Data demo mencakup 30 hari ke depan, dan kursi/harga ditentukan oleh selisih hari
(hari ini + 3), jadi angka kursi/harga di bawah tetap sama di hari mana pun.
`K1` dan `K2` adalah **nomor konfirmasi acak 6 digit** yang tampil di layar saat pemesanan berhasil (catat).

Kolom **Ketik** berisi urutan input, satu item per Enter.

### A. Validasi input menu (Penanganan Kesalahan)

| Ketik | Hasil yang diharapkan |
|---|---|
| `abc` | `! Input harus berupa angka (contoh: 0).` lalu menu diminta ulang, tidak crash |
| `9` | `! Masukkan angka antara 0 dan 6.` |

### B. Pencarian penerbangan (menu 1)

| Kasus | Ketik | Hasil yang diharapkan |
|---|---|---|
| B1. Ada hasil | `1`, `jakarta`, `bali`, `TGL1`, `2`, `n` | Tabel 3 penerbangan berurut **harga termurah**: JT-650 (Rp 1.064.000, 32 kursi), SJ-262 (Rp 1.208.000, 43 kursi), **GA-410 (Rp 1.581.000, 21 kursi)**. Kolom: no. penerbangan, maskapai, berangkat, tiba, durasi, harga/orang, total, kursi |
| B2. Tidak ada hasil | `1`, `bandung`, `medan`, `TGL1`, `1`, `n` (jawab `n` untuk "Coba cari lagi") | `Maaf, tidak ada penerbangan tersedia ...` |
| B3. Validasi | `1`, `atlantis`, `jakarta`, `bali`, `besok`, `2020-01-01`, `TGL1`, `dua`, `2`, `n` | `! Kota tidak dikenal. Pilihan: ...`, `! Format tanggal salah ...`, `! Tanggal tidak boleh sebelum 2026-10-04.`, `! Input harus berupa angka (contoh: 1).`, lalu hasil pencarian tampil normal |

### C. Pencarian hotel (menu 2)

| Ketik | Hasil yang diharapkan |
|---|---|
| `2`, `denpasar`, `TGL1`, `TGL2`, `3`, `n` | Tabel hotel Denpasar (2 malam, 3 tamu) urut harga/malam: H-203, H-201, H-204, H-202. Kuta Beach Resort (H-201) butuh **1 kamar**, total **Rp 3.300.000**; Sanur (H-203) butuh 2 kamar |

### D. Pemesanan penerbangan (menu 3)

| Ketik (urut) | Hasil yang diharapkan |
|---|---|
| `3`, `jakarta`, `bali`, `TGL1`, `2` | Tabel hasil muncul (langsung diarahkan memesan) |
| `XX-999` | `! Nomor penerbangan 'XX-999' tidak ditemukan pada hasil pencarian.` lalu diminta ulang |
| `GA-410` | Lanjut ke data penumpang |
| `12345` | `! Nama tidak valid ...` (nama penumpang 1 diminta ulang) |
| `Budi Santoso`, `Ani Lestari` | Nama penumpang 1 dan 2 diterima |
| `bukan-kontak` | `! Kontak tidak valid ...` diminta ulang |
| `081234567890` | Kotak **RINGKASAN PESANAN**, total **Rp 3.162.000** (2 x Rp 1.581.000) |
| `y` | `Pemesanan berhasil! Simpan nomor konfirmasi Anda: K1` lalu **E-TIKET PENERBANGAN** (rute, jam, 2 penumpang, kontak, total, status TERKONFIRMASI) |

### E. Pemesanan hotel (menu 4)

| Ketik (urut) | Hasil yang diharapkan |
|---|---|
| `4`, `denpasar`, `TGL1`, `TGL2`, `3` | Tabel hotel muncul |
| `H-999` | `! ID hotel 'H-999' tidak ditemukan pada hasil pencarian.` diminta ulang |
| `H-201` | Lanjut ke data tamu |
| `Budi Santoso`, `budi@example.com` | Nama dan email diterima |
| `y` | `Pemesanan berhasil! ... K2` lalu **VOUCHER HOTEL** (2 malam, 3 tamu / 1 kamar, total Rp 3.300.000) |

### F. Lihat semua pemesanan (menu 6)

| Ketik | Hasil yang diharapkan |
|---|---|
| `6` | Tabel 2 baris: K1 (Penerbangan GA-410, Rp 3.162.000) dan K2 (Hotel Kuta Beach Resort, Rp 3.300.000). `Hotel: 1 pemesanan`, `Penerbangan: 1 pemesanan`, `Total pengeluaran: Rp 6.462.000` |
| `K1` (di prompt detail) | E-tiket K1 ditampilkan lagi. Jika dikosongkan, kembali ke menu |

### G. Bukti stok kursi berkurang (menu 1)

| Ketik | Hasil yang diharapkan |
|---|---|
| `1`, `jakarta`, `bali`, `TGL1`, `1`, `n` | GA-410 sekarang **19 kursi** (sebelumnya 21, dipesan 2 kursi) |

### H. Pembatalan reservasi (menu 5)

| Kasus | Ketik | Hasil yang diharapkan |
|---|---|---|
| H1. Nomor tidak ada | `5`, `999999` | `! Reservasi dengan nomor konfirmasi 999999 tidak ditemukan.` (custom exception `ReservationNotFoundException`) |
| H2. Batal membatalkan | `5`, `K2`, `n` | Voucher ditampilkan, lalu `Pembatalan dibatalkan. Reservasi tetap aktif.` |
| H3. Batalkan hotel | `5`, `K2`, `y` | `Reservasi HOTEL Kuta Beach Resort (No. K2) berhasil dibatalkan. 1 kamar dilepas ...` |
| H4. Batalkan penerbangan | `5`, `K1`, `y` | `Reservasi PENERBANGAN GA-410 (No. K1) berhasil dibatalkan. 2 kursi dikembalikan ...` |

### I. Daftar setelah semua batal

| Ketik | Hasil yang diharapkan |
|---|---|
| `6` | `Belum ada pemesanan.` |

### G2. Bukti kursi dikembalikan

| Ketik | Hasil yang diharapkan |
|---|---|
| `1`, `jakarta`, `bali`, `TGL1`, `1`, `n` | GA-410 kembali **21 kursi** |

### J. Keluar

| Ketik | Hasil yang diharapkan |
|---|---|
| `0` | `Terima kasih telah menjelajah bareng NusaGo. Selamat jalan-jalan!` |

### Uji tambahan (sesi terpisah)

| Kasus | Cara | Hasil yang diharapkan |
|---|---|---|
| Input ditutup (EOF) | Jalankan app, ketik `1`, `jakarta`, lalu tekan **Ctrl+Z** dan Enter (Windows) | `Input ditutup. Program berhenti.` tanpa crash |
| Kamar habis | Menu 4, Denpasar, `TGL1`-`TGL2`, **2 tamu**, pilih `H-204` (hanya 2 kamar), selesaikan pemesanan, ulangi sekali lagi, lalu cari lagi (menu 2) | Kolom "Sisa kamar" H-204: 2, lalu 1 setelah pemesanan pertama, lalu H-204 **hilang dari tabel** setelah pemesanan kedua (kamar habis) |
| Check-out sebelum check-in | Menu 2, Denpasar, check-in `2026-10-09`, check-out `2026-10-07` | `! Tanggal tidak boleh sebelum 2026-10-10.` diminta ulang |
| Konfirmasi `n` saat memesan | Menu 3 sampai ringkasan, jawab `n` | `Pemesanan dibatalkan.`, kursi tidak berkurang |

## 2. Bukti hasil nyata

- [docs/test-session.txt](docs/test-session.txt): transkrip **alur A sampai J di atas** yang dijalankan pada aplikasi sungguhan
  (input terlihat sebaris dengan prompt-nya, tiap tahap diberi penanda `>>>>>>`). Nomor konfirmasi pada transkrip bersifat acak, jadi akan beda di setiap run.
- [docs/sample-session.txt](docs/sample-session.txt): satu sesi singkat (cari dan pesan penerbangan + hotel, lihat semua).

Perhitungan yang dicek manual: penerbangan 2 x Rp 1.581.000 = **Rp 3.162.000**, hotel 2 malam x 1 kamar x Rp 1.650.000 = **Rp 3.300.000**,
total pengeluaran **Rp 6.462.000**, sesuai tampilan.

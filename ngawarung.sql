-- phpMyAdmin SQL Dump
-- version 5.2.0
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Generation Time: Jul 12, 2025 at 09:05 AM
-- Server version: 8.0.30
-- PHP Version: 8.1.10

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `ngawarung`
--

-- --------------------------------------------------------

--
-- Table structure for table `barang`
--

CREATE TABLE `barang` (
  `id` int NOT NULL,
  `nama_barang` varchar(100) NOT NULL,
  `stok` int NOT NULL DEFAULT '0',
  `harga` decimal(10,2) NOT NULL,
  `kategori` varchar(50) NOT NULL DEFAULT 'Umum'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `barang`
--

INSERT INTO `barang` (`id`, `nama_barang`, `stok`, `harga`, `kategori`) VALUES
-- PRODUK NUSAMART - MINUMAN
(1, 'Aqua 600ml', 100, '3000.00', 'Minuman'),
(2, 'Teh Botol Sosro 450ml', 80, '4000.00', 'Minuman'),
(3, 'Coca Cola 330ml', 60, '5000.00', 'Minuman'),
(4, 'Sprite 330ml', 60, '5000.00', 'Minuman'),
(5, 'Fanta 330ml', 60, '5000.00', 'Minuman'),
(6, 'Pocari Sweat 500ml', 40, '7000.00', 'Minuman'),
(7, 'Mizone 500ml', 45, '6000.00', 'Minuman'),
(8, 'Le Minerale 600ml', 90, '3500.00', 'Minuman'),
(9, 'Teh Pucuk Harum 350ml', 70, '4500.00', 'Minuman'),
(10, 'Yakult 65ml', 50, '2500.00', 'Minuman'),
(11, 'Susu Ultra 250ml', 35, '5500.00', 'Minuman'),
(12, 'Kopi Kapal Api Susu', 25, '3000.00', 'Minuman'),
(13, 'Good Day Cappuccino', 30, '3500.00', 'Minuman'),
(14, 'Teh Kotak Jasmine 300ml', 55, '4000.00', 'Minuman'),
(15, 'Air Kelapa Muda', 20, '8000.00', 'Minuman'),

-- SNACK & MAKANAN RINGAN
(16, 'Chitato Sapi Panggang', 40, '8500.00', 'Snack'),
(17, 'Lays Rumput Laut', 35, '9000.00', 'Snack'),
(18, 'Taro Net 160g', 25, '12000.00', 'Snack'),
(19, 'Cheetos Jagung', 45, '7500.00', 'Snack'),
(20, 'Pringles Original', 20, '15000.00', 'Snack'),
(21, 'Oreo Original', 50, '6500.00', 'Snack'),
(22, 'Better Biskuit', 60, '4000.00', 'Snack'),
(23, 'Monde Butter Cookies', 30, '8000.00', 'Snack'),
(24, 'Richeese Nabati', 80, '2500.00', 'Snack'),

-- MAKANAN INSTAN
(25, 'Indomie Goreng', 100, '3500.00', 'Makanan Instan'),
(26, 'Indomie Soto', 80, '3500.00', 'Makanan Instan'),
(27, 'Mie Sedaap Goreng', 90, '3000.00', 'Makanan Instan'),
(28, 'Pop Mie Ayam Baso', 40, '5000.00', 'Makanan Instan'),
(29, 'Supermi Ayam Bawang', 70, '3000.00', 'Makanan Instan'),

-- TOILETRIES & KEBERSIHAN
(30, 'Sabun Lifebuoy 85g', 50, '4500.00', 'Toiletries'),
(31, 'Shampo Pantene 170ml', 25, '18000.00', 'Toiletries'),
(32, 'Pasta Gigi Pepsodent', 40, '8500.00', 'Toiletries'),
(33, 'Sikat Gigi Formula', 60, '5000.00', 'Toiletries'),
(34, 'Tissue Paseo 250s', 30, '12000.00', 'Toiletries'),
(35, 'Sabun Cuci Piring Mama Lemon', 45, '7500.00', 'Kebersihan'),
(36, 'Sabun Deterjen Rinso', 35, '15000.00', 'Kebersihan'),
(37, 'Pembersih Lantai Wipol', 25, '12000.00', 'Kebersihan'),
(38, 'Tisu Basah Baby Safe', 40, '8000.00', 'Toiletries'),
(39, 'Deodorant Rexona', 20, '12000.00', 'Toiletries'),

-- PERALATAN RUMAH TANGGA
(40, 'Sendok Plastik (pack)', 50, '5000.00', 'Peralatan'),
(41, 'Garpu Plastik (pack)', 50, '5000.00', 'Peralatan'),
(42, 'Gelas Plastik (pack)', 40, '8000.00', 'Peralatan'),
(43, 'Piring Kertas (pack)', 30, '10000.00', 'Peralatan'),
(44, 'Kantong Plastik Kecil', 100, '2000.00', 'Peralatan'),
(45, 'Kantong Plastik Besar', 80, '3000.00', 'Peralatan'),
(46, 'Korek Api Gas', 60, '3500.00', 'Peralatan'),
(47, 'Baterai AA Alkaline', 25, '8000.00', 'Elektronik'),
(48, 'Lampu LED 5W', 20, '15000.00', 'Elektronik'),

-- ROKOK & TEMBAKAU
(49, 'Gudang Garam Surya 12', 40, '17000.00', 'Rokok'),
(50, 'Marlboro Merah', 30, '23000.00', 'Rokok'),
(51, 'Sampoerna Mild 16', 35, '22000.00', 'Rokok'),
(52, 'Djarum Super 12', 25, '18000.00', 'Rokok'),
(53, 'Lucky Strike', 20, '24000.00', 'Rokok'),
(54, 'Esse Change', 25, '22000.00', 'Rokok'),

-- SEMBAKO
(55, 'Beras Premium 5kg', 15, '75000.00', 'Sembako'),
(56, 'Minyak Goreng Tropical 1L', 30, '18000.00', 'Sembako'),
(57, 'Gula Pasir Gulaku 1kg', 25, '15000.00', 'Sembako'),
(58, 'Garam Dapur Dolphin', 50, '3000.00', 'Sembako'),
(59, 'Tepung Terigu Segitiga', 20, '12000.00', 'Sembako'),
(60, 'Kecap Manis ABC 275ml', 40, '8500.00', 'Sembako'),
(61, 'Saos Tomat Del Monte', 35, '9000.00', 'Sembako'),
(62, 'Bumbu Instan Royco', 60, '2500.00', 'Sembako'),
(63, 'Telur Ayam (kg)', 20, '28000.00', 'Sembako'),
(64, 'Susu Kental Manis', 30, '12000.00', 'Sembako'),

-- FROZEN FOOD
(65, 'Nugget Fiesta 500g', 15, '25000.00', 'Frozen'),
(66, 'Sosis So Nice 500g', 18, '22000.00', 'Frozen'),
(67, 'Bakso Ikan So Good', 20, '18000.00', 'Frozen'),
(68, 'Es Krim Walls Cornetto', 25, '8000.00', 'Frozen'),
(69, 'Es Krim Magnum Classic', 20, '12000.00', 'Frozen'),

-- BUAH & SAYUR
(70, 'Apel Malang (kg)', 10, '35000.00', 'Buah'),
(71, 'Pisang Cavendish (kg)', 15, '20000.00', 'Buah'),
(72, 'Jeruk Manis (kg)', 12, '25000.00', 'Buah'),
(73, 'Tomat (kg)', 20, '18000.00', 'Sayur'),
(74, 'Bawang Merah (kg)', 8, '45000.00', 'Sayur'),
(75, 'Bawang Putih (kg)', 10, '35000.00', 'Sayur'),
(76, 'Cabai Merah (kg)', 5, '50000.00', 'Sayur'),
(77, 'Wortel (kg)', 15, '15000.00', 'Sayur'),

-- ALAT TULIS & KANTOR
(78, 'Pulpen Standard AE7', 50, '3000.00', 'ATK'),
(79, 'Pensil 2B Faber Castell', 40, '4000.00', 'ATK'),
(80, 'Penghapus Steadtler', 60, '2500.00', 'ATK'),
(81, 'Buku Tulis Sinar Dunia', 35, '5000.00', 'ATK'),
(82, 'Kertas HVS A4 (rim)', 10, '45000.00', 'ATK'),
(83, 'Lem Stick UHU', 25, '8000.00', 'ATK'),
(84, 'Spidol Snowman', 30, '6000.00', 'ATK'),

-- OBAT-OBATAN
(85, 'Paracetamol 500mg', 30, '5000.00', 'Obat'),
(86, 'Betadine 15ml', 20, '12000.00', 'Obat'),
(87, 'Plester Hansaplast', 25, '8000.00', 'Obat'),
(88, 'Minyak Kayu Putih 60ml', 15, '15000.00', 'Obat'),
(89, 'Tolak Angin Cair', 40, '3500.00', 'Obat'),
(90, 'Antimo Tablet', 35, '6000.00', 'Obat'),
(91, 'Bodrex Tablet', 50, '2500.00', 'Obat'),

-- AKSESORIS & ELEKTRONIK
(92, 'Charger HP Universal', 15, '25000.00', 'Elektronik'),
(93, 'Kabel Data USB', 20, '15000.00', 'Elektronik'),
(94, 'Earphone Basic', 18, '20000.00', 'Elektronik'),
(95, 'Powerbank 5000mAh', 8, '85000.00', 'Elektronik'),
(96, 'Kacamata Baca +1', 10, '35000.00', 'Aksesoris'),
(97, 'Masker Sensi 50pcs', 12, '45000.00', 'Kesehatan'),
(98, 'Hand Sanitizer 60ml', 30, '8000.00', 'Kesehatan');

-- --------------------------------------------------------

--
-- Table structure for table `transaksi`
--

CREATE TABLE `transaksi` (
  `id` int NOT NULL,
  `id_barang` int NOT NULL,
  `id_kasir` int NOT NULL,
  `jumlah` int NOT NULL,
  `total_harga` decimal(10,2) NOT NULL,
  `waktu` datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('admin','kasir') NOT NULL,
  `is_active` tinyint(1) DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `password`, `role`, `is_active`) VALUES
(1, 'admin', 'admin', 'admin', 1),
(2, 'kasir', 'kasir', 'kasir', 1),
(3, 'admin2', 'admin123', 'admin', 1),
(4, 'kasir2', 'kasir123', 'kasir', 1),
(5, 'yuda', 'yuda123', 'admin', 1),
(6, 'RIO', 'rio123', 'kasir', 0);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `barang`
--
ALTER TABLE `barang`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `transaksi`
--
ALTER TABLE `transaksi`
  ADD PRIMARY KEY (`id`),
  ADD KEY `id_barang` (`id_barang`),
  ADD KEY `id_kasir` (`id_kasir`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `barang`
--
ALTER TABLE `barang`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=99;

--
-- AUTO_INCREMENT for table `transaksi`
--
ALTER TABLE `transaksi`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=33;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `transaksi`
--
ALTER TABLE `transaksi`
  ADD CONSTRAINT `transaksi_ibfk_1` FOREIGN KEY (`id_barang`) REFERENCES `barang` (`id`),
  ADD CONSTRAINT `transaksi_ibfk_2` FOREIGN KEY (`id_kasir`) REFERENCES `users` (`id`);
COMMIT;

-- =====================================================
-- NUSAMART DATABASE - INFORMASI LENGKAP
-- =====================================================
-- Database: ngawarung
-- Toko: NUSAMART
-- Total Produk: 98 items dengan 15 kategori
-- Total Users: 6 (5 aktif, 1 non-aktif)
--
-- KATEGORI PRODUK:
-- 1. Minuman (15) - Air, minuman ringan, teh, kopi
-- 2. Snack (9) - Keripik, biskuit, makanan ringan
-- 3. Makanan Instan (5) - Mie instan berbagai merk
-- 4. Toiletries (6) - Sabun, shampo, pasta gigi
-- 5. Kebersihan (3) - Deterjen, pembersih
-- 6. Peralatan (7) - Alat makan, kantong plastik
-- 7. Elektronik (6) - Baterai, charger, powerbank
-- 8. Rokok (6) - Berbagai merk rokok
-- 9. Sembako (10) - Beras, minyak, gula, bumbu
-- 10. Frozen (5) - Nugget, sosis, es krim
-- 11. Buah (3) - Apel, pisang, jeruk
-- 12. Sayur (5) - Tomat, bawang, cabai
-- 13. ATK (7) - Pulpen, pensil, kertas
-- 14. Obat (7) - Paracetamol, betadine, plester
-- 15. Aksesoris & Kesehatan (6) - Masker, sanitizer
--
-- USER ACCOUNTS:
-- admin/admin (Admin) - Login utama
-- kasir/kasir (Kasir) - Login kasir utama
-- admin2/admin123 (Admin)
-- kasir2/kasir123 (Kasir)
-- yuda/yuda123 (Admin)
-- RIO/rio123 (Kasir - Non-Aktif untuk test)
--
-- FITUR DATABASE:
-- ✅ Soft delete users (is_active column)
-- ✅ Product categorization
-- ✅ Foreign key constraints
-- ✅ Transaction history
-- ✅ Multi-user support
-- =====================================================

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

package utils;

import model.Barang;
import model.TransaksiRiwayat;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportUtils {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter FILE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    /**
     * Export data produk ke CSV
     */
    public static boolean exportBarangToCSV(List<Barang> barangList, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Data Produk");
        fileChooser.setInitialFileName("data_produk_" + LocalDateTime.now().format(FILE_FORMATTER) + ".csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        java.io.File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return false;
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Header CSV
            writer.println("Kode,Nama Produk,Stok,Harga,Status,Total Nilai");
            
            // Data produk
            double totalNilaiSemua = 0;
            for (Barang barang : barangList) {
                String kode = "BRG" + String.format("%03d", barang.getId());
                String status = getStatusStok(barang.getStok());
                double totalNilai = barang.getStok() * barang.getHarga();
                totalNilaiSemua += totalNilai;
                
                writer.printf("%s,\"%s\",%d,%.2f,%s,%.2f%n",
                    kode,
                    barang.getNamaBarang(),
                    barang.getStok(),
                    barang.getHarga(),
                    status,
                    totalNilai
                );
            }
            
            // Summary
            writer.println();
            writer.println("RINGKASAN");
            writer.printf("Total Produk,%d%n", barangList.size());
            writer.printf("Total Nilai Stok,%.2f%n", totalNilaiSemua);
            writer.printf("Tanggal Export,%s%n", LocalDateTime.now().format(DATE_FORMATTER));
            
            return true;
            
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Export data transaksi ke CSV
     */
    public static boolean exportTransaksiToCSV(List<TransaksiRiwayat> transaksiList, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Data Transaksi");
        fileChooser.setInitialFileName("data_transaksi_" + LocalDateTime.now().format(FILE_FORMATTER) + ".csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        java.io.File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return false;
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Header CSV
            writer.println("No,Waktu,Nama Produk,Kasir,Jumlah,Total Harga");
            
            // Data transaksi
            double totalPendapatan = 0;
            int totalQty = 0;
            
            for (int i = 0; i < transaksiList.size(); i++) {
                TransaksiRiwayat transaksi = transaksiList.get(i);
                totalPendapatan += transaksi.getTotalHarga();
                totalQty += transaksi.getJumlah();
                
                writer.printf("%d,%s,\"%s\",\"%s\",%d,%.2f%n",
                    i + 1,
                    transaksi.getWaktu().format(DATE_FORMATTER),
                    transaksi.getNamaBarang(),
                    transaksi.getNamaKasir(),
                    transaksi.getJumlah(),
                    transaksi.getTotalHarga()
                );
            }
            
            // Summary
            writer.println();
            writer.println("RINGKASAN");
            writer.printf("Total Transaksi,%d%n", transaksiList.size());
            writer.printf("Total Quantity,%d%n", totalQty);
            writer.printf("Total Pendapatan,%.2f%n", totalPendapatan);
            writer.printf("Tanggal Export,%s%n", LocalDateTime.now().format(DATE_FORMATTER));
            
            return true;
            
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Export laporan stok lengkap ke CSV
     */
    public static boolean exportLaporanStokToCSV(List<Barang> barangList, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Laporan Stok");
        fileChooser.setInitialFileName("laporan_stok_" + LocalDateTime.now().format(FILE_FORMATTER) + ".csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        java.io.File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return false;
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Header laporan
            writer.println("LAPORAN STOK PRODUK - KASIRIN");
            writer.printf("Tanggal: %s%n", LocalDateTime.now().format(DATE_FORMATTER));
            writer.println();
            
            // Header tabel
            writer.println("Kode,Nama Produk,Stok,Harga Satuan,Total Nilai,Status,Kategori Stok");
            
            // Statistik
            int totalProduk = barangList.size();
            int stokTersedia = 0;
            int stokMenipis = 0;
            int stokHabis = 0;
            double totalNilaiStok = 0;
            
            // Data detail
            for (Barang barang : barangList) {
                String kode = "BRG" + String.format("%03d", barang.getId());
                String status = getStatusStok(barang.getStok());
                String kategori = getKategoriStok(barang.getStok());
                double totalNilai = barang.getStok() * barang.getHarga();
                totalNilaiStok += totalNilai;
                
                // Hitung statistik
                if (barang.getStok() == 0) stokHabis++;
                else if (barang.getStok() <= 10) stokMenipis++;
                else stokTersedia++;
                
                writer.printf("%s,\"%s\",%d,%.2f,%.2f,%s,%s%n",
                    kode,
                    barang.getNamaBarang(),
                    barang.getStok(),
                    barang.getHarga(),
                    totalNilai,
                    status,
                    kategori
                );
            }
            
            // Ringkasan statistik
            writer.println();
            writer.println("RINGKASAN LAPORAN");
            writer.printf("Total Produk,%d%n", totalProduk);
            writer.printf("Stok Tersedia,%d%n", stokTersedia);
            writer.printf("Stok Menipis,%d%n", stokMenipis);
            writer.printf("Stok Habis,%d%n", stokHabis);
            writer.printf("Total Nilai Stok,%.2f%n", totalNilaiStok);
            writer.println();
            
            // Analisis
            writer.println("ANALISIS");
            double persentaseStokTersedia = (double) stokTersedia / totalProduk * 100;
            double persentaseStokMenipis = (double) stokMenipis / totalProduk * 100;
            double persentaseStokHabis = (double) stokHabis / totalProduk * 100;
            
            writer.printf("Persentase Stok Tersedia,%.1f%%n", persentaseStokTersedia);
            writer.printf("Persentase Stok Menipis,%.1f%%n", persentaseStokMenipis);
            writer.printf("Persentase Stok Habis,%.1f%%n", persentaseStokHabis);
            
            return true;
            
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static String getStatusStok(int stok) {
        if (stok == 0) return "Habis";
        else if (stok <= 10) return "Menipis";
        else return "Tersedia";
    }
    
    private static String getKategoriStok(int stok) {
        if (stok == 0) return "Stok Habis";
        else if (stok <= 5) return "Stok Kritis";
        else if (stok <= 10) return "Stok Rendah";
        else if (stok <= 50) return "Stok Normal";
        else return "Stok Tinggi";
    }
}

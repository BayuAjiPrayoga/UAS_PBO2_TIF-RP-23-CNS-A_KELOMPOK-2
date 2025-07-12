package utils;

import model.Barang;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PrintUtils {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,###");
    
    /**
     * Print laporan stok produk
     */
    public static boolean printLaporanStok(List<Barang> barangList) {
        try {
            // Buat printer job
            Printer printer = Printer.getDefaultPrinter();
            if (printer == null) {
                showPrintError("Tidak ada printer yang tersedia!");
                return false;
            }
            
            PrinterJob printerJob = PrinterJob.createPrinterJob();
            if (printerJob == null) {
                showPrintError("Gagal membuat print job!");
                return false;
            }
            
            // Show print dialog
            if (!printerJob.showPrintDialog(null)) {
                return false; // User cancelled
            }
            
            // Buat content untuk print
            Node printContent = createPrintContent(barangList);
            
            // Print
            boolean success = printerJob.printPage(printContent);
            if (success) {
                printerJob.endJob();
            }
            
            return success;
            
        } catch (Exception e) {
            e.printStackTrace();
            showPrintError("Gagal mencetak laporan: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Buat content untuk print laporan stok
     */
    private static Node createPrintContent(List<Barang> barangList) {
        VBox printPage = new VBox(10);
        printPage.setPadding(new Insets(20));
        printPage.setPrefWidth(550); // A4 width minus margins
        
        // Header laporan
        Text title = new Text("LAPORAN STOK PRODUK");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextAlignment(TextAlignment.CENTER);
        
        Text subtitle = new Text("KASIRIN - NUSAMART MANAGEMENT SYSTEM");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        subtitle.setTextAlignment(TextAlignment.CENTER);
        
        Text dateTime = new Text("Tanggal Cetak: " + LocalDateTime.now().format(DATE_FORMATTER));
        dateTime.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        dateTime.setTextAlignment(TextAlignment.CENTER);
        
        // Center header
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        header.getChildren().addAll(title, subtitle, dateTime);
        
        // Separator line
        Text separator1 = new Text("═".repeat(70));
        separator1.setFont(Font.font("Courier New", 10));
        
        // Statistik
        VBox statsBox = createStatsSection(barangList);
        
        // Separator line
        Text separator2 = new Text("═".repeat(70));
        separator2.setFont(Font.font("Courier New", 10));
        
        // Table header
        VBox tableSection = createTableSection(barangList);
        
        // Footer
        VBox footer = createFooterSection(barangList);
        
        printPage.getChildren().addAll(
            header,
            new Text(""), // Space
            separator1,
            statsBox,
            separator2,
            tableSection,
            new Text(""), // Space
            footer
        );
        
        return printPage;
    }
    
    /**
     * Buat section statistik
     */
    private static VBox createStatsSection(List<Barang> barangList) {
        VBox statsBox = new VBox(5);
        
        Text statsTitle = new Text("RINGKASAN STATISTIK");
        statsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        // Hitung statistik
        int totalProduk = barangList.size();
        int stokTersedia = (int) barangList.stream().filter(b -> b.getStok() > 10).count();
        int stokMenipis = (int) barangList.stream().filter(b -> b.getStok() <= 10 && b.getStok() > 0).count();
        int stokHabis = (int) barangList.stream().filter(b -> b.getStok() == 0).count();
        double totalNilaiStok = barangList.stream().mapToDouble(b -> b.getStok() * b.getHarga()).sum();
        
        Text stat1 = new Text("Total Produk: " + totalProduk + " item");
        Text stat2 = new Text("Stok Tersedia: " + stokTersedia + " produk");
        Text stat3 = new Text("Stok Menipis: " + stokMenipis + " produk");
        Text stat4 = new Text("Stok Habis: " + stokHabis + " produk");
        Text stat5 = new Text("Total Nilai Stok: Rp " + CURRENCY_FORMAT.format(totalNilaiStok));
        
        // Set font for stats
        Font statFont = Font.font("Arial", 10);
        stat1.setFont(statFont);
        stat2.setFont(statFont);
        stat3.setFont(statFont);
        stat4.setFont(statFont);
        stat5.setFont(statFont);
        
        statsBox.getChildren().addAll(statsTitle, stat1, stat2, stat3, stat4, stat5);
        return statsBox;
    }
    
    /**
     * Buat section table
     */
    private static VBox createTableSection(List<Barang> barangList) {
        VBox tableBox = new VBox(3);
        
        Text tableTitle = new Text("DETAIL PRODUK");
        tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        // Table header
        Text headerLine = new Text(String.format("%-8s %-25s %8s %12s %10s", 
            "KODE", "NAMA PRODUK", "STOK", "HARGA", "STATUS"));
        headerLine.setFont(Font.font("Courier New", FontWeight.BOLD, 9));
        
        Text headerSeparator = new Text("-".repeat(70));
        headerSeparator.setFont(Font.font("Courier New", 9));
        
        tableBox.getChildren().addAll(tableTitle, new Text(""), headerLine, headerSeparator);
        
        // Table data (limit to prevent page overflow)
        int maxItems = Math.min(25, barangList.size()); // Limit items for print
        for (int i = 0; i < maxItems; i++) {
            Barang barang = barangList.get(i);
            String kode = "BRG" + String.format("%03d", barang.getId());
            String nama = barang.getNamaBarang();
            if (nama.length() > 22) {
                nama = nama.substring(0, 22) + "...";
            }
            String status = getStatusStok(barang.getStok());
            
            Text dataLine = new Text(String.format("%-8s %-25s %8d %12s %10s",
                kode,
                nama,
                barang.getStok(),
                "Rp " + CURRENCY_FORMAT.format(barang.getHarga()),
                status
            ));
            dataLine.setFont(Font.font("Courier New", 8));
            
            tableBox.getChildren().add(dataLine);
        }
        
        // Show if data is truncated
        if (barangList.size() > maxItems) {
            Text truncateInfo = new Text("... dan " + (barangList.size() - maxItems) + " produk lainnya");
            truncateInfo.setFont(Font.font("Arial", FontWeight.NORMAL, 9));
            tableBox.getChildren().add(truncateInfo);
        }
        
        return tableBox;
    }
    
    /**
     * Buat section footer
     */
    private static VBox createFooterSection(List<Barang> barangList) {
        VBox footerBox = new VBox(5);
        
        Text separator = new Text("═".repeat(70));
        separator.setFont(Font.font("Courier New", 10));
        
        // Analisis persentase
        int totalProduk = barangList.size();
        double persentaseStokTersedia = totalProduk > 0 ? 
            (double) barangList.stream().mapToInt(b -> b.getStok() > 10 ? 1 : 0).sum() / totalProduk * 100 : 0;
        double persentaseStokMenipis = totalProduk > 0 ? 
            (double) barangList.stream().mapToInt(b -> b.getStok() <= 10 && b.getStok() > 0 ? 1 : 0).sum() / totalProduk * 100 : 0;
        double persentaseStokHabis = totalProduk > 0 ? 
            (double) barangList.stream().mapToInt(b -> b.getStok() == 0 ? 1 : 0).sum() / totalProduk * 100 : 0;
        
        Text analysisTitle = new Text("ANALISIS STOK");
        analysisTitle.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        
        Text analysis1 = new Text(String.format("• Stok Tersedia: %.1f%%", persentaseStokTersedia));
        Text analysis2 = new Text(String.format("• Stok Menipis: %.1f%%", persentaseStokMenipis));
        Text analysis3 = new Text(String.format("• Stok Habis: %.1f%%", persentaseStokHabis));
        
        Font analysisFont = Font.font("Arial", 10);
        analysis1.setFont(analysisFont);
        analysis2.setFont(analysisFont);
        analysis3.setFont(analysisFont);
        
        // Rekomendasi
        Text recomTitle = new Text("REKOMENDASI");
        recomTitle.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        
        Text recom1 = new Text("• Segera lakukan restocking untuk produk dengan stok habis");
        Text recom2 = new Text("• Monitor produk dengan stok menipis secara berkala");
        Text recom3 = new Text("• Evaluasi pola penjualan untuk optimasi stok");
        
        recom1.setFont(analysisFont);
        recom2.setFont(analysisFont);
        recom3.setFont(analysisFont);
        
        // Print info
        Text printInfo = new Text("Dicetak oleh: KASIRIN System | " + LocalDateTime.now().format(DATE_FORMATTER));
        printInfo.setFont(Font.font("Arial", FontWeight.NORMAL, 8));
        
        footerBox.getChildren().addAll(
            separator,
            analysisTitle, analysis1, analysis2, analysis3,
            new Text(""),
            recomTitle, recom1, recom2, recom3,
            new Text(""),
            printInfo
        );
        
        return footerBox;
    }
    
    /**
     * Get status stok
     */
    private static String getStatusStok(int stok) {
        if (stok == 0) return "HABIS";
        else if (stok <= 10) return "MENIPIS";
        else return "TERSEDIA";
    }
    
    /**
     * Show print error dialog
     */
    private static void showPrintError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Print Error");
        alert.setHeaderText("Gagal Mencetak");
        alert.setContentText(message);
        alert.showAndWait();
    }
}

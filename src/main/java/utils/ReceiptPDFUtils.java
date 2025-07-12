package utils;

import controllerui.KasirController.CartItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.User;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * Utility class untuk membuat struk dalam format HTML dan Text
 * Professional receipt generation dengan HTML styling untuk PDF conversion
 */
public class ReceiptPDFUtils {
    
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,###");
    
    /**
     * Generate receipt untuk transaksi kasir dalam format HTML dan Text
     * 
     * @param cartItems List item yang dibeli
     * @param kasirUser User kasir yang melayani
     * @param uangBayar Jumlah uang yang dibayarkan
     * @param kembalian Jumlah kembalian
     * @param parentStage Parent stage untuk dialog
     * @return true jika berhasil, false jika dibatalkan atau gagal
     */
    public static boolean generateReceiptPDF(List<CartItem> cartItems, User kasirUser, 
                                           double uangBayar, double kembalian, Stage parentStage) {
        try {
            // File chooser untuk menyimpan receipt
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Simpan Struk");
            fileChooser.setInitialFileName("Struk_" + 
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("HTML Files (dapat diprint ke PDF)", "*.html"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
            
            File file = fileChooser.showSaveDialog(parentStage);
            if (file == null) {
                return false; // User cancelled
            }
            
            String fileName = file.getAbsolutePath();
            
            // Generate both HTML and text versions
            if (fileName.toLowerCase().endsWith(".html")) {
                String htmlContent = generateHTMLReceipt(cartItems, kasirUser, uangBayar, kembalian);
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(htmlContent);
                }
            } else {
                String textContent = generateTextReceipt(cartItems, kasirUser, uangBayar, kembalian);
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(textContent);
                }
            }
            
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Generate receipt dengan path yang sudah ditentukan
     */
    public static boolean generateReceiptPDF(List<CartItem> cartItems, User kasirUser, 
                                           double uangBayar, double kembalian, String outputPath) {
        try {
            if (outputPath.toLowerCase().endsWith(".html")) {
                String htmlContent = generateHTMLReceipt(cartItems, kasirUser, uangBayar, kembalian);
                try (FileWriter writer = new FileWriter(outputPath)) {
                    writer.write(htmlContent);
                }
            } else {
                String textContent = generateTextReceipt(cartItems, kasirUser, uangBayar, kembalian);
                try (FileWriter writer = new FileWriter(outputPath)) {
                    writer.write(textContent);
                }
            }
            
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Generate HTML receipt yang dapat diprint ke PDF melalui browser
     */
    private static String generateHTMLReceipt(List<CartItem> cartItems, User kasirUser, 
                                            double uangBayar, double kembalian) {
        
        StringBuilder html = new StringBuilder();
        
        // Calculate totals
        double subtotal = cartItems.stream().mapToDouble(CartItem::getTotal).sum();
        int totalItems = cartItems.stream().mapToInt(CartItem::getQuantity).sum();
        
        String currentDateTime = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        String kasirName = kasirUser != null ? kasirUser.getUsername() : "Unknown";
        String transactionId = "TRX" + System.currentTimeMillis();
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n<head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<title>Struk Pembelian - NUSAMART</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: 'Courier New', monospace; margin: 20px; background: white; }\n");
        html.append(".receipt { max-width: 400px; margin: 0 auto; padding: 20px; border: 2px solid #000; }\n");
        html.append(".header { text-align: center; font-weight: bold; font-size: 16px; margin-bottom: 10px; }\n");
        html.append(".title { text-align: center; font-weight: bold; font-size: 14px; margin-bottom: 20px; }\n");
        html.append(".info { margin-bottom: 20px; }\n");
        html.append(".items { margin-bottom: 20px; }\n");
        html.append(".items table { width: 100%; border-collapse: collapse; }\n");
        html.append(".items th, .items td { padding: 5px; text-align: left; border-bottom: 1px solid #ccc; }\n");
        html.append(".items th { font-weight: bold; border-bottom: 2px solid #000; }\n");
        html.append(".summary { margin-bottom: 20px; }\n");
        html.append(".payment { font-weight: bold; margin-bottom: 20px; }\n");
        html.append(".footer { text-align: center; margin-top: 20px; font-size: 10px; }\n");
        html.append(".separator { border-top: 2px solid #000; margin: 20px 0; }\n");
        html.append("@media print { body { margin: 0; } .receipt { border: none; } }\n");
        html.append("</style>\n</head>\n<body>\n");
        
        html.append("<div class='receipt'>\n");
        
        // Header
        html.append("<div class='header'>NUSAMART</div>\n");
        html.append("<div class='title'>STRUK PEMBELIAN</div>\n");
        html.append("<div class='separator'></div>\n");
        
        // Transaction Info
        html.append("<div class='info'>\n");
        html.append("<strong>Kasir:</strong> ").append(kasirName).append("<br>\n");
        html.append("<strong>Tanggal:</strong> ").append(currentDateTime).append("<br>\n");
        html.append("<strong>No. Transaksi:</strong> ").append(transactionId).append("<br>\n");
        html.append("</div>\n");
        
        // Items Table
        html.append("<div class='items'>\n");
        html.append("<h3 style='text-align: center; margin-bottom: 10px;'>DETAIL PEMBELIAN</h3>\n");
        html.append("<table>\n");
        html.append("<tr><th>Nama Barang</th><th>Qty</th><th>Harga</th><th>Total</th></tr>\n");
        
        for (CartItem item : cartItems) {
            html.append("<tr>\n");
            html.append("<td>").append(item.getNama()).append("</td>\n");
            html.append("<td>").append(item.getQuantity()).append("</td>\n");
            html.append("<td>Rp ").append(CURRENCY_FORMAT.format(item.getHarga())).append("</td>\n");
            html.append("<td>Rp ").append(CURRENCY_FORMAT.format(item.getTotal())).append("</td>\n");
            html.append("</tr>\n");
        }
        
        html.append("</table>\n");
        html.append("</div>\n");
        
        // Summary
        html.append("<div class='separator'></div>\n");
        html.append("<div class='summary'>\n");
        html.append("<h3 style='text-align: center; margin-bottom: 10px;'>RINGKASAN PEMBAYARAN</h3>\n");
        html.append("<strong>Total Item:</strong> ").append(totalItems).append("<br>\n");
        html.append("<strong>Subtotal:</strong> Rp ").append(CURRENCY_FORMAT.format(subtotal)).append("<br>\n");
        html.append("</div>\n");
        
        // Payment Details
        html.append("<div class='separator'></div>\n");
        html.append("<div class='payment'>\n");
        html.append("<strong>TOTAL BAYAR: Rp ").append(CURRENCY_FORMAT.format(subtotal)).append("</strong><br>\n");
        html.append("<strong>UANG BAYAR: Rp ").append(CURRENCY_FORMAT.format(uangBayar)).append("</strong><br>\n");
        html.append("<strong>KEMBALIAN: Rp ").append(CURRENCY_FORMAT.format(kembalian)).append("</strong><br>\n");
        html.append("</div>\n");
        
        // Footer
        html.append("<div class='separator'></div>\n");
        html.append("<div class='footer'>\n");
        html.append("<strong>Terima kasih atas kunjungan Anda!</strong><br><br>\n");
        html.append("Barang yang sudah dibeli tidak dapat dikembalikan<br>\n");
        html.append("kecuali ada kesalahan dari pihak toko<br><br>\n");
        html.append("Struk dicetak pada: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())).append("\n");
        html.append("</div>\n");
        
        html.append("</div>\n");
        html.append("</body>\n</html>");
        
        return html.toString();
    }
    
    /**
     * Generate text receipt (existing implementation)
     */
    private static String generateTextReceipt(List<CartItem> cartItems, User kasirUser, 
                                            double uangBayar, double kembalian) {
        return generateReceiptContent(cartItems, kasirUser, uangBayar, kembalian);
    }
    
    private static String generateReceiptContent(List<CartItem> cartItems, User kasirUser, 
                                               double uangBayar, double kembalian) {
        
        StringBuilder receipt = new StringBuilder();
        
        // Calculate totals
        double subtotal = cartItems.stream().mapToDouble(CartItem::getTotal).sum();
        int totalItems = cartItems.stream().mapToInt(CartItem::getQuantity).sum();
        
        // Header
        receipt.append("================================================================\n");
        receipt.append("                       NUSAMART STORE                           \n");
        receipt.append("                    STRUK PEMBELIAN                           \n");
        receipt.append("================================================================\n");
        receipt.append("\n");
        
        // Transaction info
        String currentDateTime = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        String kasirName = kasirUser != null ? kasirUser.getUsername() : "Unknown";
        String transactionId = "TRX" + System.currentTimeMillis();
        
        receipt.append(String.format("Kasir           : %s\n", kasirName));
        receipt.append(String.format("Tanggal         : %s\n", currentDateTime));
        receipt.append(String.format("No. Transaksi   : %s\n", transactionId));
        receipt.append("\n");
        receipt.append("----------------------------------------------------------------\n");
        receipt.append("                        DETAIL PEMBELIAN                       \n");
        receipt.append("----------------------------------------------------------------\n");
        receipt.append(String.format("%-20s %3s %10s %12s %12s\n", 
            "Nama Barang", "Qty", "Harga", "Subtotal", "Total"));
        receipt.append("----------------------------------------------------------------\n");
        
        // Items
        for (CartItem item : cartItems) {
            String itemName = item.getNama();
            if (itemName.length() > 20) {
                itemName = itemName.substring(0, 17) + "...";
            }
            
            receipt.append(String.format("%-20s %3d %10s %12s %12s\n",
                itemName,
                item.getQuantity(),
                "Rp " + CURRENCY_FORMAT.format(item.getHarga()),
                "Rp " + CURRENCY_FORMAT.format(item.getHarga() * item.getQuantity()),
                "Rp " + CURRENCY_FORMAT.format(item.getTotal())));
        }
        
        receipt.append("----------------------------------------------------------------\n");
        receipt.append("\n");
        
        // Summary
        receipt.append("                         RINGKASAN PEMBAYARAN                  \n");
        receipt.append("----------------------------------------------------------------\n");
        receipt.append(String.format("Total Item      : %d\n", totalItems));
        receipt.append(String.format("Subtotal        : Rp %s\n", CURRENCY_FORMAT.format(subtotal)));
        receipt.append("----------------------------------------------------------------\n");
        receipt.append(String.format("TOTAL BAYAR     : Rp %s\n", CURRENCY_FORMAT.format(subtotal)));
        receipt.append(String.format("UANG BAYAR      : Rp %s\n", CURRENCY_FORMAT.format(uangBayar)));
        receipt.append(String.format("KEMBALIAN       : Rp %s\n", CURRENCY_FORMAT.format(kembalian)));
        receipt.append("================================================================\n");
        receipt.append("\n");
        
        // Footer
        receipt.append("                   Terima kasih atas kunjungan Anda!           \n");
        receipt.append("                                                                \n");
        receipt.append("           Barang yang sudah dibeli tidak dapat               \n");
        receipt.append("             dikembalikan kecuali ada kesalahan               \n");
        receipt.append("                     dari pihak toko                          \n");
        receipt.append("\n");
        receipt.append("================================================================\n");
        receipt.append(String.format("Struk dicetak pada: %s\n", 
            new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())));
        receipt.append("================================================================\n");
        
        return receipt.toString();
    }
}

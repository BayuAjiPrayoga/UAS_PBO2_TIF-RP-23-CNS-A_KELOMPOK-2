package controllerui;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import controller.BarangController;
import controller.TransaksiController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.Barang;
import model.Transaksi;
import model.User;
import utils.AlertUtils;
import utils.ReceiptPDFUtils;
import utils.SoundUtils;

public class KasirController {

    // Static variable to hold current user as backup
    private static User currentKasirUser;

    // Product Table
    @FXML private TableView<Barang> barangTable;
    @FXML private TableColumn<Barang, String> colKode;
    @FXML private TableColumn<Barang, String> colNama;
    @FXML private TableColumn<Barang, Integer> colStok;
    @FXML private TableColumn<Barang, Double> colHarga;
    @FXML private TableColumn<Barang, Void> colAction;

    // Cart Table
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> cartColNama;
    @FXML private TableColumn<CartItem, Integer> cartColQty;
    @FXML private TableColumn<CartItem, Double> cartColHarga;
    @FXML private TableColumn<CartItem, Double> cartColTotal;
    @FXML private TableColumn<CartItem, Void> cartColAction;

    // Input Fields
    @FXML private TextField searchField;
    @FXML private TextField jumlahField;
    @FXML private TextField bayarField;

    // Labels
    @FXML private Label kasirNameLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Label totalItemLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label totalBayarLabel;
    @FXML private Label kembalianLabel;
    @FXML private Label selectedProductLabel; // Label untuk menampilkan produk terpilih

    private BarangController barangController = new BarangController();
    private TransaksiController transaksiController = new TransaksiController();
    private ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private DecimalFormat currencyFormat = new DecimalFormat("#,###");
    
    private User kasirUser;
    private Timer dateTimeTimer;
    private Barang selectedProduct; // Produk yang sedang dipilih
    
    // Variables untuk struk PDF
    private double lastUangBayar = 0;
    private double lastKembalian = 0;
    private boolean hasCompletedTransaction = false;
    private List<CartItem> lastCartItems = new ArrayList<>(); // Simpan cart items untuk cetak ulang struk

    // Inner class for cart items
    public static class CartItem {
        private Barang barang;
        private int quantity;
        private double total;

        public CartItem(Barang barang, int quantity) {
            this.barang = barang;
            this.quantity = quantity;
            this.total = barang.getHarga() * quantity;
        }

        public Barang getBarang() { return barang; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) {
            this.quantity = quantity;
            this.total = barang.getHarga() * quantity;
        }
        public double getTotal() { return total; }
        public String getNama() { return barang.getNamaBarang(); }
        public double getHarga() { return barang.getHarga(); }
    }

    public void initialize() {
        setupProductTable();
        setupCartTable();
        loadBarangData();
        startDateTimeUpdate();
        
        // Set default quantity
        jumlahField.setText("1");
        
        // Initialize selected product label
        updateSelectedProductDisplay();
        
        // Setup search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() >= 2 || newValue.isEmpty()) {
                filterProducts(newValue);
            }
        });
        
        // Initialize user from static backup if available
        if (currentKasirUser != null && kasirUser == null) {
            setKasirUser(currentKasirUser);
        }
    }

    private void setupProductTable() {
        colKode.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty("BRG" + String.format("%03d", data.getValue().getId())));
        colNama.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getNamaBarang()));
        colStok.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleIntegerProperty(data.getValue().getStok()).asObject());
        colHarga.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleDoubleProperty(data.getValue().getHarga()).asObject());
        
        // Format harga column
        colHarga.setCellFactory(column -> new TableCell<Barang, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Rp " + currencyFormat.format(item));
                }
            }
        });

        // Add action buttons
        colAction.setCellFactory(column -> new TableCell<Barang, Void>() {
            private final Button selectButton = new Button("Pilih");
            
            {
                selectButton.setOnAction(event -> {
                    Barang barang = getTableView().getItems().get(getIndex());
                    selectProduct(barang);
                });
                selectButton.getStyleClass().add("btn-add-cart");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(selectButton);
                }
            }
        });
    }

    private void setupCartTable() {
        cartColNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        cartColQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        cartColHarga.setCellValueFactory(new PropertyValueFactory<>("harga"));
        cartColTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        
        // Format currency columns
        cartColHarga.setCellFactory(column -> new TableCell<CartItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Rp " + currencyFormat.format(item));
                }
            }
        });
        
        cartColTotal.setCellFactory(column -> new TableCell<CartItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Rp " + currencyFormat.format(item));
                }
            }
        });

        // Add remove buttons
        cartColAction.setCellFactory(column -> new TableCell<CartItem, Void>() {
            private final Button removeButton = new Button("X");
            
            {
                removeButton.setOnAction(event -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    removeFromCart(item);
                });
                removeButton.getStyleClass().add("btn-cancel");
                removeButton.setPrefWidth(30);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });
        
        cartTable.setItems(cartItems);
    }

    private void loadBarangData() {
        List<Barang> barangList = barangController.getAllBarang();
        ObservableList<Barang> data = FXCollections.observableArrayList(barangList);
        barangTable.setItems(data);
    }

    private void filterProducts(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            loadBarangData();
            return;
        }
        
        List<Barang> allBarang = barangController.getAllBarang();
        ObservableList<Barang> filteredData = FXCollections.observableArrayList();
        
        for (Barang barang : allBarang) {
            if (barang.getNamaBarang().toLowerCase().contains(searchText.toLowerCase())) {
                filteredData.add(barang);
            }
        }
        barangTable.setItems(filteredData);
    }

    private void startDateTimeUpdate() {
        dateTimeTimer = new Timer();
        dateTimeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    dateTimeLabel.setText(sdf.format(new Date()));
                });
            }
        }, 0, 1000); // Update every second
    }

    @FXML
    public void handleTransaksi() {
        handleAddToCart();
    }

    @FXML
    public void handleAddToCart() {
        if (selectedProduct == null) {
            AlertUtils.showWarning("Pilih produk terlebih dahulu!\n\n" +
                                 "Klik tombol 'Pilih' pada produk yang diinginkan.");
            return;
        }
        
        addSelectedProductToCart();
    }

    @FXML
    public void handleLogout() {
        if (dateTimeTimer != null) {
            dateTimeTimer.cancel();
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            AnchorPane loginPane = loader.load();
            Stage stage = (Stage) barangTable.getScene().getWindow();
            stage.setScene(new Scene(loginPane));
            stage.setTitle("Login - KASIRIN");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setKasirUser(User user) {
        this.kasirUser = user;
        currentKasirUser = user; // Set static backup
        
        // Update UI if labels are available
        Platform.runLater(() -> {
            if (kasirNameLabel != null) {
                kasirNameLabel.setText("Kasir: " + user.getUsername());
            }
        });
    }

    public static void setCurrentKasirUser(User user) {
        currentKasirUser = user;
    }

    private void addSelectedProductToCart() {
        if (selectedProduct == null) {
            AlertUtils.showWarning("Tidak ada produk yang dipilih!");
            return;
        }
        
        if (selectedProduct.getStok() <= 0) {
            AlertUtils.showWarning("Stok produk " + selectedProduct.getNamaBarang() + " habis!");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(jumlahField.getText());
            if (quantity <= 0) {
                AlertUtils.showWarning("Jumlah harus lebih dari 0!");
                return;
            }
        } catch (NumberFormatException e) {
            AlertUtils.showWarning("Jumlah harus berupa angka!");
            return;
        }

        if (quantity > selectedProduct.getStok()) {
            AlertUtils.showWarning("Stok tidak mencukupi!\n" +
                                 "Stok tersedia: " + selectedProduct.getStok() + "\n" +
                                 "Jumlah yang diminta: " + quantity);
            return;
        }

        // Check if item already in cart
        CartItem existingItem = null;
        for (CartItem item : cartItems) {
            if (item.getBarang().getId() == selectedProduct.getId()) {
                existingItem = item;
                break;
            }
        }

        if (existingItem != null) {
            int newQty = existingItem.getQuantity() + quantity;
            if (newQty > selectedProduct.getStok()) {
                AlertUtils.showWarning("Total quantity melebihi stok!\n" +
                                     "Stok tersedia: " + selectedProduct.getStok() + "\n" +
                                     "Sudah di keranjang: " + existingItem.getQuantity() + "\n" +
                                     "Akan ditambah: " + quantity + "\n" +
                                     "Total akan menjadi: " + newQty);
                return;
            }
            existingItem.setQuantity(newQty);
        } else {
            cartItems.add(new CartItem(selectedProduct, quantity));
            
            // 🔊 PLAY NOTIFICATION SOUND for item added!
            SoundUtils.playNotification();
            
            AlertUtils.showInfo("Produk " + selectedProduct.getNamaBarang() + " berhasil ditambahkan ke keranjang!\n" +
                               "Quantity: " + quantity + "\n" +
                               "Subtotal: Rp " + currencyFormat.format(selectedProduct.getHarga() * quantity));
        }

        cartTable.refresh();
        updateCartSummary();
        
        // Reset selection setelah berhasil tambah ke keranjang
        selectedProduct = null;
        updateSelectedProductDisplay();
        jumlahField.setText("1");
    }

    private void removeFromCart(CartItem item) {
        cartItems.remove(item);
        updateCartSummary();
        AlertUtils.showInfoSilent("Produk dihapus dari keranjang!");
    }

    private void updateCartSummary() {
        int totalItems = 0;
        double subtotal = 0;

        for (CartItem item : cartItems) {
            totalItems += item.getQuantity();
            subtotal += item.getTotal();
        }

        totalItemLabel.setText(String.valueOf(totalItems));
        subtotalLabel.setText("Rp " + currencyFormat.format(subtotal));
        totalBayarLabel.setText("Rp " + currencyFormat.format(subtotal));
    }

    @FXML
    public void handleCalculateChange() {
        if (cartItems.isEmpty()) {
            AlertUtils.showWarning("Keranjang masih kosong!");
            return;
        }

        try {
            double bayar = Double.parseDouble(bayarField.getText());
            double total = getTotalBelanja();
            
            if (bayar < total) {
                kembalianLabel.setText("Uang tidak cukup!");
                kembalianLabel.setStyle("-fx-text-fill: red;");
            } else {
                double kembalian = bayar - total;
                kembalianLabel.setText("Rp " + currencyFormat.format(kembalian));
                kembalianLabel.setStyle("-fx-text-fill: #059669;");
            }
        } catch (NumberFormatException e) {
            AlertUtils.showWarning("Masukkan jumlah pembayaran yang valid!");
        }
    }

    @FXML
    public void handlePembayaran() {
        if (cartItems.isEmpty()) {
            AlertUtils.showWarning("Keranjang masih kosong!");
            return;
        }

        if (bayarField.getText().isEmpty()) {
            AlertUtils.showWarning("Masukkan jumlah pembayaran!");
            return;
        }

        try {
            double bayar = Double.parseDouble(bayarField.getText());
            double total = getTotalBelanja();
            
            if (bayar < total) {
                SoundUtils.playError(); // 🔊 Sound untuk insufficient payment
                AlertUtils.showWarning("Uang pembayaran tidak mencukupi!");
                return;
            }

            // Process each item in cart
            for (CartItem item : cartItems) {
                Barang barang = item.getBarang();
                
        // Create transaction
        Transaksi transaksi = new Transaksi();
        transaksi.setIdBarang(barang.getId());
        transaksi.setJumlah(item.getQuantity());
        transaksi.setTotalHarga(item.getTotal());
        
        // Use current user, fallback to static if null
        User currentUser = kasirUser != null ? kasirUser : currentKasirUser;
        if (currentUser != null) {
            transaksi.setIdKasir(currentUser.getId());
        } else {
            transaksi.setIdKasir(1); // Default kasir ID
        }
        
        transaksi.setWaktu(LocalDateTime.now());
                
                // Save transaction
                transaksiController.simpanTransaksi(transaksi);
                
                // Update stock
                int newStok = barang.getStok() - item.getQuantity();
                barangController.updateStok(barang.getId(), newStok);
            }

            // Show success message
            double kembalian = bayar - total;
            
            // 🔊 PLAY SUCCESS SOUND! 
            SoundUtils.playPaymentSuccess();
            
            // Simpan informasi untuk struk PDF (SEBELUM clearCart)
            lastUangBayar = bayar;
            lastKembalian = kembalian;
            hasCompletedTransaction = true;
            // Simpan cart items untuk cetak ulang struk nanti
            lastCartItems = new ArrayList<>(cartItems);
            
            String message = "Pembayaran berhasil!\n" +
                           "Total: Rp " + currencyFormat.format(total) + "\n" +
                           "Bayar: Rp " + currencyFormat.format(bayar) + "\n" +
                           "Kembalian: Rp " + currencyFormat.format(kembalian) + "\n\n" +
                           "Klik 'Cetak Struk' untuk mencetak struk PDF!";
            
            AlertUtils.showInfo(message);
            
            // Tanya apakah ingin langsung cetak struk
            Alert printAlert = new Alert(Alert.AlertType.CONFIRMATION);
            printAlert.setTitle("Cetak Struk");
            printAlert.setHeaderText("Pembayaran Berhasil!");
            printAlert.setContentText("Apakah Anda ingin mencetak struk sekarang?\n\n" +
                                    "💰 Total: Rp " + currencyFormat.format(total) + "\n" +
                                    "💵 Bayar: Rp " + currencyFormat.format(bayar) + "\n" +
                                    "💸 Kembalian: Rp " + currencyFormat.format(kembalian) + "\n\n" +
                                    "📄 Format: HTML (bisa diprint ke PDF)");
            
            if (printAlert.showAndWait().get() == ButtonType.OK) {
                // Generate PDF immediately (sebelum clearCart) dengan lastCartItems
                Stage stage = (Stage) cartTable.getScene().getWindow();
                boolean printSuccess = ReceiptPDFUtils.generateReceiptPDF(
                    lastCartItems, kasirUser, bayar, kembalian, stage);
                
                if (printSuccess) {
                    AlertUtils.showInfo("Struk berhasil disimpan!\n\n" +
                                       "🖨️ Cara print ke PDF:\n" +
                                       "1. Buka file HTML dengan browser\n" +
                                       "2. Klik Print (Ctrl+P)\n" +
                                       "3. Pilih 'Save as PDF' atau printer PDF");
                }
                
                // Reset form setelah cetak struk
                clearCart();
                loadBarangData();
                
                // Reset transaction state after printing
                hasCompletedTransaction = false;
                lastUangBayar = 0;
                lastKembalian = 0;
            } else {
                // Jika user tidak mau cetak sekarang, reset form tapi simpan transaction state
                clearCart();
                loadBarangData();
            }

        } catch (NumberFormatException e) {
            AlertUtils.showWarning("Masukkan jumlah pembayaran yang valid!");
        }
    }

    @FXML
    public void handleBatal() {
        if (cartItems.isEmpty()) {
            AlertUtils.showWarning("Keranjang sudah kosong!");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi");
        alert.setHeaderText("Batalkan Transaksi");
        alert.setContentText("Apakah Anda yakin ingin membatalkan transaksi ini?");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            clearCart();
            AlertUtils.showInfoSilent("Transaksi dibatalkan!");
        }
    }

    @FXML
    public void handleCetakStruk() {
        if (!hasCompletedTransaction) {
            AlertUtils.showWarning("Belum ada transaksi yang selesai untuk dicetak!\n\n" +
                                 "Silakan lakukan pembayaran terlebih dahulu.");
            return;
        }
        
        if (lastCartItems.isEmpty()) {
            AlertUtils.showWarning("Data transaksi tidak ditemukan!\n\n" +
                                 "Silakan lakukan transaksi baru.");
            return;
        }
        
        try {
            // Generate PDF receipt dengan lastCartItems yang tersimpan
            Stage stage = (Stage) cartTable.getScene().getWindow();
            boolean success = ReceiptPDFUtils.generateReceiptPDF(
                lastCartItems, kasirUser, lastUangBayar, lastKembalian, stage);
            
            if (success) {
                AlertUtils.showInfo("Struk berhasil disimpan!\n\n" +
                                   "📄 Format: HTML (dapat diprint ke PDF melalui browser)\n" +
                                   "🖨️ Cara print ke PDF:\n" +
                                   "   1. Buka file HTML dengan browser\n" +
                                   "   2. Klik Print (Ctrl+P)\n" +
                                   "   3. Pilih 'Save as PDF' atau 'Microsoft Print to PDF'\n\n" +
                                   "💰 Total: Rp " + currencyFormat.format(getTotalFromLastCart()) + "\n" +
                                   "💵 Bayar: Rp " + currencyFormat.format(lastUangBayar) + "\n" +
                                   "💸 Kembalian: Rp " + currencyFormat.format(lastKembalian));
                
                // Reset transaction state after printing
                hasCompletedTransaction = false;
                lastUangBayar = 0;
                lastKembalian = 0;
            } else {
                AlertUtils.showInfo("Pembuatan struk dibatalkan atau gagal!");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Gagal membuat struk PDF: " + e.getMessage());
        }
    }

    private void clearCart() {
        cartItems.clear();
        bayarField.clear();
        kembalianLabel.setText("Rp 0");
        updateCartSummary();
        
        // Clear product selection juga
        selectedProduct = null;
        updateSelectedProductDisplay();
        jumlahField.setText("1");
        
        // Reset transaction state
        hasCompletedTransaction = false;
        lastUangBayar = 0;
        lastKembalian = 0;
    }

    private double getTotalBelanja() {
        return cartItems.stream().mapToDouble(CartItem::getTotal).sum();
    }
    
    // Helper method untuk hitung total dari lastCartItems
    private double getTotalFromLastCart() {
        return lastCartItems.stream().mapToDouble(CartItem::getTotal).sum();
    }

    @FXML
    public void handleSearch() {
        String searchText = searchField.getText();
        filterProducts(searchText);
    }

    @FXML
    public void handleRefresh() {
        searchField.clear();
        loadBarangData();
        AlertUtils.showInfo("Data produk telah diperbarui!");
    }

    private void selectProduct(Barang barang) {
        selectedProduct = barang;
        updateSelectedProductDisplay();
        
        // Reset quantity ke 1 ketika pilih produk baru
        jumlahField.setText("1");
        
        AlertUtils.showInfoSilent("Produk " + barang.getNamaBarang() + " dipilih!\n" +
                           "Stok tersedia: " + barang.getStok() + "\n" +
                           "Harga: Rp " + currencyFormat.format(barang.getHarga()) + "\n\n" +
                           "Atur jumlah dan klik 'TAMBAH KE KERANJANG'");
    }
    
    private void updateSelectedProductDisplay() {
        if (selectedProductLabel != null) {
            if (selectedProduct != null) {
                selectedProductLabel.setText("📦 " + selectedProduct.getNamaBarang() + 
                                           " (Stok: " + selectedProduct.getStok() + 
                                           " | Rp " + currencyFormat.format(selectedProduct.getHarga()) + ")");
                selectedProductLabel.setStyle("-fx-text-fill: #059669; -fx-font-weight: bold;");
            } else {
                selectedProductLabel.setText("Belum ada produk yang dipilih");
                selectedProductLabel.setStyle("-fx-text-fill: #64748b;");
            }
        }
    }

    @FXML 
    public void handleClearSelection() {
        selectedProduct = null;
        updateSelectedProductDisplay();
        jumlahField.setText("1");
        AlertUtils.showInfoSilent("Pilihan produk dibatalkan!");
    }
}

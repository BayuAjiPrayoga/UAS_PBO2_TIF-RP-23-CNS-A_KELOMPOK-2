package controllerui;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import config.DatabaseConnection;
import controller.BarangController;
import controller.TransaksiController;
import controller.UserController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Barang;
import model.TransaksiRiwayat;
import model.User;
import utils.AlertUtils;
import utils.DatabaseSetup;
import utils.ExportUtils;
import utils.PrintUtils;

public class AdminController {

    // Dashboard Stats
    @FXML private Label adminNameLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Label totalProdukLabel;
    @FXML private Label totalTransaksiLabel;
    @FXML private Label pendapatanHariIniLabel;
    @FXML private Label stokMenipisLabel;

    // Barang Management
    @FXML private TableView<Barang> barangTable;
    @FXML private TableColumn<Barang, String> colKode;
    @FXML private TableColumn<Barang, String> colNama;
    @FXML private TableColumn<Barang, Integer> colStok;
    @FXML private TableColumn<Barang, Double> colHarga;
    @FXML private TableColumn<Barang, String> colStatus;
    @FXML private TableColumn<Barang, Void> colAction;
    @FXML private TextField searchBarangField;

    // Transaksi Management
    @FXML private TableView<TransaksiRiwayat> transaksiTable;
    @FXML private TableColumn<TransaksiRiwayat, String> colTransaksiId;
    @FXML private TableColumn<TransaksiRiwayat, String> colBarang;
    @FXML private TableColumn<TransaksiRiwayat, String> colKasir;
    @FXML private TableColumn<TransaksiRiwayat, Integer> colJumlah;
    @FXML private TableColumn<TransaksiRiwayat, String> colTotal;
    @FXML private TableColumn<TransaksiRiwayat, String> colWaktu;
    @FXML private TableColumn<TransaksiRiwayat, Void> colStruk;
    @FXML private DatePicker dateFilterPicker;
    @FXML private ComboBox<String> kasirFilterCombo;
    @FXML private Label totalTransaksiHariIni;
    @FXML private Label totalPendapatanHariIni;

    // Dashboard Aktivitas
    @FXML private TableView<TransaksiRiwayat> aktivitasTable;
    @FXML private TableColumn<TransaksiRiwayat, String> colAktivitasWaktu;
    @FXML private TableColumn<TransaksiRiwayat, String> colAktivitasKasir;
    @FXML private TableColumn<TransaksiRiwayat, String> colAktivitasBarang;
    @FXML private TableColumn<TransaksiRiwayat, Integer> colAktivitasJumlah;
    @FXML private TableColumn<TransaksiRiwayat, String> colAktivitasTotal;

    // User Management
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colUserId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colUserRole;
    @FXML private TableColumn<User, String> colUserStatus;
    @FXML private TableColumn<User, String> colUserCreated;
    @FXML private TableColumn<User, Void> colUserAction;
    @FXML private Label totalUserLabel;

    private BarangController barangController = new BarangController();
    private TransaksiController transaksiController = new TransaksiController();
    private UserController userController = new UserController();
    private DecimalFormat currencyFormat = new DecimalFormat("#,###");
    private Timer dateTimeTimer;
    private User currentAdmin;
    public void initialize() {
        // Setup database if needed
        DatabaseSetup.addIsActiveColumn();
        
        setupTables();
        loadInitialData();
        startDateTimeUpdate();
        
        // Setup search functionality
        if (searchBarangField != null) {
            searchBarangField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterBarang(newValue);
            });
        }
        
        // Initialize filters
        if (kasirFilterCombo != null) {
            loadKasirFilter();
        }
        
        // Set default admin name
        if (adminNameLabel != null) {
            adminNameLabel.setText("Admin: Administrator");
        }
    }
    
    private void setupTables() {
        setupBarangTable();
        setupTransaksiTable();
        setupAktivitasTable();
        setupUserTable();
    }
    
    private void setupBarangTable() {
        if (barangTable != null) {
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
            
            // Status column
            colStatus.setCellValueFactory(data -> {
                int stok = data.getValue().getStok();
                String status = stok > 10 ? "Tersedia" : stok > 0 ? "Menipis" : "Habis";
                return new javafx.beans.property.SimpleStringProperty(status);
            });
            
            colStatus.setCellFactory(column -> new TableCell<Barang, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        if ("Habis".equals(item)) {
                            setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                        } else if ("Menipis".equals(item)) {
                            setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #059669; -fx-font-weight: bold;");
                        }
                    }
                }
            });
            
            // Action buttons
            colAction.setCellFactory(column -> new TableCell<Barang, Void>() {
                private final Button editButton = new Button("Edit");
                private final Button deleteButton = new Button("Hapus");
                
                {
                    editButton.setOnAction(event -> {
                        Barang barang = getTableView().getItems().get(getIndex());
                        openBarangForm(barang);
                    });
                    deleteButton.setOnAction(event -> {
                        Barang barang = getTableView().getItems().get(getIndex());
                        handleHapusBarang(barang);
                    });
                    editButton.getStyleClass().add("btn-edit");
                    deleteButton.getStyleClass().add("btn-delete");
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(5);
                        box.getChildren().addAll(editButton, deleteButton);
                        setGraphic(box);
                    }
                }
            });
        }
    }
    
    private void setupTransaksiTable() {
        if (transaksiTable != null) {
            colTransaksiId.setCellValueFactory(data -> {
                int index = transaksiTable.getItems().indexOf(data.getValue()) + 1;
                return new javafx.beans.property.SimpleStringProperty(String.valueOf(index));
            });
            colBarang.setCellValueFactory(data -> 
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNamaBarang()));
            colKasir.setCellValueFactory(data -> 
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNamaKasir()));
            colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
            colTotal.setCellValueFactory(data -> 
                new javafx.beans.property.SimpleStringProperty("Rp " + currencyFormat.format(data.getValue().getTotalHarga())));
            colWaktu.setCellValueFactory(data -> 
                new javafx.beans.property.SimpleStringProperty(data.getValue().getWaktu().toString()));
            
            // Struk button
            colStruk.setCellFactory(column -> new TableCell<TransaksiRiwayat, Void>() {
                private final Button strukButton = new Button("Cetak");
                
                {
                    strukButton.setOnAction(event -> {
                        TransaksiRiwayat transaksi = getTableView().getItems().get(getIndex());
                        handleCetakStruk(transaksi);
                    });
                    strukButton.getStyleClass().add("btn-print");
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(strukButton);
                    }
                }
            });
        }
    }
    
    private void setupAktivitasTable() {
        if (aktivitasTable != null) {
            colAktivitasWaktu.setCellValueFactory(data -> {
                String waktuString = data.getValue().getWaktu().toString();
                if (waktuString.length() > 16) {
                    waktuString = waktuString.substring(0, 16);
                }
                return new javafx.beans.property.SimpleStringProperty(waktuString);
            });
            colAktivitasKasir.setCellValueFactory(data -> 
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNamaKasir()));
            colAktivitasBarang.setCellValueFactory(data -> 
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNamaBarang()));
            colAktivitasJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
            colAktivitasTotal.setCellValueFactory(data -> 
                new javafx.beans.property.SimpleStringProperty("Rp " + currencyFormat.format(data.getValue().getTotalHarga())));
        }
    }
    
    private void setupUserTable() {
        if (userTable != null) {
            colUserId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
            colUserRole.setCellValueFactory(data -> 
                new javafx.beans.property.SimpleStringProperty(data.getValue().getRole()));
            colUserStatus.setCellValueFactory(data -> 
                new javafx.beans.property.SimpleStringProperty(
                    data.getValue().isActive() ? "Aktif" : "Non-Aktif"));
            colUserCreated.setCellValueFactory(data -> 
                new javafx.beans.property.SimpleStringProperty("01/01/2024"));
            
            // User action buttons
            colUserAction.setCellFactory(column -> new TableCell<User, Void>() {
                private final Button editButton = new Button("Edit");
                private final Button toggleButton = new Button("Toggle");
                private final Button deleteButton = new Button("Delete");
                
                {
                    editButton.setOnAction(event -> {
                        User user = getTableView().getItems().get(getIndex());
                        handleEditUser(user);
                    });
                    toggleButton.setOnAction(event -> {
                        User user = getTableView().getItems().get(getIndex());
                        handleToggleUser(user);
                    });
                    deleteButton.setOnAction(event -> {
                        User user = getTableView().getItems().get(getIndex());
                        handleDeleteUser(user);
                    });
                    editButton.getStyleClass().add("btn-edit");
                    deleteButton.getStyleClass().add("btn-delete");
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        User user = getTableView().getItems().get(getIndex());
                        javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(5);
                        
                        // Ubah text dan style tombol toggle berdasarkan status user
                        if (user.isActive()) {
                            toggleButton.setText("Nonaktifkan");
                            toggleButton.getStyleClass().clear();
                            toggleButton.getStyleClass().addAll("btn-delete");
                        } else {
                            toggleButton.setText("Aktifkan");
                            toggleButton.getStyleClass().clear();
                            toggleButton.getStyleClass().addAll("btn-add-product");
                        }
                        
                        // Set style untuk delete button
                        deleteButton.setText("Delete");
                        deleteButton.getStyleClass().clear();
                        deleteButton.getStyleClass().addAll("btn-delete");
                        deleteButton.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white;");
                        
                        box.getChildren().addAll(editButton, toggleButton, deleteButton);
                        setGraphic(box);
                    }
                }
            });
        }
    }

    private void loadInitialData() {
        loadBarangData();
        loadRiwayatTransaksi();
        loadDashboardStats();
        loadRecentActivity();
        loadUserData();
    }
    
    private void startDateTimeUpdate() {
        dateTimeTimer = new Timer();
        dateTimeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (dateTimeLabel != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        dateTimeLabel.setText(sdf.format(new Date()));
                    }
                });
            }
        }, 0, 1000);
    }
    
    private void loadBarangData() {
        if (barangTable != null) {
            List<Barang> barangList = barangController.getAllBarang();
            ObservableList<Barang> data = FXCollections.observableArrayList(barangList);
            barangTable.setItems(data);
        }
    }
    
    @FXML
    public void loadRiwayatTransaksi() {
        if (transaksiTable != null) {
            List<TransaksiRiwayat> list = transaksiController.getRiwayatTransaksiLengkap();
            ObservableList<TransaksiRiwayat> data = FXCollections.observableArrayList(list);
            transaksiTable.setItems(data);
            
            // Update summary
            updateTransaksiSummary(list);
        }
    }
    
    private void loadDashboardStats() {
        try {
            List<Barang> allBarang = barangController.getAllBarang();
            List<TransaksiRiwayat> allTransaksi = transaksiController.getRiwayatTransaksiLengkap();
            
            // Total produk
            if (totalProdukLabel != null) {
                totalProdukLabel.setText(String.valueOf(allBarang.size()));
            }
            
            // Total transaksi
            if (totalTransaksiLabel != null) {
                totalTransaksiLabel.setText(String.valueOf(allTransaksi.size()));
            }
            
            // Pendapatan hari ini
            double pendapatanHariIni = calculatePendapatanHariIni(allTransaksi);
            if (pendapatanHariIniLabel != null) {
                pendapatanHariIniLabel.setText("Rp " + currencyFormat.format(pendapatanHariIni));
            }
            
            // Stok menipis
            long stokMenipis = allBarang.stream().filter(b -> b.getStok() <= 10 && b.getStok() > 0).count();
            if (stokMenipisLabel != null) {
                stokMenipisLabel.setText(String.valueOf(stokMenipis));
            }
            
        } catch (Exception e) {
            System.err.println("Error loading dashboard stats: " + e.getMessage());
        }
    }
    
    private void loadRecentActivity() {
        if (aktivitasTable != null) {
            List<TransaksiRiwayat> recentActivity = transaksiController.getRiwayatTransaksiLengkap();
            // Ambil 10 transaksi terakhir
            List<TransaksiRiwayat> limited = recentActivity.stream()
                .limit(10)
                .collect(java.util.stream.Collectors.toList());
            ObservableList<TransaksiRiwayat> data = FXCollections.observableArrayList(limited);
            aktivitasTable.setItems(data);
        }
    }
    
    private void loadUserData() {
        if (userTable != null) {
            try {
                UserController userController = new UserController();
                // Tampilkan semua user termasuk yang non-aktif untuk admin
                List<User> users = userController.getAllUsersIncludingInactive();
                
                ObservableList<User> userData = FXCollections.observableArrayList(users);
                userTable.setItems(userData);
                
                // Update total user label
                if (totalUserLabel != null) {
                    totalUserLabel.setText("Total User: " + users.size());
                }
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtils.showError("Gagal memuat data user!");
                if (totalUserLabel != null) {
                    totalUserLabel.setText("Total User: 0");
                }
            }
        }
    }
    
    private void loadKasirFilter() {
        try {
            ObservableList<String> kasirNames = FXCollections.observableArrayList();
            kasirNames.add("Semua Kasir");
            
            // Ambil daftar kasir unik dari riwayat transaksi
            List<TransaksiRiwayat> allTransaksi = transaksiController.getRiwayatTransaksiLengkap();
            Set<String> uniqueKasir = allTransaksi.stream()
                .map(TransaksiRiwayat::getNamaKasir)
                .filter(kasir -> kasir != null && !kasir.trim().isEmpty())
                .collect(java.util.stream.Collectors.toSet());
            
            // Urutkan dan tambahkan ke list
            uniqueKasir.stream()
                .sorted()
                .forEach(kasirNames::add);
            
            if (kasirFilterCombo != null) {
                kasirFilterCombo.setItems(kasirNames);
                kasirFilterCombo.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            System.err.println("Error loading kasir filter: " + e.getMessage());
            // Fallback ke data dummy jika ada error
            ObservableList<String> kasirNames = FXCollections.observableArrayList();
            kasirNames.add("Semua Kasir");
            kasirNames.add("RIO");
            kasirNames.add("kasir");
            kasirNames.add("kasir01");
            kasirNames.add("kasir02");
            
            if (kasirFilterCombo != null) {
                kasirFilterCombo.setItems(kasirNames);
                kasirFilterCombo.getSelectionModel().selectFirst();
            }
        }
    }
    
    private double calculatePendapatanHariIni(List<TransaksiRiwayat> transaksi) {
        LocalDate today = LocalDate.now();
        return transaksi.stream()
            .filter(t -> t.getWaktu().toLocalDate().equals(today))
            .mapToDouble(TransaksiRiwayat::getTotalHarga)
            .sum();
    }
    
    private void updateTransaksiSummary(List<TransaksiRiwayat> transaksi) {
        LocalDate today = LocalDate.now();
        List<TransaksiRiwayat> transaksiHariIni = transaksi.stream()
            .filter(t -> t.getWaktu().toLocalDate().equals(today))
            .collect(java.util.stream.Collectors.toList());
        
        if (totalTransaksiHariIni != null) {
            totalTransaksiHariIni.setText(String.valueOf(transaksiHariIni.size()));
        }
        
        double totalPendapatan = transaksiHariIni.stream()
            .mapToDouble(TransaksiRiwayat::getTotalHarga)
            .sum();
        
        if (totalPendapatanHariIni != null) {
            totalPendapatanHariIni.setText("Rp " + currencyFormat.format(totalPendapatan));
        }
    }
    
    private void filterBarang(String searchText) {
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
    
    // Event Handlers
    @FXML
    public void handleTambah() {
        openBarangForm(null);
    }
    
    @FXML
    public void handleRefreshBarang() {
        loadBarangData();
        if (searchBarangField != null) {
            searchBarangField.clear();
        }
        AlertUtils.showInfo("Data produk telah diperbarui!");
    }
    
    @FXML
    public void handleFilterTransaksi() {
        try {
            String selectedKasir = kasirFilterCombo.getSelectionModel().getSelectedItem();
            LocalDate selectedDate = dateFilterPicker != null ? dateFilterPicker.getValue() : null;
            
            List<TransaksiRiwayat> allTransaksi = transaksiController.getRiwayatTransaksiLengkap();
            List<TransaksiRiwayat> filteredTransaksi = allTransaksi.stream()
                .filter(transaksi -> {
                    boolean kasirMatch = selectedKasir == null || 
                                       "Semua Kasir".equals(selectedKasir) || 
                                       selectedKasir.equals(transaksi.getNamaKasir());
                    
                    boolean dateMatch = selectedDate == null || 
                                       transaksi.getWaktu().toLocalDate().equals(selectedDate);
                    
                    return kasirMatch && dateMatch;
                })
                .collect(java.util.stream.Collectors.toList());
            
            // Update table dengan data yang sudah difilter
            ObservableList<TransaksiRiwayat> data = FXCollections.observableArrayList(filteredTransaksi);
            transaksiTable.setItems(data);
            
            // Update summary dengan data yang sudah difilter
            updateTransaksiSummary(filteredTransaksi);
            
            AlertUtils.showInfo("Filter diterapkan! Menampilkan " + filteredTransaksi.size() + " transaksi.");
        } catch (Exception e) {
            System.err.println("Error filtering transactions: " + e.getMessage());
            AlertUtils.showError("Gagal menerapkan filter: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleExport() {
        exportBarangData();
    }
    
    @FXML
    public void handleLaporanStok() {
        openLaporanStokForm();
    }
    
    @FXML
    public void handlePrintStok() {
        printLaporanStok();
    }
    
    @FXML
    public void handleExportTransaksi() {
        exportTransaksiData();
    }
    
    @FXML
    public void handleTambahUser() {
        openUserForm(null);
    }
    
    @FXML
    public void handleGotoDashboard() {
        // Switch to dashboard tab (Tab index 0)
        AlertUtils.showInfo("Anda sudah berada di dashboard admin!");
    }
    
    @FXML
    public void handlePengaturan() {
        AlertUtils.showInfo("Fitur pengaturan akan segera tersedia!");
    }
    
    private void handleHapusBarang(Barang barang) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText("Hapus Produk");
        alert.setContentText("Apakah Anda yakin ingin menghapus produk: " + barang.getNamaBarang() + "?");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            if (barangController.deleteBarang(barang.getId())) {
                loadBarangData();
                loadDashboardStats();
                AlertUtils.showInfo("Produk berhasil dihapus!");
            } else {
                AlertUtils.showError("Gagal menghapus produk!\n\n" +
                    "Produk ini tidak dapat dihapus karena masih memiliki riwayat transaksi.\n" +
                    "Tip: Gunakan tombol 'HAPUS TRANSAKSI' di tab TRANSAKSI untuk menghapus riwayat terlebih dahulu.");
            }
        }
    }
    
    private void handleCetakStruk(TransaksiRiwayat transaksi) {
        AlertUtils.showInfo("Struk untuk transaksi dicetak!");
    }
    
    private void handleEditUser(User user) {
        openUserForm(user);
    }
    
    private void handleToggleUser(User user) {
        UserController userController = new UserController();
        
        if (user.isActive()) {
            // Konfirmasi soft delete
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Konfirmasi Nonaktifkan");
            alert.setHeaderText("Nonaktifkan User");
            alert.setContentText("Apakah Anda yakin ingin menonaktifkan user: " + user.getUsername() + "?");
            
            if (alert.showAndWait().get() == ButtonType.OK) {
                if (userController.deleteUser(user.getId())) {
                    AlertUtils.showInfo("User " + user.getUsername() + " berhasil dinonaktifkan!");
                    loadUserData();
                    userTable.refresh(); // Force refresh the table
                } else {
                    AlertUtils.showError("Gagal menonaktifkan user!");
                }
            }
        } else {
            // Konfirmasi restore
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Konfirmasi Restore");
            alert.setHeaderText("Aktifkan Kembali User");
            alert.setContentText("Apakah Anda yakin ingin mengaktifkan kembali user: " + user.getUsername() + "?");
            
            if (alert.showAndWait().get() == ButtonType.OK) {
                if (userController.restoreUser(user.getId())) {
                    AlertUtils.showInfo("User " + user.getUsername() + " berhasil diaktifkan kembali!");
                    loadUserData();
                    userTable.refresh(); // Force refresh the table
                } else {
                    AlertUtils.showError("Gagal mengaktifkan user!");
                }
            }
        }
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

    private void openBarangForm(Barang barang) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BarangForm.fxml"));
            AnchorPane formPane = loader.load();

            BarangFormController controller = loader.getController();
            controller.setBarang(barang);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(formPane));
            stage.setTitle(barang == null ? "Tambah Barang" : "Edit Barang");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openUserForm(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/UserForm.fxml"));
            AnchorPane formPane = loader.load();

            UserFormController controller = loader.getController();
            controller.setUser(user);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(formPane));
            stage.setTitle(user == null ? "Tambah User" : "Edit User");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openLaporanStokForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LaporanStokForm.fxml"));
            AnchorPane formPane = loader.load();

            LaporanStokFormController controller = loader.getController();

            Stage stage = new Stage();
            stage.setScene(new Scene(formPane, 1200, 800));
            stage.setTitle("📊 Laporan Stok Produk - KASIRIN");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Gagal membuka form laporan stok!");
        }
    }

    public void refreshTable() {
        loadBarangData();
        loadDashboardStats();
    }

    public void refreshUserTable() {
        loadUserData();
    }

    public void setAdminUser(User user) {
        if (adminNameLabel != null) {
            adminNameLabel.setText("Admin: " + user.getUsername());
        }
    }

    // Export Methods
    private void exportBarangData() {
        try {
            List<Barang> barangList = barangController.getAllBarang();
            
            if (barangList.isEmpty()) {
                AlertUtils.showInfo("Tidak ada data produk untuk diekspor!");
                return;
            }
            
            Stage stage = (Stage) barangTable.getScene().getWindow();
            boolean success = ExportUtils.exportBarangToCSV(barangList, stage);
            
            if (success) {
                AlertUtils.showInfo("Data produk berhasil diekspor ke CSV!");
            } else {
                AlertUtils.showInfo("Export dibatalkan atau gagal!");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Gagal mengekspor data produk: " + e.getMessage());
        }
    }
    
    private void exportTransaksiData() {
        try {
            List<TransaksiRiwayat> transaksiList = transaksiController.getRiwayatTransaksiLengkap();
            
            if (transaksiList.isEmpty()) {
                AlertUtils.showInfo("Tidak ada data transaksi untuk diekspor!");
                return;
            }
            
            Stage stage = (Stage) transaksiTable.getScene().getWindow();
            boolean success = ExportUtils.exportTransaksiToCSV(transaksiList, stage);
            
            if (success) {
                AlertUtils.showInfo("Data transaksi berhasil diekspor ke CSV!");
            } else {
                AlertUtils.showInfo("Export dibatalkan atau gagal!");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Gagal mengekspor data transaksi: " + e.getMessage());
        }
    }
    
    public void exportLaporanStok() {
        try {
            List<Barang> barangList = barangController.getAllBarang();
            
            if (barangList.isEmpty()) {
                AlertUtils.showInfo("Tidak ada data untuk laporan stok!");
                return;
            }
            
            Stage stage = (Stage) barangTable.getScene().getWindow();
            boolean success = ExportUtils.exportLaporanStokToCSV(barangList, stage);
            
            if (success) {
                AlertUtils.showInfo("Laporan stok berhasil diekspor ke CSV!");
            } else {
                AlertUtils.showInfo("Export dibatalkan atau gagal!");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Gagal mengekspor laporan stok: " + e.getMessage());
        }
    }
    
    public void printLaporanStok() {
        try {
            List<Barang> barangList = barangController.getAllBarang();
            
            if (barangList.isEmpty()) {
                AlertUtils.showInfo("Tidak ada data untuk dicetak!");
                return;
            }
            
            // Konfirmasi print
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Konfirmasi Print");
            confirmAlert.setHeaderText("Print Laporan Stok");
            confirmAlert.setContentText("Anda akan mencetak laporan stok dengan " + barangList.size() + " produk.\n\n" +
                                       "Pastikan printer sudah siap dan terpasang.\n" +
                                       "Lanjutkan dengan pencetakan?");
            
            if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                boolean success = PrintUtils.printLaporanStok(barangList);
                
                if (success) {
                    AlertUtils.showInfo("Laporan stok berhasil dicetak!\n\n" +
                                       "Laporan professional dengan:\n" +
                                       "- " + barangList.size() + " produk\n" +
                                       "- Statistik dan analisis lengkap\n" +
                                       "- Format siap presentasi");
                } else {
                    AlertUtils.showInfo("Pencetakan dibatalkan atau gagal!");
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Gagal mencetak laporan stok: " + e.getMessage());
        }
    }

    @FXML
    public void handleRefreshTransaksi() {
        try {
            // Reset filter selections
            if (kasirFilterCombo != null) {
                kasirFilterCombo.getSelectionModel().selectFirst(); // Select "Semua Kasir"
            }
            if (dateFilterPicker != null) {
                dateFilterPicker.setValue(null); // Clear date filter
            }
            
            // Reload all transaction data
            loadRiwayatTransaksi();
            
            AlertUtils.showInfo("Data transaksi telah diperbarui dan filter direset!");
        } catch (Exception e) {
            System.err.println("Error refreshing transactions: " + e.getMessage());
            AlertUtils.showError("Gagal memperbarui data transaksi: " + e.getMessage());
        }
    }

    public void refreshAllData() {
        loadBarangData();
        loadRiwayatTransaksi();
        loadUserData();
        loadDashboardStats();
        
        // Refresh filter if needed
        if (kasirFilterCombo != null) {
            loadKasirFilter();
        }
    }

    @FXML
    public void handleDeleteTransactions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/DeleteTransactionForm.fxml"));
            AnchorPane deleteTransactionPane = loader.load();
            
            DeleteTransactionController controller = loader.getController();
            controller.setParentController(this);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Hapus Transaksi");
            dialogStage.setScene(new Scene(deleteTransactionPane));
            dialogStage.setResizable(false);
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialogStage.initOwner(userTable.getScene().getWindow());
            
            dialogStage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Gagal membuka form hapus transaksi!");
        }
    }

    private void handleDeleteUser(User user) {
        UserController userController = new UserController();
        
        // Cek apakah user memiliki transaksi
        if (userController.hasTransactions(user.getId())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Tidak Dapat Menghapus");
            alert.setHeaderText("User Memiliki Riwayat Transaksi");
            alert.setContentText("User '" + user.getUsername() + "' tidak dapat dihapus secara permanen karena memiliki riwayat transaksi.\n\n" +
                               "Silakan hapus riwayat transaksi terlebih dahulu di tab 'TRANSAKSI' atau gunakan tombol 'Nonaktifkan' untuk menonaktifkan user.");
            alert.showAndWait();
            return;
        }
        
        // Konfirmasi hard delete
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus Permanen");
        alert.setHeaderText("Hapus User Secara Permanen");
        alert.setContentText("⚠️ PERINGATAN: Aksi ini akan menghapus user '" + user.getUsername() + "' secara PERMANEN!\n\n" +
                           "Data user akan hilang selamanya dan tidak dapat dikembalikan.\n\n" +
                           "Apakah Anda yakin ingin melanjutkan?");
        
        ButtonType deleteButtonType = new ButtonType("Hapus Permanen", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(deleteButtonType, cancelButtonType);
        
        if (alert.showAndWait().get() == deleteButtonType) {
            if (hardDeleteUser(user.getId())) {
                AlertUtils.showInfo("User '" + user.getUsername() + "' berhasil dihapus secara permanen!");
                loadUserData();
            } else {
                AlertUtils.showError("Gagal menghapus user secara permanen!");
            }
        }
    }
    
    private boolean hardDeleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

package controllerui;

import controller.BarangController;
import controller.BarangController.StokStatistik;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Barang;
import utils.AlertUtils;
import utils.ExportUtils;
import utils.PrintUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class LaporanStokFormController {

    // Filter Controls
    @FXML private ComboBox<String> statusComboBox;
    @FXML private ComboBox<String> kategoriComboBox;
    @FXML private TextField searchField;
    @FXML private Button filterButton;
    @FXML private Button resetButton;

    // Statistics Labels
    @FXML private Label totalProdukLabel;
    @FXML private Label stokMenipisLabel;
    @FXML private Label stokHabisLabel;
    @FXML private Label nilaiStokLabel;

    // Table
    @FXML private TableView<Barang> stokTable;
    @FXML private TableColumn<Barang, String> colKode;
    @FXML private TableColumn<Barang, String> colNama;
    @FXML private TableColumn<Barang, Integer> colStokTersedia;
    @FXML private TableColumn<Barang, String> colHargaSatuan;
    @FXML private TableColumn<Barang, String> colNilaiStok;
    @FXML private TableColumn<Barang, String> colStatus;
    @FXML private TableColumn<Barang, String> colTerakhirUpdate;
    @FXML private TableColumn<Barang, Void> colAction;

    // Footer
    @FXML private Label totalRecordsLabel;
    @FXML private Label lastUpdateLabel;

    // Export/Print buttons
    @FXML private Button exportButton;
    @FXML private Button printButton;

    private BarangController barangController = new BarangController();
    private DecimalFormat currencyFormat = new DecimalFormat("#,###");
    private ObservableList<Barang> allBarangData = FXCollections.observableArrayList();
    private ObservableList<Barang> filteredBarangData = FXCollections.observableArrayList();

    public void initialize() {
        setupComboBoxes();
        setupTable();
        setupSearchListener();
        loadData();
        updateLastUpdateTime();
    }

    private void setupComboBoxes() {
        // Setup status filter
        statusComboBox.setItems(FXCollections.observableArrayList(
            "Semua Status", "Stok Aman", "Stok Menipis", "Stok Habis"
        ));
        statusComboBox.setValue("Semua Status");

        // Setup kategori filter (bisa diperluas sesuai kebutuhan)
        kategoriComboBox.setItems(FXCollections.observableArrayList(
            "Semua Kategori", "Makanan", "Minuman", "Kebutuhan Rumah", "Perawatan"
        ));
        kategoriComboBox.setValue("Semua Kategori");
    }

    private void setupTable() {
        // Setup columns
        colKode.setCellValueFactory(data -> 
            new SimpleStringProperty("BRG" + String.format("%03d", data.getValue().getId())));
        
        colNama.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getNamaBarang()));
        
        colStokTersedia.setCellValueFactory(new PropertyValueFactory<>("stok"));
        
        colHargaSatuan.setCellValueFactory(data -> 
            new SimpleStringProperty("Rp " + currencyFormat.format(data.getValue().getHarga())));
        
        colNilaiStok.setCellValueFactory(data -> {
            double nilaiStok = data.getValue().getStok() * data.getValue().getHarga();
            return new SimpleStringProperty("Rp " + currencyFormat.format(nilaiStok));
        });
        
        colStatus.setCellValueFactory(data -> 
            new SimpleStringProperty(getStatusStok(data.getValue().getStok())));
        
        colTerakhirUpdate.setCellValueFactory(data -> 
            new SimpleStringProperty(getCurrentDateTime()));

        // Setup action column
        colAction.setCellFactory(column -> new TableCell<Barang, Void>() {
            private final Button detailButton = new Button("Detail");
            
            {
                detailButton.setOnAction(event -> {
                    Barang barang = getTableView().getItems().get(getIndex());
                    handleDetailBarang(barang);
                });
                detailButton.getStyleClass().add("btn-info");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(detailButton);
                }
            }
        });

        // Setup status column styling
        colStatus.setCellFactory(column -> new TableCell<Barang, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "Stok Habis":
                            setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                            break;
                        case "Stok Menipis":
                            setStyle("-fx-text-fill: #fd7e14; -fx-font-weight: bold;");
                            break;
                        case "Stok Aman":
                            setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterData();
        });
    }

    private void loadData() {
        try {
            List<Barang> barangList = barangController.getAllBarang();
            allBarangData.clear();
            allBarangData.addAll(barangList);
            filterData();
            updateStatistics();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Gagal memuat data barang!");
        }
    }

    @FXML
    private void handleFilter() {
        filterData();
        updateStatistics();
        AlertUtils.showInfo("Filter berhasil diterapkan!");
    }

    @FXML
    private void handleReset() {
        statusComboBox.setValue("Semua Status");
        kategoriComboBox.setValue("Semua Kategori");
        searchField.clear();
        filterData();
        updateStatistics();
        AlertUtils.showInfo("Filter berhasil direset!");
    }

    private void filterData() {
        filteredBarangData.clear();
        
        String statusFilter = statusComboBox.getValue();
        String kategoriFilter = kategoriComboBox.getValue();
        String searchText = searchField.getText().toLowerCase().trim();

        for (Barang barang : allBarangData) {
            boolean matches = true;

            // Filter by search text
            if (!searchText.isEmpty()) {
                matches = barang.getNamaBarang().toLowerCase().contains(searchText);
            }

            // Filter by status
            if (matches && !"Semua Status".equals(statusFilter)) {
                String barangStatus = getStatusStok(barang.getStok());
                matches = barangStatus.equals(statusFilter);
            }

            // Filter by kategori (implementasi sederhana berdasarkan nama)
            if (matches && !"Semua Kategori".equals(kategoriFilter)) {
                matches = getKategoriFromName(barang.getNamaBarang()).equals(kategoriFilter);
            }

            if (matches) {
                filteredBarangData.add(barang);
            }
        }

        stokTable.setItems(filteredBarangData);
        updateFooterInfo();
    }

    private String getStatusStok(int stok) {
        if (stok == 0) {
            return "Stok Habis";
        } else if (stok <= 10) {
            return "Stok Menipis";
        } else {
            return "Stok Aman";
        }
    }

    private String getKategoriFromName(String namaBarang) {
        String nama = namaBarang.toLowerCase();
        if (nama.contains("beras") || nama.contains("gula") || nama.contains("minyak") || 
            nama.contains("tahu") || nama.contains("tempe") || nama.contains("mi") || 
            nama.contains("roti") || nama.contains("telur")) {
            return "Makanan";
        } else if (nama.contains("susu") || nama.contains("kopi") || nama.contains("teh")) {
            return "Minuman";
        } else if (nama.contains("sabun") || nama.contains("shampoo") || nama.contains("pasta") || 
                   nama.contains("deterjen")) {
            return "Perawatan";
        } else {
            return "Kebutuhan Rumah";
        }
    }

    private void updateStatistics() {
        try {
            // Gunakan statistik dari database
            StokStatistik stats = barangController.getStokStatistik();
            
            totalProdukLabel.setText(String.valueOf(stats.getTotalProduk()));
            stokMenipisLabel.setText(String.valueOf(stats.getStokMenipis()));
            stokHabisLabel.setText(String.valueOf(stats.getStokHabis()));
            nilaiStokLabel.setText("Rp " + currencyFormat.format(stats.getNilaiTotalStok()));
            
        } catch (Exception e) {
            // Fallback ke perhitungan manual jika ada error
            int totalProduk = filteredBarangData.size();
            int stokMenipis = 0;
            int stokHabis = 0;
            double nilaiTotalStok = 0;

            for (Barang barang : filteredBarangData) {
                int stok = barang.getStok();
                if (stok == 0) {
                    stokHabis++;
                } else if (stok <= 10) {
                    stokMenipis++;
                }
                nilaiTotalStok += (stok * barang.getHarga());
            }

            totalProdukLabel.setText(String.valueOf(totalProduk));
            stokMenipisLabel.setText(String.valueOf(stokMenipis));
            stokHabisLabel.setText(String.valueOf(stokHabis));
            nilaiStokLabel.setText("Rp " + currencyFormat.format(nilaiTotalStok));
        }
    }

    private void updateFooterInfo() {
        totalRecordsLabel.setText("Total: " + filteredBarangData.size() + " produk");
    }

    private void updateLastUpdateTime() {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        lastUpdateLabel.setText("Terakhir diperbarui: " + currentTime);
    }

    private String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private void handleDetailBarang(Barang barang) {
        String detail = String.format(
            "Detail Produk:\n\n" +
            "Kode: BRG%03d\n" +
            "Nama: %s\n" +
            "Stok Tersedia: %d\n" +
            "Harga Satuan: Rp %s\n" +
            "Nilai Stok: Rp %s\n" +
            "Status: %s\n" +
            "Kategori: %s",
            barang.getId(),
            barang.getNamaBarang(),
            barang.getStok(),
            currencyFormat.format(barang.getHarga()),
            currencyFormat.format(barang.getStok() * barang.getHarga()),
            getStatusStok(barang.getStok()),
            getKategoriFromName(barang.getNamaBarang())
        );
        
        AlertUtils.showInfo(detail);
    }

    @FXML
    private void handleExport() {
        try {
            ObservableList<Barang> currentData = stokTable.getItems();
            List<Barang> barangList = currentData.stream().collect(java.util.stream.Collectors.toList());
            
            if (barangList.isEmpty()) {
                AlertUtils.showInfo("Tidak ada data untuk diekspor!");
                return;
            }
            
            Stage stage = (Stage) exportButton.getScene().getWindow();
            boolean success = ExportUtils.exportLaporanStokToCSV(barangList, stage);
            
            if (success) {
                AlertUtils.showInfo("Laporan stok berhasil diekspor ke CSV!\n\n" +
                                   "File berisi:\n" +
                                   "- " + barangList.size() + " produk\n" +
                                   "- Statistik lengkap stok\n" +
                                   "- Analisis persentase stok\n" +
                                   "- Detail nilai investasi stok");
            } else {
                AlertUtils.showInfo("Export dibatalkan atau gagal!");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Gagal mengekspor laporan stok: " + e.getMessage());
        }
    }

    @FXML
    private void handlePrint() {
        try {
            ObservableList<Barang> currentData = stokTable.getItems();
            List<Barang> barangList = currentData.stream().collect(java.util.stream.Collectors.toList());
            
            if (barangList.isEmpty()) {
                AlertUtils.showInfo("Tidak ada data untuk dicetak!");
                return;
            }
            
            // Konfirmasi print
            javafx.scene.control.Alert confirmAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Konfirmasi Print");
            confirmAlert.setHeaderText("Print Laporan Stok");
            confirmAlert.setContentText("Anda akan mencetak laporan stok dengan " + barangList.size() + " produk.\n\n" +
                                       "Pastikan printer sudah siap dan terpasang.\n" +
                                       "Lanjutkan dengan pencetakan?");
            
            if (confirmAlert.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
                boolean success = PrintUtils.printLaporanStok(barangList);
                
                if (success) {
                    AlertUtils.showInfo("Laporan stok berhasil dicetak!\n\n" +
                                       "Laporan berisi:\n" +
                                       "- " + barangList.size() + " produk\n" +
                                       "- Statistik lengkap stok\n" +
                                       "- Analisis dan rekomendasi\n" +
                                       "- Header dan footer profesional");
                } else {
                    AlertUtils.showInfo("Pencetakan dibatalkan atau gagal!");
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Gagal mencetak laporan stok: " + e.getMessage());
        }
    }

    public void refreshData() {
        loadData();
        updateLastUpdateTime();
        AlertUtils.showInfo("Data laporan stok berhasil diperbarui!");
    }
}

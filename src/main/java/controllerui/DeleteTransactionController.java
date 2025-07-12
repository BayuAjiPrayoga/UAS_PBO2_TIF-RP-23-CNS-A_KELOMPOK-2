package controllerui;

import controller.TransaksiController;
import controller.UserController;
import controller.BarangController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.User;
import model.Barang;
import utils.AlertUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class DeleteTransactionController {

    @FXML private RadioButton rbAll;
    @FXML private RadioButton rbPeriod;
    @FXML private RadioButton rbUser;
    @FXML private RadioButton rbProduct;

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<User> userComboBox;
    @FXML private ComboBox<Barang> productComboBox;

    @FXML private Label transactionCountLabel;
    @FXML private Button deleteButton;

    private TransaksiController transaksiController = new TransaksiController();
    private UserController userController = new UserController();
    private BarangController barangController = new BarangController();
    private AdminController parentController;

    private ToggleGroup toggleGroup;

    public void initialize() {
        setupToggleGroup();
        setupComboBoxes();
        updateTransactionCount();
        setupToggleListeners();
    }

    private void setupToggleGroup() {
        toggleGroup = new ToggleGroup();
        rbAll.setToggleGroup(toggleGroup);
        rbPeriod.setToggleGroup(toggleGroup);
        rbUser.setToggleGroup(toggleGroup);
        rbProduct.setToggleGroup(toggleGroup);

        // Default selection
        rbAll.setSelected(true);
    }

    private void setupComboBoxes() {
        // Setup user combobox
        List<User> users = userController.getAllUsersIncludingInactive();
        userComboBox.setItems(FXCollections.observableArrayList(users));
        userComboBox.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    setText(user.getUsername() + " (" + user.getRole() + ")");
                }
            }
        });
        userComboBox.setButtonCell(new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    setText(user.getUsername() + " (" + user.getRole() + ")");
                }
            }
        });

        // Setup product combobox
        List<Barang> products = barangController.getAllBarang();
        productComboBox.setItems(FXCollections.observableArrayList(products));
        productComboBox.setCellFactory(param -> new ListCell<Barang>() {
            @Override
            protected void updateItem(Barang barang, boolean empty) {
                super.updateItem(barang, empty);
                if (empty || barang == null) {
                    setText(null);
                } else {
                    setText(barang.getNamaBarang() + " (ID: " + barang.getId() + ")");
                }
            }
        });
        productComboBox.setButtonCell(new ListCell<Barang>() {
            @Override
            protected void updateItem(Barang barang, boolean empty) {
                super.updateItem(barang, empty);
                if (empty || barang == null) {
                    setText(null);
                } else {
                    setText(barang.getNamaBarang() + " (ID: " + barang.getId() + ")");
                }
            }
        });
    }

    private void setupToggleListeners() {
        toggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                updateFormVisibility();
            }
        });

        updateFormVisibility();
    }

    private void updateFormVisibility() {
        // Hide all first
        startDatePicker.setVisible(false);
        endDatePicker.setVisible(false);
        userComboBox.setVisible(false);
        productComboBox.setVisible(false);

        // Show based on selection
        if (rbPeriod.isSelected()) {
            startDatePicker.setVisible(true);
            endDatePicker.setVisible(true);
        } else if (rbUser.isSelected()) {
            userComboBox.setVisible(true);
        } else if (rbProduct.isSelected()) {
            productComboBox.setVisible(true);
        }
    }

    private void updateTransactionCount() {
        int count = transaksiController.getTransactionCount();
        transactionCountLabel.setText("Total transaksi dalam database: " + count);
    }

    public void setParentController(AdminController parentController) {
        this.parentController = parentController;
    }
    
    public void setUserModeSelected() {
        rbUser.setSelected(true);
        toggleGroup.selectToggle(rbUser);
        updateTransactionCount();
    }

    @FXML
    private void handleDelete() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Hapus Transaksi");
        confirmAlert.setHeaderText("PERINGATAN: Tindakan ini tidak dapat dibatalkan!");
        
        String contentText = "Apakah Anda yakin ingin menghapus transaksi ";
        
        if (rbAll.isSelected()) {
            contentText += "SEMUA?";
        } else if (rbPeriod.isSelected()) {
            if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
                AlertUtils.showError("Pilih tanggal mulai dan akhir periode!");
                return;
            }
            contentText += "dari " + startDatePicker.getValue() + " sampai " + endDatePicker.getValue() + "?";
        } else if (rbUser.isSelected()) {
            if (userComboBox.getValue() == null) {
                AlertUtils.showError("Pilih user!");
                return;
            }
            contentText += "dari user: " + userComboBox.getValue().getUsername() + "?";
        } else if (rbProduct.isSelected()) {
            if (productComboBox.getValue() == null) {
                AlertUtils.showError("Pilih produk!");
                return;
            }
            contentText += "untuk produk: " + productComboBox.getValue().getNamaBarang() + "?";
        }

        confirmAlert.setContentText(contentText);

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            performDelete();
        }
    }

    private void performDelete() {
        boolean success = false;
        String message = "";

        try {
            if (rbAll.isSelected()) {
                success = transaksiController.deleteAllTransactions();
                message = "Semua transaksi berhasil dihapus!";
                
            } else if (rbPeriod.isSelected()) {
                LocalDateTime startDateTime = LocalDateTime.of(startDatePicker.getValue(), LocalTime.MIN);
                LocalDateTime endDateTime = LocalDateTime.of(endDatePicker.getValue(), LocalTime.MAX);
                success = transaksiController.deleteTransactionsByPeriod(startDateTime, endDateTime);
                message = "Transaksi dalam periode berhasil dihapus!";
                
            } else if (rbUser.isSelected()) {
                success = transaksiController.deleteTransactionsByUser(userComboBox.getValue().getId());
                message = "Transaksi user " + userComboBox.getValue().getUsername() + " berhasil dihapus!";
                
            } else if (rbProduct.isSelected()) {
                success = transaksiController.deleteTransactionsByProduct(productComboBox.getValue().getId());
                message = "Transaksi produk " + productComboBox.getValue().getNamaBarang() + " berhasil dihapus!";
            }

            if (success) {
                AlertUtils.showInfo(message);
                updateTransactionCount();
                
                // Refresh parent data
                if (parentController != null) {
                    parentController.refreshAllData();
                }
                
                closeDialog();
            } else {
                AlertUtils.showError("Gagal menghapus transaksi!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Terjadi kesalahan: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) deleteButton.getScene().getWindow();
        stage.close();
    }
}

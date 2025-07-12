package controllerui;

import controller.BarangController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Barang;
import utils.AlertUtils;


public class BarangFormController {

    @FXML private TextField namaField;
    @FXML private TextField stokField;
    @FXML private TextField hargaField;
    @FXML private Button btnSimpan;

    private BarangController barangController = new BarangController();
    private Barang barang;
    private AdminController parentController;

    public void setBarang(Barang barang) {
        this.barang = barang;
        if (barang != null) {
            namaField.setText(barang.getNamaBarang());
            stokField.setText(String.valueOf(barang.getStok()));
            hargaField.setText(String.valueOf(barang.getHarga()));
        }
    }

    public void setParentController(AdminController controller) {
        this.parentController = controller;
    }

    @FXML
    public void handleSimpan() {
        String nama = namaField.getText();
        int stok;
        double harga;

        try {
            stok = Integer.parseInt(stokField.getText());
            harga = Double.parseDouble(hargaField.getText());
        } catch (NumberFormatException e) {
            showAlert("Stok dan harga harus berupa angka!");
            return;
        }

        if (nama.isEmpty()) {
            showAlert("Nama barang tidak boleh kosong!");
            return;
        }

        if (barang == null) {
            // Tambah
            barang = new Barang();
        }

        barang.setNamaBarang(nama);
        barang.setStok(stok);
        barang.setHarga(harga);

        if (barang.getId() == 0) {
            barangController.tambahBarang(barang);
        } else {
            barangController.updateBarang(barang);
        }

        parentController.refreshTable();
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnSimpan.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validasi");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

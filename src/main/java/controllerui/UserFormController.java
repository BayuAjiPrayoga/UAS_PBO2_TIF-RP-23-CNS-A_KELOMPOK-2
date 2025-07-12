package controllerui;

import controller.UserController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;

public class UserFormController {

    @FXML private Label formTitle;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label errorLabel;

    private UserController userController = new UserController();
    private AdminController parentController;
    private User currentUser;
    private boolean isEdit = false;

    public void initialize() {
        setupRoleComboBox();
    }

    private void setupRoleComboBox() {
        roleComboBox.setItems(FXCollections.observableArrayList("admin", "kasir"));
    }

    public void setUser(User user) {
        this.currentUser = user;
        this.isEdit = (user != null);
        
        if (isEdit) {
            formTitle.setText("Edit Pengguna");
            usernameField.setText(user.getUsername());
            roleComboBox.setValue(user.getRole());
            
            // Untuk edit, password tidak wajib diisi (hanya jika ingin ganti password)
            passwordField.setPromptText("Kosongkan jika tidak ingin mengubah password");
            confirmPasswordField.setPromptText("Kosongkan jika tidak ingin mengubah password");
        } else {
            formTitle.setText("Tambah Pengguna Baru");
        }
    }

    public void setParentController(AdminController parentController) {
        this.parentController = parentController;
    }

    @FXML
    public void handleSave() {
        hideError();

        // Validasi input
        if (!validateInput()) {
            return;
        }

        try {
            if (isEdit) {
                updateUser();
            } else {
                createNewUser();
            }
            
            // Refresh parent table
            if (parentController != null) {
                parentController.refreshUserTable();
            }
            
            // Close form
            closeForm();
            
        } catch (Exception e) {
            showError("Terjadi kesalahan: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleComboBox.getValue();

        // Validasi username
        if (username.isEmpty()) {
            showError("Username harus diisi!");
            usernameField.requestFocus();
            return false;
        }

        if (username.length() < 3) {
            showError("Username minimal 3 karakter!");
            usernameField.requestFocus();
            return false;
        }

        // Validasi role
        if (role == null || role.isEmpty()) {
            showError("Role harus dipilih!");
            roleComboBox.requestFocus();
            return false;
        }

        // Validasi password untuk user baru atau jika password diisi untuk edit
        if (!isEdit || !password.isEmpty()) {
            if (password.isEmpty()) {
                showError("Password harus diisi!");
                passwordField.requestFocus();
                return false;
            }

            if (password.length() < 6) {
                showError("Password minimal 6 karakter!");
                passwordField.requestFocus();
                return false;
            }

            if (!password.equals(confirmPassword)) {
                showError("Konfirmasi password tidak sesuai!");
                confirmPasswordField.requestFocus();
                return false;
            }
        }

        // Validasi username unik (untuk user baru atau jika username berubah)
        if (!isEdit || !username.equals(currentUser.getUsername())) {
            if (userController.isUsernameExists(username)) {
                showError("Username sudah digunakan!");
                usernameField.requestFocus();
                return false;
            }
        }

        return true;
    }

    private void createNewUser() {
        User newUser = new User();
        newUser.setUsername(usernameField.getText().trim());
        newUser.setPassword(passwordField.getText());
        newUser.setRole(roleComboBox.getValue());

        if (!userController.createUser(newUser)) {
            showError("Gagal menambahkan pengguna!");
        }
    }

    private void updateUser() {
        currentUser.setUsername(usernameField.getText().trim());
        currentUser.setRole(roleComboBox.getValue());
        
        // Update password hanya jika diisi
        if (!passwordField.getText().isEmpty()) {
            currentUser.setPassword(passwordField.getText());
        }

        if (!userController.updateUser(currentUser)) {
            showError("Gagal memperbarui pengguna!");
        }
    }

    @FXML
    public void handleCancel() {
        closeForm();
    }

    private void closeForm() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
    }
}

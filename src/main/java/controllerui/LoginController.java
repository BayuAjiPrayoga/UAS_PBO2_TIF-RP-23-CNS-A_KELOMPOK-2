package controllerui;

import java.io.IOException;

import controller.AuthController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import utils.AlertUtils;
import utils.SoundUtils;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private AuthController authController = new AuthController();

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        User user = authController.login(username, password);

        if (user != null) {
            // 🔊 PLAY LOGIN SUCCESS SOUND!
            if (user.getRole().equals("kasir")) {
                SoundUtils.playLoginSuccess(); // Suara khusus untuk kasir login
            } else {
                SoundUtils.playNotification(); // Suara biasa untuk admin
            }
            
            loadDashboard(user);
        } else {
            errorLabel.setText("Login gagal! Username/password salah atau akun tidak aktif.");
            errorLabel.setVisible(true);
        }
    }

    private void loadDashboard(User user) {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            FXMLLoader loader;

            if (user.getRole().equals("admin")) {
                loader = new FXMLLoader(getClass().getResource("/view/AdminDashboard.fxml"));
                javafx.scene.Parent dashboard = loader.load();
                
                // Get the controller and set the admin user
                Object controller = loader.getController();
                if (controller != null) {
                    try {
                        java.lang.reflect.Method setUserMethod = controller.getClass().getMethod("setAdminUser", User.class);
                        setUserMethod.invoke(controller, user);
                    } catch (Exception e) {
                        System.err.println("Could not set admin user via reflection: " + e.getMessage());
                    }
                }
                
                Scene scene = new Scene(dashboard);
                try {
                    scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                } catch (Exception e) {
                    System.err.println("Could not load CSS: " + e.getMessage());
                }
                stage.setScene(scene);
                stage.setTitle("Dashboard Admin - KASIRIN");
            } else {
                // Set user before loading FXML
                KasirController.setCurrentKasirUser(user);
                
                // Load Kasir Dashboard
                loader = new FXMLLoader(getClass().getResource("/view/KasirDashboard.fxml"));
                javafx.scene.Parent dashboard = loader.load();
                
                // Get the controller and set the user again as backup
                Object controller = loader.getController();
                if (controller != null) {
                    try {
                        java.lang.reflect.Method setUserMethod = controller.getClass().getMethod("setKasirUser", User.class);
                        setUserMethod.invoke(controller, user);
                    } catch (Exception e) {
                        System.err.println("Could not set user via reflection: " + e.getMessage());
                    }
                }
                
                // Create scene with CSS
                Scene scene = new Scene(dashboard);
                try {
                    scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                } catch (Exception e) {
                    System.err.println("Could not load CSS: " + e.getMessage());
                }
                stage.setScene(scene);
                stage.setTitle("Dashboard Kasir NusaMart - KASIRIN");
            }
            
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Gagal memuat dashboard: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Error loading dashboard: " + e.getMessage());
        }
    }
}

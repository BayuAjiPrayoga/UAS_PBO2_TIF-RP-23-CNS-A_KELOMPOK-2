import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import utils.SoundUtils;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 🔊 Test dan tampilkan status audio system
            System.out.println("🎵 Initializing KASIRIN Audio System...");
            SoundUtils.printAudioStatus();
            
            if (SoundUtils.isAudioSystemAvailable()) {
                System.out.println("✅ Audio system ready!");
                // Test notification sound
                SoundUtils.playNotification();
            } else {
                System.out.println("⚠️ Audio system not available, using fallback");
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            AnchorPane loginPane = loader.load();
            
            Scene scene = new Scene(loginPane);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            primaryStage.setScene(scene);
            primaryStage.setTitle("KASIRIN - Point of Sale System");
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

package Lab2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Scene scene = new Scene(loader.load());

        // Set up the stage
        primaryStage.setTitle("Login");
        primaryStage.setScene(scene);

        // 🔹 Auto-resize the window to fit all elements
        primaryStage.sizeToScene();

        // 🔹 Prevent the user from shrinking it too small (optional but nice)
        primaryStage.setMinWidth(450);
        primaryStage.setMinHeight(300);

        // 🔹 Allow resizing if you want the user to stretch it
        primaryStage.setResizable(true);

        // Show the window
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

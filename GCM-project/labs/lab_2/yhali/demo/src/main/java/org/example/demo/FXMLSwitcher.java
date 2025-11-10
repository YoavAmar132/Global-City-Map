package org.example.demo;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class FXMLSwitcher {
    public static Stage stage;

    public static void setStage(Stage s) {
        stage = s;
    }

    public static void switchScene(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(FXMLSwitcher.class.getResource(fxml));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();
    }
}

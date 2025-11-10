package org.example.lab_2;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.lab_2.controllers.LoginController;
import org.example.lab_2.controllers.WelcomeController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SceneNavigator {
    private final Stage stage;
    private final Map<Class<?>, String> routes = new HashMap<>();

    public SceneNavigator(Stage stage) {
        this.stage = stage;
        routes.put(LoginController.class, "/org/example/lab_2/LoginScreen.fxml");
        routes.put(WelcomeController.class, "/org/example/lab_2/WelcomeScreen.fxml");
    }

    public <T> void show(Class<T> controllerClass) {
        String fxml = routes.get(controllerClass);
        if (fxml == null) throw new IllegalArgumentException("No route for " + controllerClass.getName());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            stage.setScene(new Scene(loader.load()));
            if (!stage.isShowing()) stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + fxml, e);
        }
    }
}


package gcm.client.utill;

import gcm.client.controllers.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SceneNavigator {
    private final Stage stage;
    private final Map<Class<?>, String> routes = new HashMap<>();

    public SceneNavigator(Stage stage) {
        this.stage = stage;

        // NOTE: leading "/" and matching src/main/resources structure
        routes.put(LoginController.class, "/gcm/client/LoginScreen.fxml");
        routes.put(WelcomeController.class, "/gcm/client/WelcomeScreen.fxml");
        routes.put(RegistrationController.class, "/gcm/client/RegistrationScreen.fxml");
        routes.put(UserMenuController.class, "/gcm/client/UserMenuScreen.fxml");
        routes.put(ContentWorkerMenuController.class, "/gcm/client/ContentWorkerMenuScreen.fxml");
        routes.put(ManagerMenuController.class, "/gcm/client/ManagerMenuScreen.fxml");
        routes.put(CustomerSupportMenuController.class, "/gcm/client/CustomerSupportMenuScreen.fxml");

    }

    public <T> void show(Class<T> controllerClass) {
        String fxml = routes.get(controllerClass);
        if (fxml == null) {
            throw new IllegalArgumentException("No route for " + controllerClass.getName());
        }
        try {
            URL url = getClass().getResource(fxml);
            System.out.println("Loading FXML: " + fxml + " → " + url);

            if (url == null) {
                throw new IllegalStateException(
                        "FXML not found on classpath at: " + fxml +
                                "\nMake sure it is under src/main/resources" + fxml
                );
            }

            FXMLLoader loader = new FXMLLoader(url);
            stage.setScene(new Scene(loader.load()));
            if (!stage.isShowing()) stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + fxml, e);
        }
    }
}

package gcm.client.utill;

import gcm.client.controllers.*;
import gcm.client.controllers.map.MapViewerController;
import gcm.client.controllers.map.UserMapViewerController;
import gcm.client.controllers.menu.ContentWorkerMenuController;
import gcm.client.controllers.menu.CustomerSupportMenuController;
import gcm.client.controllers.menu.ManagerMenuController;
import gcm.client.controllers.menu.UserMenuController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
        routes.put(LoginController.class, "/gcm/client/Login/LoginScreen.fxml");
        routes.put(WelcomeController.class, "/gcm/client/Login/WelcomeScreen.fxml");
        routes.put(RegistrationController.class, "/gcm/client/Login/RegistrationScreen.fxml");
        routes.put(UserMenuController.class, "/gcm/client/menu/UserMenuScreen.fxml");
        routes.put(ContentWorkerMenuController.class, "/gcm/client/menu/ContentWorkerMenuScreen.fxml");
        routes.put(ManagerMenuController.class, "/gcm/client/menu/ManagerMenuScreen.fxml");
        routes.put(CustomerSupportMenuController.class, "/gcm/client/menu/CustomerSupportMenuScreen.fxml");
        routes.put(MapViewerController.class, "/gcm/client/map/MapViewerRoot.fxml");
        routes.put(UserMapViewerController.class, "/gcm/client/map/UserMapViewerRoot.fxml");


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
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/gcm/client/map/map.css").toExternalForm()
            );

            stage.setScene(scene);
            if (!stage.isShowing()) stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + fxml, e);
        }
    }
}

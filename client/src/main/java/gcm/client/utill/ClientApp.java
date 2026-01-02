package gcm.client.utill;

import gcm.client.controllers.WelcomeController;
import gcm.client.network.GcmClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import common.model.User;

public class ClientApp extends Application {

    private static SceneNavigator navigator;
    private static GcmClient gcmClient;
    private static Stage primaryStage;

    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setClient(GcmClient client) {
        gcmClient = client;
    }

    public static GcmClient getClient() {
        return gcmClient;
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        showConnectionWindow();
    }

    private void showConnectionWindow() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/gcm/client/ui/client_connection.fxml")
        );

        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.setTitle("GCM Client - Server Connection");
        primaryStage.show();
    }

    /** Called AFTER successful connection */
    public static void startMainUI() {
        navigator = new SceneNavigator(primaryStage);
        primaryStage.setTitle("Global City Map");
        navigator.show(WelcomeController.class);
    }

    /** for logout */
    public static void logout() {
        currentUser = null;
        navigator.show(WelcomeController.class);
    }

    public static SceneNavigator getNavigator() {
        return navigator;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

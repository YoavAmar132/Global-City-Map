package gcm.client.utill;

import common.messages.GcmRequest;
import common.messages.GcmResponse;
import common.messages.RequestType;
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

    private static boolean shuttingDown = false;

    private static User currentUser = null;

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

        primaryStage.setOnCloseRequest(event -> {
            shuttingDown = true;

            if (gcmClient != null) {
                try {
                    if (currentUser != null) {
                        GcmRequest request =
                                new GcmRequest(RequestType.LOGOUT, currentUser);
                        gcmClient.sendRequest(request);
                    }

                    //close the socket AFTER sending LOGOUT
                    gcmClient.closeConnection();

                } catch (Exception e) {
                    System.err.println("Error during shutdown: " + e.getMessage());
                }
            }
        });

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


    public static boolean isLoggedIn() {
        return currentUser != null;
    }


    /** for logout */
    public static void logout() {
        if (gcmClient != null && currentUser != null) {
            GcmRequest request =
                    new GcmRequest(RequestType.LOGOUT, currentUser);
            gcmClient.sendRequest(request);
        }

        currentUser = null;

        if (!shuttingDown && navigator != null) {
            navigator.show(WelcomeController.class);
        }
    }




    public static SceneNavigator getNavigator() {
        return navigator;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

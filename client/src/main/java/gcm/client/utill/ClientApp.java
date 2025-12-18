package gcm.client.utill;

import gcm.client.network.GcmClient;
import gcm.client.controllers.LoginController;
import javafx.application.Application;
import javafx.stage.Stage;
import common.model.User;



public class ClientApp extends Application {

    private static SceneNavigator navigator;
    private static GcmClient gcmClient;

    private static User currentUser;           // so we save the current logged user

    public static void setCurrentUser(User user) {
        currentUser = user;
    }
    public static User getCurrentUser() {
        return currentUser;
    }


    @Override
    public void start(Stage mainStage) throws Exception {
        try {
            gcmClient = new GcmClient("localhost", 5555);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to connect to server.");
            // it's okay for now, client still starts UI
        }

        navigator = new SceneNavigator(mainStage);
        mainStage.setTitle("Login");
        navigator.show(LoginController.class);  // this is where FXML must be found

        mainStage.show();
    }

    /**
     * for when we log out
     */
    public static void logout() {
        currentUser = null;
        navigator.show(LoginController.class);
    }

    public static SceneNavigator getNavigator() { return navigator; }
    public static GcmClient getClient() { return gcmClient; }

    public static void main(String[] args) {
        launch(args);
    }
}

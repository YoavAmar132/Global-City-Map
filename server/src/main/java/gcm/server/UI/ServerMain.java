package gcm.server.UI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerMain extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/gcm/server/ui/db_config.fxml")
        );

        stage.setScene(new Scene(loader.load()));
        stage.setTitle("GCM Server - Database Configuration");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


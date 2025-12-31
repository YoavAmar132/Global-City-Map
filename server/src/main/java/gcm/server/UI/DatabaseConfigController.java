package gcm.server.UI;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;

import gcm.server.ServerBootstrap;
import gcm.server.data.DbManager;


public class DatabaseConfigController {

    @FXML private TextField urlField;
    @FXML private TextField userField;
    @FXML private PasswordField passField;
    @FXML private Button startButton;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        urlField.setText("jdbc:mysql://localhost:3306/GCM_DB");
        userField.setText("root");

        startButton.setDisable(true);

        passField.textProperty().addListener((obs, oldVal, newVal) ->
                startButton.setDisable(newVal == null || newVal.isBlank())
        );
    }

    @FXML
    public void startServer() {
        try {
            String url = urlField.getText().trim();
            String user = userField.getText().trim();
            String pass = passField.getText();

            if (pass.isBlank()) {
                statusLabel.setText("Password is required");
                return;
            }

            DbManager.init(url, user, pass);

            // test connection
            try (Connection c = DbManager.getConnection()) {
                // success
            }

            statusLabel.setText("Connected successfully");

            Stage stage = (Stage) startButton.getScene().getWindow();
            stage.close();

            ServerBootstrap.startServer();




        } catch (Exception e) {
            statusLabel.setText("Connection failed: " + e.getMessage());
        }
    }
}

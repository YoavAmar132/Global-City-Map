package gcm.server.ui;

import gcm.server.bot.BotConfig;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.sql.Connection;

import gcm.server.ServerBootstrap;
import gcm.server.data.DbManager;


public class DatabaseConfigController {

    @FXML private TextField urlField;
    @FXML private TextField userField;
    @FXML private PasswordField passField;
    @FXML private CheckBox enableBotCheckBox;
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

        // Focus password since we change this only 99% of the times
        Platform.runLater(() -> passField.requestFocus());

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

            BotConfig botConfig = BotConfig.getInstance();
            botConfig.setEnabled(enableBotCheckBox.isSelected());

            ServerBootstrap.startServer();

        } catch (Exception e) {
            statusLabel.setText("Connection failed: " + e.getMessage());
        }
    }
}

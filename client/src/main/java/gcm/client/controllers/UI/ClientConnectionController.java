package gcm.client.controllers.UI;

import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ClientConnectionController {

    @FXML private TextField ipField;
    @FXML private Button connectButton;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        ipField.setText("localhost");
    }

    @FXML
    public void connect() {
        String ip = ipField.getText().trim();
        if (ip.isEmpty()) {
            ip = "localhost";
        }

        // UI feedback
        connectButton.setDisable(true);
        statusLabel.setText("Connecting...");

        String finalIp = ip;

        // Background thread so we don't block UI thread
        new Thread(() -> {
            try {
                GcmClient client = new GcmClient(finalIp, 5555);

                Platform.runLater(() -> {
                    ClientApp.setClient(client);

                    Stage stage = (Stage) connectButton.getScene().getWindow();
                    stage.close();

                    ClientApp.startMainUI();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Failed to connect to server");
                    connectButton.setDisable(false);
                });
            }
        }, "Client-Connect-Thread").start();
    }
}

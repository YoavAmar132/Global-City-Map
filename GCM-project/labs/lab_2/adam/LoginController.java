package Lab2;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class LoginController {

    @FXML
    private AnchorPane loginScreen;
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private Label noText;

    private static final UsersApp usersApp = new UsersApp();

    static {
        // Load users from Users.txt (must be in resources)
        try (InputStream is = LoginController.class.getResourceAsStream("/Users.txt");
             Scanner scanner = new Scanner(is)) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split("\\s+");
                if (parts.length == 2) {
                    usersApp.addUser(parts[0], parts[1]);
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load Users.txt: " + e.getMessage());
        }
    }

    @FXML
    private void onLoginClick() {
        String email = username.getText().trim();
        String pass = password.getText().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            noText.setText("Please enter both email and password.");
            return;
        }

        boolean found = usersApp.getUsersList().stream()
                .anyMatch(u -> u.getName().equals(email) && u.getPassWord().equals(pass));

        if (found) {
            noText.setText(""); // clear error
            sceneSwitch("/welcome.fxml", "Welcome", email);
        } else {
            // Show error and let user retry — do NOT disable or reset label
            noText.setText("Invalid email or password. Try again!");
            password.clear(); // clear only password for convenience
            password.requestFocus();
        }
    }



    private void sceneSwitch(String fxmlFile, String title, String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene newScene = new Scene(loader.load());

            // Pass data to next controller
            Object controller = loader.getController();
            if (controller instanceof WelcomeController wc) {
                wc.setUserEmail(email);
            }

            Stage stage = (Stage) loginScreen.getScene().getWindow();
            stage.setScene(newScene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
          //  e.printStackTrace(); kinda no need to show what failed :D
        }
    }
}

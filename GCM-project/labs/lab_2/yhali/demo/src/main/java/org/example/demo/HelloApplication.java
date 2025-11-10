package org.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        UserApp userApp = new UserApp();
        userApp.loadUsers( "users.txt");
        userApp.printUsers(); //creates usersOutput.txt

        FXMLSwitcher.setStage(stage);
        FXMLSwitcher.switchScene("login.fxml");
    }
}

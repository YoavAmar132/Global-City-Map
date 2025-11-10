package main.java.com.LoginApp;

import javafx;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("LoginScreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 300, 250);
        stage.setTitle("Login Example");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        UserDataBase db = new UserDataBase();
        launch();
    }
}

package org.example.lab_2;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.lab_2.controllers.LoginController;

import java.io.IOException;

public class WindowApplication extends Application {
    private static SceneNavigator navigator;

    @Override
    public void start(Stage MainStage) throws IOException {
            navigator = new SceneNavigator(MainStage);
            MainStage.setTitle("Login");
            navigator.show(LoginController.class); // first screen
        }

        public static SceneNavigator getNavigator () {
            return navigator;
        }
    }



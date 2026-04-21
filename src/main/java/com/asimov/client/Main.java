package com.asimov.client;

import com.asimov.client.utils.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        SceneManager.setPrimaryStage(primaryStage);
        SceneManager.switchToLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

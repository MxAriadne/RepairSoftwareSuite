package com.freyja;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Create the FXML loader and initialize the first scene.
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/apple.fxml"));
        Parent content = loader.load();

        // Create the new Scene object for the main page (atm, the Apple view)
        Scene scene = new Scene(content, 1280, 720);

        // Setting window parameters
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

}
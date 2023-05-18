/*
 *
 * XboxController
 *
 * This class holds all functions related to the Xbox tab.
 * This is a child of 'SceneController'
 *
 * 'resources/xbox.fxml'
 *
 */

package com.freyja.controllers;

import com.freyja.SceneController;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class XboxController {

    @FXML
    private Button apple;

    @FXML
    private Button android;

    @FXML
    private Button pc;

    @FXML
    private Button playstation;

    @FXML
    private Button xbox;

    @FXML
    private void handleButtonClick(ActionEvent e) throws IOException {
        Stage stage = (Stage) ((Node) e.getTarget()).getScene().getWindow();
        SceneController sceneController = new SceneController();
        Button clickedButton = (Button) e.getSource();

        if (clickedButton == apple) {
            // Apple button was clicked
            sceneController.setView(stage, "/apple.fxml");
        } else if (clickedButton == android) {
            // Android button was clicked
            sceneController.setView(stage, "/android.fxml");
        } else if (clickedButton == pc) {
            // PC button was clicked
            sceneController.setView(stage, "/pc.fxml");
        } else if (clickedButton == playstation) {
            // PlayStation button was clicked
            sceneController.setView(stage, "/playstation.fxml");
        } else if (clickedButton == xbox) {
            // Xbox button was clicked
            sceneController.setView(stage, "/xbox.fxml");
        }
    }


}

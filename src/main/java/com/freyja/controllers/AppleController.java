/*
*
* AppleController
*
* This class holds all functions related to the Apple tab.
* This is a child of 'SceneController'
*
* 'resources/apple.fxml'
*
*/

package com.freyja.controllers;

import com.freyja.SceneController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class AppleController {

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
    private void handleButtonClick(ActionEvent e) {
        Stage stage = (Stage) ((Node) e.getTarget()).getScene().getWindow();
        SceneController sceneController = new SceneController();
        Button clickedButton = (Button) e.getSource();

        if (clickedButton == apple) {
            // Apple button was clicked
            try {
                sceneController.setView(stage, "/apple.fxml");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else if (clickedButton == android) {
            // Android button was clicked
            try {
                sceneController.setView(stage, "/android.fxml");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else if (clickedButton == pc) {
            // PC button was clicked
            try {
                sceneController.setView(stage, "/pc.fxml");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else if (clickedButton == playstation) {
            // PlayStation button was clicked
            try {
                sceneController.setView(stage, "/playstation.fxml");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else if (clickedButton == xbox) {
            // Xbox button was clicked
            try {
                sceneController.setView(stage, "/xbox.fxml");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }


}

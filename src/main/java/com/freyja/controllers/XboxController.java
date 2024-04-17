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
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.io.IOException;

public class XboxController {

    @FXML
    private void handleButtonClick(ActionEvent e) throws IOException {

        Stage stage = (Stage) ((Node) e.getTarget()).getScene().getWindow();
        SceneController sceneController = new SceneController();
        Button clickedButton = (Button) e.getSource();

        switch (clickedButton.getId()) {
            case "apple" ->
                // Apple button was clicked
                    sceneController.setView(stage, "/apple.fxml");
            case "android" ->
                // Android button was clicked
                    sceneController.setView(stage, "/android.fxml");
            case "pc" ->
                // PC button was clicked
                    sceneController.setView(stage, "/pc.fxml");
            case "playstation" ->
                // PlayStation button was clicked
                    sceneController.setView(stage, "/playstation.fxml");
            case "xbox" ->
                // Xbox button was clicked
                    sceneController.setView(stage, "/xbox.fxml");
            default -> throw new IllegalStateException("Unexpected value: " + clickedButton.getId());
        }
    }
}

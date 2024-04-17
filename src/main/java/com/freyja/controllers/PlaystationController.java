/*
 *
 * PlaystationController
 *
 * This class holds all functions related to the Playstation tab.
 * This is a child of 'SceneController'
 *
 * 'resources/playstation.fxml'
 *
 */

package com.freyja.controllers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import com.freyja.SceneController;
import com.freyja.helpers.GamepadLogger;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;

import static com.freyja.helpers.CommonsHelper.downloadFile;
import static com.freyja.helpers.CommonsHelper.formatSize;

public class PlaystationController {

    @FXML
    private Pane tester, ps;

    @FXML
    private Circle buttonactive;

    @FXML
    private Text usbstatus;

    @FXML
    // This is the button to setting up Playstation update USB
    private Button installusb;

    @FXML
    // This lists the playstation models compatible with the software
    private ChoiceBox<String> psmodels;

    @FXML
    // This lists local storage devices
    private ChoiceBox<String> devices;

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
            case "installusb" -> {
                // Install PS firmware was clicked
                setupUSB(psmodels.getValue());
                Logger.getLogger(psmodels.getValue());
            }
            default -> throw new IllegalStateException("Unexpected value: " + clickedButton.getId());
        }
    }

    @FXML
    public void initialize (){
        GamepadLogger.start();

        psmodels.getItems().add("PS3");
        psmodels.getItems().add("PS4");
        psmodels.getItems().add("PS5");

        // Get all available drives on the system
        File[] drives = File.listRoots();

        // Print the drive names
        for (File drive : drives) {
            try {
                // Get the file store associated with the drive
                FileStore fileStore = Files.getFileStore(FileSystems.getDefault().getPath(String.valueOf(drive)));

                // Get the total capacity of the drive
                long totalCapacity = fileStore.getTotalSpace();

                // Convert the capacity to human-readable format
                String formattedCapacity = formatSize(totalCapacity);

                ObservableList<String> devicesitems = devices.getItems();

                devicesitems.add(drive + "    Capacity: " + formattedCapacity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static int extractDiskNumber(String diskInfoOutput) {
        // Extract the disk number from the diskpart output
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Disk \\d+");
        java.util.regex.Matcher matcher = pattern.matcher(diskInfoOutput);
        if (matcher.find()) {
            String diskNumberString = matcher.group();
            try {
                return Integer.parseInt(diskNumberString.split(" ")[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public void setupUSB(String model) {
        String targetDrive = devices.getValue().replaceAll("[Capcity: ]+\\d*[.]\\d* [A-Z]*", "").trim();
        File updateFolder = new File(targetDrive + "UPDATE");

        if (updateFolder.mkdir()) {
            Logger.getLogger("Directory created!");
        } else {
            Logger.getLogger("Directory already exists!");
        }

        URI src = URI.create("localhost");

        switch (model) {
            case "PS3" -> src = URI.create("http://dus01.ps3.update.playstation.net/update/ps3/image/us/2023_0228_05fe32f5dc8c78acbcd84d36ee7fdc5b/PS3UPDAT.PUP");
            case "PS4" -> src = URI.create("https://pc.ps4.update.playstation.net/update/ps4/image/2023_0227/sys_5da9ca74ca39e0b709b138d0d794769f/PS4UPDATE.PUP");
            case "PS5" -> src = URI.create("https://pc.ps5.update.playstation.net/update/ps5/official/tJMRE80IbXnE9YuG0jzTXgKEjIMoabr6/image/2023_0414/sys_310b2d495e0b2dd7dc1950d63a15ce0d6fe509dc93a93781eb52ed2dd073a286/PS5UPDATE.PUP");
            default -> Logger.getLogger("No device selected!");
        }

        String fileName = FilenameUtils.getName(src.getPath());
        Logger.getLogger(fileName);

        CompletableFuture.runAsync(() -> {
            try {
                if (!"C:\\".equals(targetDrive)) {
                    ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "format", targetDrive.replace("\\", ""), "/FS:FAT32", "/Q");
                    processBuilder.start();
                    Process process = processBuilder.start();

                    Thread outputThread = new Thread(() -> {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                Logger.getLogger(line);
                                System.out.println(line);
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            process.destroyForcibly();
                        }

                    });
                    outputThread.start();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        try {
            usbstatus.setText("Downloading update...");
            downloadFile(src.toURL(), updateFolder.toString() + "\\fuck.pup");
        } catch (MalformedURLException e) {
            Logger.getLogger("Invalid link!");
        }
    }


    @FXML
    void handleKeyboardInput(KeyEvent e) {
        String keyText = e.getText();
        buttonactive.setLayoutX(592);
        buttonactive.setLayoutY(214);
        buttonactive.setVisible(true);
    }
}

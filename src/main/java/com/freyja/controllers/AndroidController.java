/*
 *
 * AndroidController
 *
 * This class holds all functions related to the Apple tab.
 * This is a child of 'SceneController'
 *
 * 'resources/android.fxml'
 *
 */

package com.freyja.controllers;

import com.freyja.helpers.AndroidHelper.*;

import com.freyja.SceneController;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AndroidController {

    // Create the ProcessBuilder for ADB
    File adbexec = new File(Objects.requireNonNull(AndroidController.class.getClassLoader().getResource("platform-tools/adb.exe")).toURI());

    // Create the ProcessBuilder for Heimdall
    //File heimexec = new File(Objects.requireNonNull(AndroidController.class.getClassLoader().getResource("heimdall/heimdall.exe")).toURI());

    ADBExecuter adb = new ADBExecuter();

    @FXML
    private Button /*Cancel button for Backup pane*/ cancelbackup,
                   /*Start button for Backup pane*/ beginbackup;

    @FXML
    private Button /*Cancel button for Restore pane*/ cancelrestore,
                   /*Start button for Restore pane*/ restorebutton;

    @FXML
    private Button /*"2. OEM Unlock" Tooltop*/ oemunlocktip,
                   /*Pixel title tooltip*/ pixeltip,
                   /*Title tooltip*/ firmtip,
                   /*Samsung tooltip*/ flashtip,
                   /*Tip for backup pane*/ devicetip,
                   /*ADB pane tip*/ devicetip1;

    @FXML
    private ChoiceBox<String> /*Device selector for backup pane*/ backupchoice,
                              /*Device selector for pixel firmware flash pane*/ pixelselect,
                              /*Device selector for samsung firmware flash pane*/ samsungselect,
                              /*Device selector for the ADB quick command pane*/ adbselect,
                              /*Selector for firmware pane*/ manufacturorbox;

    @FXML
    private Text timeelapsed, samsungerrortxt, pixelerrortext, adberrortext;

    @FXML
    private ListView<String> restorelist, imagelist;

    @FXML
    private Pane firmwarepane, pixelpane, samsungpane;

    private final String appPath = System.getenv("APPDATA") + "/RepairSuite/recoveryimages/";

    public AndroidController() throws URISyntaxException {
        // TODO document why this constructor is empty
    }

    @FXML
    protected void initialize() {

        String debugging = "Devices must have USB debugging enabled.";

        customTip(cancelrestore, "Technically, it should be safe to cancel an ADB restore.\nHowever, there is a small chance of data corruption\nor additional damage. Only cancel an active\nrestore as a last resort.\n(Restores, at worst, can take over an hour.)");
        customTip(oemunlocktip, "OEM Unlock is usually a switch in developer settings.\nOn some phones this option is located in the recovery\nmode options.");
        customTip(firmtip,debugging);
        customTip(flashtip,debugging);
        customTip(pixeltip,debugging);
        customTip(devicetip,debugging);
        customTip(devicetip1,debugging);
        //This might no longer be the case. Will remain commented out until further tested can be completed.
        // customTip(flashtip, "You can only flash images onto\ncarrier UNLOCKED devices.");

        manufacturorbox.getItems().add("Google Pixel");
        manufacturorbox.getItems().add("Samsung");

        adb.devicesADB(samsungselect);
        adb.devicesADB(pixelselect);
        adb.devicesADB(backupchoice);
        adb.devicesADB(adbselect);

        listRestores();
        listImages();

    }

    public void customTip (Button tip, String tipText) {
        Tooltip temp = new Tooltip(tipText);
        temp.setShowDelay(javafx.util.Duration.millis(100));
        Tooltip.install(tip, temp);
    }

    /*
     * listRestores
     *
     * This simply iterates over the files in the chose directory
     * add adds them to the list of images assuming they fit the filter
     * settings.
     *
     */
    public void listRestores() {

        // Clear restore list so it doesn't double the contents
        restorelist.getItems().clear();

        // Set dir
        File dir = new File(System.getProperty("user.dir"));
        // Set filter
        String[] extensions = new String[]{"ab"};

        // List files
        List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);
        // Iterate over files and add to list if they fit the filter
        for (File file : files) {
            restorelist.getItems().add(file.getName());
        }
    }

    /*
     * listImages
     *
     * This simply iterates over the files in the chose directory
     * add adds them to the list of images assuming they fit the filter
     * settings.
     *
     */
    public void listImages() {

        // Clears contents of the list, so it doesn't double the items.
        imagelist.getItems().clear();

        // Set dir path
        File dir = new File(appPath);

        // Set filter to zip files
        String[] extensions = new String[]{"zip"};

        // Create file list
        List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);
        //Iterate over the files and add to the list
        for (File file : files) {
            imagelist.getItems().add(file.getName());
        }
    }

    @FXML
    private void handleButtonClick(ActionEvent e) throws IOException, URISyntaxException {
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
            case "deletebackup" -> {
                Files.deleteIfExists(Paths.get(restorelist.getSelectionModel().getSelectedItem()));
                listRestores();
            }
            case "mannext" -> {
                String manItem = manufacturorbox.getSelectionModel().getSelectedItem();
                switch (manItem) {
                    case "Google Pixel" -> {
                        firmwarepane.setVisible(false);
                        pixelpane.setVisible(true);
                    }
                    case "Samsung" -> {
                        firmwarepane.setVisible(false);
                        samsungpane.setVisible(true);
                    }
                    default -> manufacturorbox.getSelectionModel().clearSelection();
                }
            }
            case "adbboot" ->
                adb.executeADBCommand("-s " + adbselect.getSelectionModel().getSelectedItem().replaceAll(" : [A-z0-9 ]*$", "") + " reboot bootloader");
            case "adbrecovery" ->
                adb.executeADBCommand("-s " + adbselect.getSelectionModel().getSelectedItem().replaceAll(" : [A-z0-9 ]*$", "") + " reboot recovery");
            case "adbimei" ->
                adberrortext.setText(adb.executeFastbootCommand("getvar imei"));
            case "adboemunlock" ->
                adberrortext.setText(adb.executeFastbootCommand("flashing unlock"));
            case "adboemlock" ->
                adberrortext.setText(adb.executeFastbootCommand("flashing lock"));
            case "pixeldownload" -> {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI("https://developers.google.com/android/ota"));
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + clickedButton.getId());
        }
    }

    public void backupADB(ActionEvent e) {
        long start = System.nanoTime();
        Timeline timeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), event -> {
            long finish = System.nanoTime();

            Duration duration = Duration.ofNanos(finish - start);
            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;
            long seconds = duration.getSeconds() % 60;

            String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            timeelapsed.setText("Time Elapsed: " + formattedTime);
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        if (e.getSource() == beginbackup) {
            // Getting current date to save in the backup file name
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd");
            String backupname = "\"" + System.nanoTime() + " " + backupchoice.getValue().replaceAll("^[A-z0-9]* : ", "") + " " + dtf.format(LocalDateTime.now()) + ".ab\"";

            // This strips the extra characters to give us just the ID for the device.
            String deviceid = backupchoice.getValue().replaceAll(" : [A-z0-9 ]*$", "");

            // Execute the temporary file
            ProcessBuilder backupBuilder = new ProcessBuilder(adbexec.getAbsolutePath(), "-s", deviceid, "backup", "-apk", "-shared", "-all", "-system", "-f", backupname);

            CompletableFuture.runAsync(() -> {
                try {
                    Process backupProcess = backupBuilder.start();

                    cancelbackup.setVisible(true);
                    beginbackup.setVisible(false);
                    timeelapsed.setVisible(true);

                    backupProcess.waitFor(); // Wait for the backup process to complete

                    cancelbackup.setVisible(false);
                    beginbackup.setVisible(true);
                    timeelapsed.setVisible(false);
                    timeline.stop();
                    listRestores();

                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            });
        } else if (e.getSource() == cancelbackup) {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("taskkill", "/F", "/IM", "adb.exe");
                processBuilder.start();
                timeline.stop();
                timeelapsed.setVisible(false);
            } catch (IOException ex) {
                ex.printStackTrace();
                // Handle the exception appropriately
            }
            cancelbackup.setVisible(false);
            beginbackup.setVisible(true);
        }

    }

    /*
     * restoreADB
     *
     * Issues ADB command to restore selected file.
     * Also handles the buttons for this pane.
     *
     */
    public void restoreADB(ActionEvent e) {
        // Saves the name of the backup file selected.
        String backupname = restorelist.getSelectionModel().getSelectedItem();

        // This strips the extra characters to give us just the ID for the device.
        String deviceid = backupchoice.getValue().replaceAll(" : [A-z0-9 ]*$", "");

        // Execute the temporary file.
        ProcessBuilder restoreBuilder = new ProcessBuilder(adbexec.getAbsolutePath(), "-s", deviceid, "restore", backupname);

        // If the user clicks restore while a file is selected...
        if (e.getSource() == restorebutton) {
            // Run task async...
            CompletableFuture.runAsync(() -> {
                try {
                    // Issue restore command for the selected devices
                    Process restoreProcess = restoreBuilder.start();

                    // Make the cancel button visible and hide the restore button during the operation.
                    Platform.runLater(() -> {
                        cancelrestore.setVisible(true);
                        restorebutton.setVisible(false);
                    });

                    // Wait for the backup process to complete
                    restoreProcess.waitFor();

                    // After the process completes, make the restore button visible again and hide the cancel button.
                    Platform.runLater(() -> {
                        cancelrestore.setVisible(false);
                        restorebutton.setVisible(true);
                    });

                } catch (IOException | InterruptedException ex) {
                    // Crash handling
                    ex.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            });
          // If the user cancels the restore in progress...
        } else if (e.getSource() == cancelrestore) {
            try {
                // Issue taskkill command to close ADB terminal
                ProcessBuilder processBuilder = new ProcessBuilder("taskkill", "/F", "/IM", "adb.exe");
                processBuilder.start();

                // Hide the cancel button and make the restore button visible again.
                cancelrestore.setVisible(false);
                restorebutton.setVisible(true);
            } catch (IOException ex) {
                // Crash handling
                ex.printStackTrace();
            }

            // For some reason this function occasionally makes these items visible/not visible. This simply
            // corrects that should it happen. Need to look more into what causes this.
            cancelbackup.setVisible(false);
            beginbackup.setVisible(true);
        }
    }

    /*
     * bootToBootloader
     *
     * Issues ADB command to force reboot chosen device into fastboot mode,
     * or Odin Mode in the case of Samsung.
     * (They use the same ADB designation.)
     *
     */
    public void bootToBootloader(ActionEvent e) {
        // Get the pane from which this function was executed.
        Pane parentPane = (Pane) ((Button) e.getSource()).getParent();

        String deviceid = "";

        switch (parentPane.getId()) {
            case "pixelpane" -> {
                if (pixelselect.getSelectionModel().getSelectedItem() != null) {
                    // This strips the extra characters to give us just the ID for the device.
                    deviceid = pixelselect.getSelectionModel().getSelectedItem().replaceAll(" : [A-z0-9 ]*$", "");
                    adb.executeADBCommand("-s " + deviceid + " reboot bootloader");
                } else {
                    pixelerrortext.setText("No device selected.");
                    pixelerrortext.setFill(Color.RED);
                }
            }
            case "samsungpane" -> {
                if (samsungselect.getSelectionModel().getSelectedItem() != null) {
                    // This strips the extra characters to give us just the ID for the device.
                    deviceid = samsungselect.getSelectionModel().getSelectedItem().replaceAll(" : [A-z0-9 ]*$", "");
                    adb.executeADBCommand("-s " + deviceid + " reboot bootloader");
                } else {
                    samsungerrortxt.setText("No device selected.");
                    samsungerrortxt.setFill(Color.RED);
                }
            }
            case "adbpane" -> {
                if (adbselect.getSelectionModel().getSelectedItem() != null) {
                    // This strips the extra characters to give us just the ID for the device.
                    deviceid = adbselect.getSelectionModel().getSelectedItem().replaceAll(" : [A-z0-9 ]*$", "");
                    adb.executeADBCommand("-s " + deviceid + " reboot bootloader");
                } else {
                    adberrortext.setText("No device selected.");
                    adberrortext.setFill(Color.RED);
                }
            }
            default -> throw new IllegalStateException("Unexpected value: ");
        }
    }

    /*
     * selectOSImage
     *
     * This creates a file selection window. The function is currently tied
     * to both the Pixel & Samsung flash panes. This might not be the case in future revisions
     * due to the differences in folder structure for both. Pixels only need to choose one ZIP file,
     * while Samsungs need to choose like 10 files...wonk.
     * Will be working on this at a later date as I'm putting Samsungs off for the moment.
     *
     */
    public void selectOSImage(ActionEvent e) throws IOException {
        // Reset error texts.
        samsungerrortxt.setText("");
        pixelerrortext.setText("");

        // Create filechooser
        FileChooser fileChooser = new FileChooser();

        // Set window title.
        fileChooser.setTitle("Open device image.");

        // This sets file type filters. Both Samsungs and Pixels store at least the base
        // directory in a ZIP file, so this is unlikely to change.
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("ZIP", "*.zip"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        Node source = (Node) e.getSource();
        Window stage = source.getScene().getWindow();

        // Process file.
        File selectedFile = fileChooser.showOpenDialog(stage);

        // If selected file not null
        if (selectedFile != null) {
            // Generate directory and parent directories if not exists
            new File(appPath).mkdirs();

            // Sanitize file name and create full path + filename.
            String destinationFilePath = appPath + "/" + selectedFile.getName().replaceAll("[<>:\"/\\\\|?*]", "");

            // Move selected file to appdata
            Files.move(selectedFile.toPath(), Path.of(destinationFilePath), StandardCopyOption.REPLACE_EXISTING);

            // Refresh the list of images
            listImages();

        } else {
            // If file selected is null, print error.
            samsungerrortxt.setText("Error: File selected is NULL.");
            samsungerrortxt.setFill(Color.RED);

            pixelerrortext.setText("Error: File selected is NULL.");
            pixelerrortext.setFill(Color.RED);
        }
    }

    // So I realized that Pixel/Nexus devices can be flashed without unzipping the file.
    // This function is only going to be useful for Samsung/Heimdall
    // Defunt for now.
    /*public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    public void unzip(String zipPath, Button deviceType) throws IOException {
        String dataFolder = System.getenv("APPDATA");
        File destDir = new File(dataFolder + "RepairSuite/recoveryimages");

        if (deviceType == pixelfileselector) {
            destDir = new File(dataFolder + "RepairSuite/pixelimages");
        } else if (deviceType == samsungfileselector) {
            destDir = new File(dataFolder + "RepairSuite/samsungimages");
        }

        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }*/

    /*
     * flashPixelImage
     *
     * This grabs the file selected in the imagelist and then
     * runs the adb sideload command to flash the selected device.
     *
     */
    public void flashPixelImage() {
        if (pixelselect.getSelectionModel().getSelectedItem() != null) {
            long start = System.nanoTime();
            Timeline timeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), event -> {
                long finish = System.nanoTime();

                Duration duration = Duration.ofNanos(finish - start);
                long hours = duration.toHours();
                long minutes = duration.toMinutes() % 60;
                long seconds = duration.getSeconds() % 60;

                String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                pixelerrortext.setText("Time Elapsed: " + formattedTime);
            }));
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();

            // This strips the extra characters to give us just the ID for the device.
            String deviceid = pixelselect.getValue().replaceAll(" : [A-z0-9 ]*$", "");

            // Sanitize file name and create full path + filename.
            String finalFile = appPath + "/" + imagelist.getSelectionModel().getSelectedItem();

            ProcessBuilder flashBuilder = new ProcessBuilder(adbexec.getAbsolutePath(), "-s", deviceid, "sideload", finalFile);

            CompletableFuture.runAsync(() -> {
                try {
                    Process restoreProcess = flashBuilder.start();

                    int exitcode = restoreProcess.waitFor(); // Wait for the backup process to complete

                    Platform.runLater(() -> {
                        if (exitcode >= 0 && exitcode <= 7) {
                            pixelerrortext.setText("Flashing complete.");
                            pixelerrortext.setFill(Color.PURPLE);
                        }
                    });

                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            });
        } else {
            pixelerrortext.setText("There is no device selected.");
            pixelerrortext.setFill(Color.RED);
        }

    }

    // Defunt
    public void flashSamsungImage(ActionEvent e) {
        // TODO document why this method is empty
    }

}

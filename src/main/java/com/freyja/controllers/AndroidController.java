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

import com.freyja.SceneController;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class AndroidController {

    // Create the ProcessBuilder for ADB
    File adbexec = new File(Objects.requireNonNull(AndroidController.class.getClassLoader().getResource("platform-tools/adb.exe")).toURI());

    // Create the ProcessBuilder for Heimdall
    //File heimexec = new File(Objects.requireNonNull(AndroidController.class.getClassLoader().getResource("heimdall/heimdall.exe")).toURI());

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
    private Button cancelbackup;

    @FXML
    private Button beginbackup;

    @FXML
    private Button deletebackup;

    @FXML
    private Button cancelrestore;

    @FXML
    private Button restorebutton;

    // I think I removed/renamed this one? Need to check and clean up later.
    @FXML
    private Button devicetip;

    //Buttons for "Pixel Firmware Flash" section
    @FXML
    private Button /*"2. OEM Unlock" Tooltop*/ oemunlocktip,
                   /*Pixel title tooltip*/ pixeltip,
                   /*Reboot to bootloader in pixel*/ bootloaderbtnpixel,
                   /*File selector dropdown in the pixel section*/ pixelfileselector,
                   /*Initiate flashing for Pixels*/ pixelflashbtn;

    //Buttons for "Firmware Flash" section
    @FXML
    private Button /*Title tooltip*/ firmtip,
                   /*"Next" button for firmware flash*/ mannext;

    @FXML
    private Button /*Samsung image selector*/ samsungfileselector,
                   /*Samsung title tooltip*/ flashtip,
                   /*Opens Samsung firmware site*/ firmwarelink,
                   /*Installs the zadag Samsung driver*/ installdriver,
                   /*Reboots Samsung device into Odin Mode*/ odinmode;

    @FXML
    private ChoiceBox<String> backupchoice, deviceselect, pixelselect;

    // This is the selector for the firmware flash section
    @FXML
    private ChoiceBox<String> manufacturorbox;

    @FXML
    private Text timeelapsed, samsungerrortxt, pixelerrortext;

    @FXML
    private ListView<String> restorelist, imagelist;

    @FXML
    private Pane firmwarepane, pixelpane, samsungpane;

    private final String appPath = System.getenv("APPDATA") + "/RepairSuite/recoveryimages/";

    public AndroidController() throws URISyntaxException {
        // TODO document why this constructor is empty
    }

    @FXML
    protected void initialize() throws IOException {
        Tooltip cancelRestoreTip = new Tooltip("Technically, it should be safe to cancel an ADB restore.\nHowever, there is a small chance of data corruption\nor additional damage. Only cancel an active\nrestore as a last resort.\n(Restores, at worst, can take over an hour.)");
        cancelRestoreTip.setShowDelay(javafx.util.Duration.millis(100));
        Tooltip.install(cancelrestore, cancelRestoreTip);

        Tooltip deviceTip = new Tooltip("Devices must have USB debugging enabled.");
        deviceTip.setShowDelay(javafx.util.Duration.millis(100));
        Tooltip.install(devicetip, deviceTip);

        Tooltip oemUnlockTip = new Tooltip("OEM Unlock is usually a switch in developer settings.\nOn some phone this option is located in the recovery\nmode options.");
        oemUnlockTip.setShowDelay(javafx.util.Duration.millis(100));
        Tooltip.install(oemunlocktip, oemUnlockTip);

        Tooltip flashTip = new Tooltip("You can only flash images onto\ncarrier UNLOCKED devices.");
        flashTip.setShowDelay(javafx.util.Duration.millis(100));
        Tooltip.install(flashtip, flashTip);

        manufacturorbox.getItems().add("Google Pixel");
        manufacturorbox.getItems().add("Samsung");

        // Execute the temporary file
        ProcessBuilder processBuilder = new ProcessBuilder(adbexec.toString(), "devices", "-l");

        // Start the process
        Process process = processBuilder.start();

        // Read the output of the process
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip the first line (header) and empty lines
                if (!line.isEmpty() && !line.startsWith("List of devices attached")) {
                    // Split the line by whitespace and get the device ID
                    String[] parts = line.split("\\s+");
                    if (parts.length > 3) {
                        // Add device name to list
                        backupchoice.getItems().add(parts[0] + " : " + (parts[3].replace("model:", "")).replace("_", " "));
                        deviceselect.getItems().add(parts[0] + " : " + (parts[3].replace("model:", "")).replace("_", " "));
                    } else {
                        backupchoice.getItems().add("Error: USB Debugging is disabled for this device.");
                        deviceselect.getItems().add("Error: USB Debugging is disabled for this device.");
                    }
                }
            }
        }

        listRestores();
        listImages();

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

        // Clears contents of the list so it doesn't double the items.
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
        } else if (clickedButton == deletebackup) {
            Files.deleteIfExists(Paths.get(restorelist.getSelectionModel().getSelectedItem()));
            listRestores();
        } else if (clickedButton == mannext) {
            String manItem = manufacturorbox.getSelectionModel().getSelectedItem();
            switch (manItem) {
                case "Google Pixel":
                    firmwarepane.setVisible(false);
                    pixelpane.setVisible(true);
                    break;
                case "Samsung":
                    firmwarepane.setVisible(false);
                    samsungpane.setVisible(true);
                    break;
                default:
                    manufacturorbox.getSelectionModel().clearSelection();
            }
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
            ProcessBuilder backupBuilder = new ProcessBuilder(adbexec.getAbsolutePath(), "-s", deviceid, "backup", "-apk", "-shared", "-all", "-nosystem", "-f", backupname);

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
    public void bootToBootloader() throws IOException {
        // This strips the extra characters to give us just the ID for the device.
        String deviceid = deviceselect.getValue().replaceAll(" : [A-z0-9 ]*$", "");
        ProcessBuilder rebootProcess = new ProcessBuilder(adbexec.getAbsolutePath(), "-s", deviceid, "reboot", "bootloader");
        rebootProcess.start();
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

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
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AndroidController {

    // Create the ProcessBuilder with the ADB command
    File adbexec = new File(Objects.requireNonNull(AndroidController.class.getClassLoader().getResource("platform-tools/adb.exe")).toURI());

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

    @FXML
    private Button devicetip;

    @FXML
    private Button oemunlocktip;

    @FXML
    private Button flashtip;

    @FXML
    private ChoiceBox<String> backupchoice, deviceselect;

    @FXML
    private Text timeelapsed, firmwareerrortxt;

    @FXML
    private TextField pathtxt;

    @FXML
    private ListView<String> restorelist;

    public AndroidController() throws URISyntaxException {
        // TODO document why this constructor is empty
    }

    public void listRestores() throws IOException {

        restorelist.getItems().clear();

        File dir = new File(System.getProperty("user.dir"));
        String[] extensions = new String[]{"ab"};
        System.out.println("Getting all .ab files in " + dir.getCanonicalPath() + " including those in subdirectories");
        List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);
        for (File file : files) {
            restorelist.getItems().add(file.getName());
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
        }
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
        timeline.setCycleCount(Timeline.INDEFINITE);
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
                    // Handle the exception appropriately
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

    public void restoreADB(ActionEvent e) {
        String backupname = restorelist.getSelectionModel().getSelectedItem();

        // This strips the extra characters to give us just the ID for the device.
        String deviceid = backupchoice.getValue().replaceAll(" : [A-z0-9 ]*$", "");

        // Execute the temporary file
        ProcessBuilder restoreBuilder = new ProcessBuilder(adbexec.getAbsolutePath(), "-s", deviceid, "restore", backupname);

        if (e.getSource() == restorebutton) {
            CompletableFuture.runAsync(() -> {
                try {
                    Process restoreProcess = restoreBuilder.start();

                    Platform.runLater(() -> {
                        cancelrestore.setVisible(true);
                        restorebutton.setVisible(false);
                    });

                    restoreProcess.waitFor(); // Wait for the backup process to complete

                    Platform.runLater(() -> {
                        cancelrestore.setVisible(false);
                        restorebutton.setVisible(true);
                    });

                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            });
        } else if (e.getSource() == cancelrestore) {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("taskkill", "/F", "/IM", "adb.exe");
                processBuilder.start();
                cancelrestore.setVisible(false);
                restorebutton.setVisible(true);
            } catch (IOException ex) {
                ex.printStackTrace();
                // Handle the exception appropriately
            }
            cancelbackup.setVisible(false);
            beginbackup.setVisible(true);
        }
    }

    public void bootToBootloader() throws IOException {
        // This strips the extra characters to give us just the ID for the device.
        String deviceid = deviceselect.getValue().replaceAll(" : [A-z0-9 ]*$", "");
        ProcessBuilder rebootProcess = new ProcessBuilder(adbexec.getAbsolutePath(), "-s", deviceid, "reboot", "bootloader");
        rebootProcess.start();
    }

    public void selectOSImage(ActionEvent e) {
        firmwareerrortxt.setText("");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open device image.");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("ZIP", "*.zip"),
                new FileChooser.ExtensionFilter("TAR.GZ", "*.tar.gz"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        Node source = (Node) e.getSource();
        Window stage = source.getScene().getWindow();

        List<File> selectedFile = fileChooser.showOpenMultipleDialog(stage);
        if (selectedFile != null) {
            if (selectedFile.size() != 3) {
                firmwareerrortxt.setText("Error: You must choose 3 files.");
                firmwareerrortxt.setFill(Color.RED);
            } else {
                for (File file : selectedFile) {
                    pathtxt.setText(pathtxt.getText() + "\"" + file + "\"" + " ");
                }
            }
        } else {
            firmwareerrortxt.setText("Error: File selected is NULL.");
            firmwareerrortxt.setFill(Color.RED);
        }
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    public void unzip() throws IOException {
        String fileZip = "src/main/resources/unzipTest/compressed.zip";
        File destDir = new File("src/main/resources/platform-tools");

        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
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
    }

    public void flashImage(ActionEvent e) {
        long start = System.nanoTime();
        Timeline timeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), event -> {
            long finish = System.nanoTime();

            Duration duration = Duration.ofNanos(finish - start);
            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;
            long seconds = duration.getSeconds() % 60;

            String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            firmwareerrortxt.setText("Time Elapsed: " + formattedTime);
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        String backupname = restorelist.getSelectionModel().getSelectedItem();

        // This strips the extra characters to give us just the ID for the device.
        String deviceid = backupchoice.getValue().replaceAll(" : [A-z0-9 ]*$", "");



        // Execute the temporary file
        ProcessBuilder flashBuilder = new ProcessBuilder(adbexec.getAbsolutePath(), "-s", deviceid, "-w", "restoreall");

        CompletableFuture.runAsync(() -> {
            try {
                Process restoreProcess = flashBuilder.start();

                int exitcode = restoreProcess.waitFor(); // Wait for the backup process to complete

                Platform.runLater(() -> {
                    if (exitcode >= 0 && exitcode <= 7) {
                        firmwareerrortxt.setText("Flashing complete.");
                    }
                });

            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace();
                Thread.currentThread().interrupt();
            }
        });

    }
}

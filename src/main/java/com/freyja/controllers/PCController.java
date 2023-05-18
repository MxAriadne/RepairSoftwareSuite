/*
 *
 * PCController
 *
 * This class holds all functions related to the PC tab.
 * This is a child of 'SceneController'
 *
 * 'resources/pc.fxml'
 *
 */

package com.freyja.controllers;

import com.freyja.SceneController;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PCController {

    @FXML
    private ChoiceBox<String> startingdrive;

    @FXML
    private ChoiceBox<String> originaldrive;

    @FXML
    protected void initialize() {
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

                ObservableList<String> ogitems = originaldrive.getItems();
                ObservableList<String> destitems = startingdrive.getItems();

                ogitems.add(drive + "    Capacity: " + formattedCapacity);
                destitems.add(drive + "    Capacity: " + formattedCapacity);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Helper method to format the size in a human-readable format
    private static String formatSize(long size) {
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double formattedSize = size;

        while (formattedSize >= 1024 && unitIndex < units.length - 1) {
            formattedSize /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", formattedSize, units[unitIndex]);
    }

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

    // This is literally just here to convert Windows date style to standard
    public String dateFormat(String inputDate) {
        // Parse the input string to OffsetDateTime
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(inputDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        // Format the OffsetDateTime to the desired format
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy/dd/MM");

        return offsetDateTime.format(outputFormatter);
    }

    @FXML
    private Text healthpercent;

    @FXML
    private Text alerts;

    private static final String BATTERYCOMMAND = "powercfg.exe /batteryreport /xml";

    public void getBatteryReport() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", BATTERYCOMMAND);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Wait for the process to complete
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        try {
            // Grab the outputted XML containing the battery health report.
            File inputFile = new File("battery-report.xml");
            // Create a new document builder factory object
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            // This disables outside access through XML. This is required code for Java standards' compliance.
            dbFactory.setExpandEntityReferences(false);
            // Create a new document builder object
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            // Parse inputFile and save output
            Document doc = dBuilder.parse(inputFile);
            // Normalize text
            doc.getDocumentElement().normalize();

            // Get all instances of HistoryEntry
            // In battery-report1.xml each time the battery is checked it gets saved as anew HistoryEntry node
            NodeList nList = doc.getElementsByTagName("HistoryEntry");
            // We grab the very last node (meaning the most recent data)
            org.w3c.dom.Node node = nList.item(nList.getLength()-1);
            // Cast node to element
            Element element = (Element) node;
            // From the node, grab the current battery capacity
            double numerator = Integer.parseInt(element.getAttribute("FullChargeCapacity"));
            // From the node, grab the design battery capacity
            double denominator  = Integer.parseInt(element.getAttribute("DesignCapacity"));
            // If the denominator isn't 0
            if (denominator != 0) {
                // Get the percentage
                double health = ((numerator / denominator) * 100);
                // Change the Text on the UI
                healthpercent.setText("Battery Health: " + String.format("%.2f", health) + "%");
            } else {
                // If denominator is 0, likely being ran on a desktop, ie: no battery.
                healthpercent.setText("ERROR: UNKNOWN BATTERY");
                healthpercent.setFill(Color.RED);
            }

            // This map will store the dates and health percentages in order to compare and send alerts if there's a very drastic change.
            Map<String, Double> temp = new HashMap<>();
            // Now we're doing a check to see if there were any recent drastic drops in battery capacity.
            for (int i = 0; i < nList.getLength(); i++) {
                node = nList.item(i);
                element = (Element) node;
                double health = 0;

                numerator = Double.parseDouble(element.getAttribute("FullChargeCapacity"));
                // From the node, grab the design battery capacity
                denominator  = Double.parseDouble(element.getAttribute("DesignCapacity"));
                // If the denominator isn't 0
                if (denominator != 0) {
                    // Get the percentage
                    health = ((numerator / denominator) * 100);
                }

                // Add the values to the Map
                temp.put(dateFormat(element.getAttribute("StartDate")), health);
            }

            // Iterator for temp
            Iterator<Map.Entry<String, Double>> itr = temp.entrySet().iterator();

            // Compare the head and previous values
            do {
                Map.Entry<String, Double> entry = itr.next();
                // If the battery health dropped by 10% or more, indicate major failure to the user.
                if ((entry.getValue() - itr.next().getValue()) > 10) {
                    alerts.setText("Alert: There was a drastic\ndrop in capacity on\n" + itr.next().getKey() + "\n\nCapacity dropped by " + String.format("%.2f", (entry.getValue() - itr.next().getValue())) + "%");
                    alerts.setFill(Color.YELLOW);
                }

            } while (itr.hasNext());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void cloneDrives(ActionEvent e) {

        /*String sourceDrive = e.getSource().getClass()."C:\\";  // Replace with the appropriate source drive
        String targetDrive = "D:\\";  // Replace with the appropriate target drive

        try {
            // Create the ProcessBuilder with the command and parameters
            ProcessBuilder processBuilder = new ProcessBuilder("robocopy", sourceDrive, targetDrive, "/MIR");

            // Start the process
            Process process = processBuilder.start();

            // Wait for the process to complete
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Drive cloned successfully!");
            } else {
                System.out.println("Drive cloning failed.");
            }
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
            Thread.currentThread().interrupt();
        }
        */
    }

}

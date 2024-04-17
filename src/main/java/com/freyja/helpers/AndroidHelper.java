package com.freyja.helpers;

import com.freyja.controllers.AndroidController;
import javafx.application.Platform;
import javafx.scene.control.ChoiceBox;
import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class AndroidHelper {

    private AndroidHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static class ADBExecuter {
        // Create the ProcessBuilder for ADB
        private File adbexec;
        private File fastexec;

        {
            try {
                adbexec = new File(Objects.requireNonNull(AndroidController.class.getClassLoader().getResource("platform-tools/adb.exe")).toURI());
                fastexec = new File(Objects.requireNonNull(AndroidController.class.getClassLoader().getResource("platform-tools/fastboot.exe")).toURI());
            } catch (URISyntaxException e) {
                Logger.getLogger("Error: ADB executable is missing from project resources. Please recompile.");
                Thread.currentThread().interrupt();
            }
        }

        public void executeADBCommand(String command) {
            ArrayList<String> commands = new ArrayList<>(List.of(command.split(" ")));

            commands.add(0, adbexec.toString());

            for (String temp : commands) {
                System.out.println(temp);
            }

            // Create command
            CompletableFuture.runAsync(() -> {
                try {
                    Process adb = new ProcessBuilder(commands).start();
                    // Read the output of the process
                    String line;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(adb.getInputStream()));
                    while ((line = reader.readLine()) != null) {
                        // Process each line of output
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    Logger.getLogger("Error: ADB executable is missing from project resources. Please recompile.");
                    Thread.currentThread().interrupt();
                }
            });
        }

        public String executeFastbootCommand(String command) {
            ArrayList<String> commands = new ArrayList<>(List.of(command.split(" ")));

            commands.add(0, fastexec.toString());

            AtomicReference<String> s = new AtomicReference<>("");

            // Create command
            CompletableFuture.runAsync(() -> {
                try {
                    Process adb = new ProcessBuilder(commands).start();
                    // Read the output of the process
                    String line;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(adb.getInputStream()));
                    while ((line = reader.readLine()) != null) {
                        // Process each line of output
                        String finalLine = line;
                        Platform.runLater(() -> {
                            if (finalLine.startsWith("< waiting for any device >")) {
                                s.set("No device connected.");
                            } else if (finalLine.contains("FAILED")) {
                                s.set("Function not supported on this device.");
                            } else {
                                s.set(finalLine);
                            }
                        });
                    }
                } catch (IOException e) {
                    Logger.getLogger("Error: Fastboot executable is missing from project resources. Please recompile.");
                    Thread.currentThread().interrupt();
                }
            });
            return s.get();
        }

        public void devicesADB(ChoiceBox<String> selector) {
            ArrayList<String> options = new ArrayList<>();

            // Create command
            CompletableFuture.runAsync(() -> {
                try {
                    Process adb = new ProcessBuilder(adbexec.toString(), "devices", "-l").start();
                    // Read the output of the process
                    String line;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(adb.getInputStream()));

                    while ((line = reader.readLine()) != null) {
                        // Skip the first line (header) and empty lines
                        if (!line.isEmpty() && !line.startsWith("List of devices attached")) {
                            // Split the line by whitespace and get the device ID
                            String[] parts = line.split("\\s+");
                            if (parts.length > 3) {
                                // Add device name to list
                                options.add(parts[0] + " : " + (parts[3].replace("model:", "")).replace("_", " "));
                            } else {
                                options.add("Error: USB Debugging is disabled for this device.");
                            }
                        }
                    }

                    selector.getItems().addAll(options);

                } catch (IOException e) {
                    Logger.getLogger("Error: ADB executable is missing from project resources. Please recompile.");
                    Thread.currentThread().interrupt();
                }
            });
        }
    }
}
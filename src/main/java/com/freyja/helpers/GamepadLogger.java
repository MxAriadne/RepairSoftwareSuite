package com.freyja.helpers;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class GamepadLogger {

    private GamepadLogger() {
        throw new IllegalStateException("Utility class");
    }

    public static void start() {
        // Initialize GLFW
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Create a window (optional)
        long window = GLFW.glfwCreateWindow(800, 600, "Gamepad Example", 0, 0);
        if (window == 0) {
            throw new IllegalStateException("Unable to create GLFW window");
        }

        Charset charset = StandardCharsets.UTF_8;

        // Enable gamepad input
        GLFW.glfwJoystickPresent(GLFW.GLFW_JOYSTICK_1); // Check if gamepad is connected

        // Main loop
        while (!GLFW.glfwWindowShouldClose(window)) {
            // Poll events and update gamepad state
            GLFW.glfwPollEvents();

            // Get gamepad state
            GLFWGamepadState gamepadState = GLFWGamepadState.create();

            if (GLFW.glfwGetGamepadState(GLFW.GLFW_JOYSTICK_1, gamepadState)) {
                // Check if any button is pressed
                ByteBuffer buttons = GLFW.glfwGetJoystickButtons(1);
                String newContent = charset.decode(buttons).toString();
                System.out.println(newContent);

            }
        }

        // Clean up GLFW
        GLFW.glfwTerminate();
    }
}

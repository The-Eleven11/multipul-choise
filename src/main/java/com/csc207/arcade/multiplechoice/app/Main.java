package com.csc207.arcade.multiplechoice.app;

/**
 * Main entry point for the Multiple Choice Quiz application.
 * Following Clean Architecture, this class only creates and executes the AppBuilder.
 */
public class Main {
    public static void main(String[] args) {
        // Build and launch the application
        new AppBuilder()
            .build()
            .launch();
    }
}

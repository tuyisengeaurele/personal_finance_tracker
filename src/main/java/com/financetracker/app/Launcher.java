package com.financetracker.app;

/**
 * Application entry point.
 *
 * This class is intentionally separate from MainApp (which extends Application)
 * so the fat JAR produced by Maven Shade can launch JavaFX without requiring
 * the JavaFX modules to be on the module-path explicitly at the JVM level.
 */
public class Launcher {

    public static void main(String[] args) {
        MainApp.main(args);
    }
}

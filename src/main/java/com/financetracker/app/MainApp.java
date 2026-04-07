package com.financetracker.app;

import com.financetracker.dao.DatabaseInitializer;
import com.financetracker.util.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

/**
 * JavaFX Application entry point.
 * Initialises the database, loads the main FXML layout, and shows the primary stage.
 */
public class MainApp extends Application {

    private static final Logger log = LoggerFactory.getLogger(MainApp.class);
    private static final String APP_TITLE = "Personal Finance Tracker";
    private static final double MIN_WIDTH  = 1024;
    private static final double MIN_HEIGHT = 680;

    @Override
    public void init() {
        // Database initialisation runs on the JavaFX init thread (before the stage opens)
        try {
            DatabaseInitializer.initialize();
            log.info("Database initialised successfully.");
        } catch (Exception e) {
            log.error("Failed to initialise database", e);
        }
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        URL fxmlUrl = getClass().getResource("/fxml/Main.fxml");
        if (fxmlUrl == null) {
            throw new IOException("Cannot locate /fxml/Main.fxml on classpath");
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(loader.load(), MIN_WIDTH, MIN_HEIGHT);

        // Apply saved theme preference
        ThemeManager.getInstance().applyTheme(scene);

        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);

        // App icon (graceful fallback if missing)
        try {
            URL iconUrl = getClass().getResource("/images/icon.png");
            if (iconUrl != null) {
                primaryStage.getIcons().add(new Image(iconUrl.toExternalForm()));
            }
        } catch (Exception ignored) { /* icon is optional */ }

        primaryStage.show();
        log.info("Application started.");
    }

    @Override
    public void stop() {
        log.info("Application shutting down.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}

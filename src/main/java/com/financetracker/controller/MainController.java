package com.financetracker.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Root controller that owns the sidebar navigation and the central content pane.
 * Each nav button loads its corresponding FXML once and caches it.
 */
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    @FXML private StackPane contentPane;
    @FXML private Button    navDashboard;
    @FXML private Button    navTransactions;
    @FXML private Button    navReports;
    @FXML private Button    navSettings;
    @FXML private Label     appVersionLabel;

    private final Map<String, Node> viewCache = new HashMap<>();
    private Button activeButton;

    @FXML
    private void initialize() {
        appVersionLabel.setText("v1.0.0");
        navigateTo("Dashboard", navDashboard);
    }

    @FXML
    private void onDashboard()     { navigateTo("Dashboard",     navDashboard);     }
    @FXML
    private void onTransactions()  { navigateTo("Transactions",  navTransactions);  }
    @FXML
    private void onReports()       { navigateTo("Reports",       navReports);       }
    @FXML
    private void onSettings()      { navigateTo("Settings",      navSettings);      }

    private void navigateTo(String viewName, Button button) {
        try {
            Node view = viewCache.computeIfAbsent(viewName, this::loadView);
            if (view == null) return;

            contentPane.getChildren().setAll(view);

            // Update active button style
            if (activeButton != null) {
                activeButton.getStyleClass().remove("nav-active");
            }
            button.getStyleClass().add("nav-active");
            activeButton = button;

            log.debug("Navigated to {}", viewName);
        } catch (Exception e) {
            log.error("Failed to navigate to {}", viewName, e);
        }
    }

    private Node loadView(String name) {
        String path = "/fxml/" + name + ".fxml";
        URL url = getClass().getResource(path);
        if (url == null) {
            log.error("FXML not found: {}", path);
            return null;
        }
        try {
            return new FXMLLoader(url).load();
        } catch (IOException e) {
            log.error("Failed to load FXML: {}", path, e);
            return null;
        }
    }
}

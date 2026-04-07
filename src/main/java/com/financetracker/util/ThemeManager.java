package com.financetracker.util;

import javafx.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.prefs.Preferences;

/**
 * Manages Dark / Light theme switching at runtime.
 * The chosen theme is persisted via Java {@link Preferences}.
 */
public class ThemeManager {

    private static final Logger log = LoggerFactory.getLogger(ThemeManager.class);

    public enum Theme { DARK, LIGHT }

    private static final String PREF_THEME = "ui_theme";

    private static final ThemeManager INSTANCE = new ThemeManager();

    private final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);

    private Scene currentScene;

    private ThemeManager() {}

    public static ThemeManager getInstance() {
        return INSTANCE;
    }

    /** Returns the currently persisted theme. */
    public Theme getCurrentTheme() {
        String saved = prefs.get(PREF_THEME, Theme.DARK.name());
        try {
            return Theme.valueOf(saved);
        } catch (IllegalArgumentException e) {
            return Theme.DARK;
        }
    }

    /** Applies the persisted theme to the given scene and stores a reference. */
    public void applyTheme(Scene scene) {
        this.currentScene = scene;
        applyTheme(scene, getCurrentTheme());
    }

    /** Switches to the given theme and re-applies to the stored scene. */
    public void setTheme(Theme theme) {
        prefs.put(PREF_THEME, theme.name());
        if (currentScene != null) {
            applyTheme(currentScene, theme);
        }
    }

    /** Toggles between dark and light. */
    public void toggleTheme() {
        setTheme(getCurrentTheme() == Theme.DARK ? Theme.LIGHT : Theme.DARK);
    }

    private void applyTheme(Scene scene, Theme theme) {
        scene.getStylesheets().clear();

        String cssPath = theme == Theme.DARK ? "/css/dark-theme.css" : "/css/light-theme.css";
        URL url = getClass().getResource(cssPath);
        if (url != null) {
            scene.getStylesheets().add(url.toExternalForm());
            log.debug("Applied theme: {}", theme);
        } else {
            log.warn("CSS file not found: {}", cssPath);
        }
    }
}

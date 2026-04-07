package com.financetracker.util;

import javafx.scene.chart.PieChart;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * Applies category-specific colours to pie chart slices so chart colours
 * match the colours defined in the database rather than JavaFX defaults.
 */
public final class ChartColorUtil {

    private ChartColorUtil() {}

    /**
     * Applies hex colour codes (from the DB) to pie chart data slices.
     * Must be called <em>after</em> the chart has been added to a scene,
     * because JavaFX only creates the Node objects at layout time.
     *
     * @param data   ordered list of chart data
     * @param colors ordered list of hex color strings matching {@code data}
     */
    public static void applyColors(List<PieChart.Data> data, List<String> colors) {
        for (int i = 0; i < data.size() && i < colors.size(); i++) {
            PieChart.Data slice = data.get(i);
            String hex = colors.get(i);
            if (slice.getNode() != null && hex != null && !hex.isBlank()) {
                try {
                    Color color = Color.web(hex);
                    String css = toRgbCss(color);
                    slice.getNode().setStyle("-fx-pie-color: " + css + ";");
                } catch (IllegalArgumentException ignored) {
                    // Malformed hex — skip; default chart colour applies
                }
            }
        }
    }

    /** Converts a JavaFX Color to a CSS rgb() string. */
    private static String toRgbCss(Color c) {
        return String.format("rgb(%d,%d,%d)",
                (int) (c.getRed()   * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue()  * 255));
    }
}

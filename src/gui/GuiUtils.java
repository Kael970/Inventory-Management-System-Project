// Java helper for consistent GUI styling
package gui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class GuiUtils {
    public static void stylePrimary(Button b) {
        if (b == null) return;
        b.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 6 12 6 12;");
        b.setPrefHeight(32);
        // keep width flexible
    }

    public static void styleSecondary(Button b) {
        if (b == null) return;
        b.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: black; -fx-font-size: 12px; -fx-padding: 6 12 6 12;");
        b.setPrefHeight(32);
    }

    public static void styleDanger(Button b) {
        if (b == null) return;
        b.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 6 12 6 12;");
        b.setPrefHeight(32);
    }

    public static void styleWarning(Button b) {
        if (b == null) return;
        b.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-font-size: 12px; -fx-padding: 6 12 6 12;");
        b.setPrefHeight(32);
    }

    public static void styleHeaderLabel(Label l) {
        if (l == null) return;
        l.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
    }
}

// Java helper for consistent GUI styling
package gui;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

public class GuiUtils {
    public static void stylePrimary(Button b) {
        if (b == null) return;
        b.setStyle("-fx-background-color: linear-gradient(#1e90ff, #007bff); -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 14 8 14; -fx-background-radius:8; -fx-border-radius:8;");
        b.setPrefHeight(36);
        // keep width flexible
    }

    public static void styleSecondary(Button b) {
        if (b == null) return;
        b.setStyle("-fx-background-color: #f7f7f7; -fx-text-fill: #222; -fx-font-size: 14px; -fx-padding: 8 14 8 14; -fx-background-radius:8; -fx-border-radius:8; -fx-border-color:#e6e6e6; -fx-border-width:1;");
        b.setPrefHeight(36);
    }

    public static void styleDanger(Button b) {
        if (b == null) return;
        b.setStyle("-fx-background-color: linear-gradient(#ff5c5c, #dc3545); -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 14 8 14; -fx-background-radius:8; -fx-border-radius:8;");
        b.setPrefHeight(36);
    }

    public static void styleWarning(Button b) {
        if (b == null) return;
        b.setStyle("-fx-background-color: linear-gradient(#ffd54f, #ffc107); -fx-text-fill: #222; -fx-font-size: 14px; -fx-padding: 8 14 8 14; -fx-background-radius:8; -fx-border-radius:8;");
        b.setPrefHeight(36);
    }

    public static void styleHeaderLabel(Label l) {
        if (l == null) return;
        l.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #153859;");
    }

    // New helpers
    public static void styleSidebarLogo(ImageView img) {
        if (img == null) return;
        // keep the logo compact to avoid pushing the main content down
        img.setFitWidth(210);
        img.setFitHeight(160);
        img.setPreserveRatio(true);
        img.setSmooth(true);
        img.setCache(true);
    }

    public static void styleSidebarSubtitle(Label l) {
        if (l == null) return;
        // slightly larger and bold so the subtitle is clearly legible under the logo
        l.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280; -fx-font-weight: bold;");
    }

    public static void styleCard(Region r) {
        if (r == null) return;
        r.setStyle("-fx-background-color: linear-gradient(#ffffff, #fbfdff); -fx-padding: 12; -fx-background-radius:8; -fx-border-radius:8; -fx-border-color:#e6eef7; -fx-border-width:1;");
    }

    public static void styleInput(TextInputControl t) {
        if (t == null) return;
        t.setStyle("-fx-background-radius:6; -fx-border-radius:6; -fx-border-color:#e1e8f0; -fx-border-width:1; -fx-padding:8 10 8 10; -fx-font-size:13px;");
        t.setPrefHeight(36);
    }

    public static void styleComboBox(ComboBox<?> c) {
        if (c == null) return;
        c.setStyle("-fx-padding:6 10 6 10; -fx-font-size:13px; -fx-border-radius:6; -fx-background-radius:6; -fx-border-color:#e1e8f0; -fx-border-width:1;");
        c.setPrefHeight(36);
    }
}

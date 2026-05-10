package org.example.ignitron.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.ignitron.GameDetection.LauncherOption;

import java.util.*;

public class LauncherPickerController {
    @FXML
    private FlowPane launcherGrid;
    @FXML private Label countLabel;

    // Ordered map of game → checkbox so we can toggle all without rebuilding the grid
    private final Map<LauncherOption, CheckBox> checkBoxMap = new LinkedHashMap<>();

    // null = cancelled, non-null = user clicked Import (may be empty list)
    private Set<String> result = null;

    /**
     * Populates the picker grid. Must be called before the window is shown.
     */
    public void setOptions(List<LauncherOption> options) {
        checkBoxMap.clear();
        launcherGrid.getChildren().clear();

        for (LauncherOption option : options) {
            CheckBox cb = new CheckBox();
            cb.setSelected(true);
            checkBoxMap.put(option, cb);
            launcherGrid.getChildren().add(buildCard(option, cb));
        }

        updateCount();
    }

    /** Returns the launcher the user chose, or null if they cancelled. */
    public Set<String> getResult() {
        return result;
    }

    // ── Card builder ──────────────────────────────────────────────────────────

    private Node buildCard(LauncherOption option, CheckBox checkBox) {
        // Colored square with icon text (e.g. "S", "CF")
        Label iconLabel = new Label(option.getIconText());
        iconLabel.setStyle(
                "-fx-background-color: " + option.getBrandColor() + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 28px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-pref-width: 90px;" +
                        "-fx-pref-height: 90px;" +
                        "-fx-alignment: center;" +
                        "-fx-background-radius: 12px;"
        );

        // Launcher name
        Label name = new Label(option.getDisplayName());
        name.getStyleClass().add("picker-card-name");

        // Detected / Not found badge
        Label badge = new Label(option.isDetected() ? "● Detected" : "○ Not found");
        badge.getStyleClass().add(option.isDetected() ? "badge-detected" : "badge-not-found");

        VBox content = new VBox(8, iconLabel, name, badge);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(14, 10, 12, 10));

        // Checkbox — pre-checked only if detected
        checkBox.setSelected(option.isDetected());
        checkBox.getStyleClass().add("picker-checkbox");
        StackPane.setAlignment(checkBox, Pos.TOP_RIGHT);
        StackPane.setMargin(checkBox, new Insets(8, 8, 0, 0));

        StackPane card = new StackPane(content, checkBox);
        card.getStyleClass().add("picker-card");

        if (checkBox.isSelected()) {
            card.getStyleClass().add("picker-card-selected");
        }

        card.setPrefSize(156, 200);

        // Checkbox listener: update map, card border, and count label
        checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                if (!card.getStyleClass().contains("picker-card-selected")) {
                    card.getStyleClass().add("picker-card-selected");
                }
            } else {
                card.getStyleClass().remove("picker-card-selected");
            }
            updateCount();
        });

        // Clicking anywhere on the card (except directly on the checkbox) toggles it
        card.setOnMouseClicked(e -> {
            if (!(e.getTarget() instanceof CheckBox)) {
                checkBox.setSelected(!checkBox.isSelected());
            }
        });

        return card;
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private void updateCount() {
        long selected = checkBoxMap.values().stream().filter(CheckBox::isSelected).count();
        countLabel.setText(selected + " of " + checkBoxMap.size() + " selected");
    }

    // ── Button handlers ───────────────────────────────────────────────────────

    @FXML
    private void onSelectAll() {
        checkBoxMap.values().forEach(cb -> cb.setSelected(true));
    }

    @FXML
    private void onDeselectAll() {
        checkBoxMap.values().forEach(cb -> cb.setSelected(false));
    }

    @FXML
    private void onImport() {
        result = new HashSet<>();
        checkBoxMap.forEach((option, cb) -> {
            if (cb.isSelected()) result.add(option.getId());
        });
        close();
    }

    @FXML
    private void onSkip() {
        result = null;
        close();
    }

    private void close() {
        ((Stage) launcherGrid.getScene().getWindow()).close();
    }
}

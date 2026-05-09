package org.example.ignitron.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.ignitron.Game;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the CurseForge modpack selection dialog.
 * Displays all newly detected instances as selectable cards and returns
 * the user's choices via getResult() after the window closes.
 */
public class CurseForgePickerController {

    @FXML private FlowPane packGrid;
    @FXML private ScrollPane scrollPane;
    @FXML private Label countLabel;
    @FXML private Button importButton;

    // Ordered map of game → checkbox so we can toggle all without rebuilding the grid
    private final Map<Game, CheckBox> checkBoxMap = new LinkedHashMap<>();

    // null = cancelled, non-null = user clicked Import (may be empty list)
    private List<Game> result = null;

    /**
     * Populates the picker grid. Must be called before the window is shown.
     */
    public void setGames(List<Game> games) {
        checkBoxMap.clear();
        packGrid.getChildren().clear();

        for (Game game : games) {
            CheckBox cb = new CheckBox();
            cb.setSelected(true);
            checkBoxMap.put(game, cb);
            packGrid.getChildren().add(buildCard(game, cb));
        }

        updateCount();
    }

    /** Returns the games the user chose, or null if they cancelled. */
    public List<Game> getResult() {
        return result;
    }

    // ── Card builder ──────────────────────────────────────────────────────────

    private Node buildCard(Game game, CheckBox checkBox) {
        // Pack icon
        ImageView icon = new ImageView();
        icon.setFitWidth(116);
        icon.setFitHeight(116);
        icon.setPreserveRatio(true);
        if (game.getIcon() != null) {
            icon.setImage(game.getIcon());
        }

        // Pack name
        Label name = new Label(game.getName());
        name.getStyleClass().add("picker-card-name");

        VBox content = new VBox(10, icon, name);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(14, 10, 12, 10));

        // Checkbox pinned to top-right corner via StackPane overlay
        checkBox.getStyleClass().add("picker-checkbox");
        StackPane.setAlignment(checkBox, Pos.TOP_RIGHT);
        StackPane.setMargin(checkBox, new Insets(8, 8, 0, 0));

        StackPane card = new StackPane(content, checkBox);
        card.getStyleClass().addAll("picker-card", "picker-card-selected"); // start selected
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
        result = new ArrayList<>();
        checkBoxMap.forEach((game, cb) -> {
            if (cb.isSelected()) result.add(game);
        });
        close();
    }

    @FXML
    private void onCancel() {
        result = null; // null signals cancellation
        close();
    }

    private void close() {
        ((Stage) packGrid.getScene().getWindow()).close();
    }
}

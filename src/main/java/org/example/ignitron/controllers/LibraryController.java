package org.example.ignitron.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.example.ignitron.Config;
import org.example.ignitron.Game;
import org.example.ignitron.Library;
import org.example.ignitron.LibraryStorage;
import org.example.ignitron.GameDetection.curseforge.CurseForgeDetector;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LibraryController {

    @FXML private FlowPane gameGrid;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox loadingOverlay;

    private Library library;

    Config config = Config.load();

    @FXML private TextField nameField;
    @FXML private TextField pathField;
    @FXML private TextField iconField;
    @FXML private TextField tagsField;
    @FXML private Button addGameButton;


    public void initialize() {
        gameGrid.getChildren().clear();

        if(!config.isAutoAddDone()) {
            MainController.getInstance().showFirstBootPicker();
            config.setAutoAddDone(true);
            config.save();
        }
    }

    public void setLibrary(Library library) {
        this.library = library;
        refresh();
    }

    /** Shows or hides the loading overlay that sits on top of the game grid. */
    public void showLoading(boolean loading) {
        loadingOverlay.setVisible(loading);
        loadingOverlay.setManaged(loading);
    }

    public void refresh() {
        gameGrid.getChildren().clear();

        if (library != null) {
            for (Game game : library.getGames()) {

                Node card = createGameCard(game);
                gameGrid.getChildren().add(card);
            }
        }
    }

    public void search(String query) {
        if (library == null) return;

        // Filter results
        var results = library.filterByName(query);

        gameGrid.getChildren().clear();

        for (Game game : library.getGames()) {
            Node card = createGameCard(game);
            gameGrid.getChildren().add(card);
        }
    }

    public void addGameToLibrary(Game game) {
        if (library != null) {
            library.addGame(game);
            LibraryStorage.saveLibrary(library.getGames());
            refresh();
        }
    }

    public void removeGame(Game game) {
        library.removeGame(game);
        LibraryStorage.saveLibrary(library.getGames());
        refresh();
    }


    private void playGame(Game game) {
        try {
            ProcessBuilder pb;

            // Re-write the CurseForge launcher profile with fresh data before launching
            // so modloader version changes after a pack update are always picked up
            if ("curseforge".equals(game.getLauncher())) {
                new CurseForgeDetector().refreshProfile(game);
            }

            // Use the full launch command if one is set (e.g. CurseForge instances
            // need --workDir and --launch flags), otherwise just launch the exe directly
            if (game.getLaunchCommand() != null && !game.getLaunchCommand().isEmpty()) {
                pb = new ProcessBuilder(game.getLaunchCommand());
            } else {
                pb = new ProcessBuilder(game.getPath());
            }

            pb.start();
            game.setLastPlayed(LocalDateTime.now());
            refresh();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void onAddGameClicked() {

    }
        private Node createGameCard(Game game) {
            VBox card = new VBox(10);
            card.getStyleClass().add("game-card");
            card.setPrefSize(160, 220);
            card.setOnMouseClicked(e -> {
                if (e.getTarget() instanceof Button) return;
                MainController.getInstance().showGameDetails(game);
            });

            ImageView icon = new ImageView();
            icon.setFitWidth(136);
            icon.setFitHeight(136);

            if (game.getIcon() != null) {
                icon.setImage(game.getIcon());
            }

            Label name = new Label(game.getName());
            name.getStyleClass().add("game-card-name");

            Button playButton = new Button("Play");
            playButton.getStyleClass().add("card-play-button");
            playButton.setOnAction(e -> {
                e.consume();
                playGame(game);
            });

            card.getChildren().addAll(icon, name, playButton);

            return card;
        }
}

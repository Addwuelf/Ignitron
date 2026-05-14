package org.example.ignitron.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import org.example.ignitron.*;
import org.example.ignitron.GameDetection.curseforge.CurseForgeDetector;

import java.time.LocalDateTime;

import java.util.List;


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
    }

    public void setLibrary(Library library) {
        this.library = library;
        if (!config.isAutoAddDone()) {
            MainController.getInstance().showFirstBootPicker();
            config.setAutoAddDone(true);
            config.save();
        }
        refreshLibrary();
    }
    
    public void setconfig(Config config) {
        this.config = config;
    }

    /** Shows or hides the loading overlay that sits on top of the game grid. */
    public void showLoading(boolean loading) {
        loadingOverlay.setVisible(loading);
        loadingOverlay.setManaged(loading);
    }

    public void refresh(boolean starredOnly) {
        gameGrid.getChildren().clear();

        if (library == null) {
            Log.error("Library is null while refreshing", new Throwable());
            return;
        }

        for (Game game : library.getGames()) {
            if (starredOnly && !game.isFavorited()) continue;
            gameGrid.getChildren().add(createGameCard(game));
        }
    }


    public void search(String query) {
        if (library == null) return;

        gameGrid.getChildren().clear();

        List<Game> results = (query == null || query.isBlank())
                ? library.getGames()
                : library.filterByName(query);

        for (Game game : results) {
            if (config.isFavoriteToggled() && !game.isFavorited()) continue;
            gameGrid.getChildren().add(createGameCard(game));
        }
    }

    public void addGameToLibrary(Game game) {
        if (library != null) {
            library.addGame(game);
            LibraryStorage.saveLibrary(library.getGames());
            refreshLibrary();
        }
    }

    public void removeGame(Game game) {
        library.removeGame(game);
        LibraryStorage.saveLibrary(library.getGames());
        refreshLibrary();
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
            LibraryStorage.saveLibrary(library.getGames()); // persist lastPlayed
            refreshLibrary();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshLibrary() {
        refresh(config.isFavoriteToggled());
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

            Label star = new Label("★");
            star.getStyleClass().add("card-star");
            if (game.isFavorited()) star.getStyleClass().add("favorited");
            star.setVisible(game.isFavorited()); // hidden by default unless favorited
            StackPane.setAlignment(star, Pos.TOP_RIGHT);
            StackPane.setMargin(star, new Insets(10, 12, 0, 0));

            StackPane wrapper = new StackPane(card, star);
            star.setOnMouseClicked(e -> {
                e.consume(); // don't open game details
                game.setFavorited(!game.isFavorited());
                if (game.isFavorited()) {
                    star.getStyleClass().add("favorited");
                } else {
                    star.getStyleClass().remove("favorited");
                }
                LibraryStorage.saveLibrary(library.getGames());
                if (config.isFavoriteToggled()) refreshLibrary();
            });
            wrapper.setOnMouseEntered(e -> star.setVisible(true));
            wrapper.setOnMouseExited(e -> star.setVisible(game.isFavorited()));
            wrapper.setOnMouseClicked(e -> {
                if (e.getTarget() instanceof Button) return;
                MainController.getInstance().showGameDetails(game);
            });
            card.setOnMouseClicked(null);
            return wrapper;
        }
}

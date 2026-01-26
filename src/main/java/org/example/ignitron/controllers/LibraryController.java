package org.example.ignitron.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.example.ignitron.Game;
import org.example.ignitron.Library;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LibraryController {

    @FXML private FlowPane gameGrid;

    private Library library;

    @FXML private TextField nameField;
    @FXML private TextField pathField;
    @FXML private TextField iconField;
    @FXML private TextField tagsField;
    @FXML private Button addGameButton;


    public void initialize() {
        library = new Library();

        gameGrid.getChildren().clear();
        for (Game game : library.getGames()) {
            Node card = createGameCard(game);
            gameGrid.getChildren().add(card);
        }
    }

    public void setLibrary (Library library) {
        this.library = library;
        refresh();
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
            refresh();
        }
    }

    @FXML
    private void onAddGameClicked() {

    }
        private Node createGameCard(Game game) {
            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: #2a2a2a; -fx-padding: 10; -fx-background-radius: 8;");
            card.setPrefSize(150, 200);

            ImageView icon = new ImageView();
            icon.setFitWidth(130);
            icon.setFitHeight(130);

            // Load icon if you have one
            if (game.getIcon() != null) {
                icon.setImage(new Image(new File(game.getIcon()).toURI().toString()));
            }

            Label name = new Label(game.getName());
            name.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

            card.getChildren().addAll(icon, name);

            return card;
        }

//        String name = nameField.getText();
//        String path = pathField.getText();
//        String icon = iconField.getText();
//        Set<String> tags = new HashSet<String>(Arrays.asList(tagsField.getText().split(",")));
//
//        Game newGame = new Game(name, path, icon, tags);
//        addGame(newGame);
//
//        // Clear fields after adding
//        nameField.clear();
//        pathField.clear();
//        iconField.clear();
//        tagsField.clear();



}

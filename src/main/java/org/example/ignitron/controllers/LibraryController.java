package org.example.ignitron.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.example.ignitron.Game;
import org.example.ignitron.Library;

public class LibraryController {
@FXML
    private ListView<Game> gameList;

    private Library library;

    public void initalize () {
        library = new Library();
        gameList.getItems().addAll(library.getGames());
    }

    public void setLibrary (Library library) {
        this.library = library;
        refresh();
    }

    public void refresh() {
        if (library != null) {
            gameList.getItems().setAll(library.getGames());
        }
    }

    public void search(String query) {
        if (library == null) return;

        var results = library.filterByName(query);
        gameList.getItems().setAll(results);
    }

}

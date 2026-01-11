package org.example.ignitron.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.example.ignitron.IgnitronApplication;
import org.example.ignitron.Library;

import java.io.IOException;


public class MainController {
    @FXML
    private Button libraryButton;

    @FXML
    private Button settingsButton;

    @FXML
    private TextField searchField;

    @FXML
    private StackPane contentArea;

    private Library library;

    public void initialize() {
        library = new Library();

        loadView("org/example/ignitron/MainView.fxml");

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            LibraryController controller = getCurrentController();
            if (controller != null) {
                controller.search(newValue);
            }
        });
    }
    private void loadView(String path) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(IgnitronApplication.class.getResource(path));
            Node view = fxmlLoader.load();

            // Pass the shared library to controllers that need it
            Object controller = fxmlLoader.getController();
            if (controller instanceof LibraryController libController) {
                libController.setLibrary(library);
            }

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private LibraryController getCurrentController() {
        if (contentArea.getChildren().isEmpty()) return null;

        Node current = contentArea.getChildren().get(0);
        Object controller = current.getUserData();

        if (controller instanceof LibraryController libController) {
            return libController;
        }
        return null;
    }

    @FXML
    private void onLibraryClicked() {
        loadView("org/example/ignitron/LibraryView.fxml");
    }

    @FXML
    private void onSettingsClicked() {
        loadView("org/example/ignitron/SettingsView.fxml");
    }



}

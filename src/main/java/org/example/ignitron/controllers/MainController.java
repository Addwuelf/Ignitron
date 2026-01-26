package org.example.ignitron.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.example.ignitron.Game;
import org.example.ignitron.IgnitronApplication;
import org.example.ignitron.Library;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class MainController {
    @FXML
    private Button libraryButton;

    @FXML
    private Button settingsButton;

    @FXML
    private TextField searchField;

    @FXML
    private StackPane contentArea;

    @FXML private Button addGameButton;


    private Library library;


    public void initialize() {
        library = new Library();
        loadView("/org/example/ignitron/LibraryView.fxml");


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
            view.setUserData(controller);


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
    private void onAddGameClicked() {
        // Open Directory chooser dialog so user can pick folder to scan
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Stage stage = (Stage) addGameButton.getScene().getWindow();
        File folder = directoryChooser.showDialog(stage);

        if (folder != null) {
            ArrayList<File> executables = scanFolderForExecutables(folder);
            handleExecutables(executables, folder);
        }

    }

    // Scans folder to search for all .exe files
    private ArrayList<File> scanFolderForExecutables (File folder) {
        ArrayList<File> executables = new ArrayList<>();
        for (File file : folder.listFiles()) {
            if (file.getName().endsWith(".exe")) {
                executables.add(file);
            }
        }
        return executables;
    }

    private void handleExecutables (ArrayList<File> executables, File folder) {
        if (executables.isEmpty()) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("No Executables Found");
        alert.setHeaderText("This folder does not contain any .exe files");
    }
    else if (executables.size() == 1) {
       File file = executables.get(0);
        Game game = new Game(file.getPath(), folder);
        library.addGame(game);
        if (getCurrentController() != null) {
            getCurrentController().refresh();
        }
    }
    else {
        ArrayList<File> chosenGames = showMultiSelectExeDialog(executables);
        for (File file : chosenGames) {
            Game game = new Game(file.getPath(), folder);
            game.setName(file.getName());

            if (getCurrentController() != null) {
                getCurrentController().addGameToLibrary(game);
            }
        }
    }
    }

    private ArrayList<File> showMultiSelectExeDialog(List<File> executables) {
        // Create the dialog window
        Dialog<List<File>> dialog = new Dialog<>();
        dialog.setTitle("Choose Executables");
        dialog.setHeaderText("Select one or more executables for this folder.");

        // OK + Cancel buttons
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        // Create the ListView for multi-select
        ListView<File> listView = new ListView<>();
        listView.getItems().addAll(executables);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        dialog.getDialogPane().setPrefWidth(600);
        listView.setPrefWidth(580);

        // Put the ListView inside the dialog
        dialog.getDialogPane().setContent(listView);

        // Convert the result when OK is pressed
        dialog.setResultConverter(button -> {
            if (button == okButton) {
                return new ArrayList<>(listView.getSelectionModel().getSelectedItems());
            }
            return null; // user cancelled
        });

        // Show dialog and wait for result
        Optional<List<File>> result = dialog.showAndWait();
        return (ArrayList<File>) result.orElse(null);
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

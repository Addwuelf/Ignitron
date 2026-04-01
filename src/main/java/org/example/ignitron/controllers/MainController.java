package org.example.ignitron.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.example.ignitron.*;
import org.example.ignitron.GameDetection.ExeExtraction.ExeMetadataReader;
import org.example.ignitron.GameDetection.GameDetector;
import org.example.ignitron.GameDetection.LauncherInfo;
import org.example.ignitron.GameDetection.steam.SteamDetector;
import org.example.ignitron.GameDetection.steam.SteamRegistryReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;


public class MainController {
    @FXML
    private Button libraryButton;

    @FXML
    private Button settingsButton;

    @FXML
    private TextField searchField;

    @FXML
    private StackPane contentArea;

    @FXML
    private Button addGameButton;


    private Library library;

    private static MainController instance;

    Path steamPath = SteamRegistryReader.getSteamPath();

    GameDetector gameDetector = new GameDetector(
        new SteamDetector(steamPath),
        new ExeMetadataReader()
);


    public void initialize() {
        // Load saved library from disk
        List<Game> savedGames = LibraryStorage.loadLibrary();
        library = new Library();
        for (Game game : savedGames) {
            library.addGame(game);
        }

        // Load the default view
        loadView("/org/example/ignitron/LibraryView.fxml");


        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            LibraryController controller = getCurrentController();
            if (controller != null) {
                controller.search(newValue);
            }
        });
    }

    public MainController() {
        instance = this;
    }

    public static MainController getInstance() {
        return instance;
    }

    public void loadView(String path) {
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

    public void showGameDetails(Game game) {
        try {
            FXMLLoader loader = new FXMLLoader(IgnitronApplication.class.getResource(
                    "/org/example/ignitron/GameDetailsView.fxml"
            ));

            Node view = loader.load();

            // Get the controller for the game detail view
            GameDetailsController detailsController = loader.getController();
            detailsController.setGame(game);

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

    private void scanGameFolder(File folder) {
        ArrayList<File> foundFiles = new ArrayList<>();
        for (File file : folder.listFiles()) {
            if (file.getName().endsWith(".exe")) {
                foundFiles.add(file);
            } else if (file.isDirectory()) {
                scanGameFolder(file);
            }
        }

    }


    // Scans folder to search for all .exe files
    private ArrayList<File> scanFolderForExecutables(File folder) {
        ArrayList<File> executables = new ArrayList<>();
        Stack<File> stack = new Stack<>();
        stack.push(folder);

        while (!stack.isEmpty()) {
            File current = stack.pop();

            for (File file : current.listFiles()) {
                if (file.getName().endsWith(".exe")) {
                    executables.add(file);
                } else if (file.isDirectory()) {
                    stack.push(file);
                }
            }
        }

        return executables;
    }


    private void importDetectedGame(File exeFile) {
       try {
           LauncherInfo info = gameDetector.detectedGame(exeFile.toPath());
           Game game = new Game();

           if (info != null) {
               // Use Detection Pipline
               game.infoToGameObject(info);
               game.setPath(exeFile.getPath());
           }
           else {
               // Fallback: generic game
               game = new Game(exeFile.getPath(), exeFile.getParentFile());
               String name = exeFile.getName().replace(".exe", "");
               game.setName(name);
           }

           // Icon Extraction
           Image icon = IconExtractor.extract32Icon(exeFile.getPath());
           game.setIcon(icon);
           game.setIconPath(IconExtractor.saveIconToFile(icon, game.getName()));

           // Add to Library
           if (getCurrentController() != null) {
               getCurrentController().addGameToLibrary(game);
           }

       } catch (IOException e) {
           Log.error("Error importing Game through game detector", e);
       }

    }

    // Determines what to do with exe files. If not empty adds to library
    private void handleExecutables(ArrayList<File> executables, File folder) {
        if (executables.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("No Executables Found");
            alert.setHeaderText("This folder does not contain any .exe files");
            alert.show();
            return;
        }

        if (executables.size() == 1) {
            File file = executables.get(0);
            importDetectedGame(file);
            return;
        }

        ArrayList<File> chosenGames = showMultiSelectExeDialog(executables);

        for (File file : chosenGames) {
            if (file != null) {
                importDetectedGame(file);
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

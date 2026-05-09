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
import org.example.ignitron.GameDetection.curseforge.CurseForgeDetector;
import org.example.ignitron.GameDetection.epic.EpicDetector;
import org.example.ignitron.GameDetection.epic.EpicPathFinder;
import org.example.ignitron.GameDetection.steam.SteamDetector;
import org.example.ignitron.GameDetection.steam.SteamRegistryReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;


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
    Path epicPath = EpicPathFinder.getManifestDir();



    SteamDetector steamDetector;
    EpicDetector epicDetector;

    GameDetector gameDetector = new GameDetector(
            new SteamDetector(steamPath),
            new ExeMetadataReader(),
            new EpicDetector(epicPath)
    );


    public void initialize() {
        // Load saved library from disk
        List<Game> savedGames = LibraryStorage.loadLibrary();
        library = new Library();

        steamDetector = new SteamDetector(steamPath);
        epicDetector = new EpicDetector(epicPath);

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

    public Path getEpicPath() {
        return epicPath;
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

    // Manual, User has to pick folder to scan
    @FXML
    private void onAddGameClicked() {
        // Open Directory chooser dialog so user can pick folder to scan
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Stage stage = (Stage) addGameButton.getScene().getWindow();
        File folder = directoryChooser.showDialog(stage);

        if (folder != null) {
            ArrayList<File> executables = scanFolderForExecutables(folder, "Manually Added Game");

            handleExecutables(executables, folder);
        }
    }

    // On launch add all games automatically
    // Currently only Steam and Epic
    public void autoAddGames() {

        List<Game> gameList = new ArrayList<>();
        gameList.addAll(steamDetector.detectAllSteamGames());
        gameList.addAll(epicDetector.detectAllEpicGames());
        gameList.addAll(new CurseForgeDetector().detectAllInstances());

        for (Game game : gameList) {

            if (Objects.equals(game.getName(), "Steamworks Common Redistributables")) continue;

            // Only extract exe icons for games that don't already have one set.
            // CurseForge games load their icon from a URL so we skip extraction for them
            // to avoid overwriting it with the minecraft.exe icon.
            if (game.getIcon() == null && game.getPath() != null) {
                File exeFile = new File(game.getPath());
                Image icon = IconExtractor.extract32Icon(exeFile.getPath());
                game.setIcon(icon);
                game.setIconPath(IconExtractor.saveIconToFile(icon, game.getName()));
            }

            library.addGame(game);
        }
        LibraryStorage.saveLibrary(library.getGames());
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
    public ArrayList<File> scanFolderForExecutables(File folder, String manGameName) {
        ArrayList<File> executables = new ArrayList<>();

        // Safety: null folder
        if (folder == null) {
            Log.info("Folder is null");
            return executables;
        }

        // Prevent scanning system roots (Windows, Program Files, etc.)
        if (isSkippedRoot(folder)) {
            Log.info("Skipping root folder: " + folder.getAbsolutePath());
            return executables;
        }

        Path startPath = folder.toPath();

        try {
            Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    File f = dir.toFile();

                    // Skip redistributables, huge folders, unreadable folders
                    if (shouldSkipFolder(f)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    File f = file.toFile();
                    String name = f.getName().toLowerCase();

                    // Only consider .exe files
                    if (name.endsWith(".exe")) {
                        if (!shouldSkipExe(f, manGameName)) {
                            Log.info("Found executable: " + f.getAbsolutePath());
                            executables.add(f);
                        }
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    // Skip unreadable files silently
                    return FileVisitResult.CONTINUE;
                }

            });

        } catch (IOException e) {
            Log.error("Error scanning folder: " + folder.getAbsolutePath(), e);
        }

        return executables;
    }

    private boolean isSkippedRoot(File folder) {
        if (folder == null) return true;

        String path = folder.getAbsolutePath().toLowerCase();

        // Skip system roots only when the user selects them directly
        return path.equals("c:\\windows")
                || path.equals("c:\\program files")
                || path.equals("c:\\program files (x86)")
                || path.equals("c:\\programdata")
                || path.contains("\\appdata")
                || path.contains("$recycle.bin");
    }

    private boolean shouldSkipFolder(File folder) {
        if (folder == null) return true;

        String path = folder.getAbsolutePath().toLowerCase();
        String name = folder.getName().toLowerCase();

        // Skip system folders anywhere in the tree
        if (path.contains("\\windows")) return true;
        if (path.contains("\\programdata")) return true;
        if (path.contains("\\appdata")) return true;
        if (path.contains("$recycle.bin")) return true;

        // Skip redistributable folders
        String[] skipNames = {
                "commonredist", "redist", "redistributables",
                "directx", "dxredist", "_installer",
                "support", "prereqs", "vcredist", "crashreport"
        };

        for (String skip : skipNames) {
            if (name.contains(skip)) return true;
        }

        // Skip unreadable or huge folders
        File[] children = folder.listFiles();
        if (children == null) return true;

        return false;
    }

    private boolean shouldSkipExe(File file, String manGameName) {
        if (file == null) return true;

        String name = file.getName().toLowerCase();

        // Skip tiny EXEs unless they look like the real game
        if (file.length() < 1_000_000) {
            String gameName = file.getName().toLowerCase();

            // Allow small EXEs that contain the game name
            if (gameName.contains("factorygame") || gameName.contains(manGameName.toLowerCase()) || gameName.contains(file.getParentFile().getName().toLowerCase())) {
                return false;
            }

            // Allow small EXEs that are in common game folders
            String parent = file.getParentFile().getName().toLowerCase();
            if (parent.contains("win64") || parent.contains("win32") || parent.contains("binaries")) {
                return false;
            }

            // Otherwise skip small EXEs
            return true;
        }

        // Skip known junk EXEs
        String[] skipNames = {
                "setup", "install", "uninstall",
                "vc_redist", "dxsetup", "bootstrapper",
                "unitycrashhandler", "crashreport", "crashhandler",
                "beservice", "_be", "ui"
        };

        for (String skip : skipNames) {
            if (name.contains(skip)) return true;
        }

        // Skip helper/launcher tools
        String[] skipKeywords = {
                "launcher", "updater", "helper",
                "tool", "editor", "benchmark",
                "config", "compile", "util", "settings"
        };

        for (String skip : skipKeywords) {
            if (name.contains(skip)) return true;
        }

        return false;
    }


    private void importDetectedGame(File exeFile) {
        try {
            LauncherInfo info = gameDetector.detectedGame(exeFile.toPath());
            Game game = new Game();

            if (info != null) {
                // Use Detection Pipline
                game.infoToGameObject(info);
                game.setPath(exeFile.getPath());
            } else {
                // Fallback: generic game
                game = new Game(exeFile.getPath(), exeFile.getParentFile());
                String name = exeFile.getName().replace(".exe", "");
                game.setName(name);
            }

            // Icon Extraction
            Image icon = IconExtractor.extract32Icon(exeFile.getPath());
            game.setIcon(icon);
            if (icon != null) {
                game.setIconPath(IconExtractor.saveIconToFile(icon, game.getName()));
            }
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

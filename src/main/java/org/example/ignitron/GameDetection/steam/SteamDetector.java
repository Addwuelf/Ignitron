package org.example.ignitron.GameDetection.steam;

import org.example.ignitron.Game;
import org.example.ignitron.GameDetection.LauncherInfo;
import org.example.ignitron.GameDetection.LibraryParser;
import org.example.ignitron.Log;
import org.example.ignitron.controllers.MainController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SteamDetector {

    private final Path steamRoot;

    public SteamDetector(Path steamRoot) {
        this.steamRoot = steamRoot;
    }

    public SteamGameLocation findSteamGame(Path exePath) throws IOException {
        List<Path> libraries = LibraryParser.getSteamLibraries(steamRoot);

        for (Path library : libraries) {
            Path common = library.resolve("steamapps/common");


            if (exePath.startsWith(common)) {
                // we found the correct library
                Path relative = common.relativize(exePath);
                Path gameFolderName = relative.getName(0);
                Path gameFolder = common.resolve(gameFolderName);

                SteamGameLocation location = new SteamGameLocation();
                location.libraryPath = library.resolve("steamapps");
                location.commonPath = common;
                location.gameFolder = gameFolder;

                Log.info("EXE belongs to Steam Game folder: " + gameFolder);
                return location;
            }
        }

        return null;
    }

    public LauncherInfo detectSteamGame(Path exePath) throws IOException {
        SteamGameLocation location = findSteamGame(exePath);
        if (location == null) {
            return null;
        }

        List<SteamManifest> manifests = SteamManifestParser.loadManifests(location.libraryPath);

        for (SteamManifest manifest : manifests) {
            if (manifest != null &&
                    manifest.getInstallDir().equalsIgnoreCase(location.gameFolder.getFileName().toString())) {
                LauncherInfo info = new LauncherInfo("steam", manifest.getName(), location.gameFolder);
                info.setMetadata(Map.of(
                        "appid", manifest.getAppId(),
                        "installdir", manifest.getInstallDir()
                ));
                return info;
            }
        }
        return null;
    }

    public List<Game> detectAllSteamGames() {
        List<Game> games = new ArrayList<>();

        try {
            // Get the path of all steam libraries
            List<Path> libraries = LibraryParser.getSteamLibraries(steamRoot);

            Log.info("Found " + libraries.size() + " Steam libraries");

            for (Path library : libraries) {
                Path steamApps = library.resolve("steamapps");

                List<SteamManifest> manifests = SteamManifestParser.loadManifests(steamApps);

                for (SteamManifest manifest : manifests) {
                    if (manifest == null) continue;

                    Path gameFolder = steamApps.resolve("common").resolve(manifest.getInstallDir());

                    ArrayList<File> gameExes = MainController.getInstance().scanFolderForExecutables(gameFolder.toFile(), manifest.getName());

                    LauncherInfo info = new LauncherInfo("steam", manifest.getName(), gameFolder);

                    info.setMetadata(Map.of(
                            "appid", manifest.getAppId(),
                            "installdir", manifest.getInstallDir()
                    ));


                    Game game = new Game();
                    game.infoToGameObject(info);
                    File bestExe = pickBestGameExe(gameExes, manifest.getName());
                    if (bestExe != null) {
                        game.setPath(bestExe.getAbsolutePath());
                    }
                    games.add(game);
                }
            }
        }
        catch (IOException e) {
            Log.error("Failed to read Steam libraries", e);
        }
        return games;
    }

    private File pickBestGameExe(List<File> exes, String gameName) {
        if (exes == null || exes.isEmpty()) return null;

        String lowerGameName = gameName.toLowerCase();

        // 1. Highest priority: EXE that contains the game name
        for (File exe : exes) {
            String name = exe.getName().toLowerCase();
            if (name.contains(lowerGameName)) {
                return exe;
            }
        }

        // 2. Next: EXE in the root folder (not in subfolders)
        for (File exe : exes) {
            if (exe.getParentFile().getParentFile() == null) continue;
            if (exe.getParentFile().equals(exe.getParentFile().getParentFile())) {
                return exe;
            }
        }

        // 3. Next: EXE in common game folders
        String[] goodFolders = { "win64", "win32", "binaries", "bin", "game" };
        for (File exe : exes) {
            String parent = exe.getParentFile().getName().toLowerCase();
            for (String good : goodFolders) {
                if (parent.contains(good)) {
                    return exe;
                }
            }
        }

        // 4. Next: Largest EXE (most games have a large main EXE)
        File largest = exes.get(0);
        for (File exe : exes) {
            if (exe.length() > largest.length()) {
                largest = exe;
            }
        }
        return largest;
    }
}

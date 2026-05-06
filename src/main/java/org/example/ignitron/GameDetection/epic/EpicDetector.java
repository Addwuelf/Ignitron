package org.example.ignitron.GameDetection.epic;

import org.example.ignitron.Game;
import org.example.ignitron.GameDetection.LauncherInfo;
import org.example.ignitron.GameDetection.steam.SteamManifest;
import org.example.ignitron.GameDetection.steam.SteamManifestParser;
import org.example.ignitron.Log;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EpicDetector {
    private final Path epicManifestPath;

    public EpicDetector(final Path epicManifests) { this.epicManifestPath = epicManifests; }

    public LauncherInfo detectEpicGame(Path exePath, Path epicManifestPath) throws IOException {
        List<EpicManifest> manifests = EpicManifestParser.loadManifests(epicManifestPath);

        for (EpicManifest manifest : manifests) {
            if (Path.of(manifest.getLaunchExecutable()).equals(exePath)) {
                LauncherInfo info = new LauncherInfo("epic", manifest.getDisplayName(),Path.of(manifest.getInstallLoaction()));
                info.setMetadata(Map.of(
                        "appid", manifest.getAppID(),
                        "installdir", manifest.getInstallLoaction()
                ));

                Log.info("Exe Belongs to Epic Game Folder: " + manifest.getInstallLoaction());

                return info;
            }
        }
        return null;
    }

    public List<Game> detectAllEpicGames() {
        List<Game> games = new ArrayList<>();

        // Get all manifests
        List<EpicManifest> manifests = EpicManifestParser.loadManifests(epicManifestPath);

        // Loads all data from manifests
        for (EpicManifest manifest : manifests) {
            if (manifest == null) continue;

            // Build info object
            LauncherInfo info = new LauncherInfo("epic", manifest.getDisplayName(), Path.of(manifest.getInstallLoaction()));
            info.setMetadata(Map.of(
                    "appid", manifest.getAppID(),
                    "installdir", manifest.getInstallLoaction()
            ));

            // Build game object
            Game game = new Game();
            game.infoToGameObject(info);
            game.setPath(manifest.getInstallLoaction());
            games.add(game);
        }

        return games;
    }



}

package org.example.ignitron.GameDetection.steam;

import org.example.ignitron.GameDetection.LauncherInfo;
import org.example.ignitron.GameDetection.LibraryParser;
import org.example.ignitron.Log;

import java.io.IOException;
import java.nio.file.Path;
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
}

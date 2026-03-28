package org.example.ignitron.GameDetection.steam;

import org.example.ignitron.GameDetection.LauncherInfo;
import org.example.ignitron.GameDetection.LibraryParser;
import org.example.ignitron.Log;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class SteamDetector {

    private static final Path DEFAULT_STEAM =
            Path.of("C:/Program Files (x86)/Steam");

    public static SteamGameLocation findSteamGame(Path exePath) throws IOException {
        List<Path> libraries = LibraryParser.getSteamLibraries(DEFAULT_STEAM);

        for (Path library : libraries) {
            Path common = library.resolve("common");

            if (exePath.startsWith(common)) {
                // we found the correct library
                Path relative = common.relativize(exePath);
                Path gameFolderName = relative.getName(0);
                Path gameFolder = common.resolve(gameFolderName);

                SteamGameLocation location = new SteamGameLocation();
                location.libraryPath = library;
                location.commonPath = common;
                location.gameFolder = gameFolder;

                Log.info("EXE belongs to Steam Game folder: " + gameFolder);
                return location;
            }
        }

        return null;
    }

    public static LauncherInfo detectSteamGame(Path exePath) throws IOException {
        SteamGameLocation location = findSteamGame(exePath);
        if (location == null) {
            return null;
        }

        List<SteamManifest> manifests = SteamManifestParser.loadManifests(location.libraryPath);

        for (SteamManifest manifest : manifests) {
            if (manifest != null &&
                    manifest.getInstallDir().equalsIgnoreCase(location.gameFolder.toString())) {
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

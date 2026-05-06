package org.example.ignitron.GameDetection.epic;

import org.example.ignitron.Log;

import java.nio.file.Path;

public class EpicPathFinder {

    static private Path manifestDir = Path.of("C:\\ProgramData\\Epic\\EpicGamesLauncher\\Data\\Manifests");

    static public Path getManifestDir() {
        if (manifestDir == null) {
            Log.warn("Failed to get manifest dir");

            return null;
        }

        return manifestDir;
    }
}

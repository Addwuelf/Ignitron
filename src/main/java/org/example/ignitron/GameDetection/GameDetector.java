package org.example.ignitron.GameDetection;

import org.example.ignitron.GameDetection.ExeExtraction.ExeMetadata;
import org.example.ignitron.GameDetection.ExeExtraction.ExeMetadataReader;
import org.example.ignitron.GameDetection.steam.SteamDetector;
import org.example.ignitron.GameDetection.steam.SteamGameLocation;
import org.example.ignitron.Log;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class GameDetector {

    private final SteamDetector steamDetector;
    private final ExeMetadataReader metadata;

    public GameDetector(SteamDetector steamDetector, ExeMetadataReader metadata) {
        this.steamDetector = steamDetector;
        this.metadata = metadata;
    }



    public LauncherInfo detectedGame(Path exePath) throws IOException {
        // TODO create pipline steps

        // 1. Try Steam
        LauncherInfo steam = steamDetector.detectSteamGame(exePath);
        if (steam != null) {
            return steam;
        }


        // Step 2: Metadata Extraction
        ExeMetadata data = metadata.read(exePath);
        if (data != null && data.getProductName() != null) {
           LauncherInfo info = new LauncherInfo("Unknown", data.getProductName(), exePath.getParent());
           info.setMetadata(Map.of("source", "exe-metadata"));
           return info;
        }

        return null;
    }
}

package org.example.ignitron.GameDetection;

import org.example.ignitron.GameDetection.ExeExtraction.ExeMetadata;
import org.example.ignitron.GameDetection.ExeExtraction.ExeMetadataReader;

import java.nio.file.Path;

public class GameDetector {

    public DetectedGame detectedGame(Path exePath) {
        // TODO create pipline steps

        // Step 1: Metadata Extraction
        ExeMetadata data = ExeMetadataReader.read(exePath);
        if (data != null && data.getProductName() != null) {
            return new DetectedGame(meta.getProductName(), exePath, exePath.getParent(), null, data.toMap());
        }

        // Step 2: Launcher Detection
        LauncherInfo launcher = LauncherDetector.detect(exePath);
        if (launcher != null) {
            return new DetectedGame(
                    launcher.getGameName(),
                    exePath,
                    launcher.getInstallFolder(),
                    launcher.getLauncherName(),
                    launcher.getMetadata()
            );
        }
        return  null;
    }
}

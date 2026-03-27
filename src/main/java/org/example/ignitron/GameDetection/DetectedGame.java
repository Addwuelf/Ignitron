package org.example.ignitron.GameDetection;

import java.nio.file.Path;
import java.util.Map;

public class DetectedGame {
    private String gameName;
    private Path gamePath;
    private Path installFolder;
    private String launcher;
    private Map<String, String> metadata;

    public DetectedGame(String gameName, Path gamePath, Path installFolder, String launcher) {
        this.gameName = gameName;
        this.gamePath = gamePath;
        this.installFolder = installFolder;
        this.launcher = launcher;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}

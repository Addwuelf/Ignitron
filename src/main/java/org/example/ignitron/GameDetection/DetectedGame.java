package org.example.ignitron.GameDetection;

import java.nio.file.Path;
import java.util.Map;

public class DetectedGame {
    private String gameName;
    private Path gamePath;
    private Path installFolder;
    private String launcher;
    private Map<String, String> metadata;
}

package org.example.ignitron.GameDetection;

import java.io.IOException;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LauncherDetector {
    private static final Path DEFAULT_STEAM =
            Path.of("C:/Program Files (x86)/Steam");
    private List<Path> steamLibraries = new ArrayList<>();

    public void detectSteam(Path exePath) throws IOException {
        steamLibraries = LibraryParser.getSteamLibraries(exePath);
    }

    public LauncherInfo detect() {
        return null;
    }
}

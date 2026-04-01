package org.example.ignitron.GameDetection;

import org.example.ignitron.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LibraryParser {

    public static List<Path> getSteamLibraries(Path steamRoot) throws IOException {
        Path steamLibraryPath = steamRoot.resolve("steamapps/libraryfolders.vdf");

        List<Path> paths = new ArrayList<>();

        Log.info("Scanning Steam Library file: " + steamRoot);

        // Create a new BufferedReader using path
        try (BufferedReader reader = Files.newBufferedReader(steamLibraryPath)) {
            String line;

            //loop until end of file
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("\"path\"")) {
                    Log.fine("Found path entry");
                    // Get First index of library path
                    int firstindex = line.indexOf('"', 6);
                    // Find second index of library path
                    int secondindex = line.indexOf('"', firstindex + 1);

                    if (firstindex != -1 && secondindex != -1) {
                           // Create string from indexes
                            String raw = line.substring(firstindex + 1, secondindex);

                            Log.fine("Found path: " + raw);
                            paths.add(Path.of(raw));
                    }
                    else {
                        Log.warn("Invalid path format: " + line);
                    }

                }
            }
        }
        catch (IOException e) {
            Log.error("Failed to read Steam Library File: " + steamRoot, e);
        }

        return paths;
    }
}


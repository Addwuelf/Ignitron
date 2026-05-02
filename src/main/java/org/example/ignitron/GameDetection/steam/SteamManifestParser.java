package org.example.ignitron.GameDetection.steam;

import org.example.ignitron.Log;

import java.io.BufferedReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SteamManifestParser {
    public static List<SteamManifest> loadManifests(Path steamAppsFolder) {
        List<SteamManifest> manifests = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(steamAppsFolder, "appmanifest_*.acf")) {
            for (Path file : stream) {
                SteamManifest manifest = parseManifest(file);
                if (manifest != null) {
                    manifests.add(manifest);
                }
            }
        }
        catch (Exception e) {
            Log.error("Failed to read Steam manifests in: " + steamAppsFolder, e );
        }
        // - parse each one
        return manifests;
    }

    private static SteamManifest parseManifest(Path file) {
        SteamManifest manifest = new SteamManifest();

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("\"appid\"")) {
                    manifest.setAppId(extractValue(line, 7));
                }
                else if (line.startsWith("\"name\"")) {
                    manifest.setName(extractValue(line, 6));
                }
                else if (line.startsWith("\"installdir\"")) {
                    manifest.setInstallDir(extractValue(line, 13));
                }
            }
        }
        catch (Exception e) {
            Log.error("Failed to parse manifest: " + file, e);
        }

        // - extract "name" and "installdir"
        if (manifest.getInstallDir() != null) {
            Log.info("Parsed manifest: " + manifest.getAppId() + " - " + manifest.getName() + " - " + manifest.getInstallDir());
        }
        return manifest;
    }

    private static String extractValue(String line, int keyLength) {
        int firstQuote = line.indexOf('"', keyLength);
        int secondQuote = line.indexOf('"', firstQuote + 1);
        if (firstQuote != -1 && secondQuote != -1) {
            return line.substring(firstQuote + 1, secondQuote);
        }
        Log.warn("Invalid manifest format: " + line);
        return null;
    }

}

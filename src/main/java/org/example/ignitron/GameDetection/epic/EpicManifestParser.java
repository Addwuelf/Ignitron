package org.example.ignitron.GameDetection.epic;

import com.google.gson.Gson;

import org.example.ignitron.Log;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class EpicManifestParser {
    public static List<EpicManifest> loadManifests(final Path manifestsDir) {
        List<EpicManifest> manifests = new ArrayList<>();

        try (Stream<Path> files = Files.list(manifestsDir)) {
            files.filter(p -> p.toString().endsWith(".item"))
                    .forEach(p -> {
                        EpicManifest manifest = parseManifest(p);
                        if (manifest != null) {
                            manifests.add(manifest);
                        }
                    });
        } catch (IOException e) {
            Log.error("Failed to load manifests from " + manifestsDir + "!", e);
            return null;
        }
        return manifests;
    }

    private static EpicManifest parseManifest(Path file) {
        EpicManifest manifest = null;

        try (Reader reader = Files.newBufferedReader(file)) {
            Gson gson = new Gson();
            manifest = gson.fromJson(reader, EpicManifest.class);
        } catch (IOException e) {
            Log.error("Failed to Parse Manifest: " + file, e);
        }

        if (manifest != null && manifest.getDisplayName() != null) {
            Log.info("Parsed manifest: " + manifest.getAppID() + " - " + manifest.getDisplayName() + " - " + manifest.getInstallLoaction());
        }

        return manifest;
    }
}

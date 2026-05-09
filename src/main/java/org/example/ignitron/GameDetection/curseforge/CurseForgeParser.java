package org.example.ignitron.GameDetection.curseforge;

import com.google.gson.Gson;
import org.example.ignitron.Log;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Handles JSON parsing for CurseForge instance files.
 * Gson only populates fields that are mapped in the target class —
 * unmapped fields (e.g. the large library data in minecraftinstance.json) are ignored.
 */
public class CurseForgeParser {

    /**
     * Parses a CurseForge manifest.json file into a CurseForgeManifest object.
     * Contains the pack name, MC version, and modloader info.
     */
    public static CurseForgeManifest parseManifest(File file) {
        try (Reader reader = new FileReader(file)) {
            return new Gson().fromJson(reader, CurseForgeManifest.class);
        } catch (IOException e) {
            Log.error("Failed to parse CurseForge manifest: " + file, e);
            return null;
        }
    }

    /**
     * Parses a minecraftinstance.json file into a CurseForgeInstance object.
     * This file can be very large (2MB+) but Gson only reads the fields we need.
     * Used primarily to get the thumbnail URL for the pack icon.
     */
    public static CurseForgeInstance parseInstance(File file) {
        try (Reader reader = new FileReader(file)) {
            return new Gson().fromJson(reader, CurseForgeInstance.class);
        } catch (IOException e) {
            Log.error("Failed to parse minecraftinstance.json: " + file, e);
            return null;
        }
    }
}

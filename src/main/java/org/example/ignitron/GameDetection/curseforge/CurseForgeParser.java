package org.example.ignitron.GameDetection.curseforge;

import com.google.gson.Gson;
import org.example.ignitron.Log;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class CurseForgeParser {
    public static CurseForgeManifest parseManifest(File file) {
        try (Reader reader = new FileReader(file)) {
            return new Gson().fromJson(reader, CurseForgeManifest.class);
        } catch (IOException e) {
            Log.error("Failed to parse CurseForge manifest: " + file, e);
            return null;
        }
    }

    public static CurseForgeInstance parseInstance(File file) {
        try (Reader reader = new FileReader(file)) {
            return new Gson().fromJson(reader, CurseForgeInstance.class);
        } catch (IOException e) {
            Log.error("Failed to parse minecraftinstance.json: " + file, e);
            return null;
        }
    }
}

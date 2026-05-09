package org.example.ignitron.GameDetection.curseforge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.example.ignitron.Log;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.time.Instant;

/**
 * Writes a Minecraft launcher profile into CurseForge's own launcher_profiles.json.
 * This allows minecraft.exe to launch a specific modpack instance directly via --launch.
 *
 * CurseForge stores its own Minecraft installation separately from the official launcher
 * at: %USERPROFILE%/curseforge/minecraft/Install/
 */
public class CurseForgeProfileWriter {

    private static final String PROFILES_PATH =
            new File(System.getProperty("user.home"), "curseforge/minecraft/Install/launcher_profiles.json")
                    .getAbsolutePath();

    private static final String LIBRARY_DIR =
            new File(System.getProperty("user.home"), "curseforge/minecraft/Install/libraries")
                    .getAbsolutePath();

    // Default RAM to allocate if the pack doesn't specify a recommendation
    private static final int DEFAULT_RAM_MB = 4096;

    /**
     * Creates or updates a profile entry in CurseForge's launcher_profiles.json.
     * Uses JsonObject directly so any existing fields we don't know about are preserved.
     *
     * @param profileName  The instance folder name — used as both the profile key and display name
     * @param gameDir      Full path to the instance folder
     * @param versionId    Modloader version string (e.g. "neoforge-21.1.219")
     * @param ramMb        Allocated RAM in MB (0 = use default)
     * @param instanceId   CurseForge instance UUID for -DCFInstanceId JVM arg
     */
    public static void writeProfile(String profileName, String gameDir,
                                    String versionId, int ramMb, String instanceId) {
        File profilesFile = new File(PROFILES_PATH);
        if (!profilesFile.exists()) {
            Log.error("CurseForge launcher_profiles.json not found at: " + PROFILES_PATH, null);
            return;
        }

        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            // Read the full JSON as a raw object to avoid losing unknown top-level fields
            JsonObject root;
            try (Reader reader = new FileReader(profilesFile)) {
                root = gson.fromJson(reader, JsonObject.class);
            }

            JsonObject profiles = root.getAsJsonObject("profiles");
            if (profiles == null) {
                profiles = new JsonObject();
                root.add("profiles", profiles);
            }

            int allocatedRam = (ramMb > 0) ? ramMb : DEFAULT_RAM_MB;

            // Strip trailing slash from gameDir for the TargetDirectory arg
            String gameDirClean = (gameDir.endsWith("\\") || gameDir.endsWith("/"))
                    ? gameDir.substring(0, gameDir.length() - 1)
                    : gameDir;

            // Build JVM args matching the format CurseForge uses for its own profiles
            String javaArgs = "-Xmx" + allocatedRam + "m -Xms256m" +
                    " -Dminecraft.applet.TargetDirectory=\"" + gameDirClean + "\"" +
                    " -Dfml.ignorePatchDiscrepancies=true" +
                    " -Dfml.ignoreInvalidMinecraftCertificates=true" +
                    " -Duser.language=en -Duser.country=US" +
                    " -DCFInstanceId=" + instanceId +
                    " -DlibraryDirectory=\"" + LIBRARY_DIR + "\"";

            // Build the profile entry
            JsonObject profile = new JsonObject();
            profile.addProperty("created", Instant.now().toString());
            profile.addProperty("gameDir", gameDir);
            profile.addProperty("javaArgs", javaArgs);
            profile.addProperty("lastVersionId", versionId);
            profile.addProperty("name", profileName);
            profile.addProperty("type", "custom");

            // Insert or overwrite the profile using the instance folder name as the key
            profiles.add(profileName, profile);

            try (Writer writer = new FileWriter(profilesFile)) {
                gson.toJson(root, writer);
            }

            Log.info("Wrote CurseForge launcher profile: " + profileName);

        } catch (IOException e) {
            Log.error("Failed to write CurseForge launcher profile: " + profileName, e);
        }
    }
}

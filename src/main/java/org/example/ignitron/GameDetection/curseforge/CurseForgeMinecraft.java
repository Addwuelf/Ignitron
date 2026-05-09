package org.example.ignitron.GameDetection.curseforge;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Maps to the "minecraft" block inside manifest.json.
 * Contains the Minecraft version and the list of modloaders used by the pack.
 *
 * Example JSON:
 * "minecraft": {
 *   "version": "1.21.1",
 *   "modLoaders": [{ "id": "neoforge-21.1.219", "primary": true }]
 * }
 */
public class CurseForgeMinecraft {

    // Minecraft version string (e.g. "1.21.1")
    @SerializedName("version")
    private String version;

    // List of modloaders — most packs only have one (Forge, NeoForge, Fabric, etc.)
    @SerializedName("modLoaders")
    private List<CurseForgeModLoader> modLoaders;

    public String getVersion() { return version; }
    public List<CurseForgeModLoader> getModLoaders() { return modLoaders; }
}

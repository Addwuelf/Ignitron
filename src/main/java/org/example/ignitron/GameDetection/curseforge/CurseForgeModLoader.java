package org.example.ignitron.GameDetection.curseforge;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a single modloader entry from the manifest.json modLoaders array.
 * The id includes both the loader type and version (e.g. "neoforge-21.1.219").
 */
public class CurseForgeModLoader {

    // Full modloader identifier, e.g. "neoforge-21.1.219" or "forge-47.3.0"
    @SerializedName("id")
    private String id;

    public String getId() { return id; }
}

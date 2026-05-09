package org.example.ignitron.GameDetection.curseforge;

import com.google.gson.annotations.SerializedName;

/**
 * Maps to the "baseModLoader" block inside minecraftinstance.json.
 * Present on custom instances that don't have a manifest.json —
 * it holds the active modloader version string (e.g. "neoforge-21.1.219").
 */
public class CurseForgeBaseModLoader {

    // Full modloader version identifier, matching the lastVersionId used in launcher profiles
    @SerializedName("name")
    private String name;

    public String getName() { return name; }
}

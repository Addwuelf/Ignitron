package org.example.ignitron.GameDetection.curseforge;

import com.google.gson.annotations.SerializedName;

/**
 * Maps to the "installedModpack" block inside minecraftinstance.json.
 * Contains CurseForge-specific metadata about the installed pack,
 * including the URL for the pack's thumbnail image.
 */
public class CurseForgeInstalledModpack {

    // URL pointing to the pack's icon on the CurseForge CDN (media.forgecdn.net)
    // This is the same image shown in the CurseForge app
    @SerializedName("thumbnailUrl")
    private String thumbnailUrl;

    public String getThumbnailUrl() { return thumbnailUrl; }
}

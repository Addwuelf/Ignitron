package org.example.ignitron.GameDetection.curseforge;

import com.google.gson.annotations.SerializedName;

/**
 * Maps to the "installedModpack" block inside minecraftinstance.json.
 * Contains CurseForge-specific metadata about the installed pack,
 * including the thumbnail URL and the instance UUID used in launch args.
 */
public class CurseForgeInstalledModpack {

    // URL pointing to the pack's icon on the CurseForge CDN (media.forgecdn.net)
    @SerializedName("thumbnailUrl")
    private String thumbnailUrl;

    // Unique ID for this instance — passed as -DCFInstanceId in the JVM args
    @SerializedName("instanceID")
    private String instanceID;

    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getInstanceID() { return instanceID; }
}

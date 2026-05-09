package org.example.ignitron.GameDetection.curseforge;

import com.google.gson.annotations.SerializedName;

/**
 * Maps to the fields we need from minecraftinstance.json.
 * This file is very large (2MB+) as it contains embedded modloader version data,
 * but Gson only populates the fields declared here — the rest is ignored.
 *
 * Custom instances (not downloaded from the CurseForge app) won't have a manifest.json,
 * so their name, MC version, and modloader are read from this file instead.
 */
public class CurseForgeInstance {

    // Instance display name — used as the game name when manifest.json is absent
    @SerializedName("name")
    private String name;

    // Full path to the instance folder on disk
    @SerializedName("installPath")
    private String installPath;

    // Minecraft version string (e.g. "1.21.1") — used for tagging on custom instances
    @SerializedName("gameVersion")
    private String gameVersion;

    // Active modloader on custom instances (e.g. "neoforge-21.1.219")
    @SerializedName("baseModLoader")
    private CurseForgeBaseModLoader baseModLoader;

    // Contains CurseForge metadata for marketplace packs, including the thumbnail URL
    @SerializedName("installedModpack")
    private CurseForgeInstalledModpack installedModpack;

    public String getName() { return name; }
    public String getInstallPath() { return installPath; }
    public String getGameVersion() { return gameVersion; }
    public CurseForgeBaseModLoader getBaseModLoader() { return baseModLoader; }
    public CurseForgeInstalledModpack getInstalledModpack() { return installedModpack; }
}

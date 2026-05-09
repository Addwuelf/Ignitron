package org.example.ignitron.GameDetection.curseforge;

import com.google.gson.annotations.SerializedName;

/**
 * Maps to the fields we need from minecraftinstance.json.
 * This file is very large (2MB+) as it contains embedded modloader version data,
 * but Gson only populates the fields declared here — the rest is ignored.
 */
public class CurseForgeInstance {

    // Full path to the instance folder on disk
    @SerializedName("installPath")
    private String installPath;

    // Contains CurseForge metadata for the installed modpack, including the thumbnail URL
    @SerializedName("installedModpack")
    private CurseForgeInstalledModpack installedModpack;

    public String getInstallPath() { return installPath; }
    public CurseForgeInstalledModpack getInstalledModpack() { return installedModpack; }
}

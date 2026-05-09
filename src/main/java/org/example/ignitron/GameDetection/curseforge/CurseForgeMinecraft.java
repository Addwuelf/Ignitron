package org.example.ignitron.GameDetection.curseforge;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CurseForgeMinecraft {
    @SerializedName("version")
    private String version;

    @SerializedName("modLoaders")
    private List<CurseForgeModLoader> modLoaders;

    public String getVersion() { return version; }
    public List<CurseForgeModLoader> getModLoaders() { return modLoaders; }
}

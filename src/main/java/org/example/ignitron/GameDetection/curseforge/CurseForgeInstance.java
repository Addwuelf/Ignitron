package org.example.ignitron.GameDetection.curseforge;

import com.google.gson.annotations.SerializedName;

public class CurseForgeInstance {
    @SerializedName("installPath")
    private String installPath;

    @SerializedName("installedModpack")
    private CurseForgeInstalledModpack installedModpack;

    public String getInstallPath() {return installPath;}
    public CurseForgeInstalledModpack getInstalledModpack() {return installedModpack;}

}

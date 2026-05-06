package org.example.ignitron.GameDetection.epic;

import com.google.gson.annotations.SerializedName;

public class EpicManifest {
    @SerializedName("DisplayName")
    private String displayName;

    @SerializedName("InstallLocation")
    private String installLocation;

    @SerializedName("LaunchExecutable")
    private String launchExecutable;

    // Holds Epic app ID
    @SerializedName("AppName")
    private String appName;

    public String getDisplayName() {
        return displayName;
    }

    public String getInstallLoaction() {
        return installLocation;
    }

    public String getLaunchExecutable() {
        return launchExecutable;
    }

    // Returns App ID
    public String getAppID() {
        return appName;
    }


}

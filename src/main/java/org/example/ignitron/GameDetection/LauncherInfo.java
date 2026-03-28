package org.example.ignitron.GameDetection;

import java.nio.file.Path;
import java.util.Map;

public class LauncherInfo {
    public String launcherName;
    public String gameName;
    public Path installFolder;
    public Map<String, String> metadata;

    public LauncherInfo(String launcherName, String gameName, Path installFolder) {
        this.launcherName = launcherName;
        this.gameName = gameName;
        this.installFolder = installFolder;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}

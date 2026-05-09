package org.example.ignitron.GameDetection.curseforge;

import com.google.gson.annotations.SerializedName;

public class CurseForgeInstalledModpack {
    @SerializedName("thumbnailUrl")
    private String thumbnailUrl;

    public String getThumbnailUrl() {return thumbnailUrl;}
}

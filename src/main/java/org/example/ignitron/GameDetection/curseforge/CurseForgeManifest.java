package org.example.ignitron.GameDetection.curseforge;

import com.google.gson.annotations.SerializedName;

public class CurseForgeManifest {
    @SerializedName("name")
    private String name;

    @SerializedName("author")
    private String author;

    @SerializedName("minecraft")
    private CurseForgeMinecraft minecraft;

    public String getName() {return name;}
    public String getAuthor() {return author;}
    public CurseForgeMinecraft getMinecraft() {return minecraft;}

}

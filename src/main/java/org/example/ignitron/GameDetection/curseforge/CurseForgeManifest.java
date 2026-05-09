package org.example.ignitron.GameDetection.curseforge;

import com.google.gson.annotations.SerializedName;

/**
 * Maps to the manifest.json file found in each CurseForge instance folder.
 * Contains the modpack name, author, and Minecraft version/modloader info.
 */
public class CurseForgeManifest {

    // The display name of the modpack (e.g. "All the Mons")
    @SerializedName("name")
    private String name;

    // The modpack author or team name (e.g. "ATMTeam")
    @SerializedName("author")
    private String author;

    // Nested object containing the Minecraft version and modloader details
    @SerializedName("minecraft")
    private CurseForgeMinecraft minecraft;

    public String getName() { return name; }
    public String getAuthor() { return author; }
    public CurseForgeMinecraft getMinecraft() { return minecraft; }
}

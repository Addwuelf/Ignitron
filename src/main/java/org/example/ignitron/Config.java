package org.example.ignitron;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {

    static Path CONFIG_PATH =
            Paths.get(System.getenv("APPDATA"), "Ignitron", "config.json");

    private boolean autoAddDone;

    public boolean isAutoAddDone() {
        return autoAddDone;
    }

    public void setAutoAddDone(boolean autoAddDone) {
        this.autoAddDone = autoAddDone;
    }

    public static Config load() {
    try {
         if(!Files.exists(CONFIG_PATH)) return new Config();
         String json = Files.readString(CONFIG_PATH);
         Gson gson = new GsonBuilder().create();
         return gson.fromJson(json, Config.class);
     }
     catch (Exception e) {
         Log.error("Failed to load config file!", e);
         return new Config();
     }
    }

    public void save() {
        // write back to disk
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
            String json = gson.toJson(this);
            Files.writeString(CONFIG_PATH, json);
        }
        catch (Exception e) {
            Log.error("Failed to load config file!", e);
        }
    }
}

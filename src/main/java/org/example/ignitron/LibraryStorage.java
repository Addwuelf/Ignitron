package org.example.ignitron;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LibraryStorage {

    static Path LIBRARY_PATH =
            Paths.get(System.getenv("APPDATA"), "Ignitron", "library.json");

    // Use for testing only
    static void setLibraryPathForTesting(Path path) {
        LIBRARY_PATH = path;
    }

    public static void saveLibrary(List<Game> games) {
        try {
            Files.createDirectories(LIBRARY_PATH.getParent());
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .setPrettyPrinting()
                    .create();

            String json = gson.toJson(games);
            Files.writeString(LIBRARY_PATH, json);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Game> loadLibrary() {
        try {
         if(!Files.exists(LIBRARY_PATH)) return new ArrayList<>();
         String json = Files.readString(LIBRARY_PATH);
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .create();

            Type type = new TypeToken<List<Game>>(){}.getType();
            return gson.fromJson(json, type);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

}

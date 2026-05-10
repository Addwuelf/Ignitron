package org.example.ignitron;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class LibraryStorageTest {

    private Path tempFile;

    @BeforeEach
    void setUp() throws Exception {
        Path tempDir = Files.createTempDirectory("ignitron-test");
        tempFile = tempDir.resolve("library.json");

        //Override storage path for testing
        LibraryStorage.setLibraryPathForTesting(tempFile);
    }

    @Test
    void testSaveAndLoadLibrary() {
        // Arrange
        Game game1 = new Game("Gta5", "C:/Games/Test1.exe", null, new HashSet<>(), 0, LocalDateTime.now(), "Steam");
        Game game2 = new Game("TitanFall 2", "C:/Games/Test2.exe", null, new HashSet<>(), 0, LocalDateTime.now(), "Steam");

        List<Game> gamestoSave = List.of(game1, game2);

        // Act
        LibraryStorage.saveLibrary(gamestoSave);
        List<Game> loadedGames = LibraryStorage.loadLibrary();

        //Assert
        Assertions.assertEquals("Gta5", loadedGames.get(0).getName());
        Assertions.assertEquals("TitanFall 2", loadedGames.get(1).getName());
    }

    @Test
    void testvdfParse() {
       // Arrange
        List<Path> paths = new ArrayList<>();
        Path file = Path.of("C:/Program Files (x86)/Steam/steamapps/libraryfolders.vdf");

        // Act
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("\"path\"")) {
                    int firstQuote = line.indexOf('"', 6); // after "path"
                    int secondQuote = line.indexOf('"', firstQuote + 1);
                    if (firstQuote != -1 && secondQuote != -1) {
                        String raw = line.substring(firstQuote + 1, secondQuote);
                        paths.add(Path.of(raw));
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        for (Path path : paths) {

        }
        Assertions.assertEquals(2, paths.size());
    }


}
package org.example.ignitron.GameDetection.curseforge;

import javafx.scene.image.Image;
import org.example.ignitron.Game;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CurseForgeDetector {

    private static final String INSTANCES_PATH =
            System.getProperty("user.home") + "/curseforge/minecraft/Instances";

    private static final String CURSEFORGE_EXE =
            System.getenv("LOCALAPPDATA") + "/Programs/CurseForge Windows/CurseForge.exe";

    public List<Game> detectAllInstances() {
        List<Game> games = new ArrayList<>();

        File instancesDir = new File(INSTANCES_PATH);
        if (!instancesDir.exists()) {
            return games;
        }

        File[] subfolders = instancesDir.listFiles(File::isDirectory);
        if (subfolders == null) {
            return games;
        }

        for (File folder : subfolders) {
            Game game = buildGame(folder);
            if (game != null) {
                games.add(game);
            }
        }

        return games;
    }

    private Game buildGame(File folder) {
        File manifestFile = new File(folder, "manifest.json");
        File instanceFile = new File(folder, "minecraftinstance.json");

        if (!manifestFile.exists()) {
            return null;
        }

        CurseForgeManifest manifest = CurseForgeParser.parseManifest(manifestFile);
        if (manifest == null || manifest.getName() == null) {
            return null;
        }

        CurseForgeInstance instance = null;
        if (instanceFile.exists()) {
            instance = CurseForgeParser.parseInstance(instanceFile);
        }

        Game game = new Game();
        game.setName(manifest.getName());
        game.setPath(CURSEFORGE_EXE);
        game.setLauncher("curseforge");
        game.addTag("minecraft");
        game.addTag("curseforge");

        if (manifest.getMinecraft() != null) {
            game.addTag(manifest.getMinecraft().getVersion());

            List<CurseForgeModLoader> loaders = manifest.getMinecraft().getModLoaders();
            if (loaders != null && !loaders.isEmpty()) {
                game.addTag(loaders.get(0).getId());
            }
        }

        // Icon: thumbnail URL from minecraftinstance.json
        if (instance != null && instance.getInstalledModpack() != null) {
            String url = instance.getInstalledModpack().getThumbnailUrl();
            if (url != null && !url.isEmpty()) {
                game.setIcon(new Image(url, true));
            }
        }

        // Icon fallback: kubejs packicon
        if (game.getIcon() == null) {
            File packIcon = new File(folder, "kubejs/config/packicon.png");
            if (packIcon.exists()) {
                game.setIcon(new Image(packIcon.toURI().toString()));
            }
        }

        return game;
    }


}

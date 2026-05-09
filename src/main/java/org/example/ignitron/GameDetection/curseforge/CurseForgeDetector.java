package org.example.ignitron.GameDetection.curseforge;

import javafx.scene.image.Image;
import org.example.ignitron.Game;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CurseForgeDetector {

    // Default CurseForge instances folder on Windows
    private static final String INSTANCES_PATH =
            System.getProperty("user.home") + "/curseforge/minecraft/Instances";

    // Path to the CurseForge app executable — used as the launch target for all instances
    private static final String CURSEFORGE_EXE =
            System.getenv("LOCALAPPDATA") + "/Programs/CurseForge Windows/CurseForge.exe";

    /**
     * Scans the CurseForge instances directory and returns a Game object for
     * each valid modpack instance found.
     */
    public List<Game> detectAllInstances() {
        List<Game> games = new ArrayList<>();

        File instancesDir = new File(INSTANCES_PATH);
        if (!instancesDir.exists()) {
            return games;
        }

        // Each subfolder in the Instances directory is one modpack instance
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

    /**
     * Attempts to build a Game object from a single instance folder.
     * Returns null if the folder is not a valid CurseForge instance.
     */
    private Game buildGame(File folder) {
        File manifestFile = new File(folder, "manifest.json");
        File instanceFile = new File(folder, "minecraftinstance.json");

        // manifest.json is required — if it's missing this isn't a valid instance
        if (!manifestFile.exists()) {
            return null;
        }

        CurseForgeManifest manifest = CurseForgeParser.parseManifest(manifestFile);
        if (manifest == null || manifest.getName() == null) {
            return null;
        }

        // minecraftinstance.json is optional but contains the thumbnail URL
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

        // Add MC version and modloader as tags
        if (manifest.getMinecraft() != null) {
            game.addTag(manifest.getMinecraft().getVersion());

            List<CurseForgeModLoader> loaders = manifest.getMinecraft().getModLoaders();
            if (loaders != null && !loaders.isEmpty()) {
                game.addTag(loaders.get(0).getId());
            }
        }

        // Primary icon source: thumbnail URL stored in minecraftinstance.json,
        // fetched from the CurseForge CDN (media.forgecdn.net)
        if (instance != null && instance.getInstalledModpack() != null) {
            String url = instance.getInstalledModpack().getThumbnailUrl();
            if (url != null && !url.isEmpty()) {
                game.setIcon(new Image(url, true)); // true = load in background
            }
        }

        // Fallback icon: some packs include a packicon.png via the KubeJS mod
        if (game.getIcon() == null) {
            File packIcon = new File(folder, "kubejs/config/packicon.png");
            if (packIcon.exists()) {
                game.setIcon(new Image(packIcon.toURI().toString()));
            }
        }

        return game;
    }
}

package org.example.ignitron.GameDetection.curseforge;

import javafx.scene.image.Image;
import org.example.ignitron.Game;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Detects installed CurseForge Minecraft modpack instances on the local machine
 * and converts them into Game objects for the Ignitron library.
 *
 * Uses CurseForge's own bundled minecraft.exe for launching so authentication
 * and Java management are handled automatically — no need to reimplement those.
 */
public class CurseForgeDetector {

    // Default CurseForge instances folder on Windows
    private static final String INSTANCES_PATH =
            System.getProperty("user.home") + "/curseforge/minecraft/Instances";

    // CurseForge's bundled Minecraft launcher — handles auth, Java, and classpath for us
    private static final String MINECRAFT_EXE =
            new File(System.getProperty("user.home"), "curseforge/minecraft/Install/minecraft.exe")
                    .getAbsolutePath();

    // Working directory passed to minecraft.exe so it finds launcher_profiles.json
    private static final String INSTALL_DIR =
            new File(System.getProperty("user.home"), "curseforge/minecraft/Install")
                    .getAbsolutePath();

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
     * Also writes a launcher profile so minecraft.exe can launch it directly.
     *
     * Supports both marketplace packs (have manifest.json) and custom instances
     * (no manifest.json — name, MC version, and modloader come from minecraftinstance.json).
     * Returns null if neither file is present or the instance has no usable name.
     */
    private Game buildGame(File folder) {
        File manifestFile = new File(folder, "manifest.json");
        File instanceFile = new File(folder, "minecraftinstance.json");

        // minecraftinstance.json is the one file every instance must have
        if (!instanceFile.exists()) {
            return null;
        }

        CurseForgeInstance instance = CurseForgeParser.parseInstance(instanceFile);
        if (instance == null) {
            return null;
        }

        // manifest.json is only present on marketplace packs
        CurseForgeManifest manifest = manifestFile.exists()
                ? CurseForgeParser.parseManifest(manifestFile)
                : null;

        // Resolve the display name: manifest wins, then minecraftinstance name, then folder name
        String name = null;
        if (manifest != null && manifest.getName() != null) {
            name = manifest.getName();
        } else if (instance.getName() != null && !instance.getName().isEmpty()) {
            name = instance.getName();
        } else {
            name = folder.getName();
        }

        // Build the Game object
        Game game = new Game();
        game.setName(name);
        game.setPath(MINECRAFT_EXE);
        game.setLauncher("curseforge");
        game.addTag("minecraft");
        game.addTag("curseforge");

        // Add MC version and modloader as tags — prefer manifest, fall back to minecraftinstance
        if (manifest != null && manifest.getMinecraft() != null) {
            game.addTag(manifest.getMinecraft().getVersion());

            List<CurseForgeModLoader> loaders = manifest.getMinecraft().getModLoaders();
            if (loaders != null && !loaders.isEmpty()) {
                game.addTag(loaders.get(0).getId());
            }
        } else {
            // Custom instances store these directly on minecraftinstance.json
            if (instance.getGameVersion() != null) {
                game.addTag(instance.getGameVersion());
            }
            if (instance.getBaseModLoader() != null && instance.getBaseModLoader().getName() != null) {
                game.addTag(instance.getBaseModLoader().getName());
            }
        }

        // Icon: try thumbnailUrl first (fetched from CurseForge CDN)
        if (instance.getInstalledModpack() != null) {
            String url = instance.getInstalledModpack().getThumbnailUrl();
            if (url != null && !url.isEmpty()) {
                game.setIcon(new Image(url, true)); // true = load in background
            }
        }

        // Icon fallback: some packs include a packicon.png via the KubeJS mod
        if (game.getIcon() == null) {
            File packIcon = new File(folder, "kubejs/config/packicon.png");
            if (packIcon.exists()) {
                game.setIcon(new Image(packIcon.toURI().toString()));
            }
        }

        // Write a profile into CurseForge's launcher_profiles.json so --launch works
        writeProfile(folder, manifest, instance);

        // Store the full launch command: minecraft.exe --workDir <installDir> --launch <profileName>
        // The profile name is the instance folder name, matching CurseForge's own convention
        String profileName = folder.getName();
        game.setLaunchCommand(List.of(MINECRAFT_EXE, "--workDir", INSTALL_DIR, "--launch", profileName));

        return game;
    }

    /**
     * Re-reads the instance's manifest files and rewrites its launcher profile with fresh data.
     * Called every time Play is clicked so modloader version changes after a pack update
     * are always reflected without needing to re-run auto-detection.
     * Works for both marketplace packs (have manifest.json) and custom instances (don't).
     */
    public void refreshProfile(Game game) {
        List<String> cmd = game.getLaunchCommand();
        if (cmd == null || cmd.size() < 5) return;

        // Profile name is the last element of the launch command (--launch <profileName>)
        String profileName = cmd.get(4);
        File folder = new File(INSTANCES_PATH, profileName);

        if (!folder.exists()) return;

        // minecraftinstance.json must exist — manifest.json is optional (absent on custom instances)
        File instanceFile = new File(folder, "minecraftinstance.json");
        if (!instanceFile.exists()) return;

        CurseForgeInstance instance = CurseForgeParser.parseInstance(instanceFile);
        if (instance == null) return;

        File manifestFile = new File(folder, "manifest.json");
        CurseForgeManifest manifest = manifestFile.exists()
                ? CurseForgeParser.parseManifest(manifestFile)
                : null;

        writeProfile(folder, manifest, instance);
    }

    /**
     * Extracts the fields needed for the launcher profile and delegates to CurseForgeProfileWriter.
     * manifest may be null for custom instances — falls back to minecraftinstance.json fields.
     */
    private void writeProfile(File folder, CurseForgeManifest manifest, CurseForgeInstance instance) {
        String profileName = folder.getName();

        // Use installPath from minecraftinstance.json if available, otherwise fall back to folder path
        String installPath = (instance != null && instance.getInstallPath() != null)
                ? instance.getInstallPath()
                : folder.getAbsolutePath();

        // Pull instance UUID for the -DCFInstanceId JVM arg
        String instanceId = (instance != null && instance.getInstalledModpack() != null
                && instance.getInstalledModpack().getInstanceID() != null)
                ? instance.getInstalledModpack().getInstanceID()
                : "";

        // Resolve RAM and modloader version — prefer manifest, fall back to minecraftinstance.json
        int ram = 0;
        String versionId = "";

        if (manifest != null && manifest.getMinecraft() != null) {
            ram = manifest.getMinecraft().getRecommendedRam();
            List<CurseForgeModLoader> loaders = manifest.getMinecraft().getModLoaders();
            if (loaders != null && !loaders.isEmpty()) {
                versionId = loaders.get(0).getId();
            }
        } else if (instance != null && instance.getBaseModLoader() != null
                && instance.getBaseModLoader().getName() != null) {
            // Custom instances store the active modloader directly on minecraftinstance.json
            versionId = instance.getBaseModLoader().getName();
        }

        CurseForgeProfileWriter.writeProfile(profileName, installPath, versionId, ram, instanceId);
    }
}

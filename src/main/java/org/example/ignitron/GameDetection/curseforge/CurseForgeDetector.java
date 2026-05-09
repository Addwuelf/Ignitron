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

        // minecraftinstance.json is optional but contains the thumbnail URL and instance UUID
        CurseForgeInstance instance = null;
        if (instanceFile.exists()) {
            instance = CurseForgeParser.parseInstance(instanceFile);
        }

        // Build the Game object
        Game game = new Game();
        game.setName(manifest.getName());
        game.setPath(MINECRAFT_EXE);
        game.setLauncher("curseforge");
        game.addTag("minecraft");
        game.addTag("curseforge");

        // Add MC version and modloader (e.g. "1.21.1", "neoforge-21.1.219") as tags
        if (manifest.getMinecraft() != null) {
            game.addTag(manifest.getMinecraft().getVersion());

            List<CurseForgeModLoader> loaders = manifest.getMinecraft().getModLoaders();
            if (loaders != null && !loaders.isEmpty()) {
                game.addTag(loaders.get(0).getId());
            }
        }

        // Icon: try thumbnailUrl first (fetched from CurseForge CDN)
        if (instance != null && instance.getInstalledModpack() != null) {
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
     * Extracts the fields needed for the launcher profile and delegates to CurseForgeProfileWriter.
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

        // Use pack-recommended RAM if available, otherwise CurseForgeProfileWriter will use its default
        int ram = (manifest.getMinecraft() != null) ? manifest.getMinecraft().getRecommendedRam() : 0;

        String versionId = "";
        if (manifest.getMinecraft() != null) {
            List<CurseForgeModLoader> loaders = manifest.getMinecraft().getModLoaders();
            if (loaders != null && !loaders.isEmpty()) {
                versionId = loaders.get(0).getId();
            }
        }

        CurseForgeProfileWriter.writeProfile(profileName, installPath, versionId, ram, instanceId);
    }
}

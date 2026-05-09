package org.example.ignitron.GameDetection.curseforge;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.example.ignitron.Log;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Manages a local disk cache of CurseForge pack icons stored at
 * %APPDATA%\Ignitron\icons\curseforge\.
 *
 * On first detection each icon is downloaded once from the CurseForge CDN
 * and saved as a PNG. All subsequent launches load from the cache, so the
 * app works fully offline after the first run.
 *
 * Also generates a default Minecraft-style grass block icon for packs that
 * have no thumbnail. This icon is entirely original programmatic artwork
 * (colored rectangles — no Mojang/Microsoft assets) so it is safe to
 * distribute commercially without any attribution.
 */
public class CurseForgeIconCache {

    private static final File CACHE_DIR =
            new File(System.getenv("APPDATA"), "Ignitron/icons/curseforge");

    // Fixed filename for the generated default icon so it only needs to be created once
    private static final String DEFAULT_ICON_NAME = "_default_minecraft.png";

    /**
     * Returns the local file path for the pack's icon.
     * Downloads from the CDN on first call; subsequent calls return the cached file.
     * Returns null if the download fails.
     */
    public static String getOrDownload(String url, String profileName) {
        CACHE_DIR.mkdirs();

        // Sanitise the profile name so it is always a valid filename
        String safeFileName = profileName.replaceAll("[^a-zA-Z0-9._-]", "_") + ".png";
        File cachedFile = new File(CACHE_DIR, safeFileName);

        if (cachedFile.exists()) {
            return cachedFile.getAbsolutePath();
        }

        // First time: download from the CDN and persist to disk
        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, cachedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Log.info("Cached CurseForge icon: " + safeFileName);
            return cachedFile.getAbsolutePath();
        } catch (IOException e) {
            Log.error("Failed to cache icon for " + profileName, e);
            return null;
        }
    }

    /**
     * Returns the local path to the default Minecraft icon, generating and
     * saving it on first call so it persists across sessions.
     * Returns null only if the file cannot be written.
     */
    public static String getOrCreateDefaultIconPath() {
        CACHE_DIR.mkdirs();
        File defaultFile = new File(CACHE_DIR, DEFAULT_ICON_NAME);

        if (!defaultFile.exists()) {
            BufferedImage bi = buildDefaultIconImage();
            try {
                ImageIO.write(bi, "png", defaultFile);
            } catch (IOException e) {
                Log.error("Failed to save default Minecraft icon", e);
                return null;
            }
        }

        return defaultFile.getAbsolutePath();
    }

    /**
     * Convenience method that returns the default icon as a JavaFX Image.
     * Used as an in-memory fallback when the file cannot be written.
     */
    public static Image createDefaultIcon() {
        return SwingFXUtils.toFXImage(buildDefaultIconImage(), null);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Draws an original grass-block style icon using only AWT primitives.
     * No Mojang / Microsoft assets are used — entirely safe for commercial use.
     */
    private static BufferedImage buildDefaultIconImage() {
        int size = 128;
        BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        // Grass top — bright green band across the top
        g.setColor(new Color(86, 130, 47));
        g.fillRect(0, 0, size, 44);

        // Grass-to-dirt transition strip — slightly darker green
        g.setColor(new Color(64, 105, 30));
        g.fillRect(0, 38, size, 8);

        // Dirt body — warm brown
        g.setColor(new Color(139, 100, 65));
        g.fillRect(0, 44, size, size - 44);

        // Pixel-art dirt specks scattered across the dirt face
        g.setColor(new Color(112, 78, 45));
        int[][] specks = {
            {10, 52, 10, 5}, {35, 60,  8, 4}, {65, 54, 12, 5}, {96, 62,  9, 4},
            {20, 74,  8, 4}, {50, 78, 11, 5}, {80, 71,  9, 4}, {106, 80, 10, 5},
            { 8, 92, 12, 4}, {38, 96,  8, 5}, {70, 90, 10, 4}, { 98, 95,  9, 4},
            {18,108,  9, 4}, {52,110, 11, 5}, {84,106,  8, 4}
        };
        for (int[] s : specks) {
            g.fillRect(s[0], s[1], s[2], s[3]);
        }

        g.dispose();
        return bi;
    }
}

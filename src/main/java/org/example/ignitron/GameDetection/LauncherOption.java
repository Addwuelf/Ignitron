package org.example.ignitron.GameDetection;

public class LauncherOption {
    private String id;           // "steam", "epic", "curseforge"
    private String displayName;  // "Steam", "Epic Games", "CurseForge"
    private String iconText;     // "S", "EG", "CF" — shown in colored square
    private String brandColor;   // "#1b2838" — hex string for the icon background
    private boolean detected;    // checked at construction time

    public LauncherOption(String id, String displayName,
                          String iconText, String brandColor, boolean detected) {
        this.id = id;
        this.displayName = displayName;
        this.iconText = iconText;
        this.brandColor = brandColor;
        this.detected = detected;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getIconText() {
        return iconText;
    }

    public void setIconText(String iconText) {
        this.iconText = iconText;
    }

    public String getBrandColor() {
        return brandColor;
    }

    public void setBrandColor(String brandColor) {
        this.brandColor = brandColor;
    }

    public boolean isDetected() {
        return detected;
    }

    public void setDetected(boolean detected) {
        this.detected = detected;
    }
}

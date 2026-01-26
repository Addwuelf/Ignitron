package org.example.ignitron;

import javafx.scene.image.Image;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.time.LocalDateTime;


public class Game {

    private String name;
    private String path;
    private Image icon;
    private Set<String> gameTags = new HashSet<String>();
    private int playTime;
    private LocalDateTime lastPlayed;
    private String launcher;
    private File folder;

  public Game(String name, String path, Image icon, Set<String> gameTags, int playTime, LocalDateTime lastPlayed, String launcher) {
        this.name = name;
        this.path = path;
        this.icon = icon;
        this.gameTags = gameTags;
        this.playTime = playTime;
        this.lastPlayed = lastPlayed;
        this.launcher = launcher;
  }

  public Game (String path, File folder) {
      this.path = path;
      this.folder = folder;
  }


    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public Image getIcon() { return icon; }
    public void setIcon(Image icon) { this.icon = icon; }

    public Set<String> getGameTags() { return gameTags; }
    public void setGameTags(Set<String> gameTags) { this.gameTags = gameTags; }

    public int getPlayTime() { return playTime; }
    public void setPlayTime(int playTime) { this.playTime = playTime; }

    public LocalDateTime getLastPlayed() { return lastPlayed; }
    public void setLastPlayed(LocalDateTime lastPlayed) { this.lastPlayed = lastPlayed; }

    public String getLauncher() { return launcher; }
    public void setLauncher(String launcher) { this.launcher = launcher; }

    public void setFolder(File folder) {this.folder = folder; }
    public File getFolder() { return folder; }

    // Useful methods for tags
    public void addTag(String tag) { gameTags.add(tag.toLowerCase()); }
    public void removeTag(String tag) { gameTags.remove(tag.toLowerCase()); }
    public boolean hasTag(String tag) { return gameTags.contains(tag.toLowerCase()); }
}


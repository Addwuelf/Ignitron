package org.example.ignitron;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Library {

    // CopyOnWriteArrayList keeps reads safe when the background scan thread
    // adds games concurrently with the FX thread reading the list
    private List<Game> games = new CopyOnWriteArrayList<>();

   public void addGame(Game game) {
       games.add(game);
   }

   public List<Game> getGames() { return games; }

    public void removeGame(Game game) {
       games.remove(game);
    }

    // Methods for finding specific games
    public List<Game> filterByName(String query) {
       return games.stream()
               .filter(game -> game.getName().toLowerCase().contains(query.toLowerCase()))
               .toList();
    }

    public List<Game> filterByTag(String tag) {
       return games.stream()
               .filter(game -> game.hasTag(tag))
               .toList();
    }
}

package org.example.ignitron.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import org.example.ignitron.Game;

import javafx.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;



import java.io.IOException;

public class GameDetailsController {

    @FXML
    private ImageView gameIcon;

    @FXML
    private Label gameName;

    @FXML
    private FlowPane tagContainer;

    @FXML
    private Button playButton;

    @FXML
    private Label totalPlaytimeLabel;

    private Game currentGame;


    public void setGame(Game game) {
        this.currentGame = game;
        updateUI();
    }

    private void updateUI() {
        if (currentGame != null) {
            gameName.setText(currentGame.getName());
            Image icon = currentGame.getIcon();

            // Set game icon
            if (icon != null) {
                gameIcon.setImage(icon);
            } else {
                gameIcon.setImage(null);
            }

            tagContainer.getChildren().clear();

            for (String tag : currentGame.getGameTags()) {
                Label tagLabel = new Label(tag);
                tagContainer.getChildren().add(tagLabel);
            }
            updateLastPlayed();
        }
    }

    @FXML
    private void onPlayClicked(ActionEvent event) {
        if (currentGame != null) {
            String path = currentGame.getPath();
            ProcessBuilder pb = new ProcessBuilder(path);

            try {
                // Start the process
                Process process = pb.start();

                currentGame.setLastPlayed(LocalDateTime.now());
                updateLastPlayed();

                String launchMessage = "Launched Game: " + currentGame.getName();
                System.out.println(launchMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateLastPlayed() {
        if (currentGame == null) {
            // No game selected
            //gameLastPlayedLabel.setText("No game selected");
            return;
        }

        if (currentGame.getLastPlayed() == null) {
            // Game has never been launched
            //gameLastPlayedLabel.setText("Never played");
        } else {
            // Format the LocalDateTime into a readable string
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            String formatted = currentGame.getLastPlayed().format(formatter);

            // Update the UI label
            //gameLastPlayedLabel.setText("Last played: " + formatted);
        }
    }


}

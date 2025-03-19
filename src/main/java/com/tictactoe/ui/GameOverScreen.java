package com.tictactoe.ui;

import com.tictactoe.Utils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;

public class GameOverScreen {
    private App app;
    private Scene scene;

    @FXML private Label resultLabel;
    @FXML private Label durationLabel;
    @FXML private Button playAgainButton;

    // create a new game over screen
    public GameOverScreen(App app, Utils.Message message) {
        this.app = app;
        createScene(message);
    }

    // create the game over screen
    private void createScene(Utils.Message message) {
        try {
            // try to load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GameOverScreen.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            scene = new Scene(root, 300, 250);

            // initialize with game result data
            initGameResult(message);

            // set up button action
            playAgainButton.setOnAction(e -> app.showLoginScreen());

        } catch (IOException e) {
            e.printStackTrace();

            // create UI manually if FXML loading fails
            createManualGameOverScreen(message);
        }
    }

    // create the game over screen manually if FXML loading fails
    private void createManualGameOverScreen(Utils.Message message) {
        // get game result data
        String result = (String) message.getData(Utils.Keys.RESULT);
        String winner = (String) message.getData(Utils.Keys.WINNER);
        long gameDuration = (long) message.getData(Utils.Keys.GAME_DURATION);

        // create UI components
        Label titleLabel = new Label("Game Over");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        resultLabel = new Label();
        resultLabel.setFont(Font.font("Arial", 16));

        // set result text and color
        if (result.equals("win")) {
            if (winner.equals(app.getClient().getPlayerName())) {
                resultLabel.setText("You Won!");
                resultLabel.setTextFill(Color.GREEN);
            } else {
                resultLabel.setText("You Lost!");
                resultLabel.setTextFill(Color.RED);
            }
        } else {
            resultLabel.setText("It's a Tie!");
            resultLabel.setTextFill(Color.BLUE);
        }

        durationLabel = new Label("Game Duration: " + formatDuration(gameDuration));

        playAgainButton = new Button("Play Again");
        playAgainButton.setPrefWidth(100);
        playAgainButton.setOnAction(event -> app.showLoginScreen());

        // create the layout
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(
                titleLabel,
                resultLabel,
                durationLabel,
                playAgainButton
        );

        // create scene
        scene = new Scene(layout, 300, 250);
    }

    // init game result display
    private void initGameResult(Utils.Message message) {
        // get game result data
        String result = (String) message.getData(Utils.Keys.RESULT);
        String winner = (String) message.getData(Utils.Keys.WINNER);
        long gameDuration = (long) message.getData(Utils.Keys.GAME_DURATION);

        // set result text and color
        if (result.equals("win")) {
            if (winner.equals(app.getClient().getPlayerName())) {
                resultLabel.setText("You Won!");
                resultLabel.setTextFill(Color.GREEN);
            }
            else {
                resultLabel.setText("You Lost!");
                resultLabel.setTextFill(Color.RED);
            }
        }
        else {
            resultLabel.setText("It's a Tie!");
            resultLabel.setTextFill(Color.BLUE);
        }

        durationLabel.setText("Game Duration: " + formatDuration(gameDuration));
    }

    // formats duration from milliseconds to minutes:seconds
    private String formatDuration(long durationMs) {
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        seconds %= 60;

        return String.format("%d:%02d", minutes, seconds);
    }

    public Scene getScene() {
        return scene;
    }
}
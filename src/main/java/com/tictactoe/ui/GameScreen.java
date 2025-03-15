// com.tictactoe.ui.GameScreen
package com.tictactoe.ui;

import com.tictactoe.Utils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class GameScreen {
    private App app;
    private Scene scene;
    private int boardSize;

    // Game board UI
    private Button[][] boardButtons;

    @FXML private GridPane boardGrid;
    @FXML private Label statusLabel;
    @FXML private Label timerLabel;

    // Game state
    private long gameStartTime;
    private Timer gameTimer;

    public GameScreen(App app, int boardSize) {
        this.app = app;
        this.boardSize = boardSize;
        createScene();
    }

    private void createScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GameScreen.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            scene = new Scene(root, 450, 500);

            // Create game board
            createGameBoard();
        } catch (IOException e) {
            e.printStackTrace();

            // Fallback to programmatic UI creation
            // Create game board
            boardGrid = new GridPane();
            boardGrid.setAlignment(Pos.CENTER);
            boardGrid.setHgap(5);
            boardGrid.setVgap(5);

            boardButtons = new Button[boardSize][boardSize];

            int buttonSize = 400 / boardSize;

            for (int row = 0; row < boardSize; row++) {
                for (int col = 0; col < boardSize; col++) {
                    Button button = new Button();
                    button.setPrefSize(buttonSize, buttonSize);
                    button.setFont(Font.font("Arial", FontWeight.BOLD, buttonSize / 2));

                    final int finalRow = row;
                    final int finalCol = col;
                    button.setOnAction(event -> {
                        if (app.getClient().isMyTurn()) {
                            button.setText(String.valueOf(app.getClient().getPlayerSymbol()));
                            button.setDisable(true);
                            app.getClient().makeMove(finalRow, finalCol);
                        }
                    });

                    boardButtons[row][col] = button;
                    boardGrid.add(button, col, row);
                }
            }

            // Status label
            statusLabel = new Label();
            statusLabel.setFont(Font.font("Arial", 14));

            // Timer label
            timerLabel = new Label("Time: 0:00");
            timerLabel.setFont(Font.font("Arial", 14));

            // Layout
            BorderPane layout = new BorderPane();
            layout.setPadding(new Insets(20));

            VBox topBox = new VBox(10);
            topBox.setAlignment(Pos.CENTER);
            topBox.getChildren().add(statusLabel);

            BorderPane bottomPane = new BorderPane();
            bottomPane.setLeft(timerLabel);

            layout.setTop(topBox);
            layout.setCenter(boardGrid);
            layout.setBottom(bottomPane);

            scene = new Scene(layout, 450, 500);
        }
    }

    private void createGameBoard() {
        boardGrid.getChildren().clear();
        boardButtons = new Button[boardSize][boardSize];

        int buttonSize = 400 / boardSize;

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                Button button = new Button();
                button.setPrefSize(buttonSize, buttonSize);
                button.setFont(Font.font("Arial", FontWeight.BOLD, buttonSize / 2));

                final int finalRow = row;
                final int finalCol = col;
                button.setOnAction(e -> {
                    if (app.getClient().isMyTurn()) {
                        button.setText(String.valueOf(app.getClient().getPlayerSymbol()));
                        button.setDisable(true);
                        app.getClient().makeMove(finalRow, finalCol);
                    }
                });

                boardButtons[row][col] = button;
                boardGrid.add(button, col, row);
            }
        }
    }

    /**
     * Start the game
     */
    public void startGame() {
        updateGameStatus();
        startGameTimer();
    }

    /**
     * Stop the game
     */
    public void stopGame() {
        stopGameTimer();
    }

    /**
     * Handle move result message
     */
    public void handleMoveResult(Utils.Message message) {
        int row = (int) message.getData(Utils.Keys.ROW);
        int col = (int) message.getData(Utils.Keys.COL);
        char symbol = message.getData(Utils.Keys.SYMBOL).toString().charAt(0);

        Button button = boardButtons[row][col];
        button.setText(String.valueOf(symbol));
        button.setDisable(true);

        updateGameStatus();
    }

    /**
     * Update the game status label
     */
    private void updateGameStatus() {
        if (app.getClient().isMyTurn()) {
            statusLabel.setText("Your turn (" + app.getClient().getPlayerSymbol() + ")");
        } else {
            statusLabel.setText(app.getClient().getOpponentName() + "'s turn (" + app.getClient().getOpponentSymbol() + ")");
        }
    }

    /**
     * Start the game timer
     */
    private void startGameTimer() {
        gameStartTime = System.currentTimeMillis();
        gameTimer = new Timer();
        gameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - gameStartTime;
                Platform.runLater(() -> timerLabel.setText("Time: " + formatDuration(elapsed)));
            }
        }, 0, 1000);
    }

    /**
     * Stop the game timer
     */
    private void stopGameTimer() {
        if (gameTimer != null) {
            gameTimer.cancel();
            gameTimer = null;
        }
    }

    /**
     * Format duration in milliseconds to a readable string
     */
    private String formatDuration(long durationMs) {
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        seconds %= 60;

        return String.format("%d:%02d", minutes, seconds);
    }

    public Scene getScene() {
        return scene;
    }

    public int getBoardSize() {
        return boardSize;
    }
}
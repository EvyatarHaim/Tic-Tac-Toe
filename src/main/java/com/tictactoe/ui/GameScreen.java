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
    private Button[][] boardButtons;

    @FXML private GridPane boardGrid;
    @FXML private Label statusLabel;
    @FXML private Label timerLabel;

    private long gameStartTime;
    private Timer gameTimer;

    // creates a new game screen
    public GameScreen(App app, int boardSize) {
        this.app = app;
        this.boardSize = boardSize;
        createScene();
    }

    // creates the game screen
    private void createScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GameScreen.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            scene = new Scene(root, 450, 500);

            // create game board
            createGameBoard();
        } catch (IOException e) {
            e.printStackTrace();

            // fallback to manual UI creation if FXML loading fails
            createManualGameScreen();
        }
    }

    // creates the game screen manually if FXML loading fails
    private void createManualGameScreen() {
        // create game board
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

        // status label
        statusLabel = new Label();
        statusLabel.setFont(Font.font("Arial", 14));

        // timer label
        timerLabel = new Label("Time: 0:00");
        timerLabel.setFont(Font.font("Arial", 14));

        // create layout
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

    // creates the game board
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

    public void startGame() {
        updateGameStatus();
        startGameTimer();
    }

    public void stopGame() {
        stopGameTimer();
    }

    // handles a move result message
    public void handleMoveResult(Utils.Message message) {
        int row = (int) message.getData(Utils.Keys.ROW);
        int col = (int) message.getData(Utils.Keys.COL);
        char symbol = message.getData(Utils.Keys.SYMBOL).toString().charAt(0);

        Button button = boardButtons[row][col];
        button.setText(String.valueOf(symbol));
        button.setDisable(true);

        updateGameStatus();
    }

    // updates the game status label
    private void updateGameStatus() {
        if (app.getClient().isMyTurn()) {
            statusLabel.setText("Your turn (" + app.getClient().getPlayerSymbol() + ")");
        } else {
            statusLabel.setText(app.getClient().getOpponentName() + "'s turn (" + app.getClient().getOpponentSymbol() + ")");
        }
    }

    // start the game timer
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

    // stop the game timer
    private void stopGameTimer() {
        if (gameTimer != null) {
            gameTimer.cancel();
            gameTimer = null;
        }
    }

    // formats duration from milliseconds to string
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
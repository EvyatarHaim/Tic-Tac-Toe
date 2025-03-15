package com.tictactoe.client;

import com.tictactoe.Utils;
import com.tictactoe.game.Board;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.Timer;
import java.util.TimerTask;

public class App extends Application {
    private Client client;
    private Stage primaryStage;

    // UI Components
    private Scene loginScene;
    private Scene waitingScene;
    private Scene gameScene;
    private Scene gameOverScene;

    // Game board UI
    private Button[][] boardButtons;
    private Label statusLabel;
    private Label timerLabel;

    // Game state
    private long gameStartTime;
    private Timer gameTimer;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.client = new Client();

        // Create UI scenes
        createLoginScene();

        // Set up message handler
        client.setMessageHandler(this::handleMessage);

        // Show login scene
        primaryStage.setTitle("Tic Tac Toe");
        primaryStage.setScene(loginScene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // Connect to server when application starts
        if (!client.connect()) {
            showError("Could not connect to server. Please try again later.");
        }
    }

    /**
     * Handle messages from the server
     */
    private void handleMessage(Utils.Message message) {
        Platform.runLater(() -> {
            switch (message.getType()) {
                case WAIT:
                    showWaitingScene();
                    break;
                case GAME_START:
                    createGameScene(client.getBoardSize());
                    showGameScene();
                    startGameTimer();
                    updateGameStatus();
                    break;
                case MOVE_RESULT:
                    updateGameBoard(message);
                    updateGameStatus();
                    break;
                case GAME_OVER:
                    stopGameTimer();
                    showGameOverScene(message);
                    break;
                case ERROR:
                    showError((String) message.getData(Utils.Keys.MESSAGE));
                    break;
            }
        });
    }

    /**
     * Create the login scene
     */
    private void createLoginScene() {
        // Create UI components
        Label titleLabel = new Label("Tic Tac Toe");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Label nameLabel = new Label("Your Name:");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name");

        Label sizeLabel = new Label("Board Size:");
        ToggleGroup sizeGroup = new ToggleGroup();

        RadioButton size3x3 = new RadioButton("3x3");
        size3x3.setToggleGroup(sizeGroup);
        size3x3.setSelected(true);

        RadioButton size4x4 = new RadioButton("4x4");
        size4x4.setToggleGroup(sizeGroup);

        RadioButton size5x5 = new RadioButton("5x5");
        size5x5.setToggleGroup(sizeGroup);

        HBox sizeBox = new HBox(10, size3x3, size4x4, size5x5);

        Button loginButton = new Button("Play");
        loginButton.setPrefWidth(100);

        // Create layout
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(
                titleLabel,
                nameLabel,
                nameField,
                sizeLabel,
                sizeBox,
                loginButton
        );

        // Create scene
        loginScene = new Scene(layout, 300, 250);

        // Handle login button click
        loginButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                showError("Please enter your name.");
                return;
            }

            int boardSize;
            if (size3x3.isSelected()) {
                boardSize = Utils.BOARD_SIZE_3X3;
            } else if (size4x4.isSelected()) {
                boardSize = Utils.BOARD_SIZE_4X4;
            } else {
                boardSize = Utils.BOARD_SIZE_5X5;
            }

            // Send login request
            client.login(name, boardSize);
        });
    }

    /**
     * Create the waiting scene
     */
    private void createWaitingScene() {
        Label waitingLabel = new Label("Waiting for another player...");
        waitingLabel.setFont(Font.font("Arial", 16));

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(50, 50);

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(waitingLabel, progressIndicator);

        waitingScene = new Scene(layout, 300, 200);
    }

    /**
     * Create the game scene
     */
    private void createGameScene(int boardSize) {
        // Create game board
        GridPane boardGrid = new GridPane();
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
                button.setOnAction(e -> {
                    if (client.isMyTurn()) {
                        button.setText(String.valueOf(client.getPlayerSymbol()));
                        button.setDisable(true);
                        client.makeMove(finalRow, finalCol);
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

        gameScene = new Scene(layout, 450, 500);
    }

    /**
     * Create the game over scene
     */
    private void createGameOverScene(Utils.Message message) {
        String result = (String) message.getData(Utils.Keys.RESULT);
        String winner = (String) message.getData(Utils.Keys.WINNER);
        long gameDuration = (long) message.getData(Utils.Keys.GAME_DURATION);

        Label titleLabel = new Label("Game Over");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Label resultLabel = new Label();
        resultLabel.setFont(Font.font("Arial", 16));

        if (result.equals("win")) {
            if (winner.equals(client.getPlayerName())) {
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

        Label durationLabel = new Label("Game Duration: " + formatDuration(gameDuration));

        Button playAgainButton = new Button("Play Again");
        playAgainButton.setPrefWidth(100);
        playAgainButton.setOnAction(e -> primaryStage.setScene(loginScene));

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(
                titleLabel,
                resultLabel,
                durationLabel,
                playAgainButton
        );

        gameOverScene = new Scene(layout, 300, 250);
    }

    /**
     * Show the waiting scene
     */
    private void showWaitingScene() {
        if (waitingScene == null) {
            createWaitingScene();
        }
        primaryStage.setScene(waitingScene);
    }

    /**
     * Show the game scene
     */
    private void showGameScene() {
        primaryStage.setScene(gameScene);
    }

    /**
     * Show the game over scene
     */
    private void showGameOverScene(Utils.Message message) {
        createGameOverScene(message);
        primaryStage.setScene(gameOverScene);
    }

    /**
     * Update the game board after a move
     */
    private void updateGameBoard(Utils.Message message) {
        int row = (int) message.getData(Utils.Keys.ROW);
        int col = (int) message.getData(Utils.Keys.COL);
        char symbol = message.getData(Utils.Keys.SYMBOL).toString().charAt(0);

        Button button = boardButtons[row][col];
        button.setText(String.valueOf(symbol));
        button.setDisable(true);
    }

    /**
     * Update the game status label
     */
    private void updateGameStatus() {
        if (client.isMyTurn()) {
            statusLabel.setText("Your turn (" + client.getPlayerSymbol() + ")");
        } else {
            statusLabel.setText(client.getOpponentName() + "'s turn (" + client.getOpponentSymbol() + ")");
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

    /**
     * Show an error dialog
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        // Disconnect from server when application closes
        if (client.isConnected()) {
            client.disconnect();
        }

        // Stop the game timer
        stopGameTimer();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

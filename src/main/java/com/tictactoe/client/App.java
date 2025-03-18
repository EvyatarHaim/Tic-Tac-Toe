package com.tictactoe.client;

import com.tictactoe.Utils;
import com.tictactoe.db.DatabaseManager;
import com.tictactoe.db.model.GameEntity;
import com.tictactoe.db.model.PlayerEntity;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class App extends Application {
    private Client client;
    private Stage primaryStage;
    private DatabaseManager dbManager;

    // UI Components
    private Scene loginScene;
    private Scene waitingScene;
    private Scene gameScene;
    private Scene gameOverScene;
    private Scene statsScene;

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

        try {
            // Initialize database
            this.dbManager = DatabaseManager.getInstance();
            System.out.println("Database initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }

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

        HBox sizeBox = new HBox(10);
        sizeBox.setAlignment(Pos.CENTER);
        sizeBox.getChildren().addAll(size3x3, size4x4, size5x5);

        Button playButton = new Button("Play");
        playButton.setPrefWidth(100);

        Button statsButton = new Button("Statistics");
        statsButton.setPrefWidth(100);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(playButton, statsButton);

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
                buttonBox
        );

        // Create scene
        loginScene = new Scene(layout, 300, 250);

        // Handle login button click
        playButton.setOnAction(e -> {
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

        // Handle stats button click
        statsButton.setOnAction(e -> {
            showStatsScene();
        });
    }

    /**
     * Create the stats scene
     */
    private void createStatsScene() {
        // Title
        Label titleLabel = new Label("Game Statistics");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        // Player table
        Label playerLabel = new Label("Top Players");
        playerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        TableView<PlayerEntity> playerTable = new TableView<>();

        TableColumn<PlayerEntity, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(150);

        TableColumn<PlayerEntity, Integer> gamesPlayedColumn = new TableColumn<>("Games Played");
        gamesPlayedColumn.setCellValueFactory(new PropertyValueFactory<>("gamesPlayed"));
        gamesPlayedColumn.setPrefWidth(100);

        TableColumn<PlayerEntity, Integer> gamesWonColumn = new TableColumn<>("Games Won");
        gamesWonColumn.setCellValueFactory(new PropertyValueFactory<>("gamesWon"));
        gamesWonColumn.setPrefWidth(100);

        TableColumn<PlayerEntity, Integer> gamesLostColumn = new TableColumn<>("Games Lost");
        gamesLostColumn.setCellValueFactory(new PropertyValueFactory<>("gamesLost"));
        gamesLostColumn.setPrefWidth(100);

        TableColumn<PlayerEntity, Integer> gamesTiedColumn = new TableColumn<>("Games Tied");
        gamesTiedColumn.setCellValueFactory(new PropertyValueFactory<>("gamesTied"));
        gamesTiedColumn.setPrefWidth(100);

        playerTable.getColumns().addAll(nameColumn, gamesPlayedColumn, gamesWonColumn, gamesLostColumn, gamesTiedColumn);
        playerTable.setPrefHeight(200);

        // Game table
        Label gameLabel = new Label("Recent Games");
        gameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        TableView<GameEntity> gameTable = new TableView<>();

        TableColumn<GameEntity, String> player1Column = new TableColumn<>("Player 1");
        player1Column.setCellValueFactory(cellData -> {
            if (cellData.getValue().getPlayer1() != null) {
                return javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getPlayer1().getName());
            }
            return javafx.beans.binding.Bindings.createStringBinding(() -> "");
        });
        player1Column.setPrefWidth(120);

        TableColumn<GameEntity, String> player2Column = new TableColumn<>("Player 2");
        player2Column.setCellValueFactory(cellData -> {
            if (cellData.getValue().getPlayer2() != null) {
                return javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getPlayer2().getName());
            }
            return javafx.beans.binding.Bindings.createStringBinding(() -> "");
        });
        player2Column.setPrefWidth(120);

        TableColumn<GameEntity, String> resultColumn = new TableColumn<>("Result");
        resultColumn.setCellValueFactory(new PropertyValueFactory<>("result"));
        resultColumn.setPrefWidth(80);

        TableColumn<GameEntity, String> winnerColumn = new TableColumn<>("Winner");
        winnerColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getWinner() != null) {
                return javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getWinner().getName());
            } else if (cellData.getValue().getResult().equals("TIE")) {
                return javafx.beans.binding.Bindings.createStringBinding(() -> "Tie");
            }
            return javafx.beans.binding.Bindings.createStringBinding(() -> "");
        });
        winnerColumn.setPrefWidth(120);

        TableColumn<GameEntity, Integer> boardSizeColumn = new TableColumn<>("Board Size");
        boardSizeColumn.setCellValueFactory(new PropertyValueFactory<>("boardSize"));
        boardSizeColumn.setPrefWidth(80);

        TableColumn<GameEntity, String> durationColumn = new TableColumn<>("Duration");
        durationColumn.setCellValueFactory(cellData -> {
            long durationMs = cellData.getValue().getDuration();
            long seconds = durationMs / 1000;
            long minutes = seconds / 60;
            final long displaySeconds = seconds % 60;
            return javafx.beans.binding.Bindings.createStringBinding(
                    () -> String.format("%d:%02d", minutes, displaySeconds));
        });
        durationColumn.setPrefWidth(80);

        TableColumn<GameEntity, String> playedAtColumn = new TableColumn<>("Played At");
        playedAtColumn.setCellValueFactory(cellData -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return javafx.beans.binding.Bindings.createStringBinding(
                    () -> dateFormat.format(cellData.getValue().getPlayedAt()));
        });
        playedAtColumn.setPrefWidth(150);

        gameTable.getColumns().addAll(player1Column, player2Column, resultColumn, winnerColumn, boardSizeColumn, durationColumn, playedAtColumn);
        gameTable.setPrefHeight(300);

        // Back button
        Button backButton = new Button("Back to Login");
        backButton.setPrefWidth(120);
        backButton.setOnAction(event -> primaryStage.setScene(loginScene));

        // Layout
        VBox topBox = new VBox(10);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(10));
        topBox.getChildren().addAll(titleLabel);

        VBox playerBox = new VBox(10);
        playerBox.setPadding(new Insets(10));
        playerBox.getChildren().addAll(playerLabel, playerTable);

        VBox gameBox = new VBox(10);
        gameBox.setPadding(new Insets(10));
        gameBox.getChildren().addAll(gameLabel, gameTable);

        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(10));
        bottomBox.getChildren().add(backButton);

        VBox centerBox = new VBox(20);
        centerBox.getChildren().addAll(playerBox, gameBox);

        BorderPane layout = new BorderPane();
        layout.setTop(topBox);
        layout.setCenter(centerBox);
        layout.setBottom(bottomBox);

        statsScene = new Scene(layout, 800, 600);

        // Load data
        try {
            System.out.println("Loading statistics data...");
            // Load top players
            List<PlayerEntity> topPlayers = dbManager.getTopPlayers(10);
            System.out.println("Loaded " + (topPlayers != null ? topPlayers.size() : 0) + " top players");
            playerTable.setItems(FXCollections.observableArrayList(topPlayers != null ? topPlayers : new ArrayList<>()));

            // Load recent games
            List<GameEntity> recentGames = dbManager.getRecentGames(20);
            System.out.println("Loaded " + (recentGames != null ? recentGames.size() : 0) + " recent games");
            gameTable.setItems(FXCollections.observableArrayList(recentGames != null ? recentGames : new ArrayList<>()));
        } catch (Exception e) {
            System.err.println("Error loading statistics data: " + e.getMessage());
            e.printStackTrace();

            // If we can't load the data, show error message
            Label errorLabel = new Label("Error loading statistics: " + e.getMessage());
            errorLabel.setTextFill(Color.RED);
            centerBox.getChildren().add(0, errorLabel);
        }
    }

    /**
     * Show the stats scene
     */
    private void showStatsScene() {
        if (statsScene == null) {
            createStatsScene();
        }
        primaryStage.setScene(statsScene);
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
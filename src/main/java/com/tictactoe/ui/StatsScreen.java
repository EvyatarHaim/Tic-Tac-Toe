package com.tictactoe.ui;

import com.tictactoe.db.DatabaseManager;
import com.tictactoe.db.model.GameEntity;
import com.tictactoe.db.model.PlayerEntity;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class StatsScreen {
    private App app;
    private Scene scene;
    private DatabaseManager dbManager;

    private TableView<PlayerEntity> playerTable;
    private TableView<GameEntity> gameTable;
    private Button backButton;

    public StatsScreen(App app) {
        this.app = app;
        try {
            this.dbManager = DatabaseManager.getInstance();
        } catch (Exception e) {
            System.err.println("Failed to initialize DatabaseManager: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Creating stats screen");
        createScene();
    }

    private void createScene() {
        // Skip trying to load from FXML and go straight to programmatic UI
        createProgrammaticUI();
    }

    private void createProgrammaticUI() {
        System.out.println("Creating programmatic stats UI");

        // Title
        Label titleLabel = new Label("Game Statistics");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        // Player table
        Label playerLabel = new Label("Top Players");
        playerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        playerTable = new TableView<>();

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

        gameTable = new TableView<>();

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
        backButton = new Button("Back to Login");
        backButton.setPrefWidth(120);
        backButton.setOnAction(event -> app.showLoginScreen());

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

        scene = new Scene(layout, 800, 600);

        // Load data
        loadData();
    }

    private void loadData() {
        try {
            System.out.println("Loading data for statistics screen");
            if (dbManager == null) {
                System.out.println("DatabaseManager is null, initializing...");
                this.dbManager = DatabaseManager.getInstance();
            }

            // Load top players
            List<PlayerEntity> topPlayers = dbManager.getTopPlayers(10);
            System.out.println("Loaded " + (topPlayers != null ? topPlayers.size() : 0) + " top players");
            if (playerTable != null) {
                playerTable.setItems(FXCollections.observableArrayList(topPlayers != null ? topPlayers : new ArrayList<>()));
            }

            // Load recent games
            List<GameEntity> recentGames = dbManager.getRecentGames(20);
            System.out.println("Loaded " + (recentGames != null ? recentGames.size() : 0) + " recent games");
            if (gameTable != null) {
                gameTable.setItems(FXCollections.observableArrayList(recentGames != null ? recentGames : new ArrayList<>()));
            }
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Scene getScene() {
        return scene;
    }
}
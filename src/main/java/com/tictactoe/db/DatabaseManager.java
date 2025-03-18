package com.tictactoe.db;

import com.tictactoe.db.model.GameEntity;
import com.tictactoe.db.model.PlayerEntity;
import com.tictactoe.game.GameResult;
import com.tictactoe.game.Player;

import java.util.Date;
import java.util.List;

/**
 * Facade for database access
 */
public class DatabaseManager {

    private static DatabaseManager instance;

    private PlayerDB playerDB;
    private GameDB gameDB;

    /**
     * Private constructor to enforce singleton pattern
     */
    private DatabaseManager() {
        // Initialize database configuration
        DatabaseConfig.init();

        // Initialize database
        BaseDB.initDatabase();

        // Initialize DB classes
        playerDB = new PlayerDB();
        gameDB = new GameDB();
    }

    /**
     * Get the singleton instance
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Get a player by name, create if not exists
     */
    public PlayerEntity getOrCreatePlayer(String name) {
        return playerDB.getOrCreateByName(name);
    }

    /**
     * Save game result
     */
    public void saveGameResult(Player player1, Player player2, GameResult gameResult, int boardSize) {
        // Get or create player entities
        PlayerEntity player1Entity = getOrCreatePlayer(player1.getName());
        PlayerEntity player2Entity = getOrCreatePlayer(player2.getName());

        // Create game entity
        GameEntity gameEntity = new GameEntity();
        gameEntity.setPlayer1(player1Entity);
        gameEntity.setPlayer2(player2Entity);
        gameEntity.setBoardSize(boardSize);
        gameEntity.setDuration(gameResult.getGameDuration());
        gameEntity.setPlayedAt(new Date());

        // Set result and winner
        gameEntity.setResult(gameResult.getResult().toString());

        if (gameResult.getResult() == GameResult.Result.WIN) {
            if (gameResult.getWinner().equals(player1)) {
                gameEntity.setWinner(player1Entity);
            } else {
                gameEntity.setWinner(player2Entity);
            }
        }

        // Save game
        gameDB.saveGame(gameEntity);
    }

    /**
     * Get top players
     */
    public List<PlayerEntity> getTopPlayers(int limit) {
        return playerDB.getTopPlayers(limit);
    }

    /**
     * Get recent games
     */
    public List<GameEntity> getRecentGames(int limit) {
        return gameDB.getRecentGames(limit);
    }

    /**
     * Get games by player name
     */
    public List<GameEntity> getGamesByPlayerName(String playerName) {
        PlayerEntity player = playerDB.findByName(playerName);
        if (player != null) {
            return gameDB.getGamesByPlayer(player.getId());
        }
        return List.of(); // Empty list
    }

    /**
     * Get player statistics
     */
    public PlayerEntity getPlayerStatistics(String playerName) {
        return playerDB.findByName(playerName);
    }

    /**
     * Update player statistics manually
     */
    public void updatePlayerStatistics(PlayerEntity player) {
        playerDB.update(player);
        playerDB.saveChanges();
    }
}
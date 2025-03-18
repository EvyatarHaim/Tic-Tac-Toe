package com.tictactoe.db;

import com.tictactoe.db.model.GameEntity;
import com.tictactoe.db.model.PlayerEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Database access class for Game entities
 */
public class GameDB extends BaseDB<GameEntity> {

    private PlayerDB playerDB;

    public GameDB() {
        this.playerDB = new PlayerDB();
    }

    @Override
    protected String getTableName() {
        return "games";
    }

    @Override
    protected GameEntity createModel(ResultSet rs) throws SQLException {
        GameEntity game = new GameEntity();
        game.setId(rs.getInt("id"));
        game.setPlayer1Id(rs.getInt("player1_id"));
        game.setPlayer2Id(rs.getInt("player2_id"));

        int winnerId = rs.getInt("winner_id");
        if (!rs.wasNull()) {
            game.setWinnerId(winnerId);
        } else {
            game.setWinnerId(0); // 0 indicates a tie
        }

        game.setResult(rs.getString("result"));
        game.setBoardSize(rs.getInt("board_size"));
        game.setDuration(rs.getLong("duration"));
        game.setPlayedAt(new Date(rs.getTimestamp("played_at").getTime()));

        // Load related player entities
        game.setPlayer1(playerDB.getById(game.getPlayer1Id()));
        game.setPlayer2(playerDB.getById(game.getPlayer2Id()));

        if (game.getWinnerId() > 0) {
            game.setWinner(playerDB.getById(game.getWinnerId()));
        }

        return game;
    }

    @Override
    protected String createInsertSql(GameEntity entity) {
        return "INSERT INTO games (player1_id, player2_id, winner_id, result, board_size, duration, played_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String createUpdateSql(GameEntity entity) {
        return "UPDATE games SET player1_id = ?, player2_id = ?, winner_id = ?, result = ?, board_size = ?, duration = ?, played_at = ? WHERE id = ?";
    }

    @Override
    protected String createDeleteSql(GameEntity entity) {
        return "DELETE FROM games WHERE id = ?";
    }

    @Override
    protected PreparedStatement createInsertStatement(Connection conn, GameEntity entity) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(createInsertSql(entity), Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, entity.getPlayer1Id());
        pstmt.setInt(2, entity.getPlayer2Id());

        if (entity.getWinnerId() > 0) {
            pstmt.setInt(3, entity.getWinnerId());
        } else {
            pstmt.setNull(3, Types.INTEGER);
        }

        pstmt.setString(4, entity.getResult());
        pstmt.setInt(5, entity.getBoardSize());
        pstmt.setLong(6, entity.getDuration());
        pstmt.setTimestamp(7, new Timestamp(entity.getPlayedAt().getTime()));

        return pstmt;
    }

    @Override
    protected PreparedStatement createUpdateStatement(Connection conn, GameEntity entity) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(createUpdateSql(entity));
        pstmt.setInt(1, entity.getPlayer1Id());
        pstmt.setInt(2, entity.getPlayer2Id());

        if (entity.getWinnerId() > 0) {
            pstmt.setInt(3, entity.getWinnerId());
        } else {
            pstmt.setNull(3, Types.INTEGER);
        }

        pstmt.setString(4, entity.getResult());
        pstmt.setInt(5, entity.getBoardSize());
        pstmt.setLong(6, entity.getDuration());
        pstmt.setTimestamp(7, new Timestamp(entity.getPlayedAt().getTime()));
        pstmt.setInt(8, entity.getId());

        return pstmt;
    }

    @Override
    protected PreparedStatement createDeleteStatement(Connection conn, GameEntity entity) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(createDeleteSql(entity));
        pstmt.setInt(1, entity.getId());
        return pstmt;
    }

    /**
     * Save a new game and update player statistics
     */
    public void saveGame(GameEntity game) {
        // Ensure the game has a played_at date
        if (game.getPlayedAt() == null) {
            game.setPlayedAt(new Date());
        }

        // Update player statistics
        if (game.getPlayer1() != null && game.getPlayer2() != null) {
            PlayerEntity player1 = game.getPlayer1();
            PlayerEntity player2 = game.getPlayer2();

            // Update player1 statistics
            if (game.getWinnerId() == player1.getId()) {
                playerDB.updatePlayerStats(player1, "WIN");
                playerDB.updatePlayerStats(player2, "LOSE");
            } else if (game.getWinnerId() == player2.getId()) {
                playerDB.updatePlayerStats(player1, "LOSE");
                playerDB.updatePlayerStats(player2, "WIN");
            } else {
                playerDB.updatePlayerStats(player1, "TIE");
                playerDB.updatePlayerStats(player2, "TIE");
            }
        }

        // Save the game
        insert(game);
        saveChanges();

        // Save player changes
        playerDB.saveChanges();
    }

    /**
     * Get games by player ID
     */
    public List<GameEntity> getGamesByPlayer(int playerId) {
        List<GameEntity> games = new ArrayList<>();
        String sql = "SELECT * FROM games WHERE player1_id = ? OR player2_id = ? ORDER BY played_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, playerId);
            pstmt.setInt(2, playerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                games.add(createModel(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting games by player: " + e.getMessage());
        }

        return games;
    }

    /**
     * Get recent games
     */
    public List<GameEntity> getRecentGames(int limit) {
        List<GameEntity> games = new ArrayList<>();
        String sql = "SELECT * FROM games ORDER BY played_at DESC LIMIT ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                games.add(createModel(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting recent games: " + e.getMessage());
        }

        return games;
    }
}
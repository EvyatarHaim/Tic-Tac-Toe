package com.tictactoe.db;

import com.tictactoe.db.model.PlayerEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Database access class for Player entities
 */
public class PlayerDB extends BaseDB<PlayerEntity> {

    @Override
    protected String getTableName() {
        return "players";
    }

    @Override
    protected PlayerEntity createModel(ResultSet rs) throws SQLException {
        PlayerEntity player = new PlayerEntity();
        player.setId(rs.getInt("id"));
        player.setName(rs.getString("name"));
        player.setGamesPlayed(rs.getInt("games_played"));
        player.setGamesWon(rs.getInt("games_won"));
        player.setGamesLost(rs.getInt("games_lost"));
        player.setGamesTied(rs.getInt("games_tied"));
        return player;
    }

    @Override
    protected String createInsertSql(PlayerEntity entity) {
        return "INSERT INTO players (name, games_played, games_won, games_lost, games_tied) VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected String createUpdateSql(PlayerEntity entity) {
        return "UPDATE players SET name = ?, games_played = ?, games_won = ?, games_lost = ?, games_tied = ? WHERE id = ?";
    }

    @Override
    protected String createDeleteSql(PlayerEntity entity) {
        return "DELETE FROM players WHERE id = ?";
    }

    @Override
    protected PreparedStatement createInsertStatement(Connection conn, PlayerEntity entity) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(createInsertSql(entity), PreparedStatement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, entity.getName());
        pstmt.setInt(2, entity.getGamesPlayed());
        pstmt.setInt(3, entity.getGamesWon());
        pstmt.setInt(4, entity.getGamesLost());
        pstmt.setInt(5, entity.getGamesTied());
        return pstmt;
    }

    @Override
    protected PreparedStatement createUpdateStatement(Connection conn, PlayerEntity entity) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(createUpdateSql(entity));
        pstmt.setString(1, entity.getName());
        pstmt.setInt(2, entity.getGamesPlayed());
        pstmt.setInt(3, entity.getGamesWon());
        pstmt.setInt(4, entity.getGamesLost());
        pstmt.setInt(5, entity.getGamesTied());
        pstmt.setInt(6, entity.getId());
        return pstmt;
    }

    @Override
    protected PreparedStatement createDeleteStatement(Connection conn, PlayerEntity entity) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(createDeleteSql(entity));
        pstmt.setInt(1, entity.getId());
        return pstmt;
    }

    /**
     * Find a player by name
     */
    public PlayerEntity findByName(String name) {
        String sql = "SELECT * FROM players WHERE name = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return createModel(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error finding player by name: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get or create a player by name
     */
    public PlayerEntity getOrCreateByName(String name) {
        PlayerEntity player = findByName(name);

        if (player == null) {
            player = new PlayerEntity(name);
            insert(player);
            saveChanges();
        }

        return player;
    }

    /**
     * Get top players by win count
     */
    public List<PlayerEntity> getTopPlayers(int limit) {
        List<PlayerEntity> players = new ArrayList<>();
        String sql = "SELECT * FROM players ORDER BY games_won DESC LIMIT ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                players.add(createModel(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting top players: " + e.getMessage());
        }

        return players;
    }

    /**
     * Update player statistics after a game
     */
    public void updatePlayerStats(PlayerEntity player, String result) {
        player.incrementGamesPlayed();

        if (result.equals("WIN")) {
            player.incrementGamesWon();
        } else if (result.equals("LOSE")) {
            player.incrementGamesLost();
        } else if (result.equals("TIE")) {
            player.incrementGamesTied();
        }

        update(player);
    }
}
package com.tictactoe.db;

import com.tictactoe.db.model.BaseEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Base database access class that implements common database operations
 */
public abstract class BaseDB<T extends BaseEntity> {

    // Database connection information
    private static final String DB_URL = "jdbc:sqlite:tictactoe.db";
    private static final String DB_DRIVER = "org.sqlite.JDBC";

    // Change log lists to track changes
    private List<ChangeEntity> changeLog = new ArrayList<>();

    static {
        try {
            // Load the JDBC driver
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("Error loading database driver: " + e.getMessage());
        }
    }

    /**
     * Get a connection to the database
     */
    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Get the name of the table associated with this DB class
     */
    protected abstract String getTableName();

    /**
     * Create an entity from the current row of a ResultSet
     */
    protected abstract T createModel(ResultSet rs) throws SQLException;

    /**
     * Create SQL for inserting an entity
     */
    protected abstract String createInsertSql(T entity);

    /**
     * Create SQL for updating an entity
     */
    protected abstract String createUpdateSql(T entity);

    /**
     * Create SQL for deleting an entity
     */
    protected abstract String createDeleteSql(T entity);

    /**
     * Create a prepared statement for inserting an entity
     */
    protected abstract PreparedStatement createInsertStatement(Connection conn, T entity) throws SQLException;

    /**
     * Create a prepared statement for updating an entity
     */
    protected abstract PreparedStatement createUpdateStatement(Connection conn, T entity) throws SQLException;

    /**
     * Create a prepared statement for deleting an entity
     */
    protected abstract PreparedStatement createDeleteStatement(Connection conn, T entity) throws SQLException;

    /**
     * Get the entity by its ID
     */
    public T getById(int id) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return createModel(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error getting entity by ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get all entities from the table
     */
    public List<T> getAll() {
        List<T> entities = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                entities.add(createModel(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting all entities: " + e.getMessage());
        }

        return entities;
    }

    /**
     * Add entity to the insert queue
     */
    public void insert(T entity) {
        changeLog.add(new ChangeEntity(entity, ChangeType.INSERT, this::createInsertStatement));
    }

    /**
     * Add entity to the update queue
     */
    public void update(T entity) {
        changeLog.add(new ChangeEntity(entity, ChangeType.UPDATE, this::createUpdateStatement));
    }

    /**
     * Add entity to the delete queue
     */
    public void delete(T entity) {
        changeLog.add(new ChangeEntity(entity, ChangeType.DELETE, this::createDeleteStatement));
    }

    /**
     * Save all changes in the change log to the database
     */
    public void saveChanges() {
        if (changeLog.isEmpty()) {
            return;
        }

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            for (ChangeEntity change : changeLog) {
                PreparedStatement pstmt = change.createStatement(conn, (T) change.getEntity());
                pstmt.executeUpdate();

                if (change.getType() == ChangeType.INSERT) {
                    // Get the ID of the last inserted row
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            change.getEntity().setId(rs.getInt(1));
                        }
                    } catch (SQLException e) {
                        // Some JDBC drivers may not support getGeneratedKeys()
                        // In this case, we perform a separate query to get the last inserted ID
                        try (Statement stmt = conn.createStatement();
                             ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                            if (rs.next()) {
                                change.getEntity().setId(rs.getInt(1));
                            }
                        }
                    }
                }
            }

            conn.commit();
            changeLog.clear();

        } catch (SQLException e) {
            System.err.println("Error saving changes: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error rolling back transaction: " + ex.getMessage());
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Create the database tables if they don't exist
     */
    public static void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Create tables
            try (Statement stmt = conn.createStatement()) {
                // Create players table
                stmt.execute("CREATE TABLE IF NOT EXISTS players (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "games_played INTEGER DEFAULT 0," +
                        "games_won INTEGER DEFAULT 0," +
                        "games_lost INTEGER DEFAULT 0," +
                        "games_tied INTEGER DEFAULT 0" +
                        ")");

                // Create games table
                stmt.execute("CREATE TABLE IF NOT EXISTS games (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "player1_id INTEGER NOT NULL," +
                        "player2_id INTEGER NOT NULL," +
                        "winner_id INTEGER," +  // NULL or 0 for a tie
                        "result TEXT NOT NULL," +
                        "board_size INTEGER NOT NULL," +
                        "duration INTEGER NOT NULL," +
                        "played_at TIMESTAMP NOT NULL," +
                        "FOREIGN KEY (player1_id) REFERENCES players (id)," +
                        "FOREIGN KEY (player2_id) REFERENCES players (id)," +
                        "FOREIGN KEY (winner_id) REFERENCES players (id)" +
                        ")");
            }
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    /**
     * Enum representing the type of change
     */
    private enum ChangeType {
        INSERT, UPDATE, DELETE
    }

    /**
     * Interface for creating prepared statements
     */
    private interface StatementCreator<T extends BaseEntity> {
        PreparedStatement createStatement(Connection conn, T entity) throws SQLException;
    }

    /**
     * Class for storing entity changes
     */
    private class ChangeEntity {
        private BaseEntity entity;
        private ChangeType type;
        private StatementCreator<T> statementCreator;

        public ChangeEntity(BaseEntity entity, ChangeType type, StatementCreator<T> statementCreator) {
            this.entity = entity;
            this.type = type;
            this.statementCreator = statementCreator;
        }

        public BaseEntity getEntity() {
            return entity;
        }

        public ChangeType getType() {
            return type;
        }

        public PreparedStatement createStatement(Connection conn, T entity) throws SQLException {
            return statementCreator.createStatement(conn, entity);
        }
    }
}
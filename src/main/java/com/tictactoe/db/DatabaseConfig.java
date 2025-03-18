package com.tictactoe.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Database configuration utility class
 */
public class DatabaseConfig {
    private static final String DEFAULT_CONFIG_FILE = "database.properties";
    private static Properties properties = new Properties();
    private static boolean initialized = false;

    /**
     * Initialize database configuration with default properties file
     */
    public static void init() {
        init(DEFAULT_CONFIG_FILE);
    }

    /**
     * Initialize database configuration with specified properties file
     * @param configFile The configuration file path
     */
    public static void init(String configFile) {
        try (InputStream inputStream = DatabaseConfig.class.getClassLoader().getResourceAsStream(configFile)) {
            if (inputStream != null) {
                properties.load(inputStream);
                initialized = true;
                System.out.println("Database configuration loaded from " + configFile);
            } else {
                System.err.println("Configuration file not found: " + configFile);
                loadDefaultConfig();
            }
        } catch (IOException e) {
            System.err.println("Error loading database configuration: " + e.getMessage());
            loadDefaultConfig();
        }
    }

    /**
     * Load default database configuration
     */
    private static void loadDefaultConfig() {
        System.out.println("Loading default database configuration");
        properties.setProperty("db.url", "jdbc:mysql://localhost:3306/tictactoe");
        properties.setProperty("db.user", "root");
        properties.setProperty("db.password", "password");
        properties.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        initialized = true;
    }

    /**
     * Get a database configuration property
     * @param key The property key
     * @return The property value or null if not found
     */
    public static String getProperty(String key) {
        if (!initialized) {
            init();
        }
        return properties.getProperty(key);
    }

    /**
     * Get the database URL
     * @return The database URL
     */
    public static String getDbUrl() {
        return getProperty("db.url");
    }

    /**
     * Get the database username
     * @return The database username
     */
    public static String getDbUser() {
        return getProperty("db.user");
    }

    /**
     * Get the database password
     * @return The database password
     */
    public static String getDbPassword() {
        return getProperty("db.password");
    }

    /**
     * Get the database driver class name
     * @return The database driver class name
     */
    public static String getDbDriver() {
        return getProperty("db.driver");
    }
}
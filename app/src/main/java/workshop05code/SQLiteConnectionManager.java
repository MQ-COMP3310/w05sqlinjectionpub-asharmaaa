package workshop05code;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

//Import for logging exercise
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class SQLiteConnectionManager {

    private static final Logger logger = Logger.getLogger(SQLiteConnectionManager.class.getName());
    private Connection connection;
    private final String databasePath;

    public SQLiteConnectionManager(String dbPath) {
        this.databasePath = dbPath;
    }

    public boolean createNewDatabase(String fileName) {
        String url = "jdbc:sqlite:" + fileName;
        try {
            connection = DriverManager.getConnection(url);
            logger.info("Database connected successfully.");
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to connect to database.", e);
            return false;
        }
    }

    public boolean checkIfConnectionDefined() {
        return connection != null;
    }

    public boolean createWordleTables() {
        String sql = "CREATE TABLE IF NOT EXISTS valid_words (id INTEGER PRIMARY KEY, word TEXT UNIQUE NOT NULL);";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            logger.info("Database tables created successfully.");
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to create database tables.", e);
            return false;
        }
    }

    public void addValidWord(int id, String word) {
        String sql = "INSERT INTO valid_words(id, word) VALUES(?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, word);
            pstmt.executeUpdate();
            logger.info("Word added to database: " + word);
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to insert word into database: " + word, e);
        }
    }

    public boolean isValidWord(String word) {
        String sql = "SELECT COUNT(*) FROM valid_words WHERE word = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, word);
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error checking if word is valid: " + word, e);
            return false;
        }
    }

    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
                logger.info("Database connection closed.");
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to close database connection.", e);
        }
    }
}

package playerquests.utility.singleton;

import java.sql.Connection; // object describing connection to a database
import java.sql.DriverManager; // loads a database driver
import java.sql.SQLException; // thrown when a database operation fails

/**
 * API representing and providing access to the game database.
 * This when instantiated, creates and/or opens the game database.
 */
public class Database {

    /**
     * The singleton instance of this Database class.
     */
    private static Database instance = new Database();

    /**
     * The connection to the database.
     */
    private Connection connection;
    
    /**
     * Instantiates or locates a game SQLite database at a URL.
     * @param url where the database should be.
     */
    public Database() {
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:plugins/PlayerQuests/playerquests.db");
        } catch (SQLException e) {
            System.err.println("Could not connect to or create database: " + e.getMessage());
        }
    }

    /**
     * Gets the SQLite database representation.
     * @return the playerquests database API.
     */
    public static Database getInstance() {
        return instance;
    }

    /**
     * Gets the game database connection object.
     * @return an SQLite database connection
     */
    public Connection getConnection() {
        return this.connection;
    }
}

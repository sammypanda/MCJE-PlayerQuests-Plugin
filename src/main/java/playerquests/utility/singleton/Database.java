package playerquests.utility.singleton;

import java.sql.Connection; // object describing connection to a database
import java.sql.DriverManager; // loads a database driver
import java.sql.PreparedStatement; // represents prepared SQL statements
import java.sql.ResultSet; // represents SQL results
import java.sql.SQLException; // thrown when a database operation fails
import java.sql.Statement; // represents SQL statements
import java.util.List; // generic list type
import java.util.UUID; // how users are identified

import org.bukkit.Bukkit; // the Bukkit API

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
    private static Connection connection;
    
    /**
     * Database singleton should not be instantiated.
     * Use Database.getInstance().
     */
    private Database() {}

    /**
     * Creates tables if they do not already exist.
     */
    public Database init() {
        try {
            Statement statement = getConnection().createStatement();

            // Create players table
            String playersTableSQL = "CREATE TABLE IF NOT EXISTS players ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "uuid TEXT NOT NULL);";
            
            statement.execute(playersTableSQL);

        } catch (SQLException e) {
            System.err.println("Could not initialise the database: " + e.getMessage());
        }

        return this;
    }

    /**
     * Gets the SQLite database representation.
     * @return the playerquests database API.
     */
    public static Database getInstance() {
        return instance;
    }

    /**
     * Instantiates or locates a game SQLite database at a URL.
     * @return an SQLite database connection
     */
    public static Connection getConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
        } catch (SQLException e) {
            System.err.println("Could not check if existing connection was closed: " + e.getMessage());
            return null;
        }

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:plugins/PlayerQuests/playerquests.db");
            return getConnection();
        } catch (SQLException e) {
            System.err.println("Could not connect to or create database: " + e.getMessage());
            return null;
        }
    }

    /**
     * Adds a player to the database.
     * @param uuid the player UUID
     */
    public static void addPlayer(UUID uuid) {
        if (getPlayer(uuid) != null) {
            System.err.println("User with the UUID " + uuid + ", already exists in the database.");
            return;
        }

        try {
            // Add player to players table
            String addPlayerSQL = "INSERT INTO players (uuid) VALUES (?);";

            PreparedStatement preparedStatement = getConnection().prepareStatement(addPlayerSQL);

            preparedStatement.setString(1, uuid.toString()); // parameterIndex starts at 1

            preparedStatement.execute();

            getConnection().close();

        } catch (SQLException e) {
            System.err.println("Could not add the user " + Bukkit.getServer().getPlayer(uuid).getName() + ". " + e.getMessage());
        }
    }

    /**
     * Gets the id associated with a player, from the database.
     * @param uuid the player UUID
     * @return the database ID associated with this player
     */
    public static Integer getPlayer(UUID uuid) {
        try {
            // Add player to players table
            String addPlayerSQL = "SELECT * FROM players WHERE uuid = ?;";

            PreparedStatement preparedStatement = getConnection().prepareStatement(addPlayerSQL);
            preparedStatement.setString(1, uuid.toString()); // parameterIndex starts at 1

            ResultSet results = preparedStatement.executeQuery();
            Integer id = results.getInt("id");
            Boolean empty = !results.next(); // check if first row exists ("next" is misleading, "first call makes first row current")
            
            getConnection().close();

            if (empty) {
                return null;
            }

            return id;

        } catch (SQLException e) {
            System.err.println("Could not find the user associated with " + uuid.toString() + ". " + e.getMessage());
            return null;
        }
    }

    public Database addPlayers(List<UUID> uuids) {
        uuids.stream().forEach(uuid -> {
            addPlayer(uuid);
        });

        return this;
    }
}

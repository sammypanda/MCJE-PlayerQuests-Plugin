package playerquests.utility.singleton;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection; // object describing connection to a database
import java.sql.DriverManager; // loads a database driver
import java.sql.PreparedStatement; // represents prepared SQL statements
import java.sql.ResultSet; // represents SQL results
import java.sql.SQLException; // thrown when a database operation fails
import java.sql.Statement; // represents SQL statements
import java.util.ArrayList; // array list type
import java.util.List; // generic list type
import java.util.UUID; // how users are identified

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.bukkit.Bukkit; // the Bukkit API
import org.bukkit.util.FileUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import playerquests.Core;

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
        String version = "0.0";
        String version_db = Database.getPluginVersion();

        try (InputStream inputStream = getClass().getResourceAsStream("/META-INF/maven/moe.sammypanda/playerquests/pom.xml")) {
            if (inputStream != null) {
                MavenXpp3Reader reader = new MavenXpp3Reader();
                Model model = reader.read(new InputStreamReader(inputStream));
                version = model.getVersion();
            } else {
                System.err.println("Error: Resource not found.");
            }
        } catch (IOException | XmlPullParserException e) {
            System.err.println("Error reading pom.xml: " + e.getMessage());
        }

        try {
            Statement statement = getConnection().createStatement();

            // Create plugin table (for storing info)
            String pluginTableSQL = "CREATE TABLE IF NOT EXISTS plugin ("
            + "plugin TEXT PRIMARY KEY,"    
            + "version TEXT NOT NULL,"
            + "CONSTRAINT single_row_constraint UNIQUE (plugin));";
            statement.execute(pluginTableSQL);

            // Create players table
            String playersTableSQL = "CREATE TABLE IF NOT EXISTS players ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "uuid TEXT NOT NULL);";
            statement.execute(playersTableSQL);

            // Create quests table
            String questsTableSQL = "CREATE TABLE IF NOT EXISTS quests ("
                + "id TEXT PRIMARY KEY,"
                + "toggled BOOLEAN NOT NULL DEFAULT TRUE);";
            statement.execute(questsTableSQL);

            // Create diaries table
            String diariesTableSQL = "CREATE TABLE IF NOT EXISTS diaries ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "player INTEGER UNIQUE,"
                + "FOREIGN KEY (player) REFERENCES players(id));";
            statement.execute(diariesTableSQL);

            // Create diary_quests table
            String diary_questsTableSQL = "CREATE TABLE IF NOT EXISTS diary_quests ("
                + "id TEXT PRIMARY KEY,"
                + "stage TEXT NOT NULL,"
                + "action TEXT,"
                + "quest TEXT,"
                + "diary INTEGER,"
                + "FOREIGN KEY (quest) REFERENCES quests(id)"
                + "FOREIGN KEY (diary) REFERENCES diaries(id));";
            statement.execute(diary_questsTableSQL);

            statement.close();
            getConnection().close();

            Database.migrate(version, version_db);

        } catch (SQLException e) {
            System.err.println("Could not initialise the database: " + e.getMessage());
        }

        return this;
    }

    private static void migrate(String version, String version_db) {
        if (version_db.equals(version)) {
            // Nothing to migrate/update
            return;
        }

        if (version_db.equals("0.0")) {
            try {
                // Insert PlayerQuests version from plugin table
                String setPluginSQL = "INSERT INTO plugin (plugin, version) VALUES (?, ?);";
                PreparedStatement preparedStatement = getConnection().prepareStatement(setPluginSQL);
                preparedStatement.setString(1, "PlayerQuests"); // parameterIndex starts at 1
                preparedStatement.setString(2, version);
                preparedStatement.execute();

                getConnection().close();
            } catch (SQLException e) {
                System.err.println("Could not insert plugin data to db " + e.getMessage());
            }
        }

        try {
            Statement statement = getConnection().createStatement();

            switch (version_db) {
                case "0.4":
                    String addToggledSQL = "ALTER TABLE quests ADD COLUMN toggled TEXT DEFAULT true;";
                    statement.execute(addToggledSQL);
                    break;
            }
        } catch (SQLException e) {
            System.err.println("Could not patch/migrate database " + e.getMessage());
        }

        // update plugin version in db
        Database.setPluginVersion(version);
    }

    private static String getPluginVersion() {
        if (!Files.exists(Paths.get("plugins/PlayerQuests/playerquests.db"))) {
            // Don't try to continue with fetching version if no database
            return "0.0";
        }

        try {
            Statement statement = getConnection().createStatement();

            // Get PlayerQuests version from plugin table
            String getVersionSQL = "SELECT version FROM plugin WHERE plugin = 'PlayerQuests';";
            ResultSet results = statement.executeQuery(getVersionSQL);
            String version = results.getString("version");

            getConnection().close();
            return version;
        
        } catch (SQLException e) {
            System.err.println("Could not find the quest version in the db " + e.getMessage());
            return "0.0";
        }
    }

    private static void setPluginVersion(String version) {
        try {
            // Set PlayerQuests version in plugin table
            String setVersionSQL = "UPDATE plugin SET version = ? WHERE plugin = 'PlayerQuests';";
            PreparedStatement preparedStatement = getConnection().prepareStatement(setVersionSQL);
            preparedStatement.setString(1, version);
            preparedStatement.execute();

            getConnection().close();
        
        } catch (SQLException e) {
            System.err.println("Could not set the quest version in the db " + e.getMessage());
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
     * @return the player ID in database
     */
    public Integer addPlayer(UUID uuid) {
        Integer id = getPlayer(uuid);

        if (id != null) {
            return id;
        }

        try {
            // Add player to players table
            String addPlayerSQL = "INSERT INTO players (uuid) VALUES (?) RETURNING *;";

            PreparedStatement preparedStatement = getConnection().prepareStatement(addPlayerSQL);

            preparedStatement.setString(1, uuid.toString()); // parameterIndex starts at 1

            ResultSet results = preparedStatement.executeQuery();
            id = results.getInt("id");

            getConnection().close();

            return id;

        } catch (SQLException e) {
            System.err.println("Could not add the user " + Bukkit.getServer().getPlayer(uuid).getName() + ". " + e.getMessage());
            return null;
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

    /**
     * Gets the id for all quests, as stored in the database.
     * @return all known quest IDs
     */
    public List<String> getAllQuests() {
        // Create list to add results to
        List<String> ids = new ArrayList<String>();

        // Get quest IDs from quests table
        try {
            Statement statement = getConnection().createStatement();
            String allQuestsSQL = "SELECT id FROM quests;";

            ResultSet result = statement.executeQuery(allQuestsSQL);

            while(result.next()) {
                ids.add(result.getString("id"));
            }

            return ids;
            
        } catch (SQLException e) {
            System.err.println("Could not retrieve quests from database. " + e.getMessage());
            return null;
        }
    }

    /**
     * Adds a quest reference to the database.
     * @param id the ID to refer to the quest by
     */
    public static void addQuest(String id) {
        if (id == null) {
            return;
        }

        if (getQuest(id) != null) {
            return;
        }

        try {
            // Add player to players table
            String addQuestSQL = "INSERT INTO quests (id) VALUES (?);";

            PreparedStatement preparedStatement = getConnection().prepareStatement(addQuestSQL);

            preparedStatement.setString(1, id); // parameterIndex starts at 1

            preparedStatement.execute();

            getConnection().close();

        } catch (SQLException e) {
            System.err.println("Could not add the quest " + id + ". " + e.getMessage());
        }
    }

    /**
     * Adds a quest reference to the database.
     * @param id the ID to refer to the quest by
     */
    public static String getQuest(String id) {
        if (id == null) {
            return null;
        }

        try {
            // Add player to players table
            String addQuestSQL = "SELECT id FROM quests WHERE id = ?;";

            PreparedStatement preparedStatement = getConnection().prepareStatement(addQuestSQL);

            preparedStatement.setString(1, id); // parameterIndex starts at 1

            ResultSet results = preparedStatement.executeQuery();
            String quest = results.getString("id");

            getConnection().close();

            return quest;

        } catch (SQLException e) {
            System.err.println("Could not add the quest " + id + ". " + e.getMessage());
            return null;
        }
    }

    public static Boolean getQuestToggled(String id) {
        try {
            // Add player to players table
            String addQuestSQL = "SELECT toggled FROM quests WHERE id = ?;";

            PreparedStatement preparedStatement = getConnection().prepareStatement(addQuestSQL);

            preparedStatement.setString(1, id); // parameterIndex starts at 1

            ResultSet results = preparedStatement.executeQuery();
            Boolean state = results.getBoolean("toggled");

            getConnection().close();

            return state;

        } catch (SQLException e) {
            System.err.println("Could not get the quest to toggle " + id + ". " + e.getMessage());
            return null;
        }
    }

    public static void setQuestToggled(String id, Boolean state) {
        try {
            // Add player to players table
            String addQuestSQL = "UPDATE quests SET toggled = ? WHERE id = ?;";

            PreparedStatement preparedStatement = getConnection().prepareStatement(addQuestSQL);

            preparedStatement.setBoolean(1, state); // parameterIndex starts at 1
            preparedStatement.setString(2, id);
            preparedStatement.execute();

            getConnection().close();

        } catch (SQLException e) {
            System.err.println("Could not toggle the quest " + id + ". " + e.getMessage());
        }
    }

    /**
     * Removes a quest reference from the database.
     * @param id the ID to refer to the quest by
     */
    public static void removeQuest(String id) {
        if (id == null) {
            return;
        }

        try {
            PreparedStatement preparedStatement;

            // Remove quest from quests table
            String removeQuestSQL = "DELETE FROM quests WHERE id = ?;";
            preparedStatement = getConnection().prepareStatement(removeQuestSQL);
            preparedStatement.setString(1, id);
            preparedStatement.execute();

            // Remove quest reference from quest diaries
            String removeDiaryQuestSQL = "DELETE FROM diary_quests WHERE quest = ?;";
            preparedStatement = getConnection().prepareStatement(removeDiaryQuestSQL);
            preparedStatement.setString(1, id);
            preparedStatement.execute();

            getConnection().close();
        
        } catch (SQLException e) {
            System.err.println("Could not remove the quest " + id + ". " + e.getMessage());
        }
    }
}

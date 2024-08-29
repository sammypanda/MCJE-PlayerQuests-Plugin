package playerquests.utility.singleton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection; // object describing connection to a database
import java.sql.DriverManager; // loads a database driver
import java.sql.PreparedStatement; // represents prepared SQL statements
import java.sql.ResultSet; // represents SQL results
import java.sql.SQLException; // thrown when a database operation fails
import java.sql.Statement; // represents SQL statements
import java.util.ArrayList; // array list type
import java.util.HashMap;
import java.util.List; // generic list type
import java.util.Map;
import java.util.UUID; // how users are identified

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.bukkit.Bukkit; // the Bukkit API
import org.bukkit.entity.Player;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import playerquests.Core;
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.data.ConnectionsData;
import playerquests.builder.quest.data.StagePath;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.quest.QuestDiary;
import playerquests.product.Quest;
import playerquests.utility.ChatUtils;
import playerquests.utility.MigrationUtils;
import playerquests.utility.ChatUtils.MessageBuilder;
import playerquests.utility.ChatUtils.MessageStyle;
import playerquests.utility.ChatUtils.MessageTarget;
import playerquests.utility.ChatUtils.MessageType;

/**
 * Provides access to the game database, managing connections and database operations.
 * <p>
 * This class is a singleton, meaning only one instance of it exists throughout the application.
 * It handles the initialization of the database, version management, and various CRUD operations
 * related to players, quests, and diaries.
 * </p>
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
     * Private constructor to prevent instantiation from outside the class.
     * Use {@link #getInstance()} to get the singleton instance.
     */
    private Database() {}
   
    /**
     * Returns the singleton instance of the Database class.
     * 
     * @return the singleton instance of the Database class
     */
    public static Database getInstance() {
        return instance;
    }
    
    /**
     * Retrieves a connection to the database.
     * <p>
     * This method checks if there is an existing open connection and returns it. 
     * If there is no open connection, it creates a new connection to the SQLite database.
     * </p>
     * 
     * @return the database connection
     */
    private synchronized Connection getConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
        } catch (SQLException e) {
            System.err.println("Could not check if existing connection was closed: " + e.getMessage());
        }
        
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:plugins/PlayerQuests/playerquests.db");
            return connection;
        } catch (SQLException e) {
            System.err.println("Could not connect to or create database: " + e.getMessage());
            return null;
        }
    }

    /**
     * Initializes the database schema and performs any necessary migrations.
     * <p>
     * This method creates the necessary tables if they do not exist and performs
     * any required migrations based on the current version of the plugin.
     * </p>
     * 
     * @return the Database instance for method chaining
     */
    public synchronized Database init() {
        String dbVersion = getPluginVersion();
        String version = "0.0"; // default version
        
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
        
        try (Connection connection = getConnection();
            Statement statement = connection.createStatement()) {

            String pluginTableSQL = "CREATE TABLE IF NOT EXISTS plugin ("
            + "plugin TEXT PRIMARY KEY,"
            + "version TEXT NOT NULL,"
            + "CONSTRAINT single_row_constraint UNIQUE (plugin));";
            statement.execute(pluginTableSQL);
            
            String playersTableSQL = "CREATE TABLE IF NOT EXISTS players ("
            + "uuid TEXT PRIMARY KEY NOT NULL);";
            statement.execute(playersTableSQL);
            
            String questsTableSQL = "CREATE TABLE IF NOT EXISTS quests ("
            + "id TEXT PRIMARY KEY,"
            + "toggled BOOLEAN NOT NULL DEFAULT TRUE);";
            statement.execute(questsTableSQL);
            
            String diariesTableSQL = "CREATE TABLE IF NOT EXISTS diaries ("
            + "id TEXT PRIMARY KEY NOT NULL,"
            + "player TEXT UNIQUE,"
            + "FOREIGN KEY (player) REFERENCES players(uuid));";
            statement.execute(diariesTableSQL);
            
            String diary_questsTableSQL = "CREATE TABLE IF NOT EXISTS diary_quests ("
            + "id TEXT PRIMARY KEY,"
            + "stage TEXT NOT NULL,"
            + "action TEXT,"
            + "quest TEXT,"
            + "diary TEXT,"
            + "FOREIGN KEY (quest) REFERENCES quests(id)"
            + "FOREIGN KEY (diary) REFERENCES diaries(id));";
            statement.execute(diary_questsTableSQL);
            
            migrate(version, dbVersion);

        } catch (SQLException e) {
            System.err.println("Could not initialise the database: " + e.getMessage());
        }

        return this;
    }
   
    /**
     * Performs database migrations if the version has changed.
     * <p>
     * This method checks if the database schema version differs from the current plugin version
     * and applies necessary migrations. It also checks for new plugin releases and sends an alert
     * if a new version is available.
     * </p>
     * 
     * @param version the current version of the plugin
     * @param version_db the version of the database schema
     */
    private synchronized void migrate(String version, String version_db) {
        // Check if there is a new version
        MessageBuilder alert = new MessageBuilder("Could not retrieve latest version. Maybe you're offline or GitHub is unavailable?")
            .style(MessageStyle.PRETTY)
            .target(MessageTarget.WORLD)
            .type(MessageType.WARN);
        try {
            URL url = new URI("https://api.github.com/repos/sammypanda/mcje-playerquests-plugin/releases").toURL();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            // Read the entire input stream into a single string
            String content;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                content = in
                    .lines()
                    .reduce("", (accumulator, actual) -> accumulator + actual);
            }

            // Parse the JSON and print the desired value
            JsonNode tree = new ObjectMapper().readTree(content);
            String latest = tree.findValue("tag_name").textValue(); // get latest tag_name (for example "v0.5")

            // Alert if latest version is different to current
            // (this could happen on snapshots, but the compute for this edge case doesn't seem worthwhile)
            if (!("v"+version).equals(latest)) {
                alert.content("A new release is available! " + latest)
                    .send();
            }
        } catch (URISyntaxException | IOException e) {
            alert.send();
        }

        // don't migrate if no version change 
        // (pom version same as db version)
        if (version_db.equals(version)) {
            return;
        }
        
        try (Connection connection = getConnection();
            Statement statement = connection.createStatement()) {

            StringBuilder query = new StringBuilder();
            
            switch (version) {
                case "0.6":
                case "0.5.2":
                case "0.5.1":
                    query.append(MigrationUtils.dbV0_5_1());
                case "0.5":
                case "0.4":
                    query.append(MigrationUtils.dbV0_4());
            }

            statement.executeUpdate(query.toString());

        } catch (SQLException e) {
            System.err.println("Could not patch/migrate database " + e.getMessage());
        }
        
        // Update plugin version in db
        setPluginVersion(version);
        ChatUtils.message("You're on v" + version + "! https://sammypanda.moe/docs/playerquests/v" + version)
            .target(MessageTarget.WORLD)
            .type(MessageType.NOTIF)
            .send();
    }
    
    /**
     * Retrieves the current version of the plugin from the database.
     * <p>
     * This method queries the `plugin` table to get the version of the plugin stored
     * in the database. If the database file does not exist, it returns the default version "0.0".
     * </p>
     * 
     * @return the current version of the plugin
     */
    public synchronized String getPluginVersion() {
        if (!Files.exists(Paths.get("plugins/PlayerQuests/playerquests.db"))) {
            return "0.0";
        }
        
        try (Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT version FROM plugin WHERE plugin = 'PlayerQuests';")) {

            ResultSet results = statement.executeQuery();
            String version = results.getString("version");

            return version;
        } catch (SQLException e) {
            System.err.println("Could not find the quest version in the db " + e.getMessage());
            return "0.0";
        }
    }
    
    /**
     * Sets the current version of the plugin in the database.
     * <p>
     * This method updates the version in the `plugin` table to the provided version.
     * </p>
     * 
     * @param version the new version of the plugin
     */
    private synchronized void setPluginVersion(String version) {
        try (Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("""
                INSERT INTO plugin (plugin, version)
                VALUES ('PlayerQuests', ?)
                ON CONFLICT(plugin)
                DO UPDATE SET version = excluded.version;
            """)) {

            preparedStatement.setString(1, version);

            preparedStatement.execute();

        } catch (SQLException e) {
            System.err.println("Could not insert or set the quest version in the db " + e.getMessage());
        }
    }

    /**
     * Adds a new player to the database with the specified UUID.
     * 
     * This method inserts a new record into the `players` table of the database using the provided
     * UUID. The UUID is converted to a string and stored in the `uuid` column. The SQL `INSERT` statement
     * is used along with the `RETURNING *` clause to execute the query. If an {@link SQLException} occurs, 
     * an error message is logged to the standard error stream, including the name of the player associated 
     * with the given UUID.
     * 
     * <p>Note: Ensure that the player with the provided UUID is online or exists to avoid {@link NullPointerException}
     * when accessing the player's name.</p>
     * 
     * @param uuid The unique identifier for the player, represented as a {@link UUID}. This ID is
     *             used to insert a new record into the `players` table in the database.
     */
    public synchronized void addPlayer(UUID uuid) {
        try (Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO players (uuid) VALUES (?) RETURNING *;")) {
            
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.executeQuery();
        
        } catch (SQLException e) {
            System.err.println("Could not add the user " + Bukkit.getServer().getPlayer(uuid).getName() + ". " + e.getMessage());
        }
    }
    
    /**
     * Retrieves the diary record associated with the specified player ID.
     * 
     * This method queries the `diaries` table to retrieve the diary ID(s) for the player with the given
     * database player ID. If no diary is found, this method will return an empty result set.
     * 
     * <p>Note: The caller is responsible for closing the {@link ResultSet} after use.</p>
     * 
     * @param dbPlayerID The database player ID used to query the `diaries` table.
     * @return A {@link ResultSet} containing the diary records associated with the specified player ID,
     *         or null if an error occurs.
     */
    public synchronized ResultSet getDiary(Integer dbPlayerID) {
        try (Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT id FROM diaries WHERE player = ?")) {
            
            preparedStatement.setInt(1, dbPlayerID);
            
            ResultSet results = preparedStatement.executeQuery();
            
            return results;
        } catch (SQLException e) {
            System.err.println("Could not find a diary for db player ID: " + dbPlayerID + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Retrieves the quest record from a diary for a specified quest ID and diary ID.
     * 
     * This method queries the `diary_quests` table to retrieve the quest information associated with
     * the given quest ID and diary ID. If no matching record is found, this method will return an empty result set.
     * 
     * <p>Note: The caller is responsible for closing the {@link ResultSet} after use.</p>
     * 
     * @param questID The ID of the quest.
     * @param dbDiaryID The ID of the diary.
     * @return A {@link ResultSet} containing the quest records for the specified quest ID and diary ID,
     *         or null if an error occurs.
     */
    public synchronized ResultSet getDiaryQuest(String questID, Integer dbDiaryID) {
        try (Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM diary_quests WHERE quest = ? AND diary = ?")) {
            
            preparedStatement.setString(1, questID);
            preparedStatement.setInt(2, dbDiaryID);
            
            ResultSet result = preparedStatement.executeQuery();
            
            return result;
        } catch (SQLException e) {
            System.err.println("Could not find quest in the diary: " + questID + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Updates or inserts quest progress for a specified diary and quest.
     * 
     * This method updates or inserts a record into the `diary_quests` table with information about the quest
     * progress for the specified diary and quest. If a `ConnectionsData` object is provided, it updates
     * the quest progress with the current stage and action from the connections data.
     * 
     * @param diary The {@link QuestDiary} instance containing the diary information.
     * @param quest The {@link Quest} instance containing the quest information.
     */
    public synchronized void setDiaryQuest(QuestDiary diary, Quest quest) {
        setDiaryQuest(diary, quest, quest.getConnections());
    }

    /**
     * Updates or inserts quest progress for a specified diary and quest with given connections data.
     * 
     * This method updates or inserts a record into the `diary_quests` table with information about the quest
     * progress for the specified diary and quest. It uses the provided {@link ConnectionsData} to set the
     * current stage and action of the quest if available.
     * 
     * @param diary The {@link QuestDiary} instance containing the diary information.
     * @param quest The {@link Quest} instance containing the quest information.
     * @param connections The {@link ConnectionsData} instance containing the current stage and action.
     */
    public synchronized void setDiaryQuest(QuestDiary diary, Quest quest, ConnectionsData connections) {
        String questID = quest.getID(); // get the quest ID
        Player player = diary.getPlayer(); // get the player this diary represents

        try (Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT OR REPLACE INTO diary_quests (id, stage, action, quest, diary) VALUES (?, ?, ?, ?, ?)")) {
            
            preparedStatement.setString(1, player.getUniqueId().toString() + "_" + questID);
            
            // default to initial values
            preparedStatement.setString(2, quest.getEntry().getStage());
            preparedStatement.setString(3, quest.getEntry().getAction());
            
            // get the current quest stage or action
            StagePath currentConnection = connections.getCurr();
            
            // replace with current values (if possible)
            if (currentConnection != null) {
                preparedStatement.setString(2, currentConnection.getStage());
                preparedStatement.setString(3, currentConnection.getAction());
            }
            
            // set remaining values
            preparedStatement.setString(4, questID);
            preparedStatement.setString(5, diary.getDiaryID());
            
            preparedStatement.execute();
            
        } catch (SQLException e) {
            System.err.println("Could not set or update quest progress for the " + questID + " quest: " + e.getMessage());
            return;
        }
    }

    /**
     * Adds multiple new players to the database.
     * 
     * This method iterates over a list of UUIDs and adds each one to the `players` table by calling
     * {@link #addPlayer(UUID)} for each UUID in the list.
     * 
     * @param uuids A list of {@link UUID} objects representing the players to be added.
     * @return The current {@link Database} instance for method chaining.
     */
    public synchronized Database addPlayers(List<UUID> uuids) {
        for (UUID uuid : uuids) {
            addPlayer(uuid);
        }
        
        return this;
    }
    
    /**
     * Retrieves all quest IDs from the database.
     * 
     * This method queries the `quests` table to retrieve a list of all quest IDs. The IDs are collected
     * into a list and returned. If an error occurs during the query, an empty list is returned.
     * 
     * @return A list of quest IDs retrieved from the database, or an empty list if an error occurs.
     */
    public synchronized List<String> getAllQuests() {
        List<String> ids = new ArrayList<>();
        try (Connection connection = getConnection();
        Statement statement = connection.createStatement()) {
            
            String allQuestsSQL = "SELECT id FROM quests;";
            ResultSet result = statement.executeQuery(allQuestsSQL);
            
            while (result.next()) {
                ids.add(result.getString("id"));
            }
            
        } catch (SQLException e) {
            System.err.println("Could not retrieve quests from database. " + e.getMessage());
        }
        return ids;
    }
    
    /**
     * Adds a new quest to the database if it does not already exist.
     * 
     * This method inserts a new quest record into the `quests` table if the quest with the specified ID
     * does not already exist. If the quest ID is null or if the quest already exists, no action is taken.
     * 
     * @param id The ID of the quest to be added.
     */
    public synchronized void addQuest(String id) {
        if (id == null) {
            return;
        }
        
        String existingId = getQuest(id);
        
        if (existingId != null) {
            return;
        }
        try (Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO quests (id) VALUES (?);")) {
            
            preparedStatement.setString(1, id);
            preparedStatement.execute();
            
        } catch (SQLException e) {
            System.err.println("Could not add the quest " + id + ". " + e.getMessage());
        }
    }
    
    /**
     * Retrieves the quest ID from the database.
     * 
     * This method queries the `quests` table to find a quest with the specified ID. If the quest with the given
     * ID exists, the method returns the ID; otherwise, it returns null. If an error occurs during the query,
     * an error message is logged and null is returned.
     * 
     * <p>Note: This method assumes that the `id` parameter is a valid quest ID format.</p>
     * 
     * @param id The ID of the quest to retrieve.
     * @return The quest ID if found, or null if no quest with the given ID exists or if an error occurs.
     */
    public synchronized String getQuest(String id) {
        if (id == null) {
            return null;
        }
        
        try (Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT id FROM quests WHERE id = ?;")) {
            
            preparedStatement.setString(1, id);
            
            ResultSet results = preparedStatement.executeQuery();
            String quest = results.getString("id");
            
            return quest;
        } catch (SQLException e) {
            System.err.println("Could not get the quest " + id + ". " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Retrieves the toggle status of a quest.
     * 
     * This method queries the `quests` table to find the toggle status of the quest identified by the specified
     * quest ID. If the quest exists, it returns its toggle status as a {@link Boolean}. If no such quest is found
     * or if an error occurs during the query, an error message is logged and null is returned.
     * 
     * @param quest The {@link Quest} object whose toggle status is to be retrieved.
     * @return The toggle status of the quest if found, or null if no such quest exists or if an error occurs.
     */
    public synchronized Boolean getQuestToggled(Quest quest) {
        try (Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT toggled FROM quests WHERE id = ?;")) {
            
            preparedStatement.setString(1, quest.getID());
            ResultSet results = preparedStatement.executeQuery();
            Boolean result = false;
            
            if (results.next()) {
                result = results.getBoolean("toggled");
            }
            
            return result; // no result found
        } catch (SQLException e) {
            System.err.println("Could not get the quest toggle status " + quest.toString() + ". " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Updates the toggle status of a quest.
     * 
     * This method updates the toggle status of the quest identified by the specified quest ID in the `quests` table.
     * It sets the toggle status to the specified value. If an error occurs during the update, an error message is
     * logged to the standard error stream.
     * 
     * @param quest The {@link Quest} object whose toggle status is to be updated.
     * @param state The new toggle status to set for the quest.
     */
    public synchronized void setQuestToggled(Quest quest, Boolean state) {
        try (Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE quests SET toggled = ? WHERE id = ?;")) {
            
            preparedStatement.setBoolean(1, state);
            preparedStatement.setString(2, quest.getID());
            preparedStatement.execute();
            
        } catch (SQLException e) {
            System.err.println("Could not toggle the quest " + quest.toString() + ". " + e.getMessage());
        }
    }
    
    /**
     * Removes a quest from the database.
     * 
     * This method deletes the quest with the specified ID from the `quests` table and also removes any related
     * entries from the `diary_quests` table. If the provided ID is null or an error occurs during the deletion,
     * an error message is logged to the standard error stream.
     * 
     * @param id The ID of the quest to be removed.
     */
    public synchronized void removeQuest(String id) {
        if (id == null) {
            return;
        }
        
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement;
            
            String removeQuestSQL = "DELETE FROM quests WHERE id = ?;";
            preparedStatement = getConnection().prepareStatement(removeQuestSQL);
            preparedStatement.setString(1, id);
            preparedStatement.execute();
            
            String removeDiaryQuestSQL = "DELETE FROM diary_quests WHERE quest = ?;";
            preparedStatement = getConnection().prepareStatement(removeDiaryQuestSQL);
            preparedStatement.setString(1, id);
            preparedStatement.execute();
            
        } catch (SQLException e) {
            System.err.println("Could not remove the quest " + id + ". " + e.getMessage());
        }
    }

    /**
     * Inserts or updates a {@link QuestDiary} record in the database.
     * 
     * This method takes a {@link QuestDiary} object and inserts or updates the corresponding record in the
     * `diaries` table of the database. The operation is performed within a try-with-resources block to ensure
     * proper resource management. If an {@link SQLException} occurs, an error message is logged to the standard
     * error stream.
     * 
     * <p>Note: The method assumes that the `diaries` table has columns `id` and `player` where the diary ID and
     * player ID are stored, respectively. The SQL `INSERT OR REPLACE` statement is used to handle both insertion
     * of new records and updating of existing records.</p>
     * 
     * @param questDiary The {@link QuestDiary} instance containing the diary information to be inserted or updated.
     */
    public synchronized void addDiary(QuestDiary questDiary) {
        try (Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT OR REPLACE INTO diaries (id, player) VALUES (?, ?)")) {
            
            preparedStatement.setString(1, questDiary.getDiaryID());
            preparedStatement.setString(2, questDiary.getPlayerID());
            
            preparedStatement.execute();
            
        } catch (SQLException e) {
            System.err.println("Could not create a diary for: " + questDiary.getPlayerID() + ": " + e.getMessage());
        }
    }

    /**
     * Retrieves the quest progress for a given {@link QuestDiary}.
     * 
     * This method queries the `diary_quests` table to retrieve the quest progress information for the specified
     * diary. It returns a map where the keys are {@link Quest} objects and the values are {@link ConnectionsData}
     * representing the progress for each quest. If an error occurs during the retrieval, an error message is logged
     * and null is returned.
     * 
     * <p>Note: The method assumes that the `diary_quests` table contains columns for `quest`, `stage`, and `action`.
     * It also requires the presence of a valid quest registry to retrieve the quest details.</p>
     * 
     * @param questDiary The {@link QuestDiary} instance for which quest progress is to be retrieved.
     * @return A map containing quests and their corresponding progress data, or null if an error occurs.
     */
	public synchronized Map<Quest,ConnectionsData> getQuestProgress(QuestDiary questDiary) {
        Map<Quest,ConnectionsData> progress = new HashMap<Quest,ConnectionsData>();
        
		try (Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM diary_quests WHERE diary = ?")) {
            
            preparedStatement.setString(1, questDiary.getDiaryID());
            
            ResultSet results = preparedStatement.executeQuery();

            while (results.next()) {
                Quest quest = Core.getQuestRegistry().getQuest(results.getString("quest"));

                if (quest == null) {
                    continue; // skip to next
                }

                QuestStage stage = quest.getStages().get(results.getString("stage"));
                QuestAction action = stage.getActions().get(results.getString("action"));

                progress.put(
                    quest, // put the quest if found
                    new ConnectionsData(
                        null, 
                        new StagePath(stage, action), 
                        null
                    )
                );
            }

            return progress;
            
        } catch (SQLException e) {
            System.err.println("Could not find quest progress in db for " + questDiary.getPlayer() + ": " + e.getMessage());
            return null;
        }
	}
}

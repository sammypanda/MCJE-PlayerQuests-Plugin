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
import java.util.Properties;
import java.util.UUID; // how users are identified

import org.bukkit.Bukkit; // the Bukkit API
import org.bukkit.entity.Player;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import playerquests.Core;
import playerquests.builder.quest.data.StagePath;
import playerquests.client.quest.QuestDiary;
import playerquests.product.Quest;
import playerquests.utility.ChatUtils;
import playerquests.utility.ChatUtils.MessageBuilder;
import playerquests.utility.ChatUtils.MessageStyle;
import playerquests.utility.ChatUtils.MessageTarget;
import playerquests.utility.ChatUtils.MessageType;
import playerquests.utility.MigrationUtils;
import playerquests.utility.serialisable.ItemSerialisable;
import playerquests.utility.serialisable.data.ItemData;

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
    private static final Database instance = new Database();

    /**
     * The connection to the database.
     */
    private static Connection connection;

    /**
     * If this is a fresh install.
     */
    private Boolean isFresh = false;

    /**
     * Private constructor to prevent instantiation from outside the class.
     * Use {@link #getInstance()} to get the singleton instance.
     */
    private Database() {
        // Unused, is a Singleton
    }

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
            ChatUtils.message("Could not check if existing connection was closed: " + e.getMessage())
                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .send();
        }

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:plugins/PlayerQuests/playerquests.db");
            return connection;
        } catch (SQLException e) {
            ChatUtils.message("Could not connect to or create database: " + e.getMessage())
                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .send();
            return null;
        }
    }

    /**
     * Checks if we can connect to the database.
     * @return true if able to get Connection object
     */
    public boolean canConnect() {
        return (this.getConnection() != null);
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

        try (InputStream input = getClass().getResourceAsStream("/plugin.properties")) {
            Properties props = new Properties();
            props.load(input);
            version = props.getProperty("version");
        } catch (Exception e) {
            ChatUtils.message("Failed to read PlayerQuests version property from pom: " + e.getMessage())
                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .send();
        }

        try (Connection c = getConnection();
            Statement statement = c.createStatement()) {

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
            + "toggled BOOLEAN NOT NULL DEFAULT TRUE,"
            + "inventory TEXT NOT NULL DEFAULT '');";
            statement.execute(questsTableSQL);

            String diariesTableSQL = "CREATE TABLE IF NOT EXISTS diaries ("
            + "id TEXT PRIMARY KEY NOT NULL,"
            + "player TEXT UNIQUE,"
            + "FOREIGN KEY (player) REFERENCES players(uuid));";
            statement.execute(diariesTableSQL);

            String diaryEntriesTableSQL = "CREATE TABLE IF NOT EXISTS diary_entries ("
            + "diary TEXT NOT NULL,"
            + "quest TEXT NOT NULL,"
            + "action TEXT NOT NULL,"
            + "completion BOOLEAN NOT NULL,"
            + "FOREIGN KEY (quest) REFERENCES quests(id),"
            + "FOREIGN KEY (diary) REFERENCES diaries(id),"
            + "UNIQUE(diary, quest, action));";
            statement.execute(diaryEntriesTableSQL);

            // Migrate to new versions if applicable
            migrate(version, dbVersion);

            // Report to rest of plugin if a fresh install
            isFresh();

        } catch (SQLException e) {
            ChatUtils.message("Could not initialise the database: " + e.getMessage())
                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .send();
        }

        // recover the quest inventories as stored in the db
        Core.getQuestRegistry().loadQuestInventories();

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
     * @param versionDB the version of the database schema
     */
    @SuppressWarnings("java:S128") // switch-case fall-through intended
    private synchronized void migrate(String version, String versionDB) {
        // Check if there is a new version
        MessageBuilder alert = ChatUtils.message("Could not retrieve latest version. Maybe you're offline or GitHub is unavailable?")
            .style(MessageStyle.PRETTY)
            .target(MessageTarget.WORLD)
            .type(MessageType.WARN);
        try (InputStream input = getClass().getResourceAsStream("/plugin.properties")) {
            Properties props = new Properties();
            props.load(input);
            URL url = new URI(props.getProperty("versionEndpoint")).toURL();
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
        if (versionDB.equals(version)) {
            return;
        }

        try (Connection c = getConnection();
            Statement statement = c.createStatement()) {

            StringBuilder query = new StringBuilder();

            switch (version) {
                case "0.10.4":
                    query.append(MigrationUtils.getMigration("0.10.4"));
                case "0.10.3", "0.10.2", "0.10.1":
                    query.append(MigrationUtils.getMigration("0.10.1"));
                case "0.10":
                    query.append(MigrationUtils.getMigration("0.10"));
                case "0.9.2", "0.9.1", "0.9", "0.8.1", "0.8":
                    query.append(MigrationUtils.getMigration("0.8"));
                case "0.7":
                    query.append(MigrationUtils.getMigration("0.7"));
                case "0.6", "0.5.2", "0.5.1":
                    query.append(MigrationUtils.getMigration("0.5.1"));
                case "0.5", "0.4":
                    query.append(MigrationUtils.getMigration("0.4"));
                default:
                    statement.executeUpdate(query.toString()); // no break means all fall through to execution
            }
        } catch (SQLException e) {
            ChatUtils.message("No database found, creating one. If you know you already have a database, try restarting.")
                .target(MessageTarget.CONSOLE)
                .type(MessageType.NOTIF)
                .send();
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
            this.isFresh = true;
            return "0.0";
        }

        try (Connection c = getConnection();
            PreparedStatement statement = c.prepareStatement("SELECT version FROM plugin WHERE plugin = 'PlayerQuests';")) {

            ResultSet results = statement.executeQuery();
            return results.getString("version");
        } catch (SQLException e) {
            ChatUtils.message("Could not find the quest version in the db " + e.getMessage())
                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .send();
            this.isFresh = true;
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
        try (Connection c = getConnection();
            PreparedStatement preparedStatement = c.prepareStatement("""
                INSERT INTO plugin (plugin, version)
                VALUES ('PlayerQuests', ?)
                ON CONFLICT(plugin)
                DO UPDATE SET version = excluded.version;
            """)) {

            preparedStatement.setString(1, version);

            preparedStatement.execute();

        } catch (SQLException e) {
            ChatUtils.message("Could not insert or set the quest version in the db " + e.getMessage())                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .send();
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
        try (Connection c = getConnection();
        PreparedStatement preparedStatement = c.prepareStatement("INSERT OR IGNORE INTO players (uuid) VALUES (?) RETURNING *;")) {

            preparedStatement.setString(1, uuid.toString());
            preparedStatement.executeQuery();

        } catch (SQLException e) {
            ChatUtils.message("Could not add the user " + Bukkit.getServer().getPlayer(uuid).getName() + ". " + e.getMessage())
                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .send();
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
        try (Connection c = getConnection();
        PreparedStatement preparedStatement = c.prepareStatement("SELECT id FROM diaries WHERE player = ?")) {

            preparedStatement.setInt(1, dbPlayerID);

            return preparedStatement.executeQuery(); // provides ResultSet
        } catch (SQLException e) {
            ChatUtils.message("Could not find a diary for db player ID: " + dbPlayerID + ": " + e.getMessage())
                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .send();
            return null;
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
        try (Connection c = getConnection();
        Statement statement = c.createStatement()) {

            String allQuestsSQL = "SELECT id FROM quests;";
            ResultSet result = statement.executeQuery(allQuestsSQL);

            while (result.next()) {
                ids.add(result.getString("id"));
            }

        } catch (SQLException e) {
            ChatUtils.message("Could not retrieve quests from database. " + e.getMessage())                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .send();
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
        try (Connection c = getConnection();
        PreparedStatement preparedStatement = c.prepareStatement("INSERT INTO quests (id) VALUES (?);")) {

            preparedStatement.setString(1, id);
            preparedStatement.execute();

        } catch (SQLException e) {
            ChatUtils.message("Could not add the quest " + id + ". " + e.getMessage())                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .send();
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

        try (Connection c = getConnection();
        PreparedStatement preparedStatement = c.prepareStatement("SELECT id FROM quests WHERE id = ?;")) {

            preparedStatement.setString(1, id);

            ResultSet results = preparedStatement.executeQuery();
            String quest = results.getString("id");

            return quest;
        } catch (SQLException e) {
            ChatUtils.message("Could not get the quest " + id + ". " + e.getMessage())                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .send();
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
        try (Connection c = getConnection();
        PreparedStatement preparedStatement = c.prepareStatement("SELECT toggled FROM quests WHERE id = ?;")) {

            preparedStatement.setString(1, quest.getID());
            ResultSet results = preparedStatement.executeQuery();
            Boolean result = false;

            if (results.next()) {
                result = results.getBoolean("toggled");
            }

            return result; // no result found
        } catch (SQLException e) {
            ChatUtils.message("Could not get the quest toggle status " + quest.toString() + ". " + e.getMessage())
                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .send();
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
        try (Connection c = getConnection();
        PreparedStatement preparedStatement = c.prepareStatement("UPDATE quests SET toggled = ? WHERE id = ?;")) {

            preparedStatement.setBoolean(1, state);
            preparedStatement.setString(2, quest.getID());
            preparedStatement.execute();

        } catch (SQLException e) {
            ChatUtils.message("Could not toggle the quest " + quest.toString() + ". " + e.getMessage())
                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .send();
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

        try (Connection c = getConnection()) {
            PreparedStatement preparedStatement;

            String removeQuestSQL = "DELETE FROM quests WHERE id = ?;";
            preparedStatement = c.prepareStatement(removeQuestSQL);
            preparedStatement.setString(1, id);
            preparedStatement.execute();

            String removeDiaryQuestSQL = "DELETE FROM diary_entries WHERE quest = ?;";
            preparedStatement = c.prepareStatement(removeDiaryQuestSQL);
            preparedStatement.setString(1, id);
            preparedStatement.execute();

        } catch (SQLException e) {
            ChatUtils.message("Could not remove the quest " + id + ". " + e.getMessage())                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .send();
        }
    }

    /**
     * Updates the inventory for a specific quest in the database.
     *
     * This method inserts or updates the inventory associated with the given quest. If a record with the same
     * quest ID already exists in the database, the inventory will be updated with the new values provided.
     * The inventory is represented as a map of ItemSerialisable to quantities, which is converted to a string for storage.
     *
     * @param quest The {@link Quest} object representing the quest whose inventory is to be set.
     * @param inventory A {@link Map} where keys are {@link ItemSerialisable} representing the items in the inventory,
     *                  and values are {@link Integer} representing the quantities of those items.
     */
    public void setQuestInventory(Quest quest, Map<ItemSerialisable, Integer> inventory) {
        try (Connection c = getConnection();
        PreparedStatement preparedStatement = c.prepareStatement("INSERT INTO quests (id, inventory) VALUES (?, ?) ON CONFLICT(id) DO UPDATE SET inventory = EXCLUDED.inventory")) {

            // preparedStatement
            preparedStatement.setString(1, quest.getID());
            preparedStatement.setString(2, inventory.toString());
            preparedStatement.execute();

        } catch (SQLException e) {
            ChatUtils.message("Could not toggle the quest " + quest.toString() + ". " + e.getMessage())
                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .send();
        }
    }

    /**
     * Retrieves all quest inventories from the database.
     *
     * @return A map of quest inventory maps retrieved from the database, or an empty map if an error occurs.
     */
    public synchronized Map<String, Map<ItemSerialisable, Integer>> getAllQuestInventories() {
        Map<String, Map<ItemSerialisable, Integer>> inventories = new HashMap<>();

        try (Connection c = getConnection();
        Statement statement = c.createStatement()) {

            String allQuestsSQL = "SELECT id, inventory FROM quests;";
            ResultSet result = statement.executeQuery(allQuestsSQL);

            while (result.next()) {
                String[] pairs = result.getString("inventory").split("\\|"); // escape for raw "|"
                Map<ItemSerialisable, Integer> inventoryMap = new HashMap<>();

                pairs = result.getString("inventory").replaceAll("[{}]", "").split(",");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length == 2) {
                        String itemString = keyValue[0].trim().replace("{", "").replace("}", "");
                        ItemSerialisable itemSerialisable = new ItemSerialisable(itemString);

                        // skip if invalid itemSerialisable
                        if (itemSerialisable.getItemData().equals(ItemData.AIR)) {
                            continue;
                        }

                        // resolve item quantity
                        Integer quantity;
                        try {
                            quantity = Integer.parseInt(keyValue[1].trim());
                        } catch (NumberFormatException e) {
                            // Handle the case where the quantity is not a valid integer
                            ChatUtils.message("Invalid quantity format: " + keyValue[1])
                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .send();
                            continue;
                        }

                        // prepare to be shipped
                        inventoryMap.put(itemSerialisable, quantity);
                    }
                }

                // put quest id and retrieved inventory for returning
                inventories.put(result.getString("id"), inventoryMap);
            }

        } catch (SQLException e) {
            ChatUtils.message("Could not retrieve quests from database. " + e.getMessage())                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .send();
        }

        return inventories;
    }

    /**
     * Add a quest diary to the diaries table.
     * @param diary the QuestDiary to add
     */
    public synchronized void setQuestDiary(QuestDiary diary) {
        Player player = diary.getQuestClient().getPlayer();
        String playerUUIDString = player.getUniqueId().toString();
        String diaryID = diary.getID();

        try (Connection c = getConnection();
        PreparedStatement preparedStatement = c.prepareStatement("INSERT OR IGNORE INTO diaries (id, player) VALUES (?, ?)")) {

            // preparedStatement
            preparedStatement.setString(1, diaryID);
            preparedStatement.setString(2, playerUUIDString);
            preparedStatement.execute();

        } catch (SQLException e) {
            ChatUtils.message("Could not add a quest diary to the database " + diaryID + ". " + e.getMessage())                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .send();
        }
    }

    /**
     * Get all diary entries associated with a diary,
     * and their completion state.
     * @param diary the QuestDiary to get entries of
     * @return quests with each action and whether they are finished or ongoing.
     */
    public synchronized Map<Quest, List<Map<StagePath, Boolean>>> getDiaryEntries(QuestDiary diary) {
        Map<Quest, List<Map<StagePath, Boolean>>> diaryEntries = new HashMap<>();
        String diaryID = diary.getID();

        try (Connection c = getConnection();
        PreparedStatement preparedStatement = c.prepareStatement("SELECT diary, quest, action, completion FROM diary_entries WHERE diary = ?;")) {

            preparedStatement.setString(1, diaryID);
            ResultSet result = preparedStatement.executeQuery();

            while (result.next()) {
                // get quest
                Quest quest = Core.getQuestRegistry().getQuest(result.getString("quest"));

                // create StagePath
                StagePath path = new StagePath(result.getString("action"));

                // create inner map
                Map<StagePath, Boolean> completionMap = Map.of(path, result.getBoolean("completion"));

                // get parent
                List<Map<StagePath, Boolean>> entriesList = diaryEntries.getOrDefault(quest, new ArrayList<>());

                // add our result to the entries list
                entriesList.add(completionMap);

                // replace in parent diaryEntries
                diaryEntries.put(quest, entriesList);
            }

        } catch (SQLException e) {
            ChatUtils.message("Could not retrieve diary entries from database for " + diaryID + ". " + e.getMessage())                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .send();
        }

        return diaryEntries;
    }

    public synchronized void setDiaryEntryCompletion(String diaryID, String questID, StagePath actionPath, boolean completionState) {
        try (Connection c = getConnection();
        PreparedStatement preparedStatement = c.prepareStatement("REPLACE INTO diary_entries (diary, quest, action, completion) VALUES (?, ?, ?, ?)")) {

            // preparedStatement
            preparedStatement.setString(1, diaryID);
            preparedStatement.setString(2, questID);
            preparedStatement.setString(3, actionPath.toString());
            preparedStatement.setBoolean(4, completionState);
            preparedStatement.execute();

        } catch (SQLException e) {
            ChatUtils.message("Could not add a quest diary to the database " + diaryID + ". " + e.getMessage())                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .send();
        }
    }

    private void isFresh() {
        if ( ! this.isFresh) {
            return;
        }

        PlayerQuests.getPlayerListener().isFresh();
    }
}

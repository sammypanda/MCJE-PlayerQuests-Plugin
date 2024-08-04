package playerquests.utility.singleton;

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
import java.util.HashMap;
import java.util.List; // generic list type
import java.util.Map;
import java.util.UUID; // how users are identified

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.bukkit.Bukkit; // the Bukkit API
import org.bukkit.entity.Player;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import playerquests.Core;
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.data.ConnectionsData;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.quest.QuestDiary;
import playerquests.product.Quest;
import playerquests.utility.ChatUtils;
import playerquests.utility.ChatUtils.MessageStyle;
import playerquests.utility.ChatUtils.MessageTarget;
import playerquests.utility.ChatUtils.MessageType;

/**
* API representing and providing access to the game database.
* This when instantiated, creates and/or opens the game database.
*/
// TODO: unrealtime-ify the database use-case/move all database things to Database, and synchronise to one thread (use transaction probs, they seem to handle concurrency)
// TODO: ditch using id for players table, use uuid as primary key
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
    
    public static Database getInstance() {
        return instance;
    }
    
    // Synchronous method to get a connection
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
    
    public Database init() {
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
        
        try {
            Connection conn = getConnection();
            if (conn != null) {
                Statement statement = conn.createStatement();
                
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
                
                statement.close();
                conn.close();
                
                migrate(version, dbVersion);
            }
        } catch (SQLException e) {
            System.err.println("Could not initialise the database: " + e.getMessage());
        }

        return this;
    }
    
    private void migrate(String version, String version_db) {
        if (version_db.equals(version)) {
            return;
        }
        
        if (version_db.equals("0.0")) {
            try {
                String setPluginSQL = "INSERT INTO plugin (plugin, version) VALUES (?, ?);";
                PreparedStatement preparedStatement = getConnection().prepareStatement(setPluginSQL);
                preparedStatement.setString(1, "PlayerQuests");
                preparedStatement.setString(2, version);
                preparedStatement.execute();
                ChatUtils.message("Migrated/patched database: added plugin database table")
                    .target(MessageTarget.CONSOLE)
                    .style(MessageStyle.PLAIN)
                    .type(MessageType.NOTIF)
                    .send();
                getConnection().close();
            } catch (SQLException e) {
                System.err.println("Could not insert plugin data to db " + e.getMessage());
            }
        }
        
        try {
            Statement statement = getConnection().createStatement();
            
            switch (version) {
                case "0.4":
                String addToggledSQL = "ALTER TABLE quests ADD COLUMN toggled TEXT DEFAULT true;";
                statement.execute(addToggledSQL);
                break;
            }
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
    
    public String getPluginVersion() {
        if (!Files.exists(Paths.get("plugins/PlayerQuests/playerquests.db"))) {
            return "0.0";
        }
        
        try {
            Statement statement = getConnection().createStatement();
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
    
    private void setPluginVersion(String version) {
        try {
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
     * Adds a new player to the database with the specified UUID.
     * 
     * This method inserts a new record into the `players` table of the database using the provided
     * UUID. The UUID is converted to a string and stored in the `uuid` column. The SQL `INSERT` statement
     * is used along with the `RETURNING *` clause to execute the query. 
     * If an {@link SQLException} occurs, an error message is logged to
     * the standard error stream, including the name of the player associated with the given UUID.</p>
     * 
     * @param uuid The unique identifier for the player, represented as a {@link UUID}. This ID is
     *             used to insert a new record into the `players` table in the database.
     */
    public void addPlayer(UUID uuid) {
        try (Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO players (uuid) VALUES (?) RETURNING *;")) {
            
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.executeQuery();
            
            getConnection().close();
        
        } catch (SQLException e) {
            System.err.println("Could not add the user " + Bukkit.getServer().getPlayer(uuid).getName() + ". " + e.getMessage());
        }
    }
    
    public ResultSet getDiary(Integer dbPlayerID) {
        try (Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT id FROM diaries WHERE player = ?")) {
            
            preparedStatement.setInt(1, dbPlayerID);
            
            ResultSet results = preparedStatement.executeQuery();
            
            getConnection().close();
            
            return results;
        } catch (SQLException e) {
            System.err.println("Could not find a diary for db player ID: " + dbPlayerID + ": " + e.getMessage());
            return null;
        }
    }
    
    public ResultSet getDiaryQuest(String questID, Integer dbDiaryID) {
        try (Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM diary_quests WHERE quest = ? AND diary = ?")) {
            
            preparedStatement.setString(1, questID);
            preparedStatement.setInt(2, dbDiaryID);
            
            ResultSet result = preparedStatement.executeQuery();
            
            getConnection().close();
            
            return result;
        } catch (SQLException e) {
            System.err.println("Could not find quest in the diary: " + questID + ": " + e.getMessage());
            return null;
        }
    }
    
    public void setDiaryQuest(QuestDiary diary, Quest quest) {
        String questID = quest.getID(); // get the quest ID
        Map<String, QuestAction> actions = quest.getActions(); // the quest actions
        Map<String, QuestStage> stages = quest.getStages(); // the quest stages
        ConnectionsData connections = quest.getConnections(); // get where the player currently is in their quest
        Player player = diary.getPlayer(); // get the player this diary represents

        try (Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT OR REPLACE INTO diary_quests (id, stage, action, quest, diary) VALUES (?, ?, ?, ?, ?)")) {
            
            preparedStatement.setString(1, player.getUniqueId().toString() + "_" + questID);
            
            // default to initial values
            preparedStatement.setString(2, quest.getEntry());
            preparedStatement.setString(3, stages.get(quest.getEntry()).getEntryPoint().getID());
            
            // get the current quest stage or action
            String currentConnection = connections.getCurr();
            
            if (currentConnection != null) {
                // stage-based current
                if (currentConnection.contains("stage")) {
                    preparedStatement.setString(2, currentConnection);
                    preparedStatement.setString(3, stages.get(currentConnection).getEntryPoint().getID()); // get the ID of the entry action
                }
                
                // action-based current
                if (currentConnection.contains("action") && actions.containsKey(currentConnection)) {
                    String stageID = actions.get(currentConnection).getStage().getID();
                    
                    preparedStatement.setString(2, stageID);
                    preparedStatement.setString(3, currentConnection);
                }
            }
            
            // set remaining values
            preparedStatement.setString(4, questID);
            preparedStatement.setString(5, diary.getDiaryID());
            
            preparedStatement.execute();
            
            getConnection().close();
            
        } catch (SQLException e) {
            System.err.println("Could not set or update quest progress for the " + questID + " quest: " + e.getMessage());
            return;
        }
    }
    
    public Database addPlayers(List<UUID> uuids) {
        for (UUID uuid : uuids) {
            addPlayer(uuid);
        }
        
        return this;
    }
    
    public List<String> getAllQuests() {
        List<String> ids = new ArrayList<>();
        try (Connection connection = getConnection();
        Statement statement = connection.createStatement()) {
            
            String allQuestsSQL = "SELECT id FROM quests;";
            ResultSet result = statement.executeQuery(allQuestsSQL);
            
            while (result.next()) {
                ids.add(result.getString("id"));
            }
            
            getConnection().close();
        } catch (SQLException e) {
            System.err.println("Could not retrieve quests from database. " + e.getMessage());
        }
        return ids;
    }
    
    public void addQuest(String id) {
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
            
            connection.close();
        } catch (SQLException e) {
            System.err.println("Could not add the quest " + id + ". " + e.getMessage());
        }
    }
    
    public String getQuest(String id) {
        if (id == null) {
            return null;
        }
        
        try (Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT id FROM quests WHERE id = ?;")) {
            
            preparedStatement.setString(1, id);
            
            ResultSet results = preparedStatement.executeQuery();
            String quest = results.getString("id");
            
            connection.close();
            
            return quest;
        } catch (SQLException e) {
            System.err.println("Could not get the quest " + id + ". " + e.getMessage());
            return null;
        }
    }
    
    public Boolean getQuestToggled(Quest quest) {
        try (Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT toggled FROM quests WHERE id = ?;")) {
            
            preparedStatement.setString(1, quest.getID());
            ResultSet results = preparedStatement.executeQuery();
            Boolean result = false;
            
            if (results.next()) {
                result = results.getBoolean("toggled");
            }
            
            connection.close();
            
            return result; // no result found
        } catch (SQLException e) {
            System.err.println("Could not get the quest toggle status " + quest.toString() + ". " + e.getMessage());
            return null;
        }
    }
    
    // TOOD: fix quest toggling
    public void setQuestToggled(Quest quest, Boolean state) {
        try (Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE quests SET toggled = ? WHERE id = ?;")) {
            
            preparedStatement.setBoolean(1, state);
            preparedStatement.setString(2, quest.getID());
            preparedStatement.execute();
            connection.close();
            
            if (state) {
                QuestRegistry.getInstance().submit(quest);
            } else {
                QuestRegistry.getInstance().remove(quest, true);
            }
        } catch (SQLException e) {
            System.err.println("Could not toggle the quest " + quest.toString() + ". " + e.getMessage());
        }
    }
    
    public void removeQuest(String id) {
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
            
            connection.close();
        } catch (SQLException e) {
            System.err.println("Could not remove the quest " + id + ". " + e.getMessage());
        }
    }

    /**
     * Inserts or adds a player {@link QuestDiary} to the database.
     * 
     * This method takes a {@link QuestDiary} object, extracts the player ID from it, and uses
     * this ID to insert or replace an existing record in the `diaries` table of the database. The
     * database operation is performed within a try-with-resources block to ensure proper resource
     * management. If an {@link SQLException} occurs, an error message is logged to the standard error stream.
     * 
     * Note: The method assumes that the `diaries` table has a column named `player` where the
     * player ID is stored. It uses an SQL `INSERT OR REPLACE` statement to handle both insertion of
     * new records and updating of existing records.
     * 
     * @param questDiary The {@link QuestDiary} instance containing the diary information to be
     *                   inserted or updated in the database. The player ID is extracted from this
     *                   object and used to identify the corresponding record in the `diaries` table.
     */
    public void addDiary(QuestDiary questDiary) {
        try (Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT OR REPLACE INTO diaries (id, player) VALUES (?, ?)")) {
            
            preparedStatement.setString(1, questDiary.getDiaryID());
            preparedStatement.setString(2, questDiary.getPlayerID());
            
            preparedStatement.execute();
            
        } catch (SQLException e) {
            System.err.println("Could not create a diary for: " + questDiary.getPlayerID() + ": " + e.getMessage());
        }
    }

	public Map<Quest,ConnectionsData> getQuestProgress(QuestDiary questDiary) {
        Map<Quest,ConnectionsData> progress = new HashMap<Quest,ConnectionsData>();
        
		try (Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM diary_quests WHERE id = ?")) {
            
            preparedStatement.setString(1, questDiary.getDiaryID());
            
            ResultSet results = preparedStatement.executeQuery();

            while (results.next()) {
                progress.put(
                    Core.getQuestRegistry().getQuest( // get the quest
                        results.getString("quest") // ..from the ID
                    ), 
                    new ConnectionsData(null, results.getString("action"), null) // be precise and get the action, instead of the stage as the 'current' point
                );
            }

            getConnection().close();

            return progress;
            
        } catch (SQLException e) {
            System.err.println("Could not find quest progress in db for " + questDiary.getPlayer() + ": " + e.getMessage());
            return null;
        }
	}
}

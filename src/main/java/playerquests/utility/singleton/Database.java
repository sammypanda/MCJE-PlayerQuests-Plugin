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
import java.util.List; // generic list type
import java.util.Map;
import java.util.UUID; // how users are identified

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.bukkit.Bukkit; // the Bukkit API
import org.bukkit.entity.Player;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.data.ConnectionsData;
import playerquests.product.Quest;
import playerquests.utility.ChatUtils;
import playerquests.utility.ChatUtils.MessageStyle;
import playerquests.utility.ChatUtils.MessageTarget;
import playerquests.utility.ChatUtils.MessageType;

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
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "uuid TEXT NOT NULL);";
                statement.execute(playersTableSQL);
                
                String questsTableSQL = "CREATE TABLE IF NOT EXISTS quests ("
                + "id TEXT PRIMARY KEY,"
                + "toggled BOOLEAN NOT NULL DEFAULT TRUE);";
                statement.execute(questsTableSQL);
                
                String diariesTableSQL = "CREATE TABLE IF NOT EXISTS diaries ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "player INTEGER UNIQUE,"
                + "FOREIGN KEY (player) REFERENCES players(id));";
                statement.execute(diariesTableSQL);
                
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
    
    // TODO: continue putting try-with-resources
    public Integer addPlayer(UUID uuid) {
        Integer id = getPlayer(uuid);
        
        if (id != null) {
            return id;
        }
        
        try {
            String addPlayerSQL = "INSERT INTO players (uuid) VALUES (?) RETURNING *;";
            PreparedStatement preparedStatement = getConnection().prepareStatement(addPlayerSQL);
            preparedStatement.setString(1, uuid.toString());
            ResultSet results = preparedStatement.executeQuery();
            Integer idResult = results.getInt("id");
            getConnection().close();
            return idResult;
        } catch (SQLException e) {
            System.err.println("Could not add the user " + Bukkit.getServer().getPlayer(uuid).getName() + ". " + e.getMessage());
            return null;
        }
    }
    
    public Integer getPlayer(UUID uuid) {
        try (Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM players WHERE uuid = ?;")) {
            
            preparedStatement.setString(1, uuid.toString());
            
            ResultSet results = preparedStatement.executeQuery();
            Integer id = results.getInt("id");
            Boolean empty = !results.next();
            
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
    
    public UUID getPlayerUUID(Integer id) {
        try (Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM players WHERE id = ?;")) {
            
            preparedStatement.setInt(1, id);
            
            ResultSet results = preparedStatement.executeQuery();
            UUID uuidResult = UUID.fromString(results.getString("uuid"));
            Boolean empty = !results.next();
            
            getConnection().close();
            
            if (empty) {
                return null;
            }
            
            return uuidResult;
            
        } catch (SQLException e) {
            System.err.println("Could not find the user associated with db ID: " + id.toString() + ". " + e.getMessage());
            return null;
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
    
    public void setDiaryQuest(String questID, Player player, Integer dbDiaryID, ConnectionsData connections) {
        try (Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT OR REPLACE INTO diary_quests (id, stage, action, quest, diary) VALUES (?, ?, ?, ?, ?)")) {
            
            Quest quest = QuestRegistry.getInstance().getAllQuests().get(questID);
            Map<String, QuestAction> actions = quest.getActions();
            
            preparedStatement.setString(1, player.getUniqueId().toString() + "_" + questID);
            
            // default to initial values
            preparedStatement.setString(2, quest.getEntry());
            preparedStatement.setString(3, quest.getStages().get(quest.getEntry()).getEntryPoint().getID());
            
            // get the current quest stage or action
            String currentConnection = connections.getCurr();
            
            if (currentConnection != null) {
                // stage-based current
                if (currentConnection.contains("stage")) {
                    preparedStatement.setString(2, currentConnection);
                    preparedStatement.setString(3, quest.getStages().get(currentConnection).getEntryPoint().getID()); // get the ID of the entry action
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
            preparedStatement.setInt(5, dbDiaryID);
            
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
    
    public void initDiary(Integer id) {
        try (Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT OR REPLACE INTO diaries (player) VALUES (?)")) {
            
            preparedStatement.setInt(1, id);
            
            preparedStatement.execute();
            
            getConnection().close();
            
        } catch (SQLException e) {
            System.err.println("Could not create a diary for the " + id + " database player ID: " + e.getMessage());
        }
    }
}

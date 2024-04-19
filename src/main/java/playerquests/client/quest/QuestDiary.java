package playerquests.client.quest;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.data.ConnectionsData;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.product.Quest;
import playerquests.utility.singleton.Database;
import playerquests.utility.singleton.QuestRegistry;

public class QuestDiary {

    private Integer dbPlayerID = null;

    private Integer dbDiaryID = null;

    public QuestDiary(Integer dbPlayerID) {
        this.dbPlayerID = dbPlayerID;

        this.init();
    }

    public void init() {
        Integer id = null;

        id = this.getDiaryID(dbPlayerID);

        if (id == null) {
            try {
                String createQuestDiarySQL = "INSERT OR REPLACE INTO diaries (player) VALUES (?)";

                PreparedStatement preparedStatement = Database.getConnection().prepareStatement(createQuestDiarySQL);

                preparedStatement.setInt(1, dbPlayerID);

                preparedStatement.execute();

                Database.getConnection().close();
                
                id = this.getDiaryID(dbPlayerID);

            } catch (SQLException e) {
                System.err.println("Could not create a diary for the " + dbPlayerID + " database player ID: " + e.getMessage());
            }
        }

        this.dbDiaryID = id;
    }

    public Player getPlayer(Integer dbPlayerID) {
        try {
            String getPlayerSQL = "SELECT uuid FROM players WHERE id = ?";

            PreparedStatement preparedStatement = Database.getConnection().prepareStatement(getPlayerSQL);

            preparedStatement.setInt(1, dbPlayerID);

            ResultSet results = preparedStatement.executeQuery();
            UUID playerUUID = UUID.fromString(results.getString("uuid"));
            Player player = Bukkit.getPlayer(playerUUID);

            Database.getConnection().close();

            return player;
        } catch (SQLException e) {
            System.err.println("Could not get player from the " + dbPlayerID + " database ID: " + e.getMessage());
            return null;
        }
    }

    public ConnectionsData getQuestProgress(String questID) {
        ConnectionsData connections = new ConnectionsData();

        try {
            String getQuestProgressSQL = "SELECT * FROM diary_quests WHERE quest = ?";

            PreparedStatement preparedStatement = Database.getConnection().prepareStatement(getQuestProgressSQL);

            preparedStatement.setString(1, questID);

            ResultSet results = preparedStatement.executeQuery();
            String stageResult = results.getString("stage");
            String actionResult = results.getString("action");
            String questResult = results.getString("quest");

            Database.getConnection().close();

            // if no quest progress made for this quest ID
            if (questResult == null) {
                return null;
            }

            // retrieve quest details
            Quest quest = QuestRegistry.getInstance().getAllQuests().get(questID);
            QuestStage stage = quest.getStages().get(stageResult);

            // set default connections
            connections = stage.getConnections();

            // in case connections aren't set properly in the stage
            if (connections.getCurr() == null) {
                connections.setCurr(stage.getID());
            }

            // drill down specific current if available
            if (actionResult != null) {
                QuestAction action = stage.getActions().get(actionResult);
                ConnectionsData actionConnections = action.getConnections();

                if (actionConnections.getCurr() == null) {
                    connections.setCurr(action.getID());
                } else {
                    connections = actionConnections;
                }

            }

            return connections;
        } catch (SQLException e) {
            System.err.println("Could not get progress of quest " + questID + ": " + e.getMessage());
            return null;
        }
    }

    public void setQuestProgress(String questID, ConnectionsData connections) {
        Player player = this.getPlayer(this.dbPlayerID);
        Quest quest = QuestRegistry.getInstance().getAllQuests().get(questID);
        Map<String, QuestAction> actions = quest.getActions();
        System.out.println("quest progress quest (" + questID + "): " + quest);

        if (player == null) {
            System.err.println("No player found for this QuestDiary, cannot try to set Quest progress.");
            return;
        }

        try {

            String setQuestProgressSQL = "INSERT OR REPLACE INTO diary_quests (id, stage, action, quest, diary) VALUES (?, ?, ?, ?, ?)";

            PreparedStatement preparedStatement = Database.getConnection().prepareStatement(setQuestProgressSQL);

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
                    preparedStatement.setString(3, null);
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
            preparedStatement.setInt(5, this.dbDiaryID);

            preparedStatement.execute();
            
            Database.getConnection().close();

        } catch (SQLException e) {
            System.err.println("Could not set or update quest progress for the " + questID + " quest: " + e.getMessage());
            return;
        }
    }

    public Integer getDiaryID(Integer dbPlayerID) {
        if (dbDiaryID != null) {
            return dbDiaryID;
        }

        try {
            String getDiarySQL = "SELECT id FROM diaries WHERE player = ?";

            PreparedStatement preparedStatement = Database.getConnection().prepareStatement(getDiarySQL);

            preparedStatement.setInt(1, dbPlayerID);
            
            ResultSet results = preparedStatement.executeQuery();
            Integer id = results.getInt("id");

            Database.getConnection().close();

            // if not found (it will return 0)
            if (id.equals(0)) {
                return null;
            }

            return id;
        } catch (SQLException e) {
            System.err.println("Could not get diary ID, from player with the database ID: " + dbPlayerID + ": " + e.getMessage());
            return null;
        }
    }

    public void addQuest(String questID) {
        if (this.getQuestProgress(questID) != null) {
            return;
        }

        this.setQuestProgress(questID, new ConnectionsData());
    }
    
}

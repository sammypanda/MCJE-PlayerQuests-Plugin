package playerquests.client.quest;

import java.sql.ResultSet;
import java.sql.SQLException;
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
        final Integer id = this.getDiaryID(dbPlayerID);

        if (id != null) {
            Database.getInstance().initDiary(id);
            this.dbDiaryID = id;
        }
    }

    public Player getPlayer(Integer dbPlayerID) {
        UUID uuid = Database.getInstance().getPlayerUUID(dbPlayerID); // the uuid from the database
        
        // Retrieve the Player synchronously from Bukkit
        return Bukkit.getPlayer(uuid);
    }

    public ConnectionsData getQuestProgress(String questID) {
        ConnectionsData connections = new ConnectionsData();

        try {
            ResultSet results = Database.getInstance().getDiaryQuest(questID, this.dbDiaryID);

            String stageResult = results.getString("stage");
            String actionResult = results.getString("action");
            String questResult = results.getString("quest");

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
        } catch (SQLException e) {
            System.err.println("Could not get progress of quest " + questID + ": " + e.getMessage());
            return null;
        }

        return connections;
    }

    /**
     * Gets the current stage the player is up to
     * in the quest.
     * @param questID quest ID to find the action on
     * @return quest stage object
     */
    public QuestStage getStage(String questID) {
        QuestStage stage;

        try {
            ResultSet results = Database.getInstance().getDiaryQuest(questID, this.dbDiaryID);
            
            String stageResult = results.getString("stage");
            String questResult = results.getString("quest");

            // if no quest progress made for this quest ID
            if (questResult == null) {
                return null;
            }

            // retrieve quest details
            Quest quest = QuestRegistry.getInstance().getAllQuests().get(questID);
            stage = quest.getStages().get(stageResult);

        } catch (SQLException e) {
            System.err.println("Could not get progress of quest " + questID + ": " + e.getMessage());
            return null;
        }

        return stage;
    }

    /**
     * Gets the current action the player is up to
     * in the quest.
     * @param questID quest ID to find the action on
     * @return quest action object
     */
    public QuestAction getAction(String questID) {
        QuestAction action;

        try {
            ResultSet results = Database.getInstance().getDiaryQuest(questID, this.dbDiaryID);

            String actionResult = results.getString("action");
            String questResult = results.getString("quest");

            // if no quest progress made for this quest ID
            if (questResult == null) {
                return null;
            }

            // retrieve quest details
            action = this.getStage(questID).getActions().get(actionResult);

        } catch (SQLException e) {
            System.err.println("Could not get progress of quest " + questID + ": " + e.getMessage());
            return null;
        }

        return action;
    }

    public void setQuestProgress(String questID, ConnectionsData connections) {
        Player player = this.getPlayer(this.dbPlayerID);

        if (player == null) {
            System.err.println("No player found for this QuestDiary, cannot try to set Quest progress.");
            return;
        }

        Database.getInstance().setDiaryQuest(questID, player, this.dbDiaryID, connections);
    }

    // TODO: move all database things to Database, and synchronise to one thread
    public Integer getDiaryID(Integer dbPlayerID) {
        if (dbDiaryID != null) {
            return dbDiaryID;
        }

        try {
            ResultSet results = Database.getInstance().getDiary(dbPlayerID);
            Integer id = results.getInt("id");

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

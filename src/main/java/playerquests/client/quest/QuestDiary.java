package playerquests.client.quest;

import java.util.HashMap; // hash map type
import java.util.Map; // generic map type
import java.util.UUID; // unique identifiers for (usually) players
import java.util.concurrent.CompletableFuture; // async + callbacks

import org.bukkit.Bukkit; // the in-game API
import org.bukkit.entity.Player; // the in-game player object

import playerquests.builder.quest.action.QuestAction; // quest product: actions
import playerquests.builder.quest.data.ConnectionsData; // quest product: what action/stage connects to what
import playerquests.builder.quest.data.StagePath;
import playerquests.builder.quest.stage.QuestStage; // quest product: stages
import playerquests.product.Quest; // quest product
import playerquests.utility.ChatUtils; // sending messages systematically
import playerquests.utility.ChatUtils.MessageStyle; // how the message looks
import playerquests.utility.ChatUtils.MessageTarget; // who the message sends to
import playerquests.utility.ChatUtils.MessageType; // what the message is
import playerquests.utility.singleton.Database; // everything preservation store
import playerquests.utility.singleton.QuestRegistry; // quest store

/**
 * Represents a diary that tracks a player's quest progress.
 * 
 * The {@link QuestDiary} class manages and stores the progress of quests for a specific player.
 * It provides methods to retrieve and update quest progress, as well as interact with the player's
 * quests and stages.
 */
public class QuestDiary {

    /**
     * The identifier for the player this diary represents.
     */
    private String playerID; // just the Bukkit player uuid

    /**
     * The identifier for this diary.
     */
    private String diaryID; // diary_[Bukkit player uuid]

    /**
     * The state (progress) of a quest.
     */
    private Map<Quest, ConnectionsData> questProgress = new HashMap<Quest, ConnectionsData>();

    /**
     * Constructs a new {@link QuestDiary} instance for the specified player.
     * <p>
     * Initializes a {@link QuestDiary} with the given player ID and sets up the diary for storage
     * in the database. The diary is preserved in the database using {@link Database#getInstance()}.{@code addDiary(QuestDiary)}.
     * </p>
     * 
     * @param client The {@link QuestClient} representing the player for whom this diary is created.
     */
    public QuestDiary(QuestClient client) {
        // initialise values
        this.playerID = client.getPlayer().getUniqueId().toString(); // Initialize playerID
        this.diaryID = "diary_" + this.playerID; // Initialize diaryID

        // set-up for preserving the diary in the db
        Database.getInstance().addDiary(this);

        // instantiate quest progress from the db
        // and update the client when we have results!
        loadQuestProgress().thenRun(() -> {
            // fill in un-completed/un-started quests
            QuestRegistry.getInstance().getAllQuests().values().stream().forEach((quest) -> {
                // put the quest from the start 
                // (we know it's unstarted because we are using the quest's default ConnectionsData, 
                // ConnectionsData is the thing that tracks quest progress. It does it by identifying the
                // previous, current and next action/stage).
                this.questProgress.putIfAbsent(quest, quest.getConnections());
            });

            client.update();
        });
    }

    /**
     * Loads the quest progress from the database asynchronously.
     * <p>
     * This method should only be called during the first instantiation of the diary.
     * </p>
     * 
     * @return A {@link CompletableFuture} that completes once the quest progress has been loaded.
     */
    private CompletableFuture<Void> loadQuestProgress() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Retrieve the quest progress,
                // and store locally
                this.questProgress = Database.getInstance().getQuestProgress(this);
            } catch (Exception e) {
                // Report something critical went wrong
                ChatUtils.message("Failed to load quest progress for: " + this.getPlayer() + ", " + e)
                    .target(MessageTarget.CONSOLE)
                    .type(MessageType.ERROR)
                    .style(MessageStyle.PLAIN)
                    .send();
            }

            // Returning no data, only the completion handler
            return null;
        });
    }

    /**
     * Retrieves the {@link Player} object from the Bukkit server using the player's ID.
     * <p>
     * This method uses the player's ID to fetch the corresponding {@link Player} instance. If the player
     * is not online or if the ID is invalid, the method may return {@code null}.
     * </p>
     * 
     * @return The {@link Player} object associated with the specified ID, or {@code null} if the player
     *         is not online or the ID is invalid.
     */
    public Player getPlayer() {
        return Bukkit.getPlayer(
            UUID.fromString(this.playerID)
        );
    }

    /**
     * Retrieves the progress of a specified quest.
     * 
     * This method looks up the {@link ConnectionsData} associated with the given quest from the
     * internal map of quest progress. If the quest is not found in the map, it adds it with the
     * default connections data.
     * 
     * @param quest The {@link Quest} for which to retrieve the progress.
     * 
     * @return The {@link ConnectionsData} object associated with the specified quest, or
     *         the default connections data if the quest is not found in the map.
     */
    public ConnectionsData getQuestProgress(Quest quest) {
        ConnectionsData progress = this.questProgress.get(quest);

        // if no progress found, it must mean the quest isn't in the diary yet
        if (progress == null) {
            this.addQuest(quest);
            progress = quest.getConnections(); // provide
        }

        return progress;
    }

    /**
     * Retrieves the progress of all quests.
     * 
     * @return A {@link Map} where the keys are {@link Quest} objects and the values are the corresponding
     *         {@link ConnectionsData} objects.
     */
    public Map<Quest,ConnectionsData> getQuestProgress() {
        return this.questProgress;
    }

    /**
     * Gets the current stage of a specific quest.
     * 
     * @param quest The {@link Quest} for which to retrieve the current stage.
     * 
     * @return The current {@link QuestStage} object for the specified quest.
     */
    public QuestStage getStage(Quest quest) {
        return this.getStage(quest, false); // just get the current
    }

    /**
     * Gets the stage of a specific quest.
     * 
     * @param quest The {@link Quest} for which to retrieve the stage.
     * @param next Whether to get the next stage.
     * 
     * @return The {@link QuestStage} object for the specified quest, either the current stage or the next stage
     *         depending on the {@code next} parameter.
     */
    public QuestStage getStage(Quest quest, Boolean next) {
        ConnectionsData progress = this.getQuestProgress(quest);

        if (next) {
            return progress.getNext().getStage(quest);
        }

        return progress.getCurr().getStage(quest);
    }

    /**
     * Gets the current action of a specific quest.
     * 
     * @param quest The {@link Quest} for which to retrieve the current action.
     * 
     * @return The current {@link QuestAction} object for the specified quest.
     */
    public QuestAction getAction(Quest quest) {
        return this.getAction(quest, false); // just get the current
    }

    /**
     * Gets the action of a specific quest.
     * 
     * @param quest The {@link Quest} for which to retrieve the action.
     * @param next Whether to get the next action.
     * 
     * @return The {@link QuestAction} object for the specified quest, either the current action or the next action
     *         depending on the {@code next} parameter.
     */
    public QuestAction getAction(Quest quest, Boolean next) {
        ConnectionsData progress = this.getQuestProgress(quest);
        StagePath point = next ? progress.getNext() : progress.getCurr();

        // if no action found
        if (point.getAction(quest) == null) {
            return point.getStage(quest).getEntryPoint().getAction(quest);
        }

        // otherwise just return action
        return point.getAction(quest);
    }

    /**
     * Sets the progress for a specific quest.
     * 
     * If the quest is already listed in the diary, this method updates its progress. Otherwise, it adds
     * the quest with the provided connections data.
     * 
     * @param quest The {@link Quest} for which to set the progress.
     * @param connections The {@link ConnectionsData} representing the quest's progress.
     */
    public void setQuestProgress(Quest quest, ConnectionsData connections) {
        // figure out if is already listed
        if (this.questProgress.containsKey(quest)) {
            // replace the existing with new
            this.questProgress.replace(quest, connections);
            return;
        }

        // add to the list
        this.questProgress.put(quest, connections);
    }

    /**
     * Retrieves the diary ID.
     * 
     * @return The ID of the diary.
     */
    public String getDiaryID() {
        return this.diaryID;
    }

    /**
     * Retrieves the player ID.
     * 
     * @return The ID of the player associated with this diary.
     */
    public String getPlayerID() {
        return this.playerID;
    }

    /**
     * Adds a new quest to the diary with its default progress.
     * 
     * @param quest The {@link Quest} to add.
     */
    public void addQuest(Quest quest) {
        // do not add if not toggled
        if (!quest.isToggled()) {
            return;
        }
        
        // submit to diary
        this.setQuestProgress(
            quest, 
            quest.getConnections()
        );
    }

    /**
     * Removes a quest from the diary.
     * 
     * @param quest The {@link Quest} to remove.
     */
    public void removeQuest(Quest quest) {
        // destroy quest and progress :(
        this.questProgress.keySet().removeIf(localQuest -> quest.getID().equals(localQuest.getID()));
    }
    
}

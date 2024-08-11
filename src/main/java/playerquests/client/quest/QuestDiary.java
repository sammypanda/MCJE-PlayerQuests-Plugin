package playerquests.client.quest;

import java.util.HashMap; // hash map type
import java.util.Map; // generic map type
import java.util.UUID; // unique identifiers for (usually) players
import java.util.concurrent.CompletableFuture; // async + callbacks

import org.bukkit.Bukkit; // the in-game API
import org.bukkit.entity.Player; // the in-game player object

import playerquests.builder.quest.action.QuestAction; // quest product: actions
import playerquests.builder.quest.data.ConnectionsData; // quest product: what action/stage connects to what
import playerquests.builder.quest.stage.QuestStage; // quest product: stages
import playerquests.product.Quest; // quest product
import playerquests.utility.ChatUtils; // sending messages systematically
import playerquests.utility.ChatUtils.MessageStyle; // how the message looks
import playerquests.utility.ChatUtils.MessageTarget; // who the message sends to
import playerquests.utility.ChatUtils.MessageType; // what the message is
import playerquests.utility.singleton.Database; // everything preservation store
import playerquests.utility.singleton.QuestRegistry; // quest store

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
     * 
     * This constructor initializes a {@link QuestDiary} with the given player ID and sets up
     * the diary for storage in the database. The diary is preserved in the database using
     * {@link Database#getInstance()}.{@code addDiary(QuestDiary)}.
     * 
     * @param playerID The unique identifier for the player, represented as a {@link UUID}. This ID
     *                 is used to associate the diary with the player.
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
                this.questProgress.putIfAbsent(quest, quest.getConnections()); // TODO: ensure this isn't duplicating things
            });

            client.update();
        });
    }

    /**
     * Get the quest progress as stored in the database.
     * Should only run on first ever instantiation.
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
     * Retrieves a {@link Player} object from the Bukkit server using the provided player ID.
     * 
     * This method uses the player's ID to fetch the corresponding {@link Player} instance from
     * the server. If the player is not currently online or if the ID is invalid, the method may return
     * {@code null}.
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
     * This method looks up the {@link ConnectionsData} associated with the given quest ID
     * from the internal map of quest progress. If the quest ID is not found in the map, it tries
     * to find it (if it doesn't exist in {@link QuestRegistry}, then it returns {@code null}).
     * 
     * @param questID The unique identifier for the quest, represented as a {@link String}.
     *                This ID is used to locate the corresponding {@link ConnectionsData} in the
     *                quest progress map.
     * 
     * @return The {@link ConnectionsData} object associated with the specified quest ID, or
     *         {@code null} if there is no progress data associated with that quest ID.
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
     * @return The {@link ConnectionsData} object associated with the specified quest ID, or
     *         {@code null} if there is no progress data associated with that quest ID.
     */
    public Map<Quest,ConnectionsData> getQuestProgress() {
        return this.questProgress;
    }

    /**
     * Gets the current stage the player is up to
     * in the quest.
     * @param quest quest to find the action in
     * @return quest stage object
     */
    public QuestStage getStage(Quest quest) {
        return this.getStage(quest, false); // just get the current
    }

    /**
     * Gets the stage the player is up to
     * in the quest.
     * @param quest quest to find the action in
     * @param next whether to get the next stage
     * @return quest stage object
     */
    public QuestStage getStage(Quest quest, Boolean next) {
        String curr = this.getQuestProgress(quest).getCurr();

        // if the current is an action
        if (curr.contains("action")) {
            return quest
                .getActions().get(curr) // retrieve the action we are at
                .getStage(); // retrieve the stage the action we are at belongs to
        }

        // otherwise: just get the stage :)
        return quest.getStages().get(curr);
    }

    /**
     * Gets the current action the player is up to
     * in the quest.
     * @param quest quest to find the action in
     * @return quest action object
     */
    public QuestAction getAction(Quest quest) {
        return this.getAction(quest, false); // just get the current
    }

    /**
     * Gets the action the player is up to
     * in the quest.
     * @param quest quest to find the action in
     * @param next whether to get next action
     * @return quest action object
     */
    public QuestAction getAction(Quest quest, Boolean next) {
        String curr = this.getQuestProgress(quest).getCurr();

        // if the current is a stage
        if (curr.contains("stage")) {
            return quest
                .getStages().get(curr) // retrieve the stage we are at
                .getEntryPoint(); // retrieve the first action in the stage
        }

        // otherwise: just get the action :)
        return quest.getActions().get(curr);
    }

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

    public String getDiaryID() {
        return this.diaryID;
    }

    public String getPlayerID() {
        return this.playerID;
    }

    public void addQuest(Quest quest) {
        this.setQuestProgress(
            quest, 
            quest.getConnections()
        );
    }

    public void removeQuest(Quest quest) {
        // destroy quest and progress :(
        this.questProgress.remove(quest);
    }
    
}

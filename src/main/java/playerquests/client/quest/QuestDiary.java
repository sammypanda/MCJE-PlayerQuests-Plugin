package playerquests.client.quest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import playerquests.builder.quest.action.NoneAction;
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.data.StagePath;
import playerquests.product.Quest;
import playerquests.product.fx.ParticleFX;
import playerquests.utility.singleton.Database;
import playerquests.utility.singleton.QuestRegistry;

/**
 * Quest tracking for a quest client (questers).
 * Also serves as a cache between game and database.
 * @see playerquests.client.quest.QuestClient
 */
public class QuestDiary {

    /**
     * The client that owns this diary.
     */
    private final QuestClient client;

    /**
     * The ID for this quest diary.
     * Useful for persistent data storage.
     */
    private final String id;

    /**
     * Map of quest progress to inform the diary and client.
     */
    private Map<Quest, List<StagePath>> questProgress = new HashMap<>();
    
    /**
     * Constructs a new quest diary.
     * If can be found in the database, it will
     * also pull the stored quest progress.
     * @param client who the diary belongs to
     * @param currentProgress the quest progress, used instead of the default quest start points
     */
    public QuestDiary(QuestClient client) {
        // set the client
        this.client = client;

        // create the ID: [some_id]_diary
        this.id = String.format("%s_diary", 
            client.getPlayer().getUniqueId().toString()
        );

        // search database for unfinished quest actions
        Map<Quest, List<Map<StagePath, Boolean>>> diaryEntries = Database.getInstance().getDiaryEntries(this);
        Map<String, List<StagePath>> currentProgress = diaryEntries.entrySet().stream()
            .collect(Collectors.toMap(
                entry -> entry.getKey().getID(), // use the Quest ID as the key
                entry -> entry.getValue().stream() // stream over the List<Map<StagePath, Boolean>> for each Quest
                    .flatMap(map -> map.entrySet().stream()) // flatten the List of Maps into a stream of Map.Entry<StagePath, Boolean>
                    .filter(stageEntry -> !stageEntry.getValue()) // keep only entries with a true value
                    .map(Map.Entry::getKey) // extract the StagePath (the key)
                    .collect(Collectors.toList()) // collect the filtered StagePaths into a List
            ));

        // if no current progress, quest diary might never have been added
        if (currentProgress.isEmpty()) {
            Database.getInstance().setQuestDiary(this);
        }

        // load in quest progress (w/ prioritising current progress as stored in database)
        QuestRegistry.getInstance()
            .getAllQuests()
            .values()
            .stream()
            .filter(quest -> quest.isToggled())
            .forEach(quest -> {
                // default progress is the default start points
                List<StagePath> progress = quest.getStartPoints();

                // if there is current recorded progress, replace progress with that
                if (currentProgress != null && !currentProgress.isEmpty()) {
                    progress = currentProgress.get(quest.getID());
                }

                // if no paths, don't continue
                if (progress == null) {
                    return;
                }

                // put it in our diary progress list
                questProgress.put(
                    quest, 
                    progress
                );
            });

        // callback to client once finished loading diary
        this.client.start(this);
    }

    /**
     * Get the identifier for this diary.
     * @return a string unique id for the diary
     */
    public String getID() {
        return this.id;
    }

    /**
     * Gets the progress of all quests.
     * @return the quest(s) and it's associated progress
     */
    public Map<Quest, List<StagePath>> getQuestProgress() {
        return this.getQuestProgress(null);
    }

    /**
     * Gets the progress of a quest.
     * @param quest the quest to get progress for, if null it gives all progress
     * @return the quest(s) and it's associated progress
     */
    public Map<Quest, List<StagePath>> getQuestProgress(Quest quest) {
        if (quest == null) {
            return this.questProgress;
        }

        return Map.of(quest, this.questProgress.get(quest));
    }

    /**
     * Set or replace quest progress with new paths.
     * @param quest the quest to replace
     * @param paths current actions/stages ongoing
     */
    public void setQuestProgress(Quest quest, List<StagePath> paths) {
        this.questProgress.put(quest, paths);
    }

    /**
     * Adds a quest to the diary and client.
     * @param quest the quest to add.
     */
    public void add(Quest quest) {
        List<StagePath> startPoints = quest.getStartPoints();

        // start for quester
        client.start(startPoints, quest);

        // put or replace in diary
        this.setQuestProgress(quest, startPoints);
    }

    /**
     * Removes a quest from the diary and client.
     * @param quest the quest to remove.
     */
    public void remove(Quest quest) {
        // find by quest ID
        Optional<Entry<Quest, List<StagePath>>> entry = this.questProgress
            .entrySet()
            .stream()
            .filter(theQuest -> theQuest.getKey().getID().equals(quest.getID()))
            .findFirst();

        // exit if quest progress not found
        if (entry.isEmpty()) {
            return;
        }

        // stop each ongoing action in the client
        client.stop(quest);
    }

    /**
     * Get the state of completion for the action.
     * <ul>
     *  <li>0 = Uncompleted</li>
     *  <li>1 = Completed</li>
     *  <li>2 = In progress</li>
     *  <li>3 = Ineligible</li>
     * </ul>
     * @param quest
     * @param path
     * @return
     */
    public Integer getActionCompletionState(Quest quest, QuestAction action) {
        // retrieve all entries from the database
        Map<Quest, List<Map<StagePath, Boolean>>> rawEntries = Database.getInstance().getDiaryEntries(this);

        // exit if no work to do
        if (rawEntries == null || !rawEntries.containsKey(quest)) {
            return 0;
        }

        // exit if a 'None' action
        if (action instanceof NoneAction) {
            return 3;
        }

        // check if is an ongoing actions
        if (this.getQuestClient().getTrackedActions().contains(action)) {
            return 2;
        }

        // get entries for the specific quest
        List<Map<StagePath, Boolean>> questEntries = rawEntries.get(quest);

        // check for ongoing actions
        List<QuestAction> pathActions = path.getActions(quest);
        if (this.getQuestClient().getTrackedActions().stream().anyMatch(action -> pathActions.contains(action))) {
            return 2;
        }

        // check for incomplete actions
        if (questEntries.stream()
            .flatMap(pathMap -> pathMap.entrySet().stream()) // change map to entry set to be able to work with it
            .filter(pathEntry -> pathEntry.getKey().getActions(quest).contains(action)) // for every action in the path
            .noneMatch(entry -> !entry.getValue())) 
        { // if no action incomplete, all actions are complete
            return 1;
        }

        return 0;
    }

    /**
     * Get the quester this diary belongs to.
     * @return the players QuestClient.
     */
    public QuestClient getQuestClient() {
        return this.client;
    }

    /**
     * Get the particle that should show up as 
     * an indicator an action can be interacted with.
     * @return the type of particle FX
     */
    public ParticleFX getActionParticle() {
        return ParticleFX.SPARKLE;
    }
}

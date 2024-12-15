package playerquests.client.quest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;

import playerquests.builder.quest.data.StagePath;
import playerquests.product.Quest;
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
    public QuestDiary(QuestClient client, Map<String, List<StagePath>> currentProgress) {
        // set the client
        this.client = client;

        // create the ID: [some_id]_diary
        this.id = String.format("%s_diary", 
            client.getPlayer().getUniqueId().toString()
        );

        // load in quest progress
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

                // put it in our diary progress list
                questProgress.put(
                    quest, 
                    progress
                );
            });

        // callback to client once finished loading diary
        this.client.start(this);

        // TODO: remove the following debug thingy block:
        this.questProgress.forEach((quest, list) -> {
            String progressString = "";
            if (list != null) {
                progressString = list.stream().map(StagePath::toString).collect(Collectors.joining(", "));
            }

            Bukkit.broadcastMessage("added stuff to a quest diary " + this.getID() + " quest: " + quest.getID() + ": " + progressString);
        });
    }

    /**
     * Get the identifier for this diary.
     * @return a string unique id for the diary
     */
    public String getID() {
        return this.id;
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
        if (entry.get() == null) {
            System.out.println(this.questProgress);
            return;
        }

        // stop each ongoing action in the client
        client.stop(quest);
    }
}

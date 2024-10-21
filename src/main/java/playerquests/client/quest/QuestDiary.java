package playerquests.client.quest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
// TODO: plan
public class QuestDiary {

    /**
     * The ID for this quest diary.
     * Useful for persistent data storage.
     */
    private final String id;

    /**
     * 
     */
    private Map<Quest, List<StagePath>> questProgress = new HashMap<>();
    
    /**
     * Constructs a new quest diary.
     * If can be found in the database, it will
     * also pull the stored quest progress.
     * @param uniqueIdentifier any unique value that consistently identifies who the diary belongs to
     */
    public QuestDiary(String uniqueIdentifier, Map<String, List<StagePath>> currentProgress) {
        // create the ID: [some_id]_diary
        this.id = String.format("%s_diary", 
            uniqueIdentifier
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
}

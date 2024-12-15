package playerquests.client.quest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.data.QuesterData;
import playerquests.builder.quest.data.StagePath;
import playerquests.product.Quest;

/**
 * Functionality for questers (quest players).
 */
public class QuestClient {

    /**
     * The player for this quest client.
     */
    private final Player player;

    /**
     * The players quest diary.
     */
    private QuestDiary diary;

    /**
     * Tracked actions.
     */
    private List<QuestAction> trackedActions = new ArrayList<>();

    /**
     * Constructs a new client on behalf of a quester (quest player).
     * @param player the quester as a Bukkit player object
     */
    public QuestClient(Player player) {
        this.player = player;
        
        new QuestDiary(this, null);
        // TODO: search database to populate this ^ diary currentProgress, if none create one
    }

    /**
     * Gets the player.
     * @return the quest client player
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Gets the quest diary.
     * @return the diary belonging to this quester
     */
    public QuestDiary getDiary() {
        return this.diary;
    }

    /**
     * Start the quest client by initialising everything.
     * When diary is done loading, it calls this.
     * @param diary the diary of this player
     */
	public void start(QuestDiary diary) {
        if (this.diary != null) {
            throw new RuntimeException("A diary was started twice!");
        }

        this.diary = diary;

        // get all current quest progress
        diary.getQuestProgress(null).entrySet().stream()
            // and initialise on the first action for each quest
            .forEach(entry -> {
                Quest quest = entry.getKey();

                // start the actions
                this.start(entry.getValue(), quest);
            });
	}

    /**
     * Start actions from a path pointing to stages/actions.
     * @param paths the pointer
     * @param quest the quest to use the pointer on
     */
    public void start(List<StagePath> paths, Quest quest) {
        paths.forEach(path -> {
            // if no actions, point to stage start points
            if (!path.hasActions()) {
                this.start(path.getStage(quest).getStartPoints(), quest);
                return;
            }
    
            // get actions
            List<QuestAction> actions = path.getActions(quest);
    
            // for each action, start
            actions.forEach(action -> {
                // run the action
                action.run(new QuesterData(this, this.player.getLocation()));

                // track the action
                this.trackAction(action);
            });
        });
    }

    /**
     * Start tracking an action.
     * WARNING: does not run or do anything 
     * else, it's just an indication.
     * @param action the quest action to track
     */
    private void trackAction(QuestAction action) {
        this.trackedActions.add(action);
    }

    /**
     * Stop tracking an action.
     * WARNING: does not stop or do anything 
     * else, it's just removing the indication.
     * @param action the quest action to untrack
     */
    public void untrackAction(QuestAction action) {
        this.trackedActions.removeIf(theAction -> theAction.equals(action));
    }

    /**
     * Stop an aciton based on a quest.
     * @param quest quest to use the pointer on
     * @param path the pointer
     */
    public void stop(Quest quest, StagePath path) {
        // get the actions attached to this path
        path.getActions(quest).forEach(action -> {
            // stop the action
            action.stop(new QuesterData(this, this.player.getLocation()));
        });
    }

    /**
     * Stop ongoing actions based on the quest 
     * they're from.
     * @param quest quest to halt.
     */
    public void stop(Quest quest) {
        // avoid concurrent modification issues by creating a clone of state
        List<QuestAction> trackedActions_clone = new ArrayList<>(this.trackedActions);

        // filter through all the tracked actions
        this.trackedActions = trackedActions_clone.stream().filter((action) -> {
            // find the actions that match the quest
            Boolean match = action.getStage().getQuest().equals(quest);

            // if they do match the passed in quest
            if (match) {
                // ask for them to stop
                action.stop(new QuesterData(this, this.player.getLocation()));
            }

            // only return predicates that don't match 
            // (aka: clear out trackedActions of this quest)
            return !match;
        }).collect(Collectors.toList()); // get the filtered elements as a list
    }
}

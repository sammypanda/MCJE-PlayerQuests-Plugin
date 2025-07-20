package playerquests.client.quest;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.data.QuesterData;
import playerquests.builder.quest.data.StagePath;
import playerquests.product.Quest;
import playerquests.utility.ChatUtils;
import playerquests.utility.ChatUtils.MessageType;
import playerquests.utility.singleton.Database;
import playerquests.utility.singleton.QuestRegistry;

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
     * The data associated with this client.
     */
    private QuesterData data;

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

        // add to registry list
        QuestRegistry.getInstance().addQuester(this);

        // create data
        this.data = new QuesterData(this, this.player.getLocation());
        
        // create diary
        new QuestDiary(this);
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
            throw new IllegalStateException("A diary was started twice!");
        }

        this.diary = diary;

        // get all current quest progress
        diary.getQuestProgress().entrySet().stream()
            // and initialise on the first action for each quest
            .forEach(entry -> {
                Quest quest = entry.getKey();

                // start the actions
                this.start(entry.getValue(), quest);
            });
	}

    /**
     * Start actions from a path pointing to stages/actions.
     * By default this will start any action, even if it has already been completed.
     * @param paths the pointer
     * @param quest the quest to use the pointer on
     */
    public void start(List<StagePath> paths, Quest quest) {
        this.start(paths, quest, true);
    }

    /**
     * Start actions from a path pointing to stages/actions.
     * @param paths the pointer
     * @param quest the quest to use the pointer on
     * @param force the action to start even if it has already been completed
     */
    public void start(List<StagePath> paths, Quest quest, boolean force) {
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
                this.start(action, force);
            });
        });
    }

    /**
     * Start the action by an action object.
     * @param action the action to start
     * @param force the action to start even if it has already been completed
     */
    public void start(QuestAction action, boolean force) {
        Quest quest = action.getStage().getQuest(); // get the quest the action belongs to

        Integer completionState = this.getDiary().getActionCompletionState(quest, action);

        // if not force, and has completed; exit
        if (!force && completionState == 1) {
            ChatUtils.message("Already completed this quest action! ^_^")
                .player(this.getPlayer())
                .type(MessageType.NOTIF)
                .send();
            return;
        }

        // if already in progress, don't ever start/force
        if (completionState == 2) {
            ChatUtils.message(String.format("'%s' quest action already in progress! ^_^", action.getName()))
                .player(this.getPlayer())
                .type(MessageType.NOTIF)
                .send();
            return;
        }

        // if quest not allowed; exit
        if ( ! quest.isAllowed() ) {
            quest.toggle(false);
            return;
        }

        // run the action
        action.run(this.getData());

        // track the action
        this.trackAction(action);

        // update the Database
        StagePath actionPath = new StagePath(action.getStage(), List.of(action));
        Database.getInstance().setDiaryEntryCompletion(this.diary.getID(), quest.getID(), actionPath, false);
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
     * @return if the action was untracked
     */
    public boolean untrackAction(QuestAction action) {
        return this.trackedActions.removeIf(theAction -> theAction.equals(action));
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
            action.stop(this.getData());
        });
    }

    /**
     * Get the actions currently ongoing.
     * @return list of ongoing quest actions
     */
    public List<QuestAction> getTrackedActions() {
        return this.trackedActions;
    }

    /**
     * Stop ongoing actions based on the quest 
     * they're from. This does not let them continue to next.
     * @param quest quest to halt.
     */
    public void stop(Quest quest) {
        // avoid concurrent modification issues by creating a clone of state
        List<QuestAction> trackedActions_clone = new ArrayList<>(this.trackedActions);

        // filter through all the tracked actions
        this.trackedActions = (ArrayList<QuestAction>) trackedActions_clone.stream().filter((action) -> {
            // find the actions that match the quest
            Boolean match = action.getStage().getQuest().getID().equals(quest.getID());

            // if they do match the passed in quest
            if (match) {
                // ask for them to stop
                action.stop(this.getData(), true);
            }

            // only return predicates that don't match 
            // (aka: clear out trackedActions of this quest)
            return !match;
        }).toList(); // get the filtered elements as a list
    }

    /**
     * Get the quester data.
     * @return a QuesterData object.
     */
    public QuesterData getData() {
        return this.data;
    }

    /**
     * Clear what would otherwise be leftover from a QuestClient
     */
    public void clear() {
        // unregister all NPCs
        this.getData().getNPCs().forEach(npc -> {
            npc.getValue().despawn(this);
        });
    }
}

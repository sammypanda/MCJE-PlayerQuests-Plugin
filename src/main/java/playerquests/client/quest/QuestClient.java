package playerquests.client.quest;

import java.util.List;

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

        // get all quest progress
        diary.getQuestProgress(null).entrySet().stream()
            // and initialise for each quest
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
            });
        });
    }
}

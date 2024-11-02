package playerquests.client.quest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.action.option.NPCOption;
import playerquests.builder.quest.data.StagePath;
import playerquests.builder.quest.npc.QuestNPC;
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
     * List of active NPCs
     */
    private List<QuestNPC> activeNPCs;

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

                // get actions
                List<QuestAction> actions = entry.getValue().stream()
                    .map(path -> path.getActions(quest)) // get actions from the path
                    .flatMap(List::stream) // flatten out the list of actions into a one dimensional list
                    .collect(Collectors.toList()); // store as a list

                // for each action
                actions.forEach(action -> 
                    start(new StagePath(action.getStage(), List.of(action)), quest) // start the action
                );
            });
	}

    public void start(StagePath path, Quest quest) {
        List<QuestAction> actions = path.getActions(quest);

        actions.forEach(action -> {
            // find NPC option if applies
            Optional<NPCOption> npcOption = action.getData().getOptions().stream()
                .filter(NPCOption.class::isInstance)
                .map(NPCOption.class::cast)
                .findFirst();
            
            if (npcOption.isPresent()) {
                // get the NPC
                QuestNPC npc = quest.getNPCs().get(npcOption.get().getNPC());

                // spawn the NPC for this quester
                npc.place(player);
            }
        });
    }
}

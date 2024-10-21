package playerquests.client.quest;

import java.util.List;

import org.bukkit.entity.Player;

import playerquests.builder.quest.npc.QuestNPC;

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
    private final QuestDiary diary;

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
        
        this.diary = new QuestDiary(player.getUniqueId().toString(), null);
        // TODO: search database to populate this ^ diary currentProgress, if none create one
    }

    /**
     * Gets the player.
     * @return the quest client player
     */
    public Object getPlayer() {
        return this.player;
    }

    /**
     * Gets the quest diary.
     * @return the diary belonging to this quester
     */
    public QuestDiary getDiary() {
        return this.diary;
    }
}

package playerquests.client.quest;

import org.bukkit.entity.Player;

/**
 * Functionality for questers (quest players).
 */
public class QuestClient {

    /**
     * The player for this quest client.
     */
    private final Player player;

    /**
     * Constructs a new client on behalf of a quester (quest player).
     * @param player the quester as a Bukkit player object
     */
    public QuestClient(Player player) {
        this.player = player;
    }

    /**
     * Gets the player.
     * @return the quest client player
     */
    public Object getPlayer() {
        return this.player;
    }
}

package playerquests.client.quest;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

/**
 * Quest tracking and interactions for each player.
 */
// TODO: use the quest registry to get quest products and 'play' them
// TODO: player.spawnParticle on quests that are available to play (maybe search and set on login)
public class QuestClient {

    /**
     * The player who is using this quest client.
     */
    private HumanEntity player;

    /**
     * Creates a new quest client to act on behalf of a player.
     * <p>
     * A quest client enables interactions. It keeps track of 
     * player quest progress and other quest-related information.
     * @param player user of the quest client
     */
    public QuestClient(Player player) {
        this.player = player;
    }

    /**
     * Gets the player who is using the quest client.
     * @return human entity object representing the player
     */
    public HumanEntity getPlayer() {
        return player;
    }
    
}

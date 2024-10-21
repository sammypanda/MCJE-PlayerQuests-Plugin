package playerquests.utility.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import playerquests.builder.quest.npc.QuestNPC;

/**
 * Event for when a player interacts with an NPC.
 */
public class NPCInteractEvent extends Event {

    /**
     * List of event handlers.
     */
    private static final HandlerList handlers = new HandlerList();

    /**
     * The NPC interacted with.
     */
    private final QuestNPC npc;

    /**
     * The player who interacted with the NPC.
     */
    private final Player player;

    /**
     * Constructor for the when a player interacts with an NPC.
     * @param npc the npc interacted with
     * @param player the player who interacted with the npc
     */
    public NPCInteractEvent(QuestNPC npc, Player player) {
        this.npc = npc;
        this.player = player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Gets the NPC interacted with.
     * @return a quest npc
     */
    public QuestNPC getNPC() {
        return this.npc;
    }

    /**
     * Gets the player that interact with the NPC.
     * @return a bukkit player object
     */
    public Player getPlayer() {
        return this.player;
    }
}

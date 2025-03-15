package playerquests.utility.event;

import java.util.List;

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
     * The NPCs interacted with.
     */
    private final List<QuestNPC> npcs;

    /**
     * The player who interacted with the NPC.
     */
    private final Player player;

    /**
     * Constructor for the when a player interacts with an NPC.
     * @param activeNPCs the npc interacted with
     * @param player the player who interacted with the npc
     */
    public NPCInteractEvent(List<QuestNPC> activeNPCs, Player player) {
        this.npcs = activeNPCs;
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
    public List<QuestNPC> getNPCs() {
        return this.npcs;
    }

    /**
     * Gets the player that interact with the NPC.
     * @return a bukkit player object
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Get the hander list for this event.
     * @return the event handler list
     */
    static public HandlerList getHandlerList() {
        return handlers;
    }
}

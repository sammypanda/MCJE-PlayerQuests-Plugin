package playerquests.utility.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler; // indicate that a method is wanting to handle an event
import org.bukkit.event.Listener; // registering listening to Bukkit in-game events
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent; // when a player interacts with a block
import org.bukkit.inventory.EquipmentSlot;

import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import playerquests.Core; // accessing plugin singeltons
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.npc.BlockNPC; // NPCs represented by blocks
import playerquests.builder.quest.npc.QuestNPC;
import playerquests.client.quest.QuestClient;
import playerquests.utility.event.NPCInteractEvent;

/**
 * Listens for block-related events to manage interactions with BlockNPCs in the game.
 * 
 * This class handles events such as player interactions with BlockNPCs and block break events. It ensures that
 * interactions are processed correctly, prevents modifications to BlockNPC blocks, and manages the lifecycle of
 * BlockNPCs within the game.
 * 
 * <ul>
 *   <li>Registers itself with the Bukkit event system to listen for block events.</li>
 *   <li>Maintains a map of active BlockNPCs to handle interactions and block breaks.</li>
 * </ul>
 */
public class BlockListener implements Listener {

    private Map<Player, Boolean> canQuesterRefreshNPCs = new HashMap<>();

    /**
     * Constructs a new {@code BlockListener} and registers it with the Bukkit event system.
     */
    public BlockListener() {
        Bukkit.getPluginManager().registerEvents(this, Core.getPlugin());
    }

    /**
     * Removes a block NPC from the world for a specific player.
     * @param blockNPC the npc block object to remove
     * @param player the player to remove for
     */
    public void unsetBlockNPC(BlockNPC blockNPC, Player player) {
        Location npcLocation = blockNPC.getNPC().getLocation().toBukkitLocation(); // get the QuestNPC location
        BlockData emptyBlockData = Material.AIR.createBlockData(); // AIR block to replace the NPC block with

        // clear the block if no other NPCs here
        player.sendBlockChange(npcLocation, emptyBlockData); // remove the NPC
    }

    @EventHandler
    public void onChunkLoad(PlayerChunkLoadEvent event) { 
        // Get or create the refresh state for this player
        Boolean state = this.canQuesterRefreshNPCs.getOrDefault(event.getPlayer(), true);
        
        // If refresh not allowed for this player, exit
        if ( ! state) {
            return;
        }

        QuestClient quester = Core.getQuestRegistry().getQuester(event.getPlayer());
        this.canQuesterRefreshNPCs.put(event.getPlayer(), false); // block refresh for period
        
        Bukkit.getScheduler().runTaskLater(Core.getPlugin(), () -> {
            // get NPCs
            List<Entry<QuestAction, QuestNPC>> npcs = quester.getData().getNPCs().stream() // get list of matching npcs
                .filter(npc -> npc.getValue().getAssigned() instanceof BlockNPC)
                .filter(npc -> npc.getValue().getLocation().toBukkitLocation().getChunk().isLoaded())
                .toList();

            // execute refresh
            npcs.forEach(npc -> {
                BlockNPC blockNPC = (BlockNPC) npc.getValue().getAssigned();
                blockNPC.spawn(npc.getKey(), quester);
            });
            
            // clean up
            this.canQuesterRefreshNPCs.put(event.getPlayer(), true);
        }, 30 * 20); // 30 seconds (20 ticks per second)
    }
    
    /**
     * Handles player interactions with BlockNPCs.
     * @param event the {@code PlayerInteractEvent} to handle
     */
    @EventHandler
    public void onBlockNPCInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();

        if (block == null) { return; }

        Location eventBlockLocation = block.getLocation();
        QuestClient quester = Core.getQuestRegistry().getQuester(event.getPlayer());
        List<Entry<QuestAction, QuestNPC>> npcs = quester.getData().getNPCs().stream() // get list of matching npcs
            .filter(npc -> npc.getValue().getAssigned() instanceof BlockNPC)
            .filter(npc -> npc.getValue().getLocation().toBukkitLocation().equals(eventBlockLocation))
            .toList();

        // don't disturb ghost block if is a BlockNPC here
        if ( ! npcs.isEmpty() ) {
            event.setCancelled(true);
        }

        // persist client-side blocks
        Bukkit.getScheduler().runTask(Core.getPlugin(), () -> {
            npcs.forEach(npc -> {
                BlockNPC blockNPC = (BlockNPC) npc.getValue().getAssigned();
                blockNPC.spawn(npc.getKey(), quester);
            });
        });

        // conditions to not continue the event:
        if (
            block == null || // if block doesn't exist
            npcs.isEmpty() || // if block is not an active NPC
            event.getHand().equals(EquipmentSlot.OFF_HAND) || // no duplicating interaction
            !event.getAction().equals(Action.RIGHT_CLICK_BLOCK) // if the interaction is not a right click
        ) { return; }
        
        // send out the event
        Bukkit.getServer().getPluginManager().callEvent(
            new NPCInteractEvent(npcs, event.getPlayer())
        );
    }
}
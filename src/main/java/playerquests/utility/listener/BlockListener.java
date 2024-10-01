package playerquests.utility.listener;

import java.util.HashMap; // hash table map type
import java.util.Map; // generic map type
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block; // the one and only great block type
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler; // indicate that a method is wanting to handle an event
import org.bukkit.event.Listener; // registering listening to Bukkit in-game events
import org.bukkit.event.block.Action; // identifying what action was done to a block
import org.bukkit.event.block.BlockBreakEvent; // when a block is broken
import org.bukkit.event.player.PlayerInteractEvent; // when a player interacts with a block
import org.bukkit.inventory.EquipmentSlot; // identifies which hand was used to interact

import playerquests.Core; // accessing plugin singeltons
import playerquests.builder.quest.npc.BlockNPC; // NPCs represented by blocks
import playerquests.builder.quest.npc.QuestNPC; // the core information about an NPC
import playerquests.product.Quest; // final quest products

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

    /**
     * A map of active BlockNPCs, where the key is the block and the value is the BlockNPC.
     */
    private Map<Block, BlockNPC> activeBlockNPCs = new HashMap<Block, BlockNPC>();

    /**
     * Constructs a new {@code BlockListener} and registers it with the Bukkit event system.
     */
    public BlockListener() {
        Bukkit.getPluginManager().registerEvents(this, Core.getPlugin());
    }

    /**
     * Registers a BlockNPC as active, associating it with a specific block.
     * 
     * <ul>
     *   <li>Adds the given BlockNPC to the list of active BlockNPCs.</li>
     *   <li>Synchronizes access to the map to ensure thread safety.</li>
     * </ul>
     * 
     * @param block the block to which the BlockNPC is associated
     * @param blockNPC the BlockNPC to register
     */
    public void registerBlockNPC(Block block, BlockNPC blockNPC) {
        synchronized (activeBlockNPCs) {
            activeBlockNPCs.put(block, blockNPC);
        }
    }

    /**
     * Unregisters a BlockNPC, removing it from the list of active BlockNPCs and performing cleanup.
     * 
     * <ul>
     *   <li>Removes the BlockNPC from the active map.</li>
     *   <li>Removes the associated quest from the QuestRegistry.</li>
     * </ul>
     * 
     * @param blockNPC the BlockNPC to unregister
     */
    public void unregisterBlockNPC(BlockNPC blockNPC) {
        QuestNPC npc = blockNPC.getNPC();

        if (npc == null) {
            return; // don't continue if no NPC associated
        }

        synchronized (activeBlockNPCs) {
            Map<Block, BlockNPC> filteredBlockNPCs = activeBlockNPCs.entrySet().stream()
                .filter(entry -> entry.getValue() != blockNPC) // filter out the blockNPC to unregister
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)); // collect stream back to Map

            // replace the NPCs list with the filtered
            activeBlockNPCs = filteredBlockNPCs;
        }
    }
    
    /**
     * Handles player interactions with BlockNPCs.
     * 
     * <ul>
     *   <li>Checks if the block is a BlockNPC and if the interaction should be processed.</li>
     *   <li>Stops any modifications to the block and processes the interaction with the NPC.</li>
     * </ul>
     * 
     * @param event the {@code PlayerInteractEvent} to handle
     */
    @EventHandler
    public void onBlockNPCInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();

        if ( // conditions to not continue the event:
            block == null || 
            !activeBlockNPCs.containsKey(block) || 
            event.getHand().equals(EquipmentSlot.OFF_HAND) ||
            !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
        ) {
            return;
        }

        // stop accidental modification of the quest block
        event.setCancelled(true);
    }

    /**
     * Handles block break events for blocks associated with BlockNPCs.
     * 
     * <ul>
     *   <li>Prevents the block from dropping items and replaces it with air.</li>
     *   <li>Unregisters the BlockNPC if its block is broken.</li>
     * </ul>
     * 
     * @param event the {@code BlockBreakEvent} to handle
     */
    @EventHandler
    public void onBlockNPCBreak(BlockBreakEvent event) {
        Block brokenBlock = event.getBlock();

        if (!this.activeBlockNPCs.containsKey(brokenBlock)) {
            return; // don't continue if not an NPC block
        }

        event.setCancelled(true); // don't drop the block (block duplication)
        brokenBlock.getWorld().setBlockData(brokenBlock.getLocation(), Material.AIR.createBlockData()); // replace the block with air

        BlockNPC npc = this.activeBlockNPCs.get(brokenBlock);
        this.unregisterBlockNPC(npc);
    }

    /**
     * Removes all active BlockNPCs associated with a specific quest and replaces their blocks with air.
     * 
     * <ul>
     *   <li>Iterates through the map of active BlockNPCs and removes those associated with the specified quest.</li>
     *   <li>Replaces the blocks with air and removes the NPCs from the active list.</li>
     * </ul>
     * 
     * @param quest the quest whose BlockNPCs should be removed
     */
    public void remove(Quest quest) {
        BlockData replacementBlock = Material.AIR.createBlockData();

        synchronized (activeBlockNPCs) {
            this.activeBlockNPCs.entrySet().removeIf(entry -> {
                if (!entry.getValue().getNPC().getQuest().getID().equals(quest.getID())) {
                    return false; // keep entry that doesn't match quest removal
                }

                Location npcLocation = entry.getKey().getLocation();
                World npcWorld = npcLocation.getWorld();
        
                // replace the NPC block
                Bukkit.getScheduler().runTask(Core.getPlugin(), () -> { // synchronously
                    npcWorld.setBlockData(
                        npcLocation, 
                        replacementBlock
                    );
                });
        
                // remove the 'active npc'
                return true;
            });
        }
    }
}

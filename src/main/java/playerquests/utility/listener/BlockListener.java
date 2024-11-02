package playerquests.utility.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block; // the one and only great block type
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
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

    /**
     * A map of active BlockNPCs, where the key is the block and the value is the BlockNPC.
     */
    private List<BlockNPC> activeBlockNPCs = new ArrayList<BlockNPC>();

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
     * @param blockNPC the BlockNPC to register
     */
    public void registerBlockNPC(BlockNPC blockNPC, Player player) {
        synchronized (activeBlockNPCs) {
            // add the block to the list to be refreshed
            activeBlockNPCs.add(blockNPC);

            // send initial update of block
            player.sendBlockChange(
                blockNPC.getNPC().getLocation().toBukkitLocation(),
                blockNPC.getBlock()
            );
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

        // don't continue if no NPC associated
        if (npc == null) {
            return;
        }

        // merge with the active list of block NPCs
        synchronized (activeBlockNPCs) {
            List<BlockNPC> filteredBlockNPCs = activeBlockNPCs.stream()
                .filter(currentBlockNPC -> !currentBlockNPC.equals(blockNPC)) // filter out the blockNPC to unregister
                .collect(Collectors.toList()); // collect stream back to list

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
        Optional<BlockNPC> activeNPC = this.isActiveNPC(block);

        // persist client-side blocks
        Bukkit.getScheduler().runTask(Core.getPlugin(), () -> {
            if (activeNPC.isPresent()) {
                Location blockLocation = block.getLocation();
                Location barrierLocation = blockLocation.clone().add(0, 1, 0);

                // put a barrier block above the NPC to avoid 'kicked for flying'
                blockLocation.getWorld().setBlockData(barrierLocation, Material.BARRIER.createBlockData());

                // show the NPC block
                event.getPlayer().sendBlockChange(blockLocation, activeNPC.get().getBlock());
            }
        });

        // conditions to not continue the event:
        if (
            block == null || // if block doesn't exist
            activeNPC.isEmpty() || // if block is not an active NPC
            event.getHand().equals(EquipmentSlot.OFF_HAND) || // no duplicating interaction
            !event.getAction().equals(Action.RIGHT_CLICK_BLOCK) // if the interaction is not a right click
        ) { return; }

        // stop accidental modification of the quest block
        event.setCancelled(true);

        // call event
        Bukkit.getServer().getPluginManager().callEvent(
            new NPCInteractEvent(activeNPC.get().getNPC(), event.getPlayer())
        );
    }

    /**
     * Checks if the passed in block is an active block NPC.
     * @param block the block to check.
     * @return optional which may not be present.
     */
    private Optional<BlockNPC> isActiveNPC(Block block) {
        if (block == null) {
            return Optional.empty();
        }

        return activeBlockNPCs.stream() // determine if is an active NPC:
            .filter(blockNPC -> blockNPC.getNPC().getLocation().toBukkitLocation().equals(block.getLocation())) // match on location
            .findFirst();
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
        Optional<BlockNPC> possibleNPC = this.isActiveNPC(brokenBlock);

        if (possibleNPC.isEmpty()) {
            return; // don't continue if not an NPC block
        }

        event.setCancelled(true); // don't drop the block (block duplication)
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
            this.activeBlockNPCs.removeIf(blockNPC -> {
                if (!blockNPC.getNPC().getQuest().getID().equals(quest.getID())) {
                    return false; // keep entry that doesn't match quest removal
                }

                Location npcLocation = blockNPC.getNPC().getLocation().toBukkitLocation();
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

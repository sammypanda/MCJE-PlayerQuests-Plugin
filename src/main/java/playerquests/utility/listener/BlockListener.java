package playerquests.utility.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

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
    private Map<Player, BlockNPC> activeBlockNPCs = new HashMap<Player, BlockNPC>();

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
     * @param player the player to register the NPC for
     */
    public void registerBlockNPC(BlockNPC blockNPC, Player player) {
        synchronized (activeBlockNPCs) {
            // add the block to the list to be refreshed
            activeBlockNPCs.put(player, blockNPC);

            // send initial update of block
            this.setBlockNPC(blockNPC, player);
        }
    }

    private void setBlockNPC(BlockNPC blockNPC, Player player) {
        Location blockLocation = blockNPC.getNPC().getLocation().toBukkitLocation();
        Location barrierLocation = blockLocation.clone().add(0, 2, 0);
        World world = blockLocation.getWorld();

        // update the client side NPC block
        player.sendBlockChange(blockLocation, blockNPC.getBlock());

        // exit if barrier already in place
        if (barrierLocation.getBlock().getType().equals(Material.BARRIER)) {
            return;
        }
        
        // place barrier to stop "flying" when standing on NPC block
        world.setBlockData(barrierLocation, Material.BARRIER.createBlockData());
    }

    private void unsetBlockNPC(BlockNPC blockNPC) {
        Location blockLocation = blockNPC.getNPC().getLocation().toBukkitLocation();
        Location barrierLocation = blockLocation.clone().add(0, 2, 0);
        World world = blockLocation.getWorld();
        BlockData airData = Material.AIR.createBlockData();

        world.setBlockData(blockLocation, airData); // unset NPC block
        world.setBlockData(barrierLocation, airData); // unset barrier above NPC
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
     * @param player the player to register the NPC for
     */
    public void unregisterBlockNPC(BlockNPC blockNPC, Player player) {
        QuestNPC npc = blockNPC.getNPC();

        // don't continue if no NPC associated
        if (npc == null) {
            return;
        }

        // merge with the active list of block NPCs
        synchronized (activeBlockNPCs) {
            this.activeBlockNPCs.replace(player, blockNPC);
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
        Player player = event.getPlayer();
        Optional<BlockNPC> activeNPC = this.isActiveNPC(block, player);

        // persist client-side blocks
        Bukkit.getScheduler().runTask(Core.getPlugin(), () -> {
            if (activeNPC.isPresent()) {
                this.setBlockNPC(activeNPC.get(), player);
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
     * @param player the player to register the NPC for
     * @return optional which may not be present.
     */
    private Optional<BlockNPC> isActiveNPC(Block block, Player player) {
        if (block == null) {
            return Optional.empty();
        }

        return activeBlockNPCs.entrySet().stream() // determine if is an active NPC:
            .filter(entry -> entry.getValue().getNPC().getLocation().toBukkitLocation().equals(block.getLocation())) // match on location
            .filter(entry -> entry.getKey().equals(player)) // match on player
            .map(Entry::getValue)
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
        Player player = event.getPlayer();
        Optional<BlockNPC> possibleNPC = this.isActiveNPC(brokenBlock, player);

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
            this.activeBlockNPCs.forEach((player, blockNPC) -> {
                QuestNPC npc = blockNPC.getNPC();

                if (!npc.getQuest().getID().equals(quest.getID())) {
                    return; // keep entries that don't match quest to remove
                }

                Location npcLocation = npc.getLocation().toBukkitLocation();

                // synchronously replace the NPC block
                Bukkit.getScheduler().runTask(Core.getPlugin(), () -> {
                    player.sendBlockChange(
                        npcLocation, 
                        replacementBlock
                    );
                });

                // remove the 'active npc'
                this.activeBlockNPCs.remove(player, blockNPC);
            });
        }
    }

    /**
     * Clear out blockNPCs.
     */
    public void clear() {
        this.activeBlockNPCs.forEach((_, blockNPC) -> {
            // remove NPC blocks from world
            this.unsetBlockNPC(blockNPC);
        });
    }
}

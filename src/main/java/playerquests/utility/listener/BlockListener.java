package playerquests.utility.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
import playerquests.builder.quest.data.LocationData;
import playerquests.builder.quest.npc.BlockNPC; // NPCs represented by blocks
import playerquests.builder.quest.npc.QuestNPC;
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
    private Map<Player, Map<BlockNPC, LocationData>> activeBlockNPCs = new HashMap<>();

    /**
     * Constructs a new {@code BlockListener} and registers it with the Bukkit event system.
     */
    public BlockListener() {
        Bukkit.getPluginManager().registerEvents(this, Core.getPlugin());
    }

    /**
     * Registers a BlockNPC as active, associating it with a specific block.
     * @param blockNPC the BlockNPC to register
     * @param player the player to register the NPC for
     */
    public void registerBlockNPC(BlockNPC blockNPC, Player player) {
        this.unsetBlockNPC(blockNPC, player);

        // ensure the player's NPC map exists or is created
        Map<BlockNPC, LocationData> npcMap = this.activeBlockNPCs.computeIfAbsent(player, _ -> new HashMap<>());
    
        npcMap.put(blockNPC, blockNPC.getNPC().getLocation()); // add the BlockNPC and its location to the map
        this.setBlockNPCs(List.of(blockNPC), player); // add the BlockNPC to the world
    }

    /**
     * Unregisters a BlockNPC, removing it from the list of active BlockNPCs and performing cleanup.
     * @param blockNPC the BlockNPC to unregister
     * @param player the player to register the NPC for
     */
    public synchronized void unregisterBlockNPC(BlockNPC blockNPC, Player player) {
        // remove the BlockNPC from the player's active map
        this.activeBlockNPCs.computeIfPresent(player, (_, npcMap) -> {
            this.unsetBlockNPC(blockNPC, player); // remove the BlockNPC from the world
            npcMap.remove(blockNPC);  // remove the BlockNPC from the map
            return npcMap.isEmpty() ? null : npcMap;  // if the map is empty, return null to remove the entry for the player
        });
    }

    /**
     * Puts a block NPC in the world for a specific player.
     * @param blockNPCs list of npc block objects
     * @param player the player who can see the npc
     */
    private void setBlockNPCs(List<BlockNPC> blockNPCs, Player player) {
        Map<BlockNPC, LocationData> npcMap = this.activeBlockNPCs.get(player);

        // if no active blockNPCs for this player, exit
        if (npcMap == null) { return; }

        // if no blockNPCs to set, exit
        if (blockNPCs.isEmpty()) { return; }

        blockNPCs.forEach(blockNPC -> {
            // get the NPC this BlockNPC represents
            QuestNPC npc = blockNPC.getNPC();

            // ignore inactive NPCs
            if (!npcMap.keySet().contains(blockNPC)) {
                return;
            }

            // create the NPC block in the world
            player.sendBlockChange(
                npcMap.get(blockNPC).toBukkitLocation(), 
                npc.getBlock()
            );
        });
    }

    /**
     * Removes a block NPC from the world for a specific player.
     * @param blockNPC the npc block object to remove
     * @param player the player to remove for
     */
    private void unsetBlockNPC(BlockNPC blockNPC, Player player) {
        Map<BlockNPC, LocationData> npcMap = this.activeBlockNPCs.get(player);

        if (npcMap == null || npcMap.isEmpty() || npcMap.get(blockNPC) == null) { return; } // don't continue if empty map

        Location npcLocation = npcMap.get(blockNPC).toBukkitLocation();
        BlockData emptyBlockData = Material.AIR.createBlockData();

        // clear the block if no other NPCs here
        if (this.getActiveNPCs(npcLocation.getBlock(), player).size() <= 1) {
            player.sendBlockChange(npcLocation, emptyBlockData); // remove the NPC
        }
    }
    
    /**
     * Handles player interactions with BlockNPCs.
     * @param event the {@code PlayerInteractEvent} to handle
     */
    @EventHandler
    public void onBlockNPCInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        List<BlockNPC> activeBlockNPCs = this.getActiveNPCs(block, player);

        // persist client-side blocks
        Bukkit.getScheduler().runTask(Core.getPlugin(), () -> {
            if (!activeBlockNPCs.isEmpty()) {
                this.setBlockNPCs(activeBlockNPCs, player);
            }
        });

        // conditions to not continue the event:
        if (
            block == null || // if block doesn't exist
            activeBlockNPCs.isEmpty() || // if block is not an active NPC
            event.getHand().equals(EquipmentSlot.OFF_HAND) || // no duplicating interaction
            !event.getAction().equals(Action.RIGHT_CLICK_BLOCK) // if the interaction is not a right click
        ) { return; }

        // stop accidental modification of the quest block
        event.setCancelled(true);

        // collect the NPCs that the BlockNPCs list represent
        List<QuestNPC> activeNPCs = activeBlockNPCs.stream()
            .map(blockNPC -> blockNPC.getNPC())
            .toList();

        // call event
        Bukkit.getServer().getPluginManager().callEvent(
            new NPCInteractEvent(activeNPCs, event.getPlayer())
        );
    }

    /**
     * Checks if the passed in block is an active block NPC.
     * @param block the block to check.
     * @param player the player to register the NPC for
     * @return list which may be empty.
     */
    private List<BlockNPC> getActiveNPCs(Block block, Player player) {
        if (block == null || this.activeBlockNPCs.get(player) == null) { return List.of(); } 

        // get the block's location
        Location blockLocation = block.getLocation();

        List<BlockNPC> activeNPCs = this.activeBlockNPCs.get(player).entrySet().stream()
            // get all that match based on location
            .filter(entry -> entry.getValue().toBukkitLocation().equals(blockLocation))
            // just get the key (BlockNPC)
            .map(Entry::getKey)
            // collect to a list of active BlockNPCs
            .toList();

        return activeNPCs;
    }

    /**
     * Handles block break events for blocks associated with BlockNPCs.
     * @param event the {@code BlockBreakEvent} to handle
     */
    @EventHandler
    public void onBlockNPCBreak(BlockBreakEvent event) {
        Block brokenBlock = event.getBlock();
        Player player = event.getPlayer();
        List<BlockNPC> possibleNPCs = this.getActiveNPCs(brokenBlock, player);

        if (possibleNPCs.isEmpty()) {
            return; // don't continue if not an NPC block
        }

        event.setCancelled(true); // don't drop the block (block duplication)
    }

    /**
     * Removes all active BlockNPCs associated with a specific quest.
     * @param quest the quest whose BlockNPCs should be removed
     */
    public void remove(Quest quest) {
        quest.getNPCs().values().stream()
            .filter(npc -> npc.getAssigned().getClass().isAssignableFrom(BlockNPC.class))
            .map(QuestNPC::getAssigned)
            .forEach(blockNPC -> {
                this.remove(quest, (BlockNPC) blockNPC);
            });
    }

    /**
     * Removes an NPC from all players.
     * @param quest the quest the NPC belongs to
     * @param blockNPC the NPC block
     */
    public void remove(Quest quest, BlockNPC blockNPC) {
        this.remove(quest, blockNPC, null);
    }

    /**
     * Removes the BlockNPC associated with a specific player.
     * @param quest the quest whose BlockNPCs should be removed
     * @param blockNPC the npc to remove
     * @param player the player to remove the NPC from
     */
    public void remove(Quest quest, BlockNPC blockNPC, Player player) {
        // Create a deep copy of the activeBlockNPCs map (copy of outer and inner maps)
        Map<Player, Map<BlockNPC, LocationData>> activeBlockNPCsCopy = new HashMap<>();
                        
        // Create a deep copy of each player's inner map (BlockNPC -> LocationData)
        // - this avoids concurrency issues
        this.activeBlockNPCs.entrySet().stream()
            .filter(entry -> {
                if (player == null) { return true; } // don't filter out any players if no player passed in
                return entry.getKey().equals(player);
            })
            .forEach(entry -> {
                // put the key (the player) and the value (the blockNPC and its location)
                activeBlockNPCsCopy.put(entry.getKey(), new HashMap<>(entry.getValue()));
            });
            
        // Iterate over the copy and unregister the NPCs
        activeBlockNPCsCopy.keySet().forEach(thePlayer -> {
            // Unregister the BlockNPC for each player
            this.unregisterBlockNPC(blockNPC, thePlayer);
        });
    }

    /**
     * Clear all block NPCs.
     */
    public synchronized void clear() {
        // copy the entire map, but with deep copies of the inner maps
        Map<Player, Map<BlockNPC, LocationData>> activeBlockNPCsCopy = new HashMap<>();
    
        this.activeBlockNPCs.forEach((player, blockNPCs) -> {
            // Make a deep copy of the inner map
            Map<BlockNPC, LocationData> copiedBlockNPCs = new HashMap<>(blockNPCs);
            activeBlockNPCsCopy.put(player, copiedBlockNPCs);
        });
    
        // iterate over the copied map safely
        activeBlockNPCsCopy.entrySet().forEach(entry -> {
            entry.getValue().forEach((blockNPC, _) -> {
                // Unregister each block
                this.unregisterBlockNPC(blockNPC, entry.getKey());
            });
        });
    }    
}

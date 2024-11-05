package playerquests.utility.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
        // ensure the player's NPC map exists or is created
        Map<BlockNPC, LocationData> npcMap = this.activeBlockNPCs.computeIfAbsent(player, _ -> new HashMap<>());
    
        npcMap.put(blockNPC, blockNPC.getNPC().getLocation()); // add the BlockNPC and its location to the map
        this.setBlockNPC(blockNPC, player); // add the BlockNPC to the world
    }

    /**
     * Unregisters a BlockNPC, removing it from the list of active BlockNPCs and performing cleanup.
     * @param blockNPC the BlockNPC to unregister
     * @param player the player to register the NPC for
     */
    public void unregisterBlockNPC(BlockNPC blockNPC, Player player) {
        // remove the BlockNPC from the player's active map
        this.activeBlockNPCs.computeIfPresent(player, (_, npcMap) -> {
            npcMap.remove(blockNPC);  // remove the BlockNPC from the map
            this.unsetBlockNPC(blockNPC); // remove the BlockNPC from the world
            return npcMap.isEmpty() ? null : npcMap;  // if the map is empty, return null to remove the entry for the player
        });
    }

    /**
     * Puts a block NPC in the world for a specific player.
     * @param blockNPC the npc block object
     * @param player the player who can see the npc
     */
    private void setBlockNPC(BlockNPC blockNPC, Player player) {
        Map<BlockNPC, LocationData> npcMap = this.activeBlockNPCs.get(player);
        QuestNPC npc = blockNPC.getNPC();
        Location npcLocation = npcMap.get(blockNPC).toBukkitLocation();
        BlockData npcBlockData = npc.getBlock();

        player.sendBlockChange(npcLocation, npcBlockData); // create the NPC block in the world
        player.sendBlockChange(npcLocation.clone().add(0, 2, 0), Material.BARRIER.createBlockData()); // create the block to stop 'flying'
    }

    /**
     * Removes a block NPC from the world.
     * @param blockNPC the npc block object to remove
     */
    private void unsetBlockNPC(BlockNPC blockNPC) {
        this.activeBlockNPCs.keySet().forEach(player -> {
            this.unsetBlockNPC(blockNPC, player);
        });
    }

    /**
     * Removes a block NPC from the world for a specific player.
     * @param blockNPC the npc block object to remove
     * @param player the player to remove for
     */
    private void unsetBlockNPC(BlockNPC blockNPC, Player player) {
        Map<BlockNPC, LocationData> npcMap = this.activeBlockNPCs.get(player);
        Location npcLocation = npcMap.get(blockNPC).toBukkitLocation();
        BlockData emptyBlockData = Material.AIR.createBlockData();

        player.sendBlockChange(npcLocation, emptyBlockData); // remove the NPC
        player.sendBlockChange(npcLocation.clone().add(0, 2, 0), emptyBlockData); // remvove remove the barrier
    }
    
    /**
     * Handles player interactions with BlockNPCs.
     * @param event the {@code PlayerInteractEvent} to handle
     */
    @EventHandler
    public void onBlockNPCInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        Optional<BlockNPC> activeNPC = this.getActiveNPC(block, player);

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
    private Optional<BlockNPC> getActiveNPC(Block block, Player player) {
        // get the block's location
        Location blockLocation = block.getLocation();
        
        // check if the player exists in the activeBlockNPCs map
        return Optional.ofNullable(this.activeBlockNPCs.get(player))
            // stream over the inner map associated with the player
            .map(npcDataMap -> npcDataMap.keySet().stream()
                // use findFirst to get the first match based on location
                .filter(blockNPC -> blockNPC.getNPC().getLocation().toBukkitLocation().equals(blockLocation))
                .findFirst())           // find the first matching BlockNPC
            .orElse(Optional.empty());  // if no matching NPC, return an empty Optional
    }

    /**
     * Handles block break events for blocks associated with BlockNPCs.
     * @param event the {@code BlockBreakEvent} to handle
     */
    @EventHandler
    public void onBlockNPCBreak(BlockBreakEvent event) {
        Block brokenBlock = event.getBlock();
        Player player = event.getPlayer();
        Optional<BlockNPC> possibleNPC = this.getActiveNPC(brokenBlock, player);

        if (possibleNPC.isEmpty()) {
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
            .forEach((blockNPC) -> {
                this.activeBlockNPCs.keySet().forEach(player -> {
                    this.unregisterBlockNPC((BlockNPC) blockNPC, player);
                });
            });
    }

    /**
     * Clear all block NPCs.
     */
    public void clear() {
        this.activeBlockNPCs.entrySet().forEach(entry -> {
            // for each player
            entry.getValue().forEach((blockNPC, _) -> {
                // unregister each block
                this.unregisterBlockNPC(blockNPC, entry.getKey());
            });
        });
    }
}

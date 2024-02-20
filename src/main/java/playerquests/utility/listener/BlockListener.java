package playerquests.utility.listener;

import java.util.ArrayList; // array list type
import java.util.HashMap; // hash table map type
import java.util.List; // generic list type
import java.util.Map; // generic map type
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.block.Block; // the one and only great block type
import org.bukkit.event.EventHandler; // indicate that a method is wanting to handle an event
import org.bukkit.event.Listener; // registering listening to Bukkit in-game events
import org.bukkit.event.player.PlayerInteractEvent; // when a player interacts with a block

import playerquests.Core;
import playerquests.builder.quest.npc.BlockNPC; // NPCs represented by blocks

/**
 * Listens out for all block-related events to inform 
 * where needed.
 */
public class BlockListener implements Listener {

    /**
     * List of all the current 'alive'/NPC blocks.
     */
    private Map<Block, BlockNPC> activeBlockNPCs = new HashMap<Block, BlockNPC>();

    public BlockListener() {
        Bukkit.getPluginManager().registerEvents(this, Core.getPlugin());
    }

    /**
     * Bring a block NPC to life.
     * @param block
     * @param blockNPC the block to animate
     */
    public void registerBlockNPC(Block block, BlockNPC blockNPC) {
        activeBlockNPCs.put(block, blockNPC);
    }

    /**
     * Filter out the block NPC from the list of 
     * active Block NPCs so it  turns back into 
     * an average block.
     * @param blockNPC the block to un-animate
     */
    public void unregisterBlockNPC(BlockNPC blockNPC) {
        Map<Block, BlockNPC> filteredBlockNPCs = activeBlockNPCs.entrySet().stream()
            .filter(entry -> entry.getValue() != blockNPC) // filter out the blockNPC to unregister
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)); // collect stream back to Map

        // replace the NPCs list with the filtered
        activeBlockNPCs = filteredBlockNPCs;
    }
    
    @EventHandler
    public void onBlockNPCInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();

        if (block == null || !activeBlockNPCs.containsKey(block)) {
            return;
        }
        
        event.getPlayer().sendMessage("[PlayerQuests] You just interacted with an NPC Block (WIP)");
    }
}

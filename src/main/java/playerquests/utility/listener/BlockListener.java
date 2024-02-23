package playerquests.utility.listener;

import java.util.HashMap; // hash table map type
import java.util.Map; // generic map type
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.block.Block; // the one and only great block type
import org.bukkit.event.EventHandler; // indicate that a method is wanting to handle an event
import org.bukkit.event.Listener; // registering listening to Bukkit in-game events
import org.bukkit.event.block.Action; // identifying what action was done to a block
import org.bukkit.event.block.BlockBreakEvent; // when a block is broken
import org.bukkit.event.player.PlayerInteractEvent; // when a player interacts with a block
import org.bukkit.inventory.EquipmentSlot; // identifies which hand was used to interact

import playerquests.Core; // accessing plugin singeltons
import playerquests.builder.quest.npc.BlockNPC; // NPCs represented by blocks
import playerquests.builder.quest.npc.QuestNPC; // the core information about an NPC
import playerquests.client.quest.QuestClient; // player quest state
import playerquests.product.Quest; // final quest products
import playerquests.utility.singleton.QuestRegistry; // application quest state

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

        // remove the quest, as now it's missing the NPC
        Quest quest = blockNPC.getNPC().getQuest().build();
        QuestRegistry.getInstance().remove(quest);
    }
    
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

        QuestNPC npc = activeBlockNPCs.get(block).getNPC();
        QuestClient quester = QuestRegistry.getInstance().getQuester(event.getPlayer());
        
        event.getPlayer().sendMessage("[PlayerQuests] You just interacted with an NPC Block (WIP: you are = " + quester + ")");
    }

    @EventHandler
    public void onBlockNPCBreak(BlockBreakEvent event) {
        Block brokenBlock = event.getBlock();

        if (!this.activeBlockNPCs.containsKey(brokenBlock)) {
            return; // don't continue if not an NPC block
        }

        BlockNPC npc = this.activeBlockNPCs.get(brokenBlock);
        this.unregisterBlockNPC(npc);
    }
}

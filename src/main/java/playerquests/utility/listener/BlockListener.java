package playerquests.utility.listener;

import java.util.List;

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

import playerquests.Core; // accessing plugin singeltons
import playerquests.builder.quest.data.QuesterData;
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
        // add the BlockNPC to the world
        this.setBlockNPC(blockNPC, player);
    }

    /**
     * Unregisters a BlockNPC, removing it from the list of active BlockNPCs and performing cleanup.
     * @param blockNPC the BlockNPC to unregister
     * @param player the player to register the NPC for
     */
    public synchronized void unregisterBlockNPC(BlockNPC blockNPC, Player player) {
        // remove the BlockNPC from the world
        this.unsetBlockNPC(blockNPC, player);
    }

    /**
     * Puts a block NPC in the world for a specific player.
     * @param blockNPCs list of npc block objects
     * @param player the player who can see the npc
     */
    private void setBlockNPC(BlockNPC blockNPC, Player player) {
        QuestNPC questNPC = blockNPC.getNPC();
        
        // create the NPC block in the world
        player.sendBlockChange(
            questNPC.getLocation().toBukkitLocation(), 
            blockNPC.getBlock()
        );
    }

    /**
     * Removes a block NPC from the world for a specific player.
     * @param blockNPC the npc block object to remove
     * @param player the player to remove for
     */
    private void unsetBlockNPC(BlockNPC blockNPC, Player player) {
        Location npcLocation = blockNPC.getNPC().getLocation().toBukkitLocation(); // get the QuestNPC location
        BlockData emptyBlockData = Material.AIR.createBlockData(); // AIR block to replace the NPC block with

        // clear the block if no other NPCs here
        player.sendBlockChange(npcLocation, emptyBlockData); // remove the NPC
    }
    
    /**
     * Handles player interactions with BlockNPCs.
     * @param event the {@code PlayerInteractEvent} to handle
     */
    @EventHandler
    public void onBlockNPCInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        QuesterData questerData = Core.getQuestRegistry().getQuester(player).getData();
        Location eventBlockLocation = block.getLocation();

        // persist client-side blocks
        Bukkit.getScheduler().runTask(Core.getPlugin(), () -> {
            questerData.getNPCs().values().forEach(npc -> {
                this.setBlockNPC((BlockNPC) npc.getAssigned(), player);
            });
        });

        // get the NPCs matching the location of the interacted block
        List<QuestNPC> npcs = questerData.getNPCs().values().stream()
            .filter(questNPC -> questNPC.getLocation().toBukkitLocation().equals(eventBlockLocation))
            .toList();

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

    /**
     * Removes all active BlockNPCs associated with a specific quest.
     * @param quest the quest whose BlockNPCs should be removed
     */
    public void remove(Quest quest) {
        // for each quest, get each BlockNPC, and remove it
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
        this.unregisterBlockNPC(blockNPC, player);
    }

    /**
     * Clear all block NPCs.
     */
    public synchronized void clear() {
        // for each quest in the registry, remove it
        Core.getQuestRegistry().getAllQuests().values().forEach(quest -> {
            this.remove(quest);
        });
    }    
}
package playerquests.utility.singleton;

import org.bukkit.Location; // the minecraft location object
import org.bukkit.Material; // enums for game blocks/items
import org.bukkit.World; // the minecraft world
import org.bukkit.block.Block; // the minecraft block
import org.bukkit.block.data.BlockData; // all the information regarding a block

import playerquests.builder.quest.npc.BlockNPC; // a block representing an NPC
import playerquests.builder.quest.npc.QuestNPC; // core NPC object/data
import playerquests.product.Quest; // represents a quest product
import playerquests.utility.listener.BlockListener; // for block-related events
import playerquests.utility.listener.PlayerListener; // for player-related events
import playerquests.utility.listener.ServerListener; // for server-related events

public class PlayerQuests {

    /**
     * Singleton for persistent data
     */
    private static Database database = Database.getInstance();

    private static PlayerQuests instance = new PlayerQuests();
    private BlockListener blockListener = new BlockListener();
    private PlayerListener playerListener = new PlayerListener();
    private ServerListener serverListener = new ServerListener();

    public static PlayerQuests getInstance() {
        return instance;
    }

    /**
     * Puts the block in the world and registers
     * it as an NPC.
     * @param blockNPC the block details of an npc
     */
    public void putBlockNPC(BlockNPC blockNPC) {
        if (blockNPC.getNPC() == null || blockNPC.getNPC().getLocation() == null) {
            blockListener.unregisterBlockNPC(blockNPC);
            return; // if no location has been set, don't try to put
        }

        QuestNPC npc = blockNPC.getNPC();
        Location npcBukkitLocation = npc.toBukkitLocation();
        World npcWorld = npcBukkitLocation.getWorld();

        // set the block in the world for this NPC to register to
        npcWorld.setBlockData(
            npcBukkitLocation,
            blockNPC.getBlock().createBlockData()
        );

        // get the block to use it to efficiently find match in BlockListener
        Block block = npcWorld.getBlockAt(
            npcBukkitLocation
        );

        // register the block
        blockListener.registerBlockNPC(block, blockNPC);
    }

    /**
     * Removes traces of a quest from the world.
     * @param quest the quest to remove traces of
     */
    // TODO: store and revert to previous block state @ the location
    public void remove(Quest quest) {
        // remove all NPCs
        blockListener.remove(quest);
    }
    
}

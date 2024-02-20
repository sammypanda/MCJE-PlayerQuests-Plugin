package playerquests.utility.singleton;

import org.bukkit.Location;
import org.bukkit.World; // the minecraft world
import org.bukkit.block.Block;

import playerquests.builder.quest.npc.BlockNPC;
import playerquests.builder.quest.npc.QuestNPC;
import playerquests.utility.listener.BlockListener;

public class PlayerQuests {

    private static PlayerQuests instance = new PlayerQuests();
    private BlockListener blockListener = new BlockListener();

    public static PlayerQuests getInstance() {
        return instance;
    }

    /**
     * Puts the block in the world and registers
     * it as an NPC.
     * @param blockNPC the block details of an npc
     */
    public void putBlockNPC(BlockNPC blockNPC) {
        if (blockNPC.getNPC().getLocation() == null) {
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
    
}

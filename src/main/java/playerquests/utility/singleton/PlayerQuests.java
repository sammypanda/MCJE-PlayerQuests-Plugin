package playerquests.utility.singleton;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location; // the minecraft location object
import org.bukkit.World; // the minecraft world
import org.bukkit.block.Block; // the minecraft block

import playerquests.builder.quest.npc.BlockNPC; // a block representing an NPC
import playerquests.builder.quest.npc.QuestNPC; // core NPC object/data
import playerquests.client.Director; // generic director type
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
    private static BlockListener blockListener = new BlockListener();
    private static PlayerListener playerListener = new PlayerListener();
    private static ServerListener serverListener = new ServerListener();

    /**
     * Gets the PlayerQuests instance.
     * @return centralised plugin class
     */
    public static PlayerQuests getInstance() {
        return instance;
    }

    /**
     * Gets the PlayerQuests database.
     * @return centralised data store
     */
    public static Database getDatabase() {
        return database;
    }

    /**
     * Gets the PlayerQuests in-game block listener.
     * @return centralised block listener
     */
    public static BlockListener getBlockListener() {
        return blockListener;
    }

    /**
     * Gets the PlayerQuests in-game player listener.
     * @return centralised player listener
     */
    public static PlayerListener getPlayerListener() {
        return playerListener;
    }

    /**
     * Gets the PlayerQuests in-game server listener.
     * @return centralised server listener
     */
    public static ServerListener getServerListener() {
        return serverListener;
    }

    /**
     * List of directors.
     */
    private List<Director> directors = new ArrayList<Director>();

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
        Location npcBukkitLocation = npc.getLocation().toBukkitLocation();
        World npcWorld = npcBukkitLocation.getWorld();

        // set the block in the world for this NPC to register to
        npcWorld.setBlockData(
            npcBukkitLocation,
            blockNPC.getBlock()
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
    public void remove(Quest quest) {
        // remove all NPCs
        blockListener.remove(quest);
    }

    public void addDirector(Director director) {
        this.directors.add(director);
    }

    /**
     * Clear each part of the quest.
     * 
     * Clear the QuestRegistry to ensure no quests are left 
     * in memory or are in an inconsistent state after the plugin
     * is disabled. This is for maintaining the integrity 
     * of the quest data and preventing memory leaks.
     */
    public void clear() {
        QuestRegistry.getInstance().clear();
        directors.removeIf(director -> {
            director.close();  // Close the director
            return true;       // Remove the entry
        });
    }
}

package playerquests.utility.singleton;

import java.util.ArrayList;
import java.util.Arrays;
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

/**
 * Central utility class for managing various aspects of the PlayerQuests plugin.
 * <p>
 * The {@code PlayerQuests} class is a singleton that provides access to the main components of the plugin,
 * including the database, event listeners, and quest management functionalities. It ensures that only one instance
 * of the class exists and provides methods to interact with different components of the plugin.
 * </p>
 */
public class PlayerQuests {

    /**
     * Singleton instance of the database used for persistent data storage.
     */
    private static Database database = Database.getInstance();

    /**
     * Singleton instance of the PlayerQuests class.
     * <p>
     * This is the single instance of the {@code PlayerQuests} class, ensuring that there is only one central
     * utility class managing the plugin's core functionality.
     * </p>
     */
    private static PlayerQuests instance = new PlayerQuests();

    /**
     * Singleton instance of the block event listener.
     * <p>
     * This instance handles all block-related events for the plugin.
     * </p>
     */
    private static BlockListener blockListener = new BlockListener();

    /**
     * Singleton instance of the player event listener.
     * <p>
     * This instance handles all player-related events for the plugin.
     * </p>
     */
    private static PlayerListener playerListener = new PlayerListener();

    /**
     * Singleton instance of the server event listener.
     * <p>
     * This instance handles all server-related events for the plugin.
     * </p>
     */
    private static ServerListener serverListener = new ServerListener();

    /**
     * Should be accessed statically.
     */
    private PlayerQuests() {}

    /**
     * Gets the singleton instance of the PlayerQuests class.
     * 
     * @return The single instance of {@code PlayerQuests}.
     */
    public static PlayerQuests getInstance() {
        return instance;
    }

    /**
     * Gets the singleton instance of the PlayerQuests database.
     * 
     * @return The database instance used for persistent data storage.
     */
    public static Database getDatabase() {
        return database;
    }

    /**
     * Gets the singleton instance of the block event listener.
     * 
     * @return The block listener instance used for handling block-related events.
     */
    public static BlockListener getBlockListener() {
        return blockListener;
    }

    /**
     * Gets the singleton instance of the player event listener.
     * 
     * @return The player listener instance used for handling player-related events.
     */
    public static PlayerListener getPlayerListener() {
        return playerListener;
    }

    /**
     * Gets the singleton instance of the server event listener.
     * 
     * @return The server listener instance used for handling server-related events.
     */
    public static ServerListener getServerListener() {
        return serverListener;
    }

    /**
     * List of directors responsible for various tasks in the plugin.
     */
    private List<Director> directors = new ArrayList<Director>();

    /**
     * Registers a block in the world and associates it with an NPC.
     * <p>
     * This method places a block at the NPC's location in the world and registers it with the block listener.
     * If the NPC does not have a valid location, the block will not be registered.
     * </p>
     * 
     * @param blockNPC The {@link BlockNPC} object containing the block details and associated NPC.
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
     * Removes all traces of a quest from the world.
     * <p>
     * This method removes all NPCs and associated data related to the specified quest.
     * </p>
     * 
     * @param quest The {@link Quest} object whose traces are to be removed.
     */
    public static void remove(Quest quest) {
        // uninstall player interactivity
        QuestRegistry.getInstance().getQuesters().values().forEach(quester -> {
            quester.removeQuest(quest);
        });

        // remove all NPCs
        quest.getNPCs().values().forEach(npc -> {
            npc.remove();
        });
    }

    /**
     * Adds quest objects into the world.
     * 
     * @param quest The {@link Quest} object that will be installed.
     */
    public static void install(Quest quest) {
        // add all NPCs
        quest.getNPCs().values().forEach(npc -> {
            npc.setQuest(quest);
            npc.place();
        });

        // install player interactivity
        QuestRegistry.getInstance().getQuesters().values().forEach(quester -> {
            quester.addQuests(Arrays.asList(quest));
        });
    }

    /**
     * Adds a director to the list of directors.
     * <p>
     * Directors are components responsible for various tasks in the plugin, and this method adds them to the internal list.
     * </p>
     * 
     * @param director The {@link Director} instance to be added.
     */
    public void addDirector(Director director) {
        this.directors.add(director);
    }

    /**
     * Clears all quest-related data and shuts down directors.
     * <p>
     * This method clears the {@link QuestRegistry} and closes all registered directors to ensure no quests remain
     * in memory and to prevent memory leaks when the plugin is disabled.
     * </p>
     */
    public void clear() {
        QuestRegistry.getInstance().clear();
        directors.removeIf(director -> {
            director.close();  // Close the director
            return true;       // Remove the entry
        });
    }
}

package playerquests.utility.singleton;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;
import playerquests.Core;
import playerquests.client.Director; // generic director type
import playerquests.product.Quest; // represents a quest product
import playerquests.utility.ChatUtils;
import playerquests.utility.ChatUtils.MessageStyle;
import playerquests.utility.ChatUtils.MessageTarget;
import playerquests.utility.ChatUtils.MessageType;
import playerquests.utility.enums.DependencyIssue;
import playerquests.utility.listener.BlockListener; // for block-related events
import playerquests.utility.listener.EntityListener;
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
     * Singleton instance of the entity event listener.
     * <p>
     * This instance handles all entity-related events for the plugin.
     * </p>
     */
    private static EntityListener entityListener = new EntityListener();

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
     * Map of plugin dependencies.
     */
    private Map<String, Boolean> dependencies = new HashMap<>();

    /**
     * Non-persistent citizens NPC registry, required for citizens plugin API.
     */
    private NPCRegistry citizensRegistry;

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
     * Gets the Citizens2 plugin.
     *
     * @return the citizens2 plugin or null if doesn't exist.
     */
    public static Plugin getCitizens2() {
        // NOTE: the actual plugin name is Citizens, it's just referred commonly referred to as Citizens2 because it's in v2
        return Bukkit.getServer().getPluginManager().getPlugin("Citizens");
    }

    /**
     * Gets the PlayerQuests Citizens plugin registry.
     * !! Please check hasCitizens2 before using.
     * @return a non-persistent citizens registry named 'playerquests'
     */
    public NPCRegistry getCitizensRegistry() {
        // exit if not has citizens 2
        if ( ! this.hasCitizens2() ) {
            throw new IllegalAccessError("Tried to access CitizensRegistry without Citizens");
        }

        // set if not set
        try {
            if (this.citizensRegistry == null) {
                Class<?> memoryNPCDataStoreClass = Class.forName("net.citizensnpcs.api.npc.MemoryNPCDataStore");
                Object dataStore = memoryNPCDataStoreClass.getDeclaredConstructor().newInstance();
                this.citizensRegistry = CitizensAPI.createNamedNPCRegistry("playerquests", (NPCDataStore) dataStore);
            }
            return this.citizensRegistry;
        } catch (Exception e) {
            throw new NoSuchMethodError("Failed to initialize Citizens registry");
        }
    }

    /**
     * Simple method that just checks state without any corrections to quickly determine if has citizens2
     *
     * @return if citizens2 plugin has been installed to the server environment.
     */
    public boolean hasCitizens2() {
        final Boolean mappedSupport = this.dependencies.get("Citizens2");

        // return instantiated support check
        if (mappedSupport != null) {
            return mappedSupport;
        }

        // ---
        // if we go beyond this point it means that this is the first time checking
        // the server we are running inside for Citizens2
        // ---

        // verify support
        Boolean isSupported = false;
        DependencyIssue dependencyIssue = DependencyIssue.MISSING;

        if ( PlayerQuests.getCitizens2() != null ) {
            String[] versionParts = PlayerQuests.getCitizens2().getPluginMeta().getVersion().split("\\D+"); // e.g: 2, 0, 36 (also ignores "-SNAPSHOT (BUILD XYZ)")
            Integer majorVersion = Integer.parseInt(versionParts[0]); // e.g: 2
            Integer minorVersion = Integer.parseInt(versionParts[1]); // e.g: 0
            Integer patchVersion = Integer.parseInt(versionParts[2]); // e.g: 36   
            
            String expectedVersion = null;
            Integer expectedMajorVersion = majorVersion; // (equalise to allow Citizens use if this process fails; no false warnings)
            Integer expectedMinorVersion = minorVersion;
            Integer expectedPatchVersion = patchVersion;
            try (InputStream input = getClass().getResourceAsStream("/plugin.properties")) {
                Properties props = new Properties();
                props.load(input);

                expectedVersion = props.getProperty("expectedCitizensVersion");
                String[] expectedVersionParts = expectedVersion.split("\\.");
                expectedMajorVersion = Integer.parseInt(expectedVersionParts[0]);
                expectedMinorVersion = Integer.parseInt(expectedVersionParts[1]);
                expectedPatchVersion = Integer.parseInt(expectedVersionParts[2]);
            } catch (Exception e) {
                ChatUtils.message("POM.XML is missing the expected citizensFlatVersion (got " + expectedVersion + ") for this PlayerQuests release, please report this to sammypanda")
                    .type(MessageType.ERROR)
                    .style(MessageStyle.PRETTY)
                    .target(MessageTarget.CONSOLE)
                    .send();
            }

            final boolean isSameMajor = majorVersion.equals(expectedMajorVersion);
            final boolean isHigherMinor = minorVersion > expectedMinorVersion;
            final boolean isSameMinor = minorVersion.equals(expectedMinorVersion);
            final boolean isPatchSufficient = patchVersion >= expectedPatchVersion;

            isSupported = (
                isSameMajor && 
                (isHigherMinor || (isSameMinor && isPatchSufficient))
            );

            // notify if version older than expected
            if ( ! isSupported && majorVersion <= expectedMajorVersion ) {
                dependencyIssue = DependencyIssue.OUT_OF_DATE;
            }

            // notify if major version too new
            if ( majorVersion > expectedMajorVersion ) {
                dependencyIssue = DependencyIssue.TOO_NEW;
            }
        }

        if ( ! isSupported ) {
            // send message about what is wrong with Citizens2
            dependencyIssue.sendMessage(
                "To unlock all the NPC types, consider " + dependencyIssue.getRemedyPresentPrinciple() + " Citizens! Without it, some NPC types will be unavailable. <3", 
                "https://ci.citizensnpcs.co/job/Citizens2/"
            );
        }

        // instantiate the 'quickdraw' support checking
        this.dependencies.put("Citizens2", isSupported);
        
        // provide the check result
        return isSupported;
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
     * Gets the singleton instance of the entity event listener.
     *
     * @return The entity listener instance used for handling entity-related events.
     */
    public static EntityListener getEntityListener() {
        return entityListener;
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
    private List<Director> directors = new ArrayList<>();

    /**
     * Removes all traces of a quest from the world.
     * Not from data.
     *
     * @param quest The {@link Quest} object whose traces are to be removed.
     */
    public static void remove(Quest quest) {
        // remove from each quester
        Core.getQuestRegistry().getAllQuesters().forEach(quester -> {
            quester.getDiary().remove(quest);
        });
    }

    /**
     * Install a quest.
     *
     * @param quest The {@link Quest} object that will be installed.
     */
    public static void install(Quest quest) {
        // set quest NPCs should belong to
        quest.getNPCs().values().forEach(npc -> {
            npc.setQuest(quest);
        });

        // put quest into each quester
        Core.getQuestRegistry().getAllQuesters().forEach(quester -> {
            quester.getDiary().add(quest);
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
        // clear all questers
        QuestRegistry.getInstance().getAllQuesters().forEach(quester -> {
            quester.clear();
        });

        // despawn the citizens and remove citizens registry
        if ( PlayerQuests.getInstance().hasCitizens2() ) {
            this.getCitizensRegistry().despawnNPCs(DespawnReason.REMOVAL);
            CitizensAPI.removeNamedNPCRegistry("playerquests");
        }

        // clear the quest registry
        QuestRegistry.getInstance().clear();

        // clear the directors
        directors.removeIf(director -> {
            director.close();  // Close the director
            return true;       // Remove the entry
        });
    }
}

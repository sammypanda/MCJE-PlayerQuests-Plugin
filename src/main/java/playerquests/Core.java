package playerquests;

import java.io.File;

import org.bukkit.plugin.Plugin; // export the plugin for use elsewhere
import org.bukkit.plugin.java.JavaPlugin; // essential for initialising the plugin

import playerquests.client.chat.command.Commandplayerquest; // command to enter the main GUI
import playerquests.utility.singleton.Database; // API for game persistent data
import playerquests.utility.singleton.KeyHandler; // special class for using keys to reference any method
import playerquests.utility.singleton.PlayerQuests; // for cross-communication of game/plugin components
import playerquests.utility.singleton.QuestRegistry; // the registry of quest products

/**
 * Entry point for the plugin.
 * <ul>
 * <li>{@link #onLoad()}
 * <ul>
 * <li>Creates a test GUI.
 * </ul>
 * </ul> 
 */
// TODO: automatically initiate the plugin commands
public class Core extends JavaPlugin {

    /**
     * Singleton of the Plugin
     */
    private static Plugin plugin;

    /**
     * Singleton of the key handler
     */
    private static KeyHandler keyHandler = KeyHandler.getInstance();

    /**
     * Singleton of the quest registry
     */
    private static QuestRegistry questRegistry = QuestRegistry.getInstance();

    /**
     * Core class, to be instantiated by server.
     */
    public Core() {}

    @Override
    public void onEnable() {
        plugin = this;

        // call the playerquests game class
        PlayerQuests.getInstance();

        // initiate /playerquests command
        new Commandplayerquest();

        // Save the demo quest to the server
        saveResource("quest/templates/tina-says-hi-bonus.json", true);
    }

    /**
     * Mechanism to make the Plugin accessible for other classes.
     * @return the main plugin instance
     */
    public static Plugin getPlugin() {
        return plugin;
    }

    /**
     * Returns key handler used to access methods via key names.
     * @return the singleton instance of the plugin's key handler
     */
    public static KeyHandler getKeyHandler() {
        return keyHandler;
    }

    /**
     * Returns quest registry used for final quest products.
     * @return the singleton instance of the plugin's quest registry
     */
    public static QuestRegistry getQuestRegistry() {
        return questRegistry;
    }
}

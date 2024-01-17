package playerquests;

import org.bukkit.plugin.Plugin; // export the plugin for use elsewhere
import org.bukkit.plugin.java.JavaPlugin; // essential for initialising the plugin

import playerquests.client.chat.command.Commandplayerquest;

/**
 * Entry point for the plugin.
 * <ul>
 * <li>{@link #onLoad()}
 * <ul>
 * <li>Creates a test GUI.
 * </ul>
 * </ul> 
 */
public class Core extends JavaPlugin {

    /**
     * Singleton of the Plugin
     */
    private static Plugin plugin;

    /**
     * Singleton of the key handler
     */
    // private static KeyHandler keyHandler = KeyHandler.getInstance();

    /**
     * Core class, to be instantiated by server.
     */
    public Core() {}

    @Override
    public void onEnable() {
        plugin = this;

        // TODO: automatically initiate the commands
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
     * Returns key handler used to access methods with keys.
     * @return the singleton instance of the plugin's key handler
     */
    // public static KeyHandler getKeyHandler() {
    //     // TODO: refactor KeyHandler
    //     // return keyHandler;
    // }
}

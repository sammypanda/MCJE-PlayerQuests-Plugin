package playerquests;

import org.bukkit.plugin.Plugin; // export the plugin for use elsewhere
import org.bukkit.plugin.java.JavaPlugin; // essential for initialising the plugin
import playerquests.chat.command.Commandplayerquest; // used to show the plugin as a GUI
import playerquests.utils.KeyHandler; // special class for using keys to reference any method

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

    private static Plugin plugin; // singelton of the plugin
    private static KeyHandler keyHandler = KeyHandler.getInstance(); // singelton of the key handler

    /**
     * Core class, to be instantiated by server.
     */
    public Core() {}

    @Override
    public void onEnable() {
        plugin = this;

        // TODO: automatically initiate the commands
        new Commandplayerquest();
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
    public static KeyHandler getKeyHandler() {
        return keyHandler;
    }
}

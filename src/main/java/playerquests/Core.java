package playerquests;

import java.lang.reflect.InvocationTargetException;

import org.bstats.bukkit.Metrics; // plugin usage metrics
import org.bukkit.NamespacedKey; // custom object data/metadata
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.plugin.Plugin; // export the plugin for use elsewhere
import org.bukkit.plugin.java.JavaPlugin; // essential for initialising the plugin

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
public class Core extends JavaPlugin {

    /**
     * Singleton of the Plugin
     */
    private static Plugin plugin;

    /**
     * Singleton of the quest registry
     */
    private static QuestRegistry questRegistry = QuestRegistry.getInstance();

    /**
     * Where in the playerquests dir the quests are to be located.
     */
    private static String questsPath = "quests/";

    @Override
    public void onEnable() {
        Core.setPlugin(this);

        // call the playerquests game class
        PlayerQuests.getInstance();

        // initiate all commands
        PluginCommandYamlParser.parse(plugin).forEach(command -> {
            try {
                Class.forName("playerquests.client.chat.command.Command" + command.getName())
                    .getDeclaredConstructor()
                    .newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
            }
        });

        // mount bStats for some minimal usage info
        new Metrics(this, 22692);
    }

    /**
     * Set the singleton of the plugin for future access.
     * @param core this class.
     */
    private static void setPlugin(Core core) {
        plugin = core;
    }

    @Override
    public void onDisable() {
        PlayerQuests.getServerListener().onDisable();
    }

    /**
     * Mechanism to make the Plugin accessible for other classes.
     * @return the main plugin instance
     */
    public static Plugin getPlugin() {
        return plugin;
    }

    /**
     * Returns quest registry used for final quest products.
     * @return the singleton instance of the plugin's quest registry
     */
    public static QuestRegistry getQuestRegistry() {
        return questRegistry;
    }

    /**
     * Gets the ?/ path for quest resources.
     * - No preceding slash
     * - Includes proceeding slash
     * @return resource path for where quest JSON files are
     */
    public static String getQuestsPath() {
        return questsPath;
    }

    /**
     * Returns a NamspacedKey for the GUI persistent metadata.
     * @return a NamespacedKey for metadata specifying a candidate as part of a GUI
     */
    public static NamespacedKey getGUIKey() {
        return new NamespacedKey(plugin, "GUI"); // values: true, false
    }
}

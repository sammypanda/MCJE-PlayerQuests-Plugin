package playerquests;

import org.bukkit.plugin.java.JavaPlugin; // essential for initialising the plugin
import playerquests.chat.command.Commandplayerquest; // used to show the plugin as a GUI

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

    @Override
    public void onEnable() {
        // TODO: automatically initiate the commands
        new Commandplayerquest();
    }
}

package playerquests;

import org.bukkit.plugin.java.JavaPlugin; // essential for initialising the plugin
import playerquests.gui.GUILoader; // parses template JSON files into GUI types
import playerquests.chat.command.Commandplayerquest;
import playerquests.gui.GUI; // used to create and control GUIs

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

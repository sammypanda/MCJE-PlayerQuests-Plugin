package playerquests;

import org.bukkit.plugin.java.JavaPlugin; // essential for initialising the plugin
import org.bukkit.Bukkit; // required for getServer/Bukkit
import playerquests.gui.GUI; // used to create and control GUIs

public class Core extends JavaPlugin {

    @Override
    public void onEnable() {
    }

    @Override
    public void onLoad() {
	    getServer().broadcastMessage("Working (/dev/sammy)");

        // Testing
        GUI gui = new GUI(getServer().getPlayer("sammy0panda"));
        gui.open();
	}
}

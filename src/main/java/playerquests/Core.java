package playerquests;

import org.bukkit.plugin.java.JavaPlugin; // essential for initialising the plugin

public class Core extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().broadcastMessage("Working");
    }
}

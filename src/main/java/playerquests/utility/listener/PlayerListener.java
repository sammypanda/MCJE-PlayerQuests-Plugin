package playerquests.utility.listener;

import org.bukkit.Bukkit; // bukkit API
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener; // registering listening to Bukkit in-game events
import org.bukkit.event.player.PlayerJoinEvent;

import playerquests.Core; // accessing plugin singeltons

/**
 * Listens for player-related events to manage quest tracking and interactions.
 */
public class PlayerListener implements Listener {

    /**
     * Constructs a new {@code PlayerListener} and registers it with the Bukkit event system.
     * <p>
     * This constructor automatically registers the {@code PlayerListener} instance with the
     * Bukkit event system to listen for player events.
     * </p>
     */
    public PlayerListener() {
        Bukkit.getPluginManager().registerEvents(this, Core.getPlugin());
    }

    /**
     * Ran when a player joins the server.
     * @param event player join event data
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

    }
}

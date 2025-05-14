package playerquests.utility.listener;

import java.sql.DatabaseMetaData;

import org.bukkit.Bukkit; // bukkit API
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener; // registering listening to Bukkit in-game events
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.md_5.bungee.api.ChatColor;
import playerquests.Core; // accessing plugin singeltons
import playerquests.utility.ChatUtils;
import playerquests.utility.ChatUtils.MessageStyle;
import playerquests.utility.ChatUtils.MessageType;
import playerquests.utility.singleton.Database;

/**
 * Listens for player-related events to manage quest tracking and interactions.
 */
public class PlayerListener implements Listener {

    private boolean isFresh = false;

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
        Player player = event.getPlayer();

        // wait 30 ticks for join to finish
        Bukkit.getScheduler().runTaskLater(Core.getPlugin(), () -> {
            // create quester
            Core.getQuestRegistry().createQuester(player);

            // send intro message if is fresh instance
            if (this.isFresh) {
                ChatUtils.message(
                    String.format("Thank you for trying PlayerQuests, use %s/playerquests%s to get started. You can also find a tutorial here! %shttps://sammypanda.moe/docs/playerquests/v%s%s",
                        ChatColor.AQUA,
                        ChatColor.GRAY,
                        ChatColor.UNDERLINE,
                        Database.getInstance().getPluginVersion(),
                        ChatColor.RESET)
                    )
                    .player(player)
                    .style(MessageStyle.PRETTY)
                    .type(MessageType.NOTIF)
                    .send();
            }
        }, 60);
    }

    /**
     * Ran when a player leaves the server.
     * @param event player quit event data
     */
    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Core.getQuestRegistry().removeQuester(event.getPlayer());
    }

    public void isFresh() {
        this.isFresh = true;
    }
}

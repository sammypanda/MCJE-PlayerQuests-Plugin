package playerquests.utility.listener;

import org.bukkit.Bukkit; // bukkit API
import org.bukkit.event.EventHandler; // indicate that a method is wanting to handle an event
import org.bukkit.event.Listener; // registering listening to Bukkit in-game events
import org.bukkit.event.server.ServerLoadEvent; // when a server is reloaded or started up

import playerquests.Core; // accessing plugin singeltons
import playerquests.client.quest.QuestClient; // represents a quest player/quest tracking
import playerquests.utility.singleton.Database; // where game data is stored
import playerquests.utility.singleton.QuestRegistry; // place where quests are stored

/**
 * Listens out for all server-related events to inform 
 * where needed.
 */
public class ServerListener implements Listener {

    public ServerListener() {
        Bukkit.getPluginManager().registerEvents(this, Core.getPlugin());
    }

    @EventHandler
    public void onReload(ServerLoadEvent event) {
        if (!event.getType().equals(ServerLoadEvent.LoadType.RELOAD)) {
            return;
        }

        // for every user, create a quest client and add it to registry.
        // this enables a player to interact with and track quests.
        Bukkit.getServer().getOnlinePlayers().stream()
            .forEach(player -> {
                Database.addPlayer(player.getUniqueId());
                QuestClient quester = new QuestClient(player);
                QuestRegistry.getInstance().addQuester(quester);
            });
    }
}

package playerquests.utility.listener;

import org.bukkit.Bukkit; // bukkit API
import org.bukkit.event.EventHandler; // indicate that a method is wanting to handle an event
import org.bukkit.event.Listener; // registering listening to Bukkit in-game events
import org.bukkit.event.player.PlayerJoinEvent; // called when players have loaded into the game

import playerquests.Core; // accessing plugin singeltons
import playerquests.client.quest.QuestClient; // represents a quest player/quest tracking
import playerquests.utility.singleton.QuestRegistry;

/**
 * Listens out for all player-related events to inform 
 * where needed.
 */
public class PlayerListener implements Listener {

    public PlayerListener() {
        Bukkit.getPluginManager().registerEvents(this, Core.getPlugin());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // create a quest client and add it to registry.
        // this enables a player to interact with and track quests.
        QuestClient quester = new QuestClient(event.getPlayer());
        QuestRegistry.getInstance().addQuester(quester);
    }
}

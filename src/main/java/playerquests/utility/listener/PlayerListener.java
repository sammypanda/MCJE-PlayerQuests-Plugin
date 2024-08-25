package playerquests.utility.listener;

import org.bukkit.Bukkit; // bukkit API
import org.bukkit.event.EventHandler; // indicate that a method is wanting to handle an event
import org.bukkit.event.Listener; // registering listening to Bukkit in-game events
import org.bukkit.event.player.PlayerJoinEvent; // called when players have loaded into the game

import playerquests.Core; // accessing plugin singeltons
import playerquests.client.quest.QuestClient; // represents a quest player/quest tracking
import playerquests.utility.singleton.QuestRegistry; // where available quests are stored

/**
 * Listens for player-related events to manage quest tracking and interactions.
 * <p>
 * This class listens for player-related events, specifically when a player joins the game.
 * Upon player join, it creates a {@link QuestClient} for the player and registers it with the
 * {@link QuestRegistry}. This allows the player to interact with and track quests.
 * </p>
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
     * Handles the {@link PlayerJoinEvent} when a player joins the game.
     * 
     * When a player joins the game, this method is invoked to:
     * <ul>
     *     <li>Create a new {@link QuestClient} instance for the player.</li>
     *     <li>Add the {@code QuestClient} to the {@link QuestRegistry}.</li>
     * </ul>
     * This ensures that the player can interact with quests and track their progress.
     * 
     * @param event The {@code PlayerJoinEvent} that contains details about the player joining the game.
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // create a quest client and add it to registry.
        // this enables a player to interact with and track quests.
        QuestClient quester = new QuestClient(event.getPlayer());
        QuestRegistry.getInstance().addQuester(quester);
    }
}

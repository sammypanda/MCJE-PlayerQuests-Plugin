package playerquests.builder.quest.action.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import playerquests.Core;
import playerquests.builder.quest.action.QuestAction;
import playerquests.client.quest.QuestClient;

/**
 * An abstract class representing a listener for specific actions related to quests.
 * 
 * @param <A> the type of action that this listener can handle, which must extend {@link QuestAction}
 */
public abstract class ActionListener<A extends QuestAction> implements Listener {

    /**
     * The class that owns this listener.
     */
    protected final A action;

    /**
     * The player to listen to item gathering on.
     */
    protected final Player player;

    /**
     * The quest client for the player.
     */
    protected final QuestClient quester;

    /**
     * Constructs a new abstract action listener.
     *
     * @param action the quest action this listener is for.
     * @param quester the quest client for the player.
     */
    public ActionListener(A action, QuestClient quester) {
        this.action = action;
        this.player = quester.getPlayer();
        this.quester = quester;

        // register this listener
        Bukkit.getPluginManager().registerEvents(this, Core.getPlugin());

        // check quester inventory from beginning
        action.Check(quester, this);
    }

    /**
     * Unregister the listener.
     */
    public void close() {
        HandlerList.unregisterAll(this);
    }
}

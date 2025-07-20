package playerquests.builder.quest.action.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import playerquests.Core;
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.data.QuesterData;

/**
 * Triggers checking if the related action 
 * was successful.
 * @param <A> the type of action this listener is handling, must be a subclass of {@link QuestAction}
 * @see playerquests.builder.quest.action.QuestAction
 */
public abstract class ActionListener<A extends QuestAction> implements Listener {

    /**
     * The action that owns this listener.
     */
    protected final A action;

    /**
     * The quester that the action listener is for.
     */
    protected final QuesterData questerData;

    /**
     * Constructs a new abstract action listener.
     * @param action the quest action this listener is for.
     * @param questerData the quester the listener is for.
     */
    protected ActionListener(A action, QuesterData questerData) {
        this.action = action;
        this.questerData = questerData;
        questerData.addListener(action, this);

        // register the events
        Bukkit.getPluginManager().registerEvents(this, Core.getPlugin());
    }

    /**
     * Unregister the listener.
     * Used to stop listening when the action was 
     * a success.
     */
    public void close() {
        // unregister the events
        HandlerList.unregisterAll(this);
    }

    /**
     * If the passed in player passes listener player check
     * @param player the player to check
     * @return true if passed
     */
    protected boolean passedPlayerCheck(Player player) {
        return player.equals(this.questerData.getQuester().getPlayer());
    }
}

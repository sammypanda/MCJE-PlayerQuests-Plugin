package playerquests.builder.quest.action.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import playerquests.Core;
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.data.ActionData;

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
     * The quest client for the player.
     */
    protected final ActionData actionData;

    /**
     * Constructs a new abstract action listener.
     * @param action the quest action this listener is for.
     * @param actionData the action data for the current runtime.
     */
    public ActionListener(A action, ActionData actionData) {
        this.action = action;
        this.actionData = new ActionData(actionData.getQuester(), actionData.getPlayer(), actionData.getWorld(), actionData.getLocation(), this);

        // register the events
        Bukkit.getPluginManager().registerEvents(this, Core.getPlugin());

        // check if has already been completed
        action.check(actionData);
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
}

package playerquests.utility.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.data.QuesterData;

public class ActionCompletionEvent extends Event {
    
    /**
     * List of event handlers.
     */
    private static final HandlerList handlers = new HandlerList();

    /**
     * The data of the quester.
     */
    private final QuesterData questerData;

    /**
     * The action.
     */
    private final QuestAction<?,?> action;

    /**
     * Constructor for the when a player interacts with an NPC.
     * @param activeNPCs the action that was completed.
     * @param questerData the data of the quester who completed the action.
     */
    public ActionCompletionEvent(QuestAction<?,?> action, QuesterData questerData) {
        this.action = action;
        this.questerData = questerData;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Gets the quester that completed the action.
     * @return the data of the quester
     */
    public QuesterData getQuesterData() {
        return this.questerData;
    }

    /**
     * Gets the action the quester completed.
     * @return the action
     */
    public QuestAction<?,?> getAction() {
        return this.action;
    }

    /**
     * Get the hander list for this event.
     * @return the event handler list
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}

package playerquests.builder.quest.data;

import java.util.HashMap;

import org.bukkit.Location;

import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.action.listener.ActionListener;
import playerquests.client.quest.QuestClient;

/**
 * The data about the quester playing the action.
 */
public class QuesterData {

    /**
     * Useful for communicating action progress.
     * May not be present if some other client is in use.
     */
    private final QuestClient quester;

    /**
     * Useful for checking location/biome/other.
     * May not be present for something like 'has player completed x quest'.
     */
    private final Location location;

    /**
     * Useful for stopping listening to an action that has been completed.
     */
    private HashMap<QuestAction, ActionListener<?>> listeners = new HashMap<>();

    /**
     * The context of data useful for working with a QuestClient.
     * @param quester the QuestClient
     * @param location location of the quester
     */
    public QuesterData(QuestClient quester, Location location) {
        this.quester = quester;
        this.location = location;
    }

    /**
     * Returns the QuestClient associated with this action.
     * @return the quester
     */
    public QuestClient getQuester() {
        return quester;
    }

    /**
     * Returns the location this action is taking place in.
     * @return the location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Adds a listener to this quester data.
     * @param actionListener the listener that will trigger action checking
     * @return the passed in listener
     */
    public ActionListener<?> addListener(QuestAction action, ActionListener<?> actionListener) {
        this.listeners.put(action, actionListener);
        return actionListener;
    }

    /**
     * Gets a listener from this quester data by its type.
     * @param listenerType
     * @return
     */
    public ActionListener<?> getListener(QuestAction action) {
        return this.listeners.get(action);
    }
}

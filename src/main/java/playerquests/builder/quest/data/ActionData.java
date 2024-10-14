package playerquests.builder.quest.data;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import playerquests.builder.quest.action.listener.ActionListener;
import playerquests.client.quest.QuestClient;

/**
 * The entire game context that could possibly 
 * be needed for actions.
 * Especially useful for checking conditionals.
 */
public class ActionData {

    /**
     * Useful for communicating action progress.
     * May not be present if some other client is in use.
     */
    private final QuestClient quester;

    /**
     * Useful for checking player inventory and more.
     * May not be present for something like 'plants growing'.
     */
    private final Player player;

    /**
     * Useful for checking time, world environment and more
     * May not be present for something like 'has player completed x quest'.
     */
    private final World world;

    /**
     * Useful for checking location/biome/other.
     * May not be present for something like 'has player completed x quest'.
     */
    private final Location location;

    /**
     * Useful for pulling in values.
     */
    private final ActionListener<?> listener;

    /**
     * Constructor for providing action context.
     * Args (if you're sure they aren't needed) can be nullified.
     * @param quester the QuestClient associated with this action
     * @param player the player this action is happening with
     * @param world the world this action is taking place in
     * @param location the location this action is taking place in
     * @param listener the action listener for this action
     */
    public ActionData(
        QuestClient quester, 
        Player player, 
        World world, 
        Location location, 
        ActionListener<?> listener
    ) {
        this.quester = quester;
        this.player = player;
        this.world = world;
        this.location = location;
        this.listener = listener;
    }

    /**
     * Returns the QuestClient associated with this action.
     * @return the quester
     */
    public QuestClient getQuester() {
        return quester;
    }

    /**
     * Returns the player this action is happening with.
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the world this action is taking place in.
     * @return the world
     */
    public World getWorld() {
        return world;
    }

    /**
     * Returns the location this action is taking place in.
     * @return the location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Returns the action listener associated with this action.
     * @return the listener
     */
    public ActionListener<?> getListener() {
        return listener;
    }
}

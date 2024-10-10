package playerquests.builder.quest.data;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * The entire game context that could possibly 
 * be needed for actions.
 * Especially useful for checking conditionals.
 */
// TODO: add all possible dependencies for conditionals to constructor.
public class ActionData {

    /**
     * Useful for checking player inventory and more.
     * May not be present for something like 'plants growing'.
     */
    public final Player player;

    /**
     * Useful for checking time, world environment and more
     * May not be present for something like 'has player completed x quest'.
     */
    public final World world;

    /**
     * Useful for checking location/biome/other.
     * May not be present for something like 'has player completed x quest'.
     */
    public final Location location;

    /**
     * Constructor for providing action context.
     * Args (if you're sure they aren't needed) can be nullified.
     * @param player the player this action is happening with
     * @param world the world this action is taking place in
     * @param location the location this action is taking place in
     */
    public ActionData(Player player, World world, Location location) {
        this.player = player;
        this.world = world;
        this.location = location;
    }
}

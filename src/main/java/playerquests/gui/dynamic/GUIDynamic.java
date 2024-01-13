package playerquests.gui.dynamic;

import org.bukkit.entity.HumanEntity; // the type for the player

/**
 * Passes and handles any GUIs which include dynamic content.
 * <p>
 * Options:
 * <ul>
 * <li>myquests
 * </ul>
 */
public abstract class GUIDynamic {

    /**
     * The player who should see the dynamic GUI.
     */
    protected HumanEntity player;

    /**
     * Method to be overridden by each meta action class.
     */
    public abstract void execute();

    /**
     * Set the player who should see the dynamic GUI.
     * @param player the GUI viewer.
     */
    public void setPlayer(HumanEntity player) {
        this.player = player;
    }
}

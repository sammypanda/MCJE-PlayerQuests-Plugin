package playerquests.builder.gui.dynamic;

import playerquests.client.ClientDirector; // enables the dynamic GUI to retrieve info

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
    protected ClientDirector director;

    /**
     * Not intended to be created directly, is abstract class for dynamic GUI screens.
     * <p>
     * See docs/README for list of dynamic GUI screens.
     * @param player who should see the dynamic GUI.
    */
    public GUIDynamic(ClientDirector director) {
        this.director = director;
    }

    /**
     * Method to be overridden by each meta action class.
     */
    public abstract void execute();
}

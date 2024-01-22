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
     * The screen to return to on exit.
     */
    protected String previousScreen;

    /**
     * Not intended to be created directly, is abstract class for dynamic GUI screens.
     * <p>
     * See docs/README for list of dynamic GUI screens.
     * @param director how the dynamic gui controls the plugin
     * @param previousScreen the screen to go back to
    */
    public GUIDynamic(ClientDirector director, String previousScreen) {
        this.director = director;
        this.previousScreen = previousScreen;
    }

    /**
     * Method to be overridden by each meta action class.
     */
    public abstract void execute();
}

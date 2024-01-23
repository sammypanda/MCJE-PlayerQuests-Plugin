package playerquests.builder.gui.dynamic;

import playerquests.builder.gui.GUIBuilder; // creating the Dynamic GUI on the screen
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
     * If the setup has been ran
     */
    protected Boolean wasSetUp = false;

    /**
     * the GUI instance
     */
    protected GUIBuilder gui;

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
     * Setting important values for the GUI.
     */
    public void setUp() {
        this.wasSetUp = true;

        // close the previous GUI
        this.director.getGUI().getResult().close();

        // create the new GUI to show the quests in
        this.gui = new GUIBuilder(this.director, false);

        // to-be implemented set up processes
        setUp_custom();

        // send back to the execute() body to continue
        this.execute();
    }

    /**
     * Method to be overridden by each meta action class.
     */
    public void execute() {
        // run setup on first time
        if (!this.wasSetUp) {
            this.setUp();
            return;
        }

        // to-be implemented execute processes
        execute_custom();

        // replace current guibuilder with the new guibuilder
        this.director.setCurrentInstance(this.gui);

        // show the results
        if (!this.gui.getResult().isOpen()) {
            this.gui.getResult().open(); // open GUI for first time
        } else {
            this.gui.getResult().draw(); // draw on the already open GUI
        }
    }

    protected abstract void setUp_custom();

    protected abstract void execute_custom();
}

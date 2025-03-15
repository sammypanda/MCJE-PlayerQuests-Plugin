package playerquests.builder.gui.dynamic;

import java.util.function.Consumer;

import playerquests.builder.gui.GUIBuilder; // creating the Dynamic GUI on the screen
import playerquests.builder.gui.function.GUIFunction;
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
     * If the setup has been ran.
     */
    protected Boolean wasSetUp = false;

    /**
     * the GUI instance.
     */
    protected GUIBuilder gui;

    /**
     * The code to run when GUI finishes.
     */
    protected Consumer<GUIDynamic> onFinish;

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
        
        // get the new GUI to show the quests in
        this.gui = director.getGUI();
    }

    /**
     * Setting important values for the GUI.
     */
    public void setUp() {
        this.wasSetUp = true;

        // set the screen name on the builder
        this.gui.setScreenName(this.getClass().getSimpleName().split("Dynamic")[1]);

        // to-be implemented set up processes
        setUp_custom();

        // send back to the execute() body to continue
        this.execute();
    }

    /**
     * Method to be overridden by each gui function class.
     */
    public void execute() {
        // run setup on first time
        if (!this.wasSetUp) {
            this.setUp();
            return;
        }

        // to-be implemented execute processes
        execute_custom();

        // show the results
        if (!this.gui.getResult().isOpen()) {
            this.gui.getResult().open(); // open GUI for first time
        } else {
            this.gui.getResult().draw(); // draw on the already open GUI
        }
    }

    /**
     * For declaring values/variables.
     */
    protected abstract void setUp_custom();

    /**
     * For creating the GUI/functionality.
     */
    protected abstract void execute_custom();

    /**
     * For when everything is done in the GUI.
     */
    protected void finish() {
        if (this.onFinish != null) {
            onFinish.accept(this);
        }
    }

    /**
     * Sets the code to be executed when the function is finished.
     * <p>
     * This method allows for defining custom actions that should take place once the function has completed its execution.
     * </p>
     * 
     * @param onFinish The code to run when the function completes, implemented as a {@link Consumer} of {@link GUIFunction}.
     * @return The current instance of {@code GUIFunction} for method chaining.
     */
    public GUIDynamic onFinish(Consumer<GUIDynamic> onFinish) {
        this.onFinish = onFinish;
        return this;
    }

    /**
     * Refreshes the GUI screen.
     */
    public void refresh() {
        this.gui.clearSlots();
        this.execute();
    }
}

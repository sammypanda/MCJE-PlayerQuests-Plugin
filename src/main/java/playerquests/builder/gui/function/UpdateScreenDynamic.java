package playerquests.builder.gui.function;

import java.lang.reflect.InvocationTargetException; // thrown when the dynamic screen cannot be invoked
import java.util.ArrayList; // used to store the params for this meta action

import org.bukkit.entity.HumanEntity; // the player to show the dynamic GUI for

import playerquests.builder.gui.component.GUISlot; // holds information about the GUI slot
import playerquests.builder.gui.dynamic.GUIDynamic; // the dynamic GUI abstract base class
import playerquests.client.ClientDirector; // powers functionality for functions
import playerquests.utility.ChatUtils; // used to send error messages in-game

/**
 * Function for switching to a dynamic/generated GUI screen.
 */
public class UpdateScreenDynamic extends GUIFunction {

    /**
     * The screen that was come from.
     */
    protected String previousScreen;

    /**
     * If loading the dynamic screen had an error.
     */
    protected Boolean errored;

    /**
     * Not intended to be created directly.
     * <p>
     * Switches the GUI screen to a dynamic one.
     * @param params 1. pre-defined dynamic screen 2. screen to go back to
     * @param director to control the plugin
     * @param slot slot this function belongs to
     */
    public UpdateScreenDynamic(ArrayList<Object> params, ClientDirector director, GUISlot slot) {
        super(params, director, slot);
    }

    /**
     * Replaces an old GUI window with a new one as described by a template file.
     */
    @Override
    public void execute() {
        validateParams(this.params, String.class, String.class);

        // collect params
        String screenName = (String) params.get(0);
        String screenName_prev = (String) params.get(1);

        // start generating the GUI
        String frameTitle_previous = this.director.getGUI().getFrame().getTitle(); // get title of previous GUI
        this.previousScreen = screenName_prev; // set the previous screen for dynamic exit/back buttons.
        HumanEntity player = this.director.getPlayer(); // the player to send messages to

        try {
            // get the class from the dynamic screen name
            Class<?> screenClass = Class.forName("playerquests.builder.gui.dynamic.Dynamic" + screenName.toLowerCase());
            try {
                // instantiate the dynamic GUI class
                GUIDynamic guiDynamic = (GUIDynamic) screenClass
                    .getDeclaredConstructor(ClientDirector.class)
                    .newInstance(this.director);
                guiDynamic.execute(); // generate the dynamic GUI
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                this.errored = true;
                ChatUtils.sendError(player, "The " + screenName + " screen could not be initialised.", e);
            }
        } catch (ClassNotFoundException e) {
            this.errored = true;
            ChatUtils.sendError(player, "The " + screenName + " dynamic screen requested in the " + frameTitle_previous + " screen, is not valid.", e);
        }
    }
    
}

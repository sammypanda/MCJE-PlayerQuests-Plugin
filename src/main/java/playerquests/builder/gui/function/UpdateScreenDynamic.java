package playerquests.builder.gui.function;

import java.lang.reflect.InvocationTargetException; // thrown when the dynamic screen cannot be invoked
import java.util.ArrayList; // used to store the params for this meta action

import org.bukkit.entity.HumanEntity; // the player to show the dynamic GUI for

import playerquests.builder.gui.GUIBuilder; // controls and modifies the GUI
import playerquests.builder.gui.component.GUISlot; // holds information about the GUI slot
import playerquests.builder.gui.dynamic.GUIDynamic; // the dynamic GUI abstract base class
import playerquests.client.ClientDirector; // powers functionality for functions
import playerquests.product.GUI; // GUI product
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

        GUIBuilder guiBuilder_previous = this.director.getGUI(); // the builder for the gui
        GUI gui_previous = guiBuilder_previous.getResult(); // the gui product

        // close the existing GUI with the main thread so we can swap to a new one
        // Bukkit.getScheduler().runTask(Core.getPlugin(), () -> { // async request an event to occur
            if (gui_previous.isOpen()) { // if gui inventory view exists
                gui_previous.minimise(); // gently close the GUI in case needed later (fully closed on replace)
            }
        // });

        // collect params
        String screenName = (String) params.get(0);
        String screenName_prev = (String) params.get(1);

        // trigger generating the GUI
        String frameTitle_previous = guiBuilder_previous.getFrame().getTitle(); // get title of previous GUI
        this.previousScreen = screenName_prev; // set the previous screen for dynamic exit/back buttons.
        HumanEntity player = this.director.getPlayer();

        try {
            // get the class from the dynamic screen name
            Class<?> screenClass = Class.forName("playerquests.builder.gui.dynamic.Dynamic" + screenName.toLowerCase());
            try {
                // instantiate the dynamic GUI class
                GUIDynamic guiDynamic = (GUIDynamic) screenClass.getDeclaredConstructor().newInstance(player);
                guiDynamic.execute(); // generate the dynamic GUI
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                this.errored = true;
                ChatUtils.sendError(player, "The " + screenName + " screen could not be initialised. ");
            }
        } catch (ClassNotFoundException e) {
            this.errored = true;
            ChatUtils.sendError(player, "The " + screenName + " dynamic screen requested in the " + frameTitle_previous + " screen, is not valid. ");
        }

        if (errored) {
            gui_previous.open(); // go back to the old gui
        }
    }
    
}

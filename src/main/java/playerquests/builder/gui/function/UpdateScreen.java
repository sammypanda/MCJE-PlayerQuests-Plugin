package playerquests.builder.gui.function;

import java.io.IOException; // thrown of replacement GUI screen template file not found
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList; // used to store the params for this meta action

import playerquests.builder.gui.GUIBuilder; // used to parse the GUI screen template
import playerquests.builder.gui.component.GUISlot; // holds information about the GUI slot
import playerquests.builder.gui.dynamic.GUIDynamic;
import playerquests.client.ClientDirector; // powers functionality for functions
import playerquests.utility.ChatUtils; // used to send error message in-game

/**
 * Changes the GUI screen to a different template file.
 */
public class UpdateScreen extends GUIFunction {

    /**
     * The actual exception thrown
     */
    private Exception exception;

    /**
     * Next GUI screen to create into
     */
    GUIBuilder guiBuilder;

    /**
     * The previous GUI screen
     */
    private GUIBuilder screen_previous;

    private String screenName_previous;

    private String screenName;

    private Class<?> screenName_dynamic;

    /**
     * The error message to send
     */
    private String error;



    /**
     * Not intended to be created directly.
     * <p>
     * Switches the GUI screen to another template.
     * @param params 1. the template file
     * @param director to control the plugin
     * @param slot slot this function belongs to
     */
    public UpdateScreen(ArrayList<Object> params, ClientDirector director, GUISlot slot) {
        super(params, director, slot);
    }

    /**
     * Replaces an old GUI window with a new one as described by a template file.
     */
    @Override
    public void execute() {
        validateParams(this.params, String.class);

        this.director.getGUI().getResult().close();

        // Create the next GUI
        this.guiBuilder = new GUIBuilder(this.director, false); // do not replace as current

        // collect params
        this.screenName = (String) params.get(0);

        // dynamic GUI path
        this.screenName_dynamic = this.getDynamicClassFromName(screenName.toLowerCase());

        // try screenName as dynamic GUI, otherwise as a template GUI
        if (this.screenName_dynamic != null) { // if a dynamic screen of this name exists
            this.fromDynamic();
        } else { // check if template file of screenName exists
            this.error = null;
            this.fromFile();
        }

        if (this.error != null) {
            ChatUtils.sendError(director.getPlayer(), this.error, this.exception);
            return;
        }

        this.slot.executeNext(this.director.getPlayer()); // run the next function
    }

    /**
     * Try to load the GUI screen from file
     * @return if errored
     */
    private void fromFile() {
        try {
            this.guiBuilder.load(this.screenName); // load this next screen
            this.director.setCurrentInstance(this.guiBuilder);
            this.director.getGUI().getResult().open(); // open the next GUI
        } catch (IOException e) {
            this.error = "Could not load GUI screen at: " + screenName + ", will cancel rest of functions";
            this.exception = e;
        }
    }

    /**
     * Try to load the GUI screen from dynamic GUIs
     * @return if errored
     */
    private void fromDynamic() {
        try {
            // instantiate the dynamic GUI class
            GUIDynamic guiDynamic = (GUIDynamic) this.screenName_dynamic
                .getDeclaredConstructor(ClientDirector.class, String.class)
                .newInstance(this.director, "main");
            guiDynamic.execute(); // generate the dynamic GUI
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            this.exception = e;
            this.error = "The " + screenName + " screen could not be initialised.";
        }
    }

    /**
     * Check if a dynamic class exists by name
     * @param className the dynamic GUI class
     * @return the Dynamic GUI class type
     */
    private Class<?> getDynamicClassFromName(String name) {
        try {
            // get the class from the dynamic screen name
            return Class.forName("playerquests.builder.gui.dynamic.Dynamic" + name);
        } catch (ClassNotFoundException e) {
            this.error = "The " + screenName + " dynamic screen requested in the " + screenName_previous + " screen, is not valid.";
            this.exception = e;
        }

        return null;
    }

}

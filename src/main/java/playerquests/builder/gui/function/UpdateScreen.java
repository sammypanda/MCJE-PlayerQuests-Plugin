package playerquests.builder.gui.function;

import java.lang.reflect.InvocationTargetException;
import java.util.List; // used to store the params for this gui function

import playerquests.builder.gui.GUIBuilder; // used to work with the GUI screen
import playerquests.builder.gui.dynamic.GUIDynamic;
import playerquests.client.ClientDirector; // powers functionality for functions
import playerquests.utility.ChatUtils; // used to send error message in-game
import playerquests.utility.ChatUtils.MessageTarget;
import playerquests.utility.ChatUtils.MessageType;
import playerquests.utility.PluginUtils; // used to validate function params

/**
 * Changes the GUI screen to a different GUI.
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
     * The previous GUI screen name
     */
    private String screenName_previous;

    /**
     * The screen name we are updating to.
     */
    private String screenName;

    /**
     * The screen name if updating to a dynamic GUI.
     */
    private Class<?> screenName_dynamic;

    /**
     * The error message to send
     */
    private String error;

    /**
     * The GUIDynamic which may be found.
     */
    private GUIDynamic dynamicGUI;

    /**
     * Switches the GUI screen to another GUI.
     * @param params 1. the name of the GUI (with 'dynamic' omitted)
     * @param director to control the plugin
     */
    public UpdateScreen(List<Object> params, ClientDirector director) {
        super(params, director);
    }

    /**
     * Replaces an old GUI window with a new one.
     */
    @Override
    public void execute() {
        try {
            PluginUtils.validateParams(this.params, String.class);
        } catch (IllegalArgumentException e) {
            this.errored = true;
            ChatUtils.message(e.getMessage())
                .player(this.director.getPlayer())
                .type(MessageType.ERROR)
                .send();
        }

        this.director.getGUI().getResult().close();

        // Create the next GUI
        this.guiBuilder = new GUIBuilder(this.director, false); // do not replace as current

        // collect params
        this.screenName = (String) params.get(0);

        // dynamic GUI path
        List<String> screenNames_previous = this.director.getGUI().getPreviousScreens();
        this.screenName_dynamic = this.getDynamicClassFromName(screenName.toLowerCase());
        this.screenName_previous = this.director.getGUI().getScreenName(); // predicted previous screen

        // if where we are heading is not the same as where we came from (not going back)
        // NOTE: using contains is the lazy way of doing so
        if (screenNames_previous.contains(this.screenName)) {
            screenNames_previous.remove(screenNames_previous.size() - 1); // if going backwards
        } else if ( this.screenName_previous != null && ! this.screenName_previous.equals(this.screenName) ) { // as long as not doubling up
            screenNames_previous.add(this.screenName_previous); // if going forwards
        }
        
        // set the previous screens in next GUI
        this.guiBuilder.setPreviousScreens(screenNames_previous);

        // swap to new GUI
        this.director.setCurrentInstance(this.guiBuilder);

        // replace the predicted previous screen name with the tracked one (if available)
        if (screenNames_previous.size() >= 1) {
            this.screenName_previous = screenNames_previous.get(screenNames_previous.size() - 1);
        }

        // try screenName as dynamic GUI, otherwise error
        if (this.screenName_dynamic != null) { // if a dynamic screen of this name exists
            this.fromDynamic();
        } else { // report could not load the GUI
            this.error = "Could not load dynamic GUI (static GUI's are deprecated)";
        }

        if (this.error != null) {
            ChatUtils.message(this.error)
                .player(this.director.getPlayer())
                .type(MessageType.ERROR)
                .send();
            ChatUtils.message(this.exception.toString())
                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .send();
            return;
        }

        this.finished(); // running onFinish code
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
                .newInstance(this.director, this.screenName_previous);
            guiDynamic.execute(); // generate the dynamic GUI
            this.dynamicGUI = guiDynamic;
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

    /**
     * Get the dynamic GUI, null if this update screen is not 
     * updating to a dynamic GUI.
     * @return the dynamic GUI instance
     */
    public GUIDynamic getDynamicGUI() {
        return this.dynamicGUI;
    }

    /**
     * Retrieves the name of the previous screen before the update.
     * @return The name of the previous screen.
     */
    public String getPreviousScreen() {
        return this.screenName_previous;
    }
}

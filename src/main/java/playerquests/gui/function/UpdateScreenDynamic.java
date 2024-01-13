package playerquests.gui.function;

import java.lang.reflect.InvocationTargetException; // occurs if a dynamic GUI class cannot be invoked

import org.bukkit.entity.HumanEntity; // the type for the player

import playerquests.gui.dynamic.GUIDynamic; // the type the actual dynamic GUIs extend

public class UpdateScreenDynamic extends GUIFunction {

    /**
     * Replaces an old GUI window with a new one as described by a template file.
     */
    @Override
    public void execute() {
        validateParams(this.params, String.class, String.class);

        // collect information from old gui before closing it
        HumanEntity previousViewer = this.player;
        String previousTitle = this.parentGui.getTitle();

        // move on from the existing GUI so we can swap to a new one
        this.parentGui.close();

        // collect params
        String screenName = (String) params.get(0);
        String screenNamePrev = (String) params.get(1);

        // trigger generating the GUI
        try {
            // get the class from the dynamic screen name
            Class<?> screenClass = Class.forName("playerquests.gui.dynamic.Dynamic" + screenName.toLowerCase());
            try {
                // instantiate the dynamic GUI class
                GUIDynamic guiDynamic = (GUIDynamic) screenClass.getDeclaredConstructor().newInstance();
                guiDynamic.setPlayer(previousViewer);
                guiDynamic.execute(); // generate the dynamic GUI
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                throw new RuntimeException("The " + screenName + " could not be initialised. ", e);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("The " + screenName + " dynamic screen requested in the " + previousTitle + " screen, is not valid. ", e);
        }
    }
}

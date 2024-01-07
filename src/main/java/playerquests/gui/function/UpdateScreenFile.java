package playerquests.gui.function;

import org.bukkit.entity.HumanEntity; // used to collect the viewer from the previous gui screen

import playerquests.gui.GUI; // used to open a new GUI
import playerquests.gui.GUILoader; // used to parse the GUI screen template

/**
 * Meta action to swiftly move to another GUI screen based on another template JSON file. 
 */
public class UpdateScreenFile extends GUIFunction {

    /**
     * Replaces an old GUI window with a new one as described by a template file.
     */
    @Override
    public void execute() {
        validateParams(this.params, String.class);

        // collect information from old gui before closing it
        HumanEntity previousViewer = this.parentGui.getViewer();

        this.parentGui.close(); // move on from the existing GUI so we can swap to a new one

        // collect params
        String fileName = (String) params.get(0);

        // open the new GUI
        GUILoader guiLoader = new GUILoader(previousViewer);
        GUI gui = guiLoader.load(fileName);
        gui.open();
    }
}

package playerquests.gui.function;

import org.bukkit.Bukkit; // to access the scheduler
import org.bukkit.entity.HumanEntity; // used to collect the viewer from the previous gui screen

import playerquests.Core; // to access the Plugin singleton
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
        HumanEntity previousViewer = this.player;

        // close the existing GUI with the main thread so we can swap to a new one
        Bukkit.getScheduler().runTask(Core.getPlugin(), () -> { // async request an event to occur
            if (this.parentGui.isOpen()) { this.parentGui.close(); }
        });

        // collect params
        String fileName = (String) params.get(0);

        // open the new GUI on the main thread
        Bukkit.getScheduler().runTask(Core.getPlugin(), () -> { // async request an event to occur
            GUILoader guiLoader = new GUILoader(previousViewer);
            GUI gui = guiLoader.load(fileName);
            gui.open();

            this.parentSlot.executeNext(previousViewer); // run the next function
        });
    }
}

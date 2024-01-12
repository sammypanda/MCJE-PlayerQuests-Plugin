package playerquests.gui.function;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;

import playerquests.Core;

/**
 * Meta action to swiftly move to another GUI screen based on another template JSON file. 
 */
public class ChatPrompt extends GUIFunction {

    /**
     * Replaces an old GUI window with a new one as described by a template file.
     */
    @Override
    public void execute() {
        validateParams(this.params, String.class, String.class);

        String prompt = (String) params.get(0);
        String key = (String) params.get(1);
        
        // TODO: get actual value from capturing chat prompt
        String value = "[mock user input]";

        // collect information from old gui before closing it
        HumanEntity previousViewer = this.player;

        // TODO: Uncomment after testing:
        // this.parentGui.close(); // move on from the existing GUI so we can swap to a new one

        previousViewer.sendMessage("opening chat prompt (unimplemented)"); // show that we reached this point

        Core.getKeyHandler().setValue(this.parentGui, key, value);
    }
}

package playerquests.builder.gui.function;

import java.util.ArrayList; // array type of list

import playerquests.builder.gui.component.GUISlot; // GUI button
import playerquests.client.ClientDirector; // controls the plugin

public class SelectBlock extends GUIFunction {

    /**
     * Provides input as a user selected block.
     * <ul>
     * <li>By hitting the physical block
     * <li>By selecting the block in an inventory
     * </ul>
     * @param params 1. the prompt to show the user 2. list of blacklisted blocks
     * @param director to set values
     * @param slot slot this function belongs to
     */
    public SelectBlock(ArrayList<Object> params, ClientDirector director, GUISlot slot) {
        super(params, director, slot);
    }

    @Override
    public void execute() {
        this.finished(); // running onFinish code

        this.slot.executeNext(this.director.getPlayer()); // run the next function
    }
    
}

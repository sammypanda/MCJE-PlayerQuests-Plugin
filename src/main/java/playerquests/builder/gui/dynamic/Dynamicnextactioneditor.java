package playerquests.builder.gui.dynamic;

import playerquests.builder.quest.data.ActionData;
import playerquests.client.ClientDirector;

/**
 * Shows a GUI used for selecting actions which come next.
 */
public class Dynamicnextactioneditor extends GUIDynamic {

    /**
     * Creates a dynamic GUI to select the next actions.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicnextactioneditor(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        this.director.getCurrentInstance(ActionData.class);
    }

    @Override
    protected void execute_custom() {}
    
}

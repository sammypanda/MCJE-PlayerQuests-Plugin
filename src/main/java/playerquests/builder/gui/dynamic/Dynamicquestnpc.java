package playerquests.builder.gui.dynamic;

import playerquests.client.ClientDirector; // for controlling the plugin

/**
 * Creates a dynamic GUI for editing a quest NPC.
 */
public class Dynamicquestnpc extends GUIDynamic {

    /**
     * Creates a dynamic GUI for editing a quest NPC.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicquestnpc(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        System.err.println("'questnpc' screen unimplemented");
    }

    @Override
    protected void execute_custom() {
        this.generatePage();
    }

    private void generatePage() {    
    }
    
}

package playerquests.builder.gui.dynamic;

import playerquests.client.ClientDirector; // for controlling the plugin

public class Dynamicquestnpcs extends GUIDynamic {

    public Dynamicquestnpcs(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {}

    @Override
    protected void execute_custom() {
        throw new UnsupportedOperationException("Unimplemented screen 'questnpcs'");
    }
    
}

package playerquests.builder.gui.dynamic;

import playerquests.builder.gui.component.GUIFrame;
import playerquests.client.ClientDirector;

public class Dynamicselectconnection extends GUIDynamic {

    public Dynamicselectconnection(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
    }

    @Override
    protected void execute_custom() {
        GUIFrame frame = gui.getFrame();
        
        frame.setTitle("Select Connection");
        frame.setSize(27);
    }
    
}

package playerquests.builder.gui.dynamic;

import playerquests.builder.quest.data.ConnectionsData;
import playerquests.client.ClientDirector;

public class Dynamicconnectioneditor extends GUIDynamic {

    /**
     * The connections we are editing.
     */
    ConnectionsData connections;

    public Dynamicconnectioneditor(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        this.connections = (ConnectionsData) this.director.getCurrentInstance(ConnectionsData.class);
    }

    @Override
    protected void execute_custom() {
        this.director.getPlayer().sendMessage(this.connections.toString());
    }
    
}

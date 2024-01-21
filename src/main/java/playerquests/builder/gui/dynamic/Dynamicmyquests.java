package playerquests.builder.gui.dynamic;

import playerquests.client.ClientDirector; // enables controlling the plugin
import playerquests.utility.ChatUtils;

public class Dynamicmyquests extends GUIDynamic {

    /**
     * Creates a dynamic GUI with a list of 'my quests'.
     * @param director director for the client
     */
    public Dynamicmyquests(ClientDirector director) {
        super(director);
    }

    @Override
    public void execute() {
        // TODO: implement myquests dynamic gui.
        ChatUtils.sendError(this.director.getPlayer(), "Dynamic 'my quests' screen not yet implemented.");
    }
    
}

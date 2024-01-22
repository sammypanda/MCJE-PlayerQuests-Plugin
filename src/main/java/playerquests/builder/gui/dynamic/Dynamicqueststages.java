package playerquests.builder.gui.dynamic;

import playerquests.builder.quest.QuestBuilder; // for managing the quest
import playerquests.client.ClientDirector; // for controlling the plugin

/**
 * Shows a dynamic GUI listing the players current quest stages.
 */
public class Dynamicqueststages extends GUIDynamic {

    /**
     * Creates a dynamic GUI with a list of the current quests stages.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicqueststages(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    public void execute() {
        System.out.println(this.director.getCurrentInstance(QuestBuilder.class));
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }
    
}

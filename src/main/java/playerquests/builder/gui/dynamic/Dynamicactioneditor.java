package playerquests.builder.gui.dynamic;

import playerquests.builder.quest.action.QuestAction;
import playerquests.client.ClientDirector;

/**
 * Shows a GUI used for editing a quest action.
 */
public class Dynamicactioneditor extends GUIDynamic {

    /**
     * The action being edited.
     */
    private QuestAction action;

    /**
     * Creates a dynamic GUI to edit a quest stage action.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicactioneditor(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        this.action = (QuestAction) this.director.getCurrentInstance(QuestAction.class);
    }

    @Override
    protected void execute_custom() {
        // set frame title/style
        this.gui.getFrame().setTitle(String.format("%s Editor", this.action.getID()))
                           .setSize(18);
    }
}

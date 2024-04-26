package playerquests.builder.gui.dynamic;

import playerquests.builder.gui.component.GUIFrame; // the outer frame of the GUI window
import playerquests.builder.quest.QuestBuilder; // for quest management
import playerquests.client.ClientDirector; // how a player client interacts with the plugin

public class Dynamicmyquest extends GUIDynamic {

    /**
     * The current quest
     */
    QuestBuilder questBuilder;

    public Dynamicmyquest(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        // retrieve the current quest from the client director
        this.questBuilder = (QuestBuilder) this.director.getCurrentInstance(QuestBuilder.class);
    }

    @Override
    protected void execute_custom() {
        GUIFrame guiFrame = this.gui.getFrame();

        // set the GUI window title
        String questTitle = this.questBuilder.getTitle();
        Integer questTitleLimit = 12;
        guiFrame.setTitle(
            String.format(
                "Quest: %s", 
                // obscure quest title in GUI frame title with '...' if character limit reached:
                questTitle.length() > questTitleLimit - 1 ? questTitle.substring(0, questTitleLimit) + "..." : questTitle
            )
        );
    }
    
}

package playerquests.builder.gui.dynamic;

import playerquests.builder.gui.component.GUIFrame; // the outer frame of the GUI
import playerquests.client.ClientDirector; // backend for a player client
import playerquests.product.Quest; // quest product to view

public class Dynamictheirquest extends GUIDynamic {

    /**
     * The current quest
     */
    Quest questProduct;

    public Dynamictheirquest(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        // retrieve the current quest from the client director
        this.questProduct = (Quest) this.director.getCurrentInstance(Quest.class);
    }

    @Override
    protected void execute_custom() {
        GUIFrame guiFrame = this.gui.getFrame();

        // set the GUI window title
        String questTitle = this.questProduct.getTitle();
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

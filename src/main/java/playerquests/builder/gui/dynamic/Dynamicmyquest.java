package playerquests.builder.gui.dynamic;

import java.util.ArrayList; // list array type
import java.util.Arrays; // generic arrays type

import playerquests.builder.gui.component.GUIFrame; // the outer frame of the GUI window
import playerquests.builder.gui.component.GUISlot; // inventory slots representing GUI buttons
import playerquests.builder.gui.function.UpdateScreen; // GUI function to change GUI
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

        // create back button
        new GUISlot(gui, 1)
            .setItem("OAK_DOOR")
            .setLabel("Back")
            .addFunction(new UpdateScreen(
                new ArrayList<>(Arrays.asList(previousScreen)), 
                director
            ));

        // create edit button
        new GUISlot(gui, 3)
            .setItem("WRITABLE_BOOK")
            .setLabel("Edit")
            .addFunction(new UpdateScreen(
                new ArrayList<>(Arrays.asList("questeditor")), 
                director
            ));
    }
    
}

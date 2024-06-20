package playerquests.builder.gui.dynamic;

import java.util.ArrayList; // list array type
import java.util.Arrays; // generic array type

import com.fasterxml.jackson.core.JsonProcessingException;

import playerquests.builder.gui.component.GUIFrame; // the outer frame of the GUI
import playerquests.builder.gui.component.GUISlot; // creates a GUI button
import playerquests.builder.gui.function.UpdateScreen; // changes the GUI to another
import playerquests.builder.quest.QuestBuilder; // for creating and modifying quests
import playerquests.client.ClientDirector; // backend for a player client
import playerquests.product.Quest; // quest product to view
import playerquests.utility.ChatUtils;

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
                "Quest: %s (Shared)", 
                // obscure quest title in GUI frame title with '...' if character limit reached:
                questTitle.length() > questTitleLimit - 1 ? questTitle.substring(0, questTitleLimit) + "..." : questTitle
            )
        );

        // create back button
        new GUISlot(gui, 1)
            .setLabel("Back")
            .setItem("OAK_DOOR")
            .addFunction(new UpdateScreen(
                new ArrayList<>(Arrays.asList(previousScreen)), 
                director
            ));
        
        // create clone button
        new GUISlot(gui, 3)
            .setLabel("Clone")
            .setItem("CHAIN")
            .onClick(() -> {
                // create a quest builder from the current quest
                // (using setDirector sets it as the current quest in director)
                try {
                    new QuestBuilder(
                        this.director,
                        Quest.fromTemplateString(this.questProduct.toTemplateString())
                    ).setDirector(this.director);
                } catch (JsonProcessingException e) {
                    ChatUtils.sendError(this.director.getPlayer(), "Could not clone this quest, the quest template is invalid.");
                }

                // enter editing mode for cloned quest
                new UpdateScreen(
                    new ArrayList<>(Arrays.asList("questeditor")), 
                    director
                ).execute();
            });
    }
    
}

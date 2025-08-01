package playerquests.builder.gui.dynamic;

import java.util.Arrays; // generic array type

import org.bukkit.Material;

import com.fasterxml.jackson.core.JsonProcessingException;

import playerquests.builder.gui.component.GUIFrame; // the outer frame of the GUI
import playerquests.builder.gui.component.GUISlot; // creates a GUI button
import playerquests.builder.gui.function.UpdateScreen; // changes the GUI to another
import playerquests.builder.quest.QuestBuilder; // for creating and modifying quests
import playerquests.client.ClientDirector; // backend for a player client
import playerquests.product.Quest; // quest product to view
import playerquests.utility.ChatUtils;
import playerquests.utility.ChatUtils.MessageType;

/**
 * A dynamic GUI screen for viewing and cloning quests.
 * <p>
 * This screen displays information about a shared quest and provides options to go back
 * or clone the quest for further editing. The clone functionality creates a copy of the
 * current quest using its JSON string.
 * </p>
 */
public class Dynamictheirquest extends GUIDynamic {

    /**
     * The current quest displayed in this screen.
     */
    Quest questProduct;

    /**
     * Constructs a new {@code Dynamictheirquest} instance.
     * @param director the client director that manages GUI interactions.
     * @param previousScreen the identifier of the previous screen to navigate back to.
     */
    public Dynamictheirquest(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setupCustom() {
        // retrieve the current quest from the client director
        this.questProduct = (Quest) this.director.getCurrentInstance(Quest.class);
    }

    @Override
    protected void executeCustom() {
        GUIFrame guiFrame = this.gui.getFrame();

        // set the GUI window title
        String questTitle = this.questProduct.getTitle();
        guiFrame.setTitle(
            String.format(
                "Quest: %s (Shared)", 
                // obscure quest title in GUI frame title with '...' if character limit reached:
                ChatUtils.shortenString(questTitle, 18)
            )
        );

        // create back button
        new GUISlot(gui, 1)
            .setLabel("Back")
            .setItem(Material.OAK_DOOR)
            .addFunction(new UpdateScreen(
                Arrays.asList(previousScreen), 
                director
            ));
        
        // create clone button
        new GUISlot(gui, 3)
            .setLabel("Clone")
            .setItem(Material.CHAIN)
            .onClick(() -> {
                // create a quest builder from the current quest
                // (using setDirector sets it as the current quest in director)
                try {
                    new QuestBuilder(
                        this.director,
                        Quest.fromJSONString(this.questProduct.toJSONString())
                    ).setDirector(this.director);
                } catch (JsonProcessingException e) {
                    ChatUtils.message("Could not clone this quest, the quest is invalid.")
                        .player(this.director.getPlayer())
                        .type(MessageType.ERROR)
                        .send();
                }

                // enter editing mode for cloned quest
                new UpdateScreen(
                    Arrays.asList("questeditor"), 
                    director
                ).execute();
            });
    }
}

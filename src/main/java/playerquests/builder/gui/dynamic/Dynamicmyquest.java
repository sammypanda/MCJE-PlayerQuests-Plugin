package playerquests.builder.gui.dynamic;

import java.util.ArrayList; // list array type
import java.util.Arrays; // generic arrays type

import playerquests.builder.gui.component.GUIFrame; // the outer frame of the GUI window
import playerquests.builder.gui.component.GUISlot; // inventory slots representing GUI buttons
import playerquests.builder.gui.function.UpdateScreen; // GUI function to change GUI
import playerquests.builder.quest.QuestBuilder; // instantiating a builder for the quest (for editing an existing quest)
import playerquests.client.ClientDirector; // how a player client interacts with the plugin
import playerquests.product.Quest; // complete quest objects
import playerquests.utility.singleton.QuestRegistry; // tracking quests/questers

/**
 * A dynamic GUI screen for displaying and managing a specific quest.
 * <p>
 * This screen allows users to view the details of a quest, edit the quest, delete the quest with confirmation,
 * and toggle the quest's active status. The layout includes buttons for each of these actions.
 * </p>
 */
public class Dynamicmyquest extends GUIDynamic {

    /**
     * The quest product
     */
    Quest quest;

    /**
     * Flag indicating whether the delete confirmation button should be shown.
     * <p>
     * If {@code true}, the button will prompt for confirmation before deleting the quest.
     * If {@code false}, the button will initiate the delete confirmation process.
     * </p>
     */
    Boolean confirm_delete = false;

    /**
     * Constructs a new {@code Dynamicmyquest} instance.
     * @param director the client director that manages the GUI and interactions.
     * @param previousScreen the identifier of the previous screen to navigate back to.
     */
    public Dynamicmyquest(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        // retrieve the current quest from the client director
        this.quest = (Quest) this.director.getCurrentInstance(Quest.class);
    }

    @Override
    protected void execute_custom() {
        GUIFrame guiFrame = this.gui.getFrame();

        // set the GUI window title
        String questTitle = this.quest.getTitle();
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
            .onClick(() -> {
                // create a quest builder (for editing)
                director.setCurrentInstance(new QuestBuilder(director, this.quest));

                // open the editor
                new UpdateScreen(
                    new ArrayList<>(Arrays.asList("questeditor")), 
                    director
                ).execute();
            });

        // create remove quest button (with confirmation check)
        if (confirm_delete.equals(false)) {
            new GUISlot(gui, 8)
                .setItem("RED_DYE")
                .setLabel("Delete")
                .onClick(() -> {
                    this.confirm_delete = true;
                    this.execute();
                });
        } else {
            new GUISlot(gui, 8)
                .setItem("RED_WOOL")
                .setLabel("Delete (Confirm)")
                .onClick(() -> {
                    // delete the quest
                    Boolean deleted = QuestRegistry.getInstance().delete(quest, true);

                    // don't continue if not deleted
                    if (!deleted) {
                        return;
                    }

                    // go back to previous screen
                    new UpdateScreen(
                        new ArrayList<>(Arrays.asList(previousScreen)), 
                        director
                    ).execute();
                });
        }

        // create quest toggle button
        Boolean isToggled = quest.isToggled();
        
        new GUISlot(gui, 9)
            .setItem(isToggled ? "GREEN_STAINED_GLASS_PANE" : "GRAY_STAINED_GLASS_PANE")
            .setLabel(isToggled ? "Toggle Off" : "Toggle On")
            .onClick(() -> {
                quest.toggle();
                this.execute(); // refresh UI
            });
    }
    
}

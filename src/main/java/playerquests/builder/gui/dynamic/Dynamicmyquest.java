package playerquests.builder.gui.dynamic;

import java.util.Arrays; // generic arrays type

import org.bukkit.Material;

import playerquests.builder.gui.component.GUIFrame; // the outer frame of the GUI window
import playerquests.builder.gui.component.GUISlot; // inventory slots representing GUI buttons
import playerquests.builder.gui.function.UpdateScreen; // GUI function to change GUI
import playerquests.builder.quest.QuestBuilder; // instantiating a builder for the quest (for editing an existing quest)
import playerquests.client.ClientDirector; // how a player client interacts with the plugin
import playerquests.product.Quest; // complete quest objects
import playerquests.utility.ChatUtils;
import playerquests.utility.singleton.QuestRegistry; // tracking quests/questers

/**
 * A dynamic GUI screen for displaying and managing a specific quest.
 * <p>
 * This screen allows users to view the details of a quest, edit the quest, delete the quest with confirmation,
 * and toggle the quest's active status.
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
    Boolean confirmDeletion = false;

    /**
     * Constructs a new {@code Dynamicmyquest} instance.
     * @param director the client director that manages the GUI and interactions.
     * @param previousScreen the identifier of the previous screen to navigate back to.
     */
    public Dynamicmyquest(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setupCustom() {
        // retrieve the current quest from the client director
        this.quest = (Quest) this.director.getCurrentInstance(Quest.class);
    }

    @Override
    protected void executeCustom() {
        GUIFrame guiFrame = this.gui.getFrame();

        // set the GUI window title
        String questTitle = this.quest.getTitle();
        guiFrame.setTitle(
            String.format(
                "Quest: %s", 
                // obscure quest title in GUI frame title with '...' if character limit reached:
                ChatUtils.shortenString(questTitle, 20)
            )
        );

        // create back button
        new GUISlot(gui, 1)
            .setItem(Material.OAK_DOOR)
            .setLabel("Back")
            .addFunction(new UpdateScreen(
                Arrays.asList(previousScreen), 
                director
            ));

        // create edit button
        new GUISlot(gui, 3)
            .setItem(Material.WRITABLE_BOOK)
            .setLabel("Edit")
            .onClick(() -> {
                // create a quest builder (for editing)
                director.setCurrentInstance(new QuestBuilder(director, this.quest));

                // open the editor
                new UpdateScreen(
                    Arrays.asList("questeditor"), 
                    director
                ).execute();
            });

        // create quest inventory button
        new GUISlot(gui, 4)
            .setItem(Material.ITEM_FRAME)
            .setLabel("Quest Inventory")
            .onClick(() -> {
                new UpdateScreen(
                    Arrays.asList("questinventory"), 
                    director
                ).execute();
            });

        // create remove quest button (with confirmation check)
        if (confirmDeletion.equals(false)) {
            new GUISlot(gui, 8)
                .setItem(Material.RED_DYE)
                .setLabel("Delete")
                .onClick(() -> {
                    this.confirmDeletion = true;
                    this.execute();
                });
        } else {
            new GUISlot(gui, 8)
                .setItem(Material.RED_WOOL)
                .setLabel("Delete (Confirm)")
                .onClick(() -> {
                    // delete the quest
                    Boolean deleted = QuestRegistry.getInstance().delete(quest, true, true, true);

                    // don't continue if not deleted
                    if (!deleted) {
                        return;
                    }

                    // go back to previous screen
                    new UpdateScreen(
                        Arrays.asList(previousScreen), 
                        director
                    ).execute();
                });
        }

        // create quest toggle button
        boolean isToggled = quest.isToggled();
        
        new GUISlot(gui, 9)
            .setItem(isToggled ? Material.GREEN_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE)
            .setLabel(isToggled ? "Toggle Off" : "Toggle On")
            .onClick(() -> {
                quest.toggle();
                this.execute(); // refresh UI
            });
    }
}

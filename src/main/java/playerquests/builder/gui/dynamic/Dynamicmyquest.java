package playerquests.builder.gui.dynamic;

import java.util.ArrayList; // list array type
import java.util.Arrays; // generic arrays type

import playerquests.builder.gui.component.GUIFrame; // the outer frame of the GUI window
import playerquests.builder.gui.component.GUISlot; // inventory slots representing GUI buttons
import playerquests.builder.gui.function.UpdateScreen; // GUI function to change GUI
import playerquests.builder.quest.QuestBuilder; // for quest management
import playerquests.client.ClientDirector; // how a player client interacts with the plugin
import playerquests.product.Quest; // complete quest objects
import playerquests.utility.singleton.QuestRegistry; // tracking quests/questers

public class Dynamicmyquest extends GUIDynamic {

    /**
     * The current quest
     */
    QuestBuilder questBuilder;

    /**
     * The quest product
     */
    Quest quest;

    /**
     * Whether to show delete confirmation button
     */
    Boolean confirm_delete = false;

    public Dynamicmyquest(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        // retrieve the current quest from the client director
        this.questBuilder = (QuestBuilder) this.director.getCurrentInstance(QuestBuilder.class);
        this.quest = questBuilder.build();
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
                    Boolean deleted = QuestRegistry.getInstance().delete(questBuilder.build());

                    // go back if successful
                    if (deleted) {
                        new UpdateScreen(
                            new ArrayList<>(Arrays.asList(previousScreen)), 
                            director
                        ).execute();
                    }
                });
        }

        // create quest toggle button
        // to toggle on
        GUISlot toggleButton = new GUISlot(gui, 9)
            .setItem("GRAY_STAINED_GLASS_PANE")
            .setLabel("Toggle On");

        if (quest.isToggled()) {
            // to toggle off
            toggleButton
                .setItem("GREEN_STAINED_GLASS_PANE")
                .setLabel("Toggle Off");
        }

        toggleButton.onClick(() -> {
            quest.toggle();
            this.execute(); // refresh UI
        });
    }
    
}

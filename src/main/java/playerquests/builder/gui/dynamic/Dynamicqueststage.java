package playerquests.builder.gui.dynamic;

import java.util.Arrays; // generic array handling

import playerquests.builder.gui.component.GUISlot; // modifying gui slots
import playerquests.builder.gui.function.UpdateScreen; // going to previous screen
import playerquests.builder.quest.QuestBuilder;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.ClientDirector; // controlling the plugin
import playerquests.utility.singleton.QuestRegistry;

/**
 * Shows a dynamic for modifying the current quest stage.
 */
public class Dynamicqueststage extends GUIDynamic {

    /**
     * The current quest stage
     */
    QuestStage questStage;

    /**
     * Staging to delete the stage
     */
    Boolean confirm_delete = false;

    /**
     * The builder object for this quest
     */
    QuestBuilder questBuilder;

    /**
     * Creates a dynamic GUI to edit a quest stage.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicqueststage(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        // set the quest stage instance
        this.questStage = (QuestStage) this.director.getCurrentInstance(QuestStage.class);

        // get the quest builder
        this.questBuilder = (QuestBuilder) this.director.getCurrentInstance(QuestBuilder.class);
    }

    @Override
    protected void execute_custom() {
        this.gui.getFrame().setTitle("{QuestStage} Editor");
        this.gui.getFrame().setSize(18);

        // the back button
        GUISlot exitButton = new GUISlot(this.gui, 10);
        exitButton.setLabel("Back");
        exitButton.setItem("OAK_DOOR");
        exitButton.addFunction(new UpdateScreen( // set function as 'UpdateScreen'
            Arrays.asList("queststages"), // set the previous screen 
            director // set the client director
        ));

        // left side dividers
        new GUISlot(this.gui, 2)
            .setItem("BLACK_STAINED_GLASS_PANE");

        new GUISlot(this.gui, 11)
            .setItem("BLACK_STAINED_GLASS_PANE");

        // add 'delete stage' button (with confirm)
        if (!this.confirm_delete) { // if delete hasn't been confirmed
            new GUISlot(this.gui, this.gui.getFrame().getSize() - 1)
                .setItem("RED_DYE")
                .setLabel("Delete Stage")
                .onClick(() -> {
                    this.confirm_delete = true;
                    this.execute();
                });
        } else {
            new GUISlot(this.gui, this.gui.getFrame().getSize() - 1)
                .setItem("RED_WOOL")
                .setLabel("Delete")
                .onClick(() -> {
                    if (this.questBuilder.removeStage(this.questStage)) { // if quest was removed
                        new UpdateScreen(
                            Arrays.asList(previousScreen), 
                            this.director
                        ).execute();

                        // update the quest
                        QuestRegistry.getInstance().submit(this.questBuilder.build());
                    }

                    // update the quest
                    QuestRegistry.getInstance().submit(this.questBuilder.build());
                });
        }

        if (!this.questBuilder.removeStage(this.questStage, true)) { // if cannot delete the stage
            new GUISlot(this.gui, this.gui.getFrame().getSize() - 1)
                .setItem("GRAY_DYE")
                .setLabel("Cannot Delete")
                .setDescription("This stage is connected to other stages and actions.");
        }

    }
}

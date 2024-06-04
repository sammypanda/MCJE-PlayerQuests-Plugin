package playerquests.builder.gui.dynamic;

import java.util.ArrayList; // stores parameters for next GUI function
import java.util.Arrays; // working with literal arrays
import java.util.stream.IntStream; // fills slots procedually

import playerquests.builder.gui.component.GUISlot; // for managing gui slots
import playerquests.builder.gui.data.GUIMode; // how the GUI can be interacted with
import playerquests.builder.gui.function.UpdateScreen; // another GUI function to go to
import playerquests.builder.quest.QuestBuilder; // for managing the quest
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.ClientDirector; // for controlling the plugin

/**
 * Shows a dynamic GUI listing the players current quest stages.
 */
public class Dynamicqueststages extends GUIDynamic {

    /**
     * Quest builder for managing/accessing the quest
     */
    private QuestBuilder questBuilder;

    /**
     * the GUI title
     */
    private String guiTitle = "Quest Stages";

    /**
     * Creates a dynamic GUI with a list of the current quests stages.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicqueststages(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    /**
     * Create the GUI and set up.
     */
    public void setUp_custom() {
        this.questBuilder = (QuestBuilder) this.director.getCurrentInstance(QuestBuilder.class);

        this.guiTitle = this.guiTitle + " (" + this.questBuilder.getTitle() + ")";
    }

    /**
     * Main quest stages GUI loop
     */
    @Override
    public void execute_custom() {
        this.generatePages();
    }
    
    /**
     * Drawing repeated GUI elements
     */
    public void generatePages() {
        this.gui.getFrame().setTitle(this.guiTitle); // set the GUI title
        this.gui.getFrame().setSize( // set number of slots in the GUI
            Math.min( // get up to 54 (maximum slots)
                (this.questBuilder.getStages().size() + 9) / 9 * 9, // only multiples of 9
                54 // 54 maximum slots
            )
        );

        // dividers (first two rows)
        IntStream.iterate(1, n -> n + 9).limit(54/9).forEach((divSlot) -> {
            new GUISlot(this.gui, divSlot)
                .setItem("BLACK_STAINED_GLASS_PANE");

            new GUISlot(this.gui, divSlot + 1)
                .setItem("BLACK_STAINED_GLASS_PANE");
        });
        
        // when the exit button is pressed
        GUISlot exitButton = new GUISlot(this.gui, this.gui.getFrame().getSize() - 8);
        exitButton.setLabel("Back");
        exitButton.setItem("OAK_DOOR");
        exitButton.addFunction(new UpdateScreen( // set function as 'UpdateScreen'
            new ArrayList<>(Arrays.asList(this.previousScreen)), // set the previous screen 
            director // set the client director
        ));

        // add new stage button
        new GUISlot(this.gui, this.gui.getFrame().getSize())
            .setLabel("Add Stage")
            .setItem("LIME_DYE")
            .onClick(() -> {
                questBuilder.addStage(
                    new QuestStage(
                        this.questBuilder.build(), 
                        Integer.parseInt(this.questBuilder.getStages().getLast().substring(6) + 1)
                    )
                );

                this.gui.clearSlots();

                this.execute(); // rebuild GUI
            });

        IntStream.range(0, this.questBuilder.getStages().size()).anyMatch(index -> {

            String stage = this.questBuilder.getStages().get(index);
            Integer nextEmptySlot = this.gui.getEmptySlot();
            GUISlot questSlot = new GUISlot(this.gui, nextEmptySlot);
            questSlot.setItem("DIRT_PATH");
            questSlot.setLabel(stage);
            questSlot.onClick(() -> {
                if (!this.gui.getFrame().getMode().equals(GUIMode.CLICK)) {
                    return;
                }

                // set the stage as the current instance to modify
                this.director.setCurrentInstance(this.questBuilder.getQuestPlan().get(stage));

                // change to the quest stage GUI screen
                new UpdateScreen(
                    new ArrayList<>(Arrays.asList("queststage")), 
                    director
                ).execute();;
            });

            return false; // continue the loop
        });
    }
}

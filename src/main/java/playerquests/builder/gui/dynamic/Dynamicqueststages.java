package playerquests.builder.gui.dynamic;

import java.util.ArrayList; // stores parameters for next GUI function
import java.util.Arrays; // working with literal arrays
import java.util.stream.IntStream; // fills slots procedually

import playerquests.builder.gui.component.GUISlot; // for managing gui slots
import playerquests.builder.gui.function.UpdateScreen; // another GUI function to go to
import playerquests.builder.quest.QuestBuilder; // for managing the quest
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
                (this.questBuilder.getStages().size() + 8) / 9 * 9, // only multiples of 9
                54 // 54 maximum slots
            )
        );
        
        // when the exit button is pressed
        GUISlot exitButton = new GUISlot(this.gui, 1);
        exitButton.setLabel("Exit");
        exitButton.setItem("OAK_DOOR");
        exitButton.addFunction(new UpdateScreen( // set function as 'UpdateScreen'
            new ArrayList<>(Arrays.asList(this.previousScreen)), // set the previous screen 
            director // set the client director
        ));

        IntStream.range(0, this.questBuilder.getStages().size()).anyMatch(index -> {

            String stage = this.questBuilder.getStages().get(index);
            Integer nextEmptySlot = this.gui.getEmptySlot();
            GUISlot questSlot = new GUISlot(this.gui, nextEmptySlot);
            questSlot.setItem("DIRT_PATH");
            questSlot.setLabel(stage);
            questSlot.addFunction(new UpdateScreen(
                new ArrayList<>(Arrays.asList("queststage")), 
                director
            ));

            return false; // continue the loop
        });
    }
}

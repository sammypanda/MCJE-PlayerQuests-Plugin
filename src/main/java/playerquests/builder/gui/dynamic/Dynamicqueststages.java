package playerquests.builder.gui.dynamic;

import java.util.Arrays; // working with literal arrays
import java.util.stream.IntStream; // fills slots procedually

import org.bukkit.Material;

import playerquests.builder.gui.component.GUISlot; // for managing gui slots
import playerquests.builder.gui.data.GUIMode; // how the GUI can be interacted with
import playerquests.builder.gui.function.UpdateScreen; // another GUI function to go to
import playerquests.builder.quest.QuestBuilder; // for managing the quest
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.ClientDirector; // for controlling the plugin
import playerquests.utility.ChatUtils;
import playerquests.utility.singleton.QuestRegistry;

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
    public void setupCustom() {
        this.questBuilder = (QuestBuilder) this.director.getCurrentInstance(QuestBuilder.class);

        this.guiTitle = this.guiTitle + " (" + ChatUtils.shortenString(this.questBuilder.getTitle(), 18) + ")";
    }

    /**
     * Main quest stages GUI loop
     */
    @Override
    public void executeCustom() {
        this.generatePages();
    }

    /**
     * Drawing repeated GUI elements
     */
    public void generatePages() {
        this.gui.getFrame().setTitle(this.guiTitle); // set the GUI title
        this.gui.getFrame().setSize( // set number of slots in the GUI
            Math.min( // get up to 54 (maximum slots)
                (this.questBuilder.getStages().size() + 20) / 9 * 9, // only multiples of 9
                54 // 54 maximum slots
            )
        );

        // restart inv in-case GUI size is changed
        this.gui.getResult().minimise();
        this.gui.getResult().open();

        // dividers (first two rows)
        IntStream.iterate(1, n -> n + 9).limit(54/9).forEach((divSlot) -> {
            new GUISlot(this.gui, divSlot)
                .setItem(Material.BLACK_STAINED_GLASS_PANE);

            new GUISlot(this.gui, divSlot + 1)
                .setItem(Material.BLACK_STAINED_GLASS_PANE);
        });

        // when the exit button is pressed
        GUISlot exitButton = new GUISlot(this.gui, this.gui.getFrame().getSize() - 8);
        exitButton.setLabel("Back");
        exitButton.setItem(Material.OAK_DOOR);
        exitButton.addFunction(new UpdateScreen( // set function as 'UpdateScreen'
            Arrays.asList(this.previousScreen), // set the previous screen
            director // set the client director
        ));

        // add new stage button
        if (this.questBuilder.getStages().size() < 42) {
            new GUISlot(this.gui, this.gui.getFrame().getSize())
                .setLabel("Add Stage")
                .setItem(Material.LIME_DYE)
                .onClick(() -> {
                    questBuilder.addStage(
                        new QuestStage(
                            this.questBuilder.build(),
                            this.questBuilder.getStages().isEmpty() ? 0 : Integer.parseInt(this.questBuilder.getStages().getLast().getID().substring(6)) + 1
                        )
                    );

                    // update the quest
                    QuestRegistry.getInstance().submit(this.questBuilder.build());

                    // update UI
                    this.gui.clearSlots();
                    this.execute(); // rebuild GUI
                });
        }

        IntStream.range(0, this.questBuilder.getStages().size()).anyMatch(index -> {

            QuestStage stage = this.questBuilder.getStages().get(index);
            Integer nextEmptySlot = this.gui.getEmptySlot();
            GUISlot questSlot = new GUISlot(this.gui, nextEmptySlot);
            questSlot.setItem(Material.DIRT_PATH);
            questSlot.setLabel(stage.getLabel());
            questSlot.onClick(() -> {
                if (!this.gui.getFrame().getMode().equals(GUIMode.CLICK)) {
                    return;
                }

                // set the stage as the current instance to modify
                this.director.setCurrentInstance(this.questBuilder.getQuestPlan().get(stage.getID()));

                // change to the quest stage GUI screen
                new UpdateScreen(
                    Arrays.asList("queststage"),
                    director
                ).execute();
            });

            return false; // continue the loop
        });
    }
}

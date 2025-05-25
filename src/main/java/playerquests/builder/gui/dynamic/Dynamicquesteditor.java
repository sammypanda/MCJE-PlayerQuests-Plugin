package playerquests.builder.gui.dynamic;

import java.util.Arrays; // generic array handling
import java.util.List;
import java.util.Map;

import org.bukkit.Material;

import playerquests.builder.gui.component.GUIFrame; // describes the outer GUI frame/window
import playerquests.builder.gui.component.GUISlot; // describes a GUI button
import playerquests.builder.gui.function.ChatPrompt; // GUI taking input from chat box
import playerquests.builder.gui.function.UpdateScreen; // changing the GUI screen to another
import playerquests.builder.quest.QuestBuilder; // controlling a quest
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.ClientDirector; // accessing the client state
import playerquests.product.Quest;
import playerquests.utility.ChatUtils;
import playerquests.utility.singleton.QuestRegistry;

/**
 * Shows a dynamic GUI used for editing a quest.
 */
public class Dynamicquesteditor extends GUIDynamic {

    /**
     * The quest being edited.
     */
    private QuestBuilder questBuilder;

    /**
     * Creates a dynamic GUI to edit a quest.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicquesteditor(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        // get the current client quest builder
        questBuilder = (QuestBuilder) director.getCurrentInstance(QuestBuilder.class);
    }

    @Override
    protected void execute_custom() {
        GUIFrame guiFrame = gui.getFrame();
        String questTitle = questBuilder.getTitle();

        // set the GUI size
        guiFrame.setSize(9);

        // set the GUI title as: Edit Quest ([quest title])
        guiFrame.setTitle(
            String.format("Edit Quest %s", 
                questTitle != null ? "("+ChatUtils.shortenString(questTitle, 18)+")" : null
            )
        );

        // add the buttons
        new GUISlot(gui, 1) // back button
            .setItem("OAK_DOOR")
            .setLabel("Back")
            .addFunction(
                new UpdateScreen(
                    Arrays.asList(previousScreen),
                    director
                )
            );

        new GUISlot(gui, 3) // set quest title button
            .setItem("ACACIA_HANGING_SIGN")
            .setLabel("Set Title")
            .onClick(() -> {
                new ChatPrompt(
                    Arrays.asList("Enter quest title", "none"), 
                    director
                ).onFinish(guiFunction -> {
                    ChatPrompt function = (ChatPrompt) guiFunction;

                    // TODO: could modify in place rather than replacing

                    // get current quest (before replacing)
                    Quest quest = questBuilder.build();

                    // get current inventory
                    Map<Material, Integer> questInventory = QuestRegistry.getInstance().getInventory(quest);

                    // delete current quest
                    QuestRegistry.getInstance().delete(quest, true, false, true);
                    
                    // change title
                    questBuilder.setTitle(function.getResponse());

                    // create and save new
                    Quest newQuest = questBuilder.build();
                    newQuest.save();

                    // restore inventory
                    QuestRegistry.getInstance().setInventory(newQuest, questInventory);

                    // update quest reference
                    this.director.setCurrentInstance(newQuest, Quest.class);

                    this.execute(); // refresh UI to reflect title change
                })
                .execute();
            });

        GUISlot stagesSlot = new GUISlot(gui, 4) // view quest stages button (blocked)
            .setItem("GRAY_STAINED_GLASS_PANE")
            .setLabel("Quest Stages")
            .setDescription(List.of("Add an NPC to add Stages"));
        
        if (!questBuilder.getQuestNPCs().isEmpty()) { // view quest stages button (unblocked)
            stagesSlot.setItem("CHEST")
            .setLabel("Quest Stages")
            .setDescription(List.of("")) // clear the description
            .addFunction(
                new UpdateScreen(
                    Arrays.asList("queststages"), 
                    director
                )
            ); 
        }

        new GUISlot(gui, 5) // view quest NPCs button
            .setItem("ENDER_CHEST")
            .setLabel("Quest NPCs")
            .addFunction(
                new UpdateScreen(
                    Arrays.asList("questnpcs"), 
                    director
                )
            );

        new GUISlot(gui, 6) // quest start points button
            .setItem(Material.PISTON)
            .setLabel(
                this.questBuilder.build().getStartPoints().isEmpty() ? "Set start points" : "Edit start points")
            .onClick(() -> {
                // go to action select screen
                this.director.removeCurrentInstance(QuestStage.class); // do not default select stage
                new UpdateScreen(List.of("actionselector"), director)
                    .onFinish((f) -> {
                        UpdateScreen updateScreen = (UpdateScreen) f;
                        Dynamicactionselector actionSelector = (Dynamicactionselector) updateScreen.getDynamicGUI();

                        this.questBuilder.setStartPoints(actionSelector.getSelectedActions());
                    })
                    .execute();
            });

        new GUISlot(gui, 9) // save quest button
            .setItem("GREEN_DYE")
            .setLabel("Save")
            .onClick(() -> {
                // save the quest
                this.questBuilder.build().save();

                // close the GUI
                this.gui.getResult().close();
            });
    }
}

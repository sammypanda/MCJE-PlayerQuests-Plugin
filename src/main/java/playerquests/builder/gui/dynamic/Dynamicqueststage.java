package playerquests.builder.gui.dynamic;

import java.util.ArrayList;
import java.util.Arrays; // generic array handling
import java.util.List;
import java.util.stream.IntStream;

import org.bukkit.Material;

import playerquests.builder.gui.component.GUISlot; // modifying gui slots
import playerquests.builder.gui.data.GUIMode;
import playerquests.builder.gui.function.ChatPrompt;
import playerquests.builder.gui.function.UpdateScreen; // going to previous screen
import playerquests.builder.quest.QuestBuilder;
import playerquests.builder.quest.action.NoneAction;
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.data.StagePath;
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
    private QuestStage questStage;

    /**
     * The action IDs
     */
    private List<QuestAction> actionKeys;

    /**
     * Staging to delete the stage
     */
    private boolean confirmDelete = false;

    /**
     * Specify if actionKeys has already been looped through
     */
    private boolean confirmActionKeys = false;

    /**
     * The builder object for this quest
     */
    private QuestBuilder questBuilder;

    /**
     * Creates a dynamic GUI to edit a quest stage.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicqueststage(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setupCustom() {
        // set the quest stage instance
        this.questStage = (QuestStage) this.director.getCurrentInstance(QuestStage.class);

        // get the quest builder
        this.questBuilder = (QuestBuilder) this.director.getCurrentInstance(QuestBuilder.class);
    }

    @Override
    protected void executeCustom() {
        // set actionKeys
        this.actionKeys = new ArrayList<QuestAction>(this.questStage.getOrderedActions());

        // set frame title/style
        this.gui.getFrame().setTitle(String.format("%s Editor", questStage.getLabel()));
        this.gui.getFrame().setSize(27);

        // the back button
        this.createBackButton();

        // setting stage label
        this.createStageLabelButton();

        // setting startpoint actions
        this.createStartPointButton();

        // left side dividers
        this.createDividerSlots();

        // produce slots listing current actions
        if (!confirmActionKeys) {
            for (var index : IntStream.range(0, actionKeys.size()).toArray()) {
                if (this.createActionButton(index)) {
                    break; // exit early
                }
            }

            // set actionKeys as confirmed
            this.confirmActionKeys = true;
        }

        // add 'delete stage' button (with confirm)
        this.createDeleteButton();

        // add 'new action' button
        this.createNewActionButton();
    }

    private void createNewActionButton() {
        GUISlot newActionButton = new GUISlot(this.gui, this.gui.getFrame().getSize());
        if (this.questStage.getActions().size() >= 18) {
            newActionButton.setLabel("No More Action Slots");
            newActionButton.setItem(Material.BARRIER);
            return;
        }
        

        newActionButton.setLabel("Add Action");
        newActionButton.setItem(Material.LIME_DYE);
        newActionButton.onClick(() -> {
            // create the new action
            questStage.addAction(new NoneAction(questStage));

            // refresh UI
            this.confirmActionKeys = false; // set actionKeys to be looped through again
            this.gui.clearSlots();
            this.execute(); // re-run to see new action in list
        });
    }

    private void createDeleteButton() {
        if (!this.questBuilder.removeStage(this.questStage, true)) { // if cannot delete the stage
            new GUISlot(this.gui, this.gui.getFrame().getSize() - 1)
                .setItem(Material.GRAY_DYE)
                .setLabel("Cannot Delete")
                .setDescription(List.of("This stage is connected to other stages and actions."));
            return;
        }

        if (!this.confirmDelete) { // if delete hasn't been confirmed
            new GUISlot(this.gui, this.gui.getFrame().getSize() - 1)
                .setItem(Material.RED_DYE)
                .setLabel("Delete Stage")
                .onClick(() -> {
                    this.confirmDelete = true;
                    this.execute();
                });
            return;
        }

        new GUISlot(this.gui, this.gui.getFrame().getSize() - 1)
            .setItem(Material.RED_WOOL)
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

    private boolean createActionButton(int index) {
        QuestAction action = actionKeys.get(index);
        Integer nextEmptySlot = this.gui.getEmptySlot();
        GUISlot actionSlot = new GUISlot(this.gui, nextEmptySlot);
        String typeString = String.format("Type: %s", action.getName());
        List<String> description;

        // check if this action is a start point 
        boolean isStartPoint = this.questStage.getStartPoints().stream()
            .anyMatch(p -> 
                p.getStage().equals(this.questStage.getID()) &&
                p.getActions().contains(action.getID())
            );

        // set description
        if (isStartPoint) {
            description = List.of(typeString, "Is an entry point");
        } else {
            description = List.of(typeString);
        }

        actionSlot
            .setLabel(String.format("%s", action.getLabel()))
            .setDescription(description)
            .setItem(action.getActionStateItem(isStartPoint));

        actionSlot.onClick(() -> {
            if (!this.gui.getFrame().getMode().equals(GUIMode.CLICK)) {
                return;
            }

            // set the action as the current action to modify
            this.director.setCurrentInstance(action, QuestAction.class);

            // go to action editor screen
            actionSlot.addFunction(new UpdateScreen(
                Arrays.asList("actioneditor"),
                director
            )).execute(this.director.getPlayer());
        });

        return false; // continue the loop
    }

    private void createDividerSlots() {
        new GUISlot(this.gui, 2)
            .setItem(Material.BLACK_STAINED_GLASS_PANE);
        new GUISlot(this.gui, 11)
            .setItem(Material.BLACK_STAINED_GLASS_PANE);
        new GUISlot(this.gui, 20)
            .setItem(Material.BLACK_STAINED_GLASS_PANE);
    }

    private void createStartPointButton() {
        List<StagePath> startPoints = this.questStage.getStartPoints();
        new GUISlot(gui, 10)
            .setItem(Material.PISTON)
            .setLabel(String.format("%s start point actions",
                startPoints.isEmpty() ? "Set" : "Change"
            ))
            .onClick(() -> {
                this.director.setCurrentInstance(this.questStage.getQuest());

                new UpdateScreen(List.of("actionselector"), director)
                    .onFinish((f) -> {
                        UpdateScreen updateScreen = (UpdateScreen) f;
                        Dynamicactionselector actionSelector = (Dynamicactionselector) updateScreen.getDynamicGUI();

                        this.questStage.setStartPoints(actionSelector.getSelectedActions());
                    })
                    .execute();
            });
    }

    private void createStageLabelButton() {
        new GUISlot(gui, 1)
            .setItem(Material.OAK_SIGN)
            .setLabel(String.format("%s stage label",
                this.questStage.hasLabel() ? "Change" : "Set"
            ))
            .onClick(() -> {
                new ChatPrompt(
                    Arrays.asList("Type a label to help you remember the stage", "none"),
                    director
                ).onFinish((func) -> {
                    ChatPrompt function = (ChatPrompt) func;
                    String response = function.getResponse();
                    this.questStage.setLabel(response);
                    this.refresh();
                }).execute();
            });
    }

    private void createBackButton() {
        GUISlot exitButton = new GUISlot(this.gui, 19);
        exitButton.setLabel("Back");
        exitButton.setItem(Material.OAK_DOOR);
        exitButton.addFunction(new UpdateScreen( // set function as 'UpdateScreen'
            Arrays.asList("queststages"), // set the previous screen
            director // set the client director
        ));
    }
}

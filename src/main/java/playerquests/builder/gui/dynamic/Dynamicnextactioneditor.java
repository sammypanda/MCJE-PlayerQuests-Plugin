package playerquests.builder.gui.dynamic;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import playerquests.Core;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.data.ActionData;
import playerquests.builder.quest.data.StagePath;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.ClientDirector;
import playerquests.utility.ChatUtils;
import playerquests.utility.ChatUtils.MessageStyle;

/**
 * Shows a GUI used for selecting actions which come next.
 */
public class Dynamicnextactioneditor extends GUIDynamic {

    /**
     * The action itself.
     */
    QuestAction<?,?> action;

    /**
     * The context of the action to get next
     * actions from.
     */
    ActionData actionData;

    /**
     * The selected stage to look inside.
     */
    QuestStage selectedStage;

    /**
     * The selected next action paths
     */
    List<StagePath> nextActions;

    /**
     * Creates a dynamic GUI to select the next actions.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicnextactioneditor(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setupCustom() {
        this.action = (QuestAction<?,?>) this.director.getCurrentInstance(QuestAction.class);
        this.selectedStage = (QuestStage) this.director.getCurrentInstance(QuestStage.class);
        this.actionData = this.action.getData();
        this.nextActions = new ArrayList<>(this.actionData.getNextActions());
    }

    @Override
    protected void executeCustom() {
        // set outer frame style
        this.gui.getFrame()
                .setTitle("Select Next Actions")
                .setSize(27);

        // put back button
        this.createBackButton();

        // put dynamic button states
        if (this.selectedStage == null) {
            // make back button go to prev screen
            this.createBackButton();

            // show stages
            this.action.getStage().getQuest().getStages().entrySet().stream()
                .sorted((entry1, entry2) -> {
                    int intValue1 = Integer.parseInt(entry1.getKey().split("_")[1]);
                    int intValue2 = Integer.parseInt(entry2.getKey().split("_")[1]);
                    return Integer.compare(intValue1, intValue2); // compare in ascending order
                }).forEach(entry -> {
                    this.createStageButton(entry.getValue());
                });
        } else {
            // make back button go to stages
            this.createBackButton();

            // just select the stage button
            new GUISlot(gui, 2)
                .setLabel(this.stageIsSelected() ? "Unselect this stage" : "Select this stage")
                .setItem(this.stageIsSelected() ? Material.ORANGE_DYE : Material.YELLOW_DYE)
                .onClick(() -> {
                    Bukkit.getScheduler().runTask(Core.getPlugin(), () -> {
                        StagePath stagePath = new StagePath(this.selectedStage, null);

                        if (this.stageIsSelected()) {
                            // remove the stage
                            this.nextActions.removeIf(path -> !path.hasActions() && path.getStage().equals(this.selectedStage.getID()));
                        } else {
                            // add the stage
                            this.nextActions.add(stagePath);
                        }

                        this.refresh();
                    });
                });

            // show actions
            List<QuestAction<?,?>> actions = this.selectedStage.getOrderedActions();
            actions.forEach((a) -> {
                this.createActionButton(a);
            });
        }
    }

    /**
     * Create a stage button.
     * These buttons show a list of actions that
     * belong to it.
     * @param stageID the id of the stage
     * @param stage the quest stage object
     * @return a GUI slot button
     */
    private GUISlot createStageButton(QuestStage stage) {
        return new GUISlot(gui, this.gui.getEmptySlot())
            .setLabel(stage.getLabel())
            .setItem(Material.CHEST)
            .onClick(() -> {
                this.director.setCurrentInstance(stage, QuestStage.class);
                this.refresh();
            });
    }

    /**
     * Create an action button.
     * These buttons are selectable.
     * @param action the stage action object
     * @return a GUI slot button
     */
    private GUISlot createActionButton(QuestAction<?,?> action) {
        String actionID = action.getID();

        boolean isStartPoint = this.stageIsSelected() && this.action.getStage().getStartPoints()
            .stream()
            .anyMatch(path -> 
                path.hasActions() && 
                path.getActions().contains(actionID)
            );

        boolean isSelected = this.nextActions
            .stream()
            .anyMatch(path -> 
                path.hasActions() && // isn't just a stage in the path
                path.getStage().contains(action.getStage().getID()) && // and the stage ID matches
                path.getActions().contains(actionID) // and the action ID matches
            );

        return new GUISlot(gui, this.gui.getEmptySlot())
            // conditionals: if is selected, if is not selected, if is selected by being a start point
            .setLabel(String.format("%s (%s)", action.getLabel(), action.getActionStateText(isSelected, isStartPoint)))
            .setDescription(List.of(String.format("Type: %s", action.getName())))
            .setItem(action.getActionStateItem(isSelected, isStartPoint))
            .onClick(() -> {
                this.handleActionButtonClick(isSelected, actionID, isStartPoint);
            });
    }

    private void handleActionButtonClick(boolean isSelected, String actionID, boolean isStartPoint) {
        StagePath stagePath = new StagePath(action.getStage(), List.of(action));

        // unselect
        if (isSelected) {
            this.nextActions.removeIf(path -> {
                if (!path.hasActions()) {
                    return false;
                }

                return path.getActions().contains(actionID);
            });
        }

        // select
        else if (!isStartPoint && !isSelected) {
            this.nextActions.add(stagePath);
        }

        // send message
        else if (isStartPoint) {
            ChatUtils.message("Cannot unset start point action.")
                .player(this.director.getPlayer())
                .style(MessageStyle.PRETTY)
                .send();
        }

        this.actionData.setNextActions(nextActions);
        this.refresh();
    }

    /**
     * Creates a dynamic back button.
     * @return a GUI slot button
     */
    private GUISlot createBackButton() {
        return new GUISlot(gui, 1)
            .setLabel("Back")
            .setItem(Material.OAK_DOOR)
            .onClick(() -> {
                this.actionData.setNextActions(this.nextActions);

                // go to previous screen if not viewing a stage
                if (this.selectedStage == null) {
                    new UpdateScreen(List.of(this.previousScreen), director).execute();
                } else { // go to stages view if is viewing stage actions list
                    this.director.removeCurrentInstance(QuestStage.class); // unset the stage to go back
                    this.refresh();
                }
            });
    }

    private boolean stageIsSelected() {
        String stageID = this.selectedStage.getID(); // find selected stage ID

        return this.nextActions.stream()
            .filter(path -> path.getStage().equals(stageID)) // stage is the same as the selected one
            .anyMatch(stage -> !stage.hasActions()); // stage has no actions (just a pointer to the stage)
    }
}

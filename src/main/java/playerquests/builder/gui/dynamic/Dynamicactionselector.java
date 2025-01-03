package playerquests.builder.gui.dynamic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;

import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.action.condition.CompletionCondition;
import playerquests.builder.quest.data.StagePath;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.ClientDirector;
import playerquests.product.Quest;

/**
 * Shows a GUI used for selecting actions.
 */
public class Dynamicactionselector extends GUIDynamic {

    /**
     * The quest to apply to.
     */
    Quest quest;

    /**
     * The stage to select actions for.
     */
    QuestStage selectedStage;

    /**
     * Whether the user can select other stages or not.
     */
    boolean stageSelection = true;

    /**
     * Get the selected actions.
     */
    List<StagePath> selectedActions = new ArrayList<StagePath>();
    // TODO: make selectedActions quest-agnostic; select actions from any quest: Map<Quest, List<StagePath>> + add ``boolean questSelection`` field 

    /**
     * Creates a dynamic GUI to select actions.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicactionselector(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        this.quest = (Quest) this.director.getCurrentInstance(Quest.class);
        this.selectedStage = (QuestStage) this.director.getCurrentInstance(QuestStage.class);

        // set up for if coming from a CompletionCondition
        CompletionCondition completionCondition = (CompletionCondition) this.director.getCurrentInstance(CompletionCondition.class);
        if (completionCondition != null) {
            this.selectedActions = completionCondition.getRequiredActions().getOrDefault(this.quest, selectedActions);
            this.stageSelection = false;
        }

        // set up for if coming from queststage screen
        if (this.previousScreen.equals("queststage")) {
            this.selectedActions = this.selectedStage.getStartPoints();
            this.stageSelection = false;
        }

        // set up for if coming from questeditor screen
        if (this.previousScreen.equals("questeditor")) {
            this.selectedActions = this.quest.getStartPoints();
        }
    }

    @Override
    protected void execute_custom() {
        // set outer frame style
        this.gui.getFrame()
            .setTitle("Select Actions");

        // put dynamic button states
        if (this.selectedStage == null) {
            // put back button
            this.createBackButton();

            // show stages
            Map<String, QuestStage> stages = this.quest.getStages();
            stages.forEach((stage_id, stage) -> {
                this.createStageButton(stage_id, stage);
            });
        } else {
            // make back button go to stages
            this.createBackButton();

            // show actions
            Map<String, QuestAction> actions = this.selectedStage.getActions();
            actions.forEach((action_id, action) -> {
                this.createActionButton(action_id, action);
            });
        }
    }

    /**
     * Creates a dynamic back button.
     * @return a GUI slot button
     */
    private void createBackButton() {
        new GUISlot(gui, 1)
            .setItem(Material.OAK_DOOR)
            .setLabel("Back")
            .onClick(() -> {
                if (this.selectedStage == null || this.stageSelection != true) {
                    new UpdateScreen(List.of(this.previousScreen), director).execute();
                    this.onFinish.accept(this); // trigger onfinish logics
                } else {
                    this.selectedStage = null;
                    this.refresh();
                }
            });
    }

    /**
     * Create a stage button.
     * These buttons show a list of actions that 
     * belong to it.
     * @param stage_id the id of the stage
     * @param stage the quest stage object
     * @return a GUI slot button
     */
    private GUISlot createStageButton(String stage_id, QuestStage stage) {
        return new GUISlot(gui, this.gui.getEmptySlot())
            .setLabel(stage_id)
            .setItem(Material.CHEST)
            .onClick(() -> {
                this.selectedStage = stage;
                this.refresh();
            });
    }

    /**
     * Create an action button.
     * @param action_id the id of the action
     * @param action the quest action object
     * @return a GUI slot button
     */
    private GUISlot createActionButton(String action_id, QuestAction action) {
        // (p = stage path)

        Boolean isPresent = this.selectedActions.stream()
            .filter(p -> p.getStage().equals(action.getStage().getID()))
            .filter(p -> p.getActions().contains(action_id))
            .findFirst()
            .isPresent();

        return new GUISlot(gui, this.gui.getEmptySlot())
            .setLabel(String.format("%s%s", 
                action_id,
                isPresent ? " (Selected)" : ""))
            .setItem(
                isPresent ? Material.POWERED_RAIL : Material.RAIL)
            .onClick(() -> {
                // construct a path to this action
                StagePath stagePath = new StagePath(action.getStage(), List.of(action));

                // if already in list
                if (isPresent) {
                    // then remove
                    this.selectedActions.removeIf(p -> p.getActions().contains(action_id));
                } else {
                    // otherwise, add to the list
                    this.selectedActions.add(stagePath);
                }

                this.refresh();
            });
    }
    
    /**
     * Get the list of actions that have been selected.
     * @return list of paths to the selected actions.
     */
    public List<StagePath> getSelectedActions() {
        return this.selectedActions;
    }
}

package playerquests.builder.quest.action.condition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;

import com.fasterxml.jackson.annotation.JsonProperty;

import playerquests.Core;
import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.dynamic.Dynamicactionselector;
import playerquests.builder.gui.dynamic.GUIDynamic;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.data.ActionData;
import playerquests.builder.quest.data.QuesterData;
import playerquests.builder.quest.data.StagePath;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.ClientDirector;
import playerquests.product.Quest;
import playerquests.utility.ChatUtils;
import playerquests.utility.event.ActionCompletionEvent;

public class CompletionCondition extends ActionCondition {
    
    /**
     * The actions that have been added as requirements for this 
     * condition to pass. Keyed by quest ID.
     */
    @JsonProperty("actions")
    Map<String, List<StagePath>> requiredActions = new HashMap<>();

    /**
     * Default constructor for Jackson.
     */
    public CompletionCondition() {}

    public CompletionCondition(ActionData actionData) {
        super(actionData);
    }

    @Override
    public Boolean isMet(QuesterData questerData) {
        // check if all the required actions are completed
        return this.getRequiredActions().entrySet().stream().allMatch(entry -> { // if all required actions report complete
            Quest quest = Core.getQuestRegistry().getQuest(entry.getKey());
            
            // CHECKING IF ALL ARE COMPLETED:
            return entry.getValue().stream() // unpack all paths
                .flatMap(path -> path.getActions(quest).stream()) // for each action in the path
                .allMatch(action -> // if any don't match, it will return false
                    questerData.getQuester().getDiary().getActionCompletionState(quest, action) == 1
                );
        });
    }

    public Map<String, List<StagePath>> getRequiredActions() {
        return this.requiredActions;
    }

    @Override
    public GUISlot createSlot(GUIDynamic screen, GUIBuilder gui, Integer slot, ClientDirector director) {
        return new GUISlot(gui, slot)
            .setLabel("Action Completion")
            .setItem(Material.DETECTOR_RAIL);
    }

    @Override
    public String getName() {
        return "Completion";
    }

    @Override
    public void createEditorGUI(GUIDynamic screen, GUIBuilder gui, ClientDirector director) {
        new GUISlot(gui, 3)
            .setLabel(
                "Select actions"
            )
            .setDescription(List.of("Action can only be played after these actions."))
            .setItem(Material.CHEST)
            .onClick(() -> {
                director.removeCurrentInstance(QuestStage.class); // do not default select stage
                director.setCurrentInstance(this, CompletionCondition.class);

                new UpdateScreen(List.of("actionselector"), director)
                    .onFinish((f) -> {
                        UpdateScreen function = (UpdateScreen) f;
                        Dynamicactionselector actionSelector = (Dynamicactionselector) function.getDynamicGUI();
                        
                        actionSelector.onFinish((_) -> {
                            Quest quest = (Quest) director.getCurrentInstance(Quest.class);

                            // save selected actions to this CompletionCondition
                            this.setRequiredActions(Map.of(quest.getID(), actionSelector.getSelectedActions()));
                            
                            // cleanup
                            director.removeCurrentInstance(CompletionCondition.class);
                            director.setCurrentInstance(this.getActionData().getAction().getStage(), QuestStage.class);
                        });
                    })
                    .execute();
            });
    }

    private void setRequiredActions(Map<String, List<StagePath>> requiredActions) {
        this.requiredActions = requiredActions;
    }

    @Override
    public List<String> getDetails() {
        List<String> actions = this.requiredActions.values().stream()
            .flatMap(List::stream)           
            .map(StagePath::toString)
            .toList();
        String actionsString = String.join(", ", actions);

        return List.of(
            "Requires",
            String.format("%s",
                ChatUtils.shortenString(actionsString, 22)
            ),
            "to complete"
        );  
    }

    @Override
    public List<String> getDescription() {
        return List.of("Set actions for before", "this action can be played");
    }

    @Override
    public void startListener(QuesterData questerData) {
        new CompletionConditionListener(this, questerData);
    }

    class CompletionConditionListener extends ActionConditionListener<CompletionCondition> {

        public CompletionConditionListener(CompletionCondition actionCondition, QuesterData questerData) {
            super(actionCondition, questerData);
        }

        /**
         * Event for when an action has been completed.
         * @param event the data about this event
         */
        @EventHandler
        private void onActionCompletion(ActionCompletionEvent event) {
            // if a different quester triggered the event, exit
            if (!event.getQuesterData().getQuester().equals(questerData.getQuester())) {
                return;
            }

            // if action still not finished, exit
            if (!this.actionCondition.isMet(questerData)) {
                return;
            }

            this.trigger();
        }
    }
}
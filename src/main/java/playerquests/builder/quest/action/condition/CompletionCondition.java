package playerquests.builder.quest.action.condition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Material;

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
import playerquests.client.ClientDirector;
import playerquests.product.Quest;

public class CompletionCondition extends ActionCondition {
    
    /**
     * The actions that have been added as requirements for this 
     * condition to pass.
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
        // - this is done by getting the questers diary and asking it if it thinks
        //   the action passed in has been completed.
        return this.getRequiredActions().entrySet().stream()
            // filter out any action that hasn't been completed (the key is quest, the value is the path **LIST**)
                // checking every path in the lists belonging to each quest
            .filter(entry -> entry.getValue().stream()
                .anyMatch(path -> !questerData.getQuester().getDiary().hasCompletedAction(
                    Core.getQuestRegistry().getQuest(entry.getKey()), // search quest registry for the quest
                    path
                )))
            // collect back to a set to check if is empty
            .collect(Collectors.toSet())
            // if it's empty that means we succeeded
            // if not, that means there is one here that hasn't been completed
            .isEmpty();
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
                director.setCurrentInstance(this, CompletionCondition.class);

                new UpdateScreen(List.of("actionselector"), director)
                    .onFinish((f) -> {
                        UpdateScreen function = (UpdateScreen) f;
                        Dynamicactionselector actionSelector = (Dynamicactionselector) function.getDynamicGUI();
                        
                        actionSelector.onFinish((_) -> {
                            Quest quest = (Quest) director.getCurrentInstance(Quest.class);

                            this.setRequiredActions(Map.of(quest.getID(), actionSelector.getSelectedActions())); // TODO: save selected actions to this CompletionCondition
                            director.removeCurrentInstance(CompletionCondition.class); // cleanup
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
        return List.of(
            // String.format("%s to %s", this.startTime, this.endTime)
            "woof woof"
        );
    }

    @Override
    public List<String> getDescription() {
        return List.of("Set actions for before", "this action can be played");
    }
}
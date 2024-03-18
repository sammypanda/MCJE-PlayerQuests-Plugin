package playerquests.builder.quest.action;

import java.util.ArrayList; // array list type
import java.util.List; // generic list type

import playerquests.builder.quest.data.ActionOption; // enums for possible options to add to an action
import playerquests.builder.quest.stage.QuestStage; // refers to a stage which this action may belong to
import playerquests.client.quest.QuestClient; // the quester themselves

/**
 * Skips the quest action.
 */
public class None extends QuestAction {

    /**
     * Default constructor (for Jackson)
    */
    public None() {}
    
    /**
     * Skips the currently tasked quest action.
     * @param stage stage this action belongs to
     */
    public None(QuestStage stage) {
        super(stage);
    }

    @Override
    public List<ActionOption> initOptions() {
        return new ArrayList<ActionOption>();
    }

    @Override
    public void Run(QuestClient quester) {}
}

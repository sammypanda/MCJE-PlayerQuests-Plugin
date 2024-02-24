package playerquests.builder.quest.action;

import playerquests.builder.quest.data.ActionOptionData; // the options on this action
import playerquests.builder.quest.stage.QuestStage; // refers to a stage which this action may belong to
import playerquests.client.quest.QuestClient; // the quester themselves

/**
 * Skips the quest action.
 */
public class None extends QuestAction {
    
    /**
     * Skips the currently tasked quest action.
     * @param parentStage stage this action belongs to
     */
    public None(QuestStage parentStage) {
        super(parentStage);
    }

    @Override
    public ActionOptionData getActionOptionData() {
        return new ActionOptionData();
    }

    @Override
    public void Run(QuestClient quester) {}
}

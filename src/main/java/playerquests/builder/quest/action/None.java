package playerquests.builder.quest.action;

import playerquests.builder.quest.stage.QuestStage;

/**
 * Skips the quest action.
 */
public class None extends QuestAction {
    
    /**
     * Skips the currently tasked quest action.
     * @param parentStage stage this action belongs to
     * @param unassigned if this stage is given a valid ID
     */
    public None(QuestStage parentStage, Boolean unassigned) {
        super(parentStage, unassigned);
    }
}

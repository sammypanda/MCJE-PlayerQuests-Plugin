package playerquests.builder.quest.action;

import playerquests.builder.quest.stage.QuestStage;

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
}

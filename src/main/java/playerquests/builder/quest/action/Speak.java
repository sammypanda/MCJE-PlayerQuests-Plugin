package playerquests.builder.quest.action;

import playerquests.builder.quest.stage.QuestStage;

public class Speak extends QuestAction {
    
    /**
     * Produces dialogue from an NPC.
     * @param parentStage stage this action belongs to
     */
    public Speak(QuestStage parentStage) {
        super(parentStage);
    }
}

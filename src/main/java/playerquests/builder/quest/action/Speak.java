package playerquests.builder.quest.action;

import playerquests.builder.quest.stage.QuestStage;

public class Speak extends QuestAction {
    
    /**
     * Produces dialogue from an NPC.
     * @param parentStage stage this action belongs to
     * @param unassigned if this stage is given a valid ID
     */
    public Speak(QuestStage parentStage, Boolean unassigned) {
        super(parentStage, unassigned);
    }
}

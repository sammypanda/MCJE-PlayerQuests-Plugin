package playerquests.builder.quest.action;

import playerquests.builder.quest.npc.QuestNPC;
import playerquests.builder.quest.stage.QuestStage;

/**
 * Makes an NPC speak to the quester.
 */
public class Speak extends QuestAction {
    
    /**
     * Produces dialogue from an NPC.
     * @param parentStage stage this action belongs to
     */
    public Speak(QuestStage parentStage, QuestNPC npc) {
        super(parentStage);
        this.npc = npc;
    }
}

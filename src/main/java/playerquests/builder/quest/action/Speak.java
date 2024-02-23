package playerquests.builder.quest.action;

import java.util.ArrayList;
import java.util.List;

import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.quest.data.ActionOption;
import playerquests.builder.quest.data.ActionOptionData;
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

    @Override
    public ActionOptionData getActionOptionData() {
        ActionOptionData options = new ActionOptionData();

        options.add(ActionOption.NPC);

        return options;
    }
}

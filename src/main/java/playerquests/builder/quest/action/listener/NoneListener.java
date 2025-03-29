package playerquests.builder.quest.action.listener;

import java.util.Optional;

import org.bukkit.event.EventHandler;

import playerquests.builder.quest.action.NoneAction;
import playerquests.builder.quest.action.option.NPCOption;
import playerquests.builder.quest.data.QuesterData;
import playerquests.utility.event.NPCInteractEvent;

/**
 * Listener for immediately moving on from an action.
 */
public class NoneListener extends ActionListener<NoneAction> {

    /**
     * Constructs a new empty action listener.
     * @param action the quest action this listener is for.
     * @param questerData the data about the quester.
     */
    public NoneListener(NoneAction action, QuesterData questerData) {
        super(action, questerData);
    }
    
    /**
     * Event for when an NPC has been interacted with.
     * @param event the data about this event
     */
    @EventHandler
    private void onNPCInteract(NPCInteractEvent event) {
        // continue if an NPC does not exist for this action
        Optional<NPCOption> npc = (Optional<NPCOption>) this.action.getData().getOption(NPCOption.class);
        if (npc.isEmpty()) {
            action.check(questerData);
        }

        // exit if the is different from the player in the interact event
        if (!event.getPlayer().equals(this.questerData.getQuester().getPlayer())) {
            return;
        }

        // exit if the NPC is different from all in the interact event
        if (!event.getNPCs().contains(npc.get().getNPC(action.getStage().getQuest()))) {
            return;
        }
        
        // then proceed to check this action
        action.check(questerData);
    }
}

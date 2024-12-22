package playerquests.builder.quest.action.listener;

import java.util.Optional;

import org.bukkit.event.EventHandler;

import playerquests.builder.quest.action.SpeakAction;
import playerquests.builder.quest.action.option.NPCOption;
import playerquests.builder.quest.data.QuesterData;
import playerquests.utility.event.NPCInteractEvent;

/**
 * Listener for NPCs speaking.
 */
public class SpeakListener extends ActionListener<SpeakAction> {

    /**
     * Constructs a new speak action listener.
     * @param action the quest action this listener is for.
     * @param questerData the data about the quester.
     */
    public SpeakListener(SpeakAction action, QuesterData questerData) {
        super(action, questerData);
    }

    /**
     * Event for when an NPC has been interacted with.
     * @param event the data about this event
     */
    @EventHandler
    private void onNPCInteract(NPCInteractEvent event) {
        // see if an NPC exists for this action
        Optional<NPCOption> npc = (Optional<NPCOption>) this.action.getData().getOption(NPCOption.class);
        if (npc.isEmpty()) {
            return; // nothing here to do
        }

        // if the NPC is the same as the one from the interact event
        if (npc.get().getNPC(action.getStage().getQuest()).equals(event.getNPC())) {
            // then proceed to check this action
            action.check(questerData);
        }
    }
}

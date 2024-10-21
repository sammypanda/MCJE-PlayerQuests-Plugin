package playerquests.builder.quest.action.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

import playerquests.builder.quest.action.SpeakAction;
import playerquests.builder.quest.data.QuesterData;
import playerquests.utility.event.NPCInteractEvent;

public class SpeakListener extends ActionListener<SpeakAction> {

    public SpeakListener(SpeakAction action, QuesterData questerData) {
        super(action, questerData);
    }

    @EventHandler
    private void onNPCInteract(NPCInteractEvent event) {
        Bukkit.broadcastMessage("interacted with " + event.getNPC().getName());
    }
    
}

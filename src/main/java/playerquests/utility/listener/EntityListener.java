package playerquests.utility.listener;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import playerquests.Core;
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.npc.QuestNPC;
import playerquests.client.quest.QuestClient;
import playerquests.utility.event.NPCInteractEvent;

public class EntityListener implements Listener {

    /**
     * Constructs a new {@code EntityListener} and registers it with the Bukkit event system.
     */
    public EntityListener() {
        Bukkit.getPluginManager().registerEvents(this, Core.getPlugin());
    }

    /**
     * Handles player interactions with entities.
     * @param event the {@code PlayerInteractEntityEvent} to handle
     */
    @EventHandler
    public void onEntityNPCInteract(NPCRightClickEvent event) {
        NPC citizen = event.getNPC();

        if (citizen == null) { return; }

        // find matching entity to be sure it is an NPC
        QuestClient quester = Core.getQuestRegistry().getQuester(event.getClicker());
        List<Entry<QuestAction<?,?>, QuestNPC>> npcs = quester.getData().getNPCs().stream() // get list of matching npcs
            .filter(npc -> { // check if is NPC
                return citizen.equals(quester.getData().getCitizenNPC(npc.getKey(), npc.getValue()));
            })
            .toList(); // put into list for calling event


        // conditions to not continue the event:
        if (
            npcs.isEmpty() // if entity is not an active NPC
        ) { return; }

        // send out the event
        Bukkit.getServer().getPluginManager().callEvent(
            new NPCInteractEvent(npcs, event.getClicker())
        );
    }
}

package playerquests.utility.listener;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

import playerquests.Core;
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.npc.EntityNPC;
import playerquests.builder.quest.npc.QuestNPC;
import playerquests.client.quest.QuestClient;
import playerquests.utility.event.NPCInteractEvent;
import playerquests.utility.serialisable.EntitySerialisable;

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
    public void onEntityNPCInteract(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();

        if (entity == null) { return; }

        // find matching entity to be sure it is an NPC
        Location eventEntityLocation = entity.getLocation();
        EntitySerialisable eventEntityObject = new EntitySerialisable(entity);
        QuestClient quester = Core.getQuestRegistry().getQuester(event.getPlayer());
        List<Entry<QuestAction, QuestNPC>> npcs = quester.getData().getNPCs().stream() // get list of matching npcs
            .filter(npc -> npc.getValue().getAssigned() instanceof EntityNPC) // check if NPC is actually an 'entity type NPC'
            .filter(npc -> npc.getValue().getLocation().toBukkitLocation().distance(eventEntityLocation) < 1) // check if entity locations match
            .filter(npc -> { // check if entity attributes match
                EntityNPC entityNPC = (EntityNPC) npc.getValue().getAssigned();
                return entityNPC.getEntity().toString().equals(eventEntityObject.toString());
            })
            .toList(); // put into list for calling event


        // conditions to not continue the event:
        if (
            npcs.isEmpty() || // if entity is not an active NPC
            event.getHand().equals(EquipmentSlot.OFF_HAND) // no duplicating interaction
        ) { return; }

        // send out the event
        Bukkit.getServer().getPluginManager().callEvent(
            new NPCInteractEvent(npcs, event.getPlayer())
        );
    }
}

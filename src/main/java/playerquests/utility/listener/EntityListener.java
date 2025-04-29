package playerquests.utility.listener;

import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

import playerquests.Core;
import playerquests.builder.quest.data.QuesterData;
import playerquests.builder.quest.npc.EntityNPC;
import playerquests.builder.quest.npc.QuestNPC;
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
    public void onEntityNPCInteract(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();

        if (entity == null) { return; }

        Player player = event.getPlayer();
        QuesterData questerData = Core.getQuestRegistry().getQuester(player).getData();
        Location eventLocation = entity.getLocation();

        // get the NPCs matching the location of the interacted entity
        List<QuestNPC> npcs = questerData.getNPCs().stream()
            .filter(questNPC -> eventLocation.distance(questNPC.getLocation().toBukkitLocation()) <= 1)
            .toList();

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

    /**
     * Registers an EntityNPC as active.
     * @param entityNPC the entity NPC to register
     * @param player the player to register the NPC for
     */
    public void registerEntityNPC(EntityNPC entityNPC, Player player) {
        this.setEntityNPC(entityNPC, player);
    }

    private void setEntityNPC(EntityNPC entityNPC, Player player) {
        QuestNPC questNPC = entityNPC.getNPC();
        Location location = questNPC.getLocation().toBukkitLocation();
        World world = location.getWorld();

        // center on origin
        location.add(.5, 0, .5);

        // spawn entity in world
        Entity entity = world.spawnEntity(location, entityNPC.getEntity());
        
        // disable damage
        entity.setInvulnerable(true);
        
        // disable movement
        entity.setGravity(false);
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            livingEntity.setAI(false);
            livingEntity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0);
            livingEntity.setCollidable(false);
        }
    }

    /**
     * Unregisters an EntityNPC.
     * @param entityNPC the entity NPC to unregister
     * @param player the player to unregister the NPC for
     */
    public synchronized void unregisterEntityNPC(EntityNPC entityNPC, Player player) {
        this.unsetEntityNPC(entityNPC, player);
    }

    /**
     * Unregisters all isntances of an EntityNPC.
     * @param entityNPC the entity NPC to unregister
     */
    public synchronized void unregisterEntityNPC(EntityNPC entityNPC) {
        this.unsetEntityNPC(entityNPC, null);
    }

    /**
     * Hides or removes entity NPC from the world.
     * @param entityNPC the entityNPC to unset
     * @param player the player to unset for
     */
    private void unsetEntityNPC(EntityNPC entityNPC, @Nullable Player player) {
        Location location = entityNPC.getNPC().getLocation().toBukkitLocation();

        // remove all location+type matching entities
        location.getWorld().getNearbyEntities(location, 1, 1, 1).stream()
            .filter(entity -> entity.getType().equals(entityNPC.getEntity()))
            .forEach(entity -> entity.remove());
    }

    /**
     * Clear all entity NPCs.
     */
    public synchronized void clear() {
        // for each quest in the registry, remove it
        Core.getQuestRegistry().getAllQuests().values().forEach(quest -> {
            quest.getNPCs().forEach((_, npc) -> npc.remove());
        });
    }   
}

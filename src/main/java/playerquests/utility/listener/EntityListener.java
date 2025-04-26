package playerquests.utility.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import playerquests.Core;
import playerquests.builder.quest.npc.EntityNPC;
import playerquests.builder.quest.npc.QuestNPC;

public class EntityListener implements Listener {
    
    /**
     * Constructs a new {@code EntityListener} and registers it with the Bukkit event system.
     */
    public EntityListener() {
        Bukkit.getPluginManager().registerEvents(this, Core.getPlugin());
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

    private void unsetEntityNPC(EntityNPC entityNPC, Player player) {
        // TODO: find via getNearbyEntities.. entityNPC.getNPC().getLocation();
    }
}

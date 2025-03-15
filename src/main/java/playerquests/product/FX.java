package playerquests.product;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import playerquests.Core;
import playerquests.builder.quest.data.LocationData;
import playerquests.product.fx.ParticleFX;

/**
 * Base class for all FX effects.
 * Provides common functionality for all effects, such as applying and stopping them.
 */
public class FX {
    protected BukkitTask effectTask;
    protected LocationData location;
    protected ParticleFX particleFX;

    /**
     * Constructs a new FX instance for PlayerQuests.
     * @param location the location of the FX
     * @param particleFX the particle to add 
     */
    public FX(LocationData location, ParticleFX particleFX) {
        this.location = location;
        this.particleFX = particleFX;
    }

    /**
     * Start the effect task for this FX.
     * @param player the player to show the FX to
     */
    public void applyEffect(Player player) {
        this.effectTask = Bukkit.getServer().getScheduler().runTaskTimer(Core.getPlugin(), () -> { // synchronous
            player.spawnParticle(
                this.particleFX.toBukkitParticle(),
                (double) this.location.getX(),
                (double) this.location.getY(),
                (double) this.location.getZ(),
                5
            );
        }, 0, 20);
    };

    /**
     * Stop this effect.
     */
    public void stopEffect() {
        if (this.effectTask != null) {
            this.effectTask.cancel();
        }
    }

    /**
     * Set the location for this effect.
     * @param location the location data object
     */
    public void setLocation(LocationData location) {
        this.location = location;
    }

    /**
     * Get the current location of this effect.
     * @return the location data object
     */
    public LocationData getLocation() {
        return this.location;
    }
}

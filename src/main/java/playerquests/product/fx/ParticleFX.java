package playerquests.product.fx;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import playerquests.Core;
import playerquests.builder.quest.data.LocationData;
import playerquests.product.FX;

/**
 * Enum representing various particle-based effects.
 * Each effect corresponds to a specific type of visual particle effect.
 */
public enum ParticleFX implements FX {
    SPARKLE(Particle.WAX_ON),
    SMOKE(Particle.SMOKE);

    private final Particle bukkitParticle;
    private BukkitTask effectTask;
    private LocationData location;

    ParticleFX(Particle bukkitParticle) {
        this.bukkitParticle = bukkitParticle;
    }

    @Override
    public void applyEffect(Player player) {
        this.effectTask = Bukkit.getServer().getScheduler().runTaskTimer(Core.getPlugin(), () -> { // synchronous
            player.spawnParticle(
                this.bukkitParticle,
                (double) this.location.getX(),
                (double) this.location.getY(),
                (double) this.location.getZ(),
                5
            );
        }, 0, 20);

    }

    @Override
    public void stopEffect() {
        this.effectTask.cancel();
    }

    /**
     * Set the location this particle spawns at.
     * @param location a location data object
     */
    public void setLocation(LocationData location) {
        this.location = location;
    }

    /**
     * Get the location this particle spawns at.
     * @return a location data object
     */
    public LocationData getLocation() {
        return this.location;
    }
}

package playerquests.product.fx;

import org.bukkit.Particle;

/**
 * Enum representing various particle-based effects.
 * Each effect corresponds to a specific type of visual particle effect.
 */
public enum ParticleFX {
    SPARKLE(Particle.WAX_ON),
    SMOKE(Particle.SMOKE);

    private final Particle bukkitParticle;

    ParticleFX(Particle bukkitParticle) {
        this.bukkitParticle = bukkitParticle;
    }

    /**
     * Get the bukkit particle this particleFX represents.
     * @return a particle in it's bukkit-api-friendly form.
     */
    public Particle toBukkitParticle() {
        return this.bukkitParticle;
    }
}

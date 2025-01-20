package playerquests.builder.fx;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import playerquests.builder.quest.data.LocationData;
import playerquests.product.FX;
import playerquests.product.fx.ParticleFX;

/**
 * Constructs a bunch of FXs to run at the same time.
 * - Fireworks shows! :D
 */
public class FXBuilder {

    private List<FX> effects = new ArrayList<>();

    public void addParticle(ParticleFX particleFX, LocationData location) {
        this.effects.add(new FX(location, particleFX));
    }

    /**
     * Get the list of effects in this FX builder.
     * @return a list of different effects
     */
    private List<FX> getEffects() {
        return this.effects;
    }

    /**
     * Run each effect in this builder.
     * @param player
     * @return
     */
    public List<FX> run(Player player) {
        List<FX> effects = this.getEffects();

        effects.forEach(effect -> {
            effect.applyEffect(player);
        });

        return effects;
    }
    
}

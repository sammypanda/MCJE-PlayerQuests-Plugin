package playerquests.product;

import org.bukkit.entity.Player;

/**
 * Represents a range of FX that can be applied.
 * It is implemented by multiple enums to provide different kinds of effects.
 */
public interface FX {
    /**
     * Start an effect task for this FX.
     * @param player the player to show the FX to
     */
    public void applyEffect(Player player);

    /**
     * Stop this effect.
     */
    public void stopEffect();
}

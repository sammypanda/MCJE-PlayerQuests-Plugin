package playerquests.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Helpful tools which can reduce the verbosity of GUI classes.
 */
public class GUIUtils {

    /**
     * Converts a {@link String} to an {@link ItemStack}.
     * @param item closest string to the {@link Material} ENUM
     * @return itemStack an instance of an {@link ItemStack} with the matching item 
     */
    public static ItemStack toItemStack(String item) {
        return new ItemStack(Material.matchMaterial(item, false));
    }
}

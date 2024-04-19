package playerquests.utility;

import org.bukkit.Material; // used to get a usable material instance
import org.bukkit.inventory.ItemStack; // important type used to fill GUI slots


/**
 * Helpful tools which can reduce the verbosity of handling blocks.
 */
public class MaterialUtils {

    /**
     * GUIUtils should not be instantiated.
     */
    private MaterialUtils() {
        throw new AssertionError("BlockUtils should not be instantiated.");
    }

    /**
     * Converts a {@link String} to an {@link ItemStack}.
     * @param item closest string to the {@link Material} ENUM
     * @return itemStack an instance of an {@link ItemStack} with the matching item 
     */
    public static ItemStack toItemStack(String item) {
        Material material = Material.matchMaterial(item); // get the actual material from the rough string
        
        if (material == null) { // if material doesn't exist
            throw new IllegalArgumentException( // when unable to resolve the item to a real Material
                "Invalid item, probably couldn't find the Material ENUM for: " + item // report that the material was null
            ); // this is important since ItemStack won't correctly construct with a missing material
        } else if (!material.isItem()) { // if material is not an item type
            material = Material.matchMaterial(material.getTranslationKey()); // last ditch effort to try to get the material as an item
        }

        return new ItemStack(material); // otherwise return a healthy ItemStack
    }
}

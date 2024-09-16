package playerquests.utility;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import org.bukkit.Material;

import playerquests.product.Quest;

/**
 * Utility class containing methods for common plugin-related operations.
 */
public class PluginUtils {

    /**
     * Should be accessed statically.
     */
    private PluginUtils() {}

    /**
     * Validates that the parameters match the expected types.
     * 
     * This method checks if the number and types of the provided parameters 
     * align with the expected types. It ensures that each parameter is an instance 
     * of the corresponding type specified in the expectedTypes array.
     * 
     * @param params The list of parameters to validate.
     * @param expectedTypes The array of expected types for the parameters.
     * @throws IllegalArgumentException If the number of parameters does not match 
     *                                  the number of expected types, or if any 
     *                                  parameter does not match its expected type.
     */
    public static void validateParams(List<Object> params, Class<?>... expectedTypes) throws IllegalArgumentException {
        Objects.requireNonNull(params, "Params cannot be null");

        // check if the size of the params list is the same as the size of the expectedTypes list
        if (params.size() != expectedTypes.length) {
            throw new IllegalArgumentException("Incorrect number of parameters");
        }

        // check with a filter if any param is not an instance of it's expected type
        IntStream.range(0, params.size())
        .filter(i -> !expectedTypes[i].isInstance(params.get(i)))
        .findFirst()
        .ifPresent(index -> {
            throw new IllegalArgumentException("Parameter at index " + index + " does not match the expected type");
        });
    }

    public static Map<Material, Integer> getPredictiveInventory(Quest quest, Map<Material, Integer> inventory) {
        // create inventory of required (and out of stock) and stocked
        Map<Material, Integer> predictiveInventory = new LinkedHashMap<>();
        Map<Material, Integer> requiredInventory = quest.getRequiredInventory();

        // put required items (as missing)
        if (requiredInventory != null) { 
            requiredInventory.forEach((material, count) -> predictiveInventory.put(material, -1)); 
        }
        
        // put stocked items (and replace/compensate for missing)
        if (inventory != null) { 
            predictiveInventory.putAll(inventory); 
        }
        
        return predictiveInventory;
    }
}

package playerquests.utility;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import playerquests.product.Quest;
import playerquests.utility.serialisable.ItemSerialisable;

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

    /**
     * Get the current inventory and what is needed in one map.
     * @param quest the quest to predict needed inventory for.
     * @param inventory the current inventory without any required.
     * @return the current inventory combined with the required one.
     */
    public static Map<ItemSerialisable, Integer> getPredictiveInventory(Quest quest, Map<ItemSerialisable, Integer> inventory) {
        Map<ItemSerialisable, Integer> predictiveInventory = new LinkedHashMap<>();
        Map<ItemSerialisable, Integer> requiredInventory = quest.getRequiredInventory();

        // Step 1: Subtract required amounts (initialize with negative values)
        if (requiredInventory != null) {
            requiredInventory.forEach((item, amount) ->
                predictiveInventory.merge(item, -amount, Integer::sum)
            );
        }

        // Step 2: Add inventory amounts (merge with existing values)
        if (inventory != null) {
            inventory.forEach((item, amount) ->
                predictiveInventory.merge(item, amount, Integer::sum)
            );
        }

        return predictiveInventory;
    }
}

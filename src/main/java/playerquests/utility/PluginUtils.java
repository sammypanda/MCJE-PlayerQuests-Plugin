package playerquests.utility;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Methods for repeated code in this plugin.
 */
public class PluginUtils {
    /**
     * Essential utility which checks that the params suit this meta action. 
     * @param params the values the meta action requires.
     * @param expectedTypes the type of values the meta action requires.
     * @throws IllegalArgumentException if the parameters are invalid.
     */
    public static void validateParams(ArrayList<Object> params, Class<?>... expectedTypes) throws IllegalArgumentException {
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
}

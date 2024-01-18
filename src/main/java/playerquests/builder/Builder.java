package playerquests.builder;

import java.io.IOException; // thrown if a file is invalid/not found

/**
 * Interface for builders.
 */
public interface Builder {
    /**
     * Resets the builder and product to defaults.
     */
    void reset();

    /**
     * Loads the product from a template file.
     * <p>
     * Expects the file name without .json on the end.
     * JSON template layout in README.
     * @param templateFile The name of the template json file excluding .json
     * @throws IOException when a file is invalid or not found
     */
    void load(String templateFile) throws IOException;

    /**
     * Loads the product from a template string.
     * <p>
     * JSON template layout in README.
     * @param templateJSONString the template json as a string
     */
    void parse(String templateJSONString);

    /**
     * Gets the resulting product built by the builder.
     * @return the instance of the product.
     */
    Object getResult();
}

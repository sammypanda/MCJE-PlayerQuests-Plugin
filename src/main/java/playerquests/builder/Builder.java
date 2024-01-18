package playerquests.builder;

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
     * @param templateFile a template file (without .json extension).
     */
    void load(String templateFile);

    /**
     * Loads the product from a template string.
     * @param templateJSONString a JSON template as a string.
     */
    void parse(String templateJSONString);

    /**
     * Gets the resulting product built by the builder.
     * @return the product.
     */
    Object getResult();
}

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
     * Gets the resulting product built by the builder.
     * @return the instance of the product.
     */
    Object getResult();
}

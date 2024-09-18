package playerquests.utility.singleton;

import java.lang.reflect.InvocationTargetException; // report if a method accessed via reflection cannot invoke
import java.lang.reflect.Method; // holds instances of methods
import java.security.InvalidParameterException;
import java.util.Arrays; // utilities for array literals
import java.util.HashMap; // handles a map of values, important for this key-pair pattern {@see keyRegistry}
import java.util.List; // utilities for list literals
import java.util.Map; // utilities for map literals
import java.util.Optional; // handles if values are null
import java.util.stream.Collectors; // additional functional actions on stream types

import playerquests.utility.annotation.Key; // the annotation used to define a key name

/**
 * Singleton for mapping a key string/name to a method anywhere in the plugin.
 * <ul>
 * <li>Used to set values for a class with just the key.</li>
 * </ul>
 * <pre>
 * Usage example:
 * <code>
 * {@literal @}Key("category.value")
 * </code>
 * </pre>
 */
public class KeyHandler {

    /**
     * Singleton instance of {@code KeyHandler}.
     */
    private static final KeyHandler instance = new KeyHandler();

    /**
     * Registry mapping class instances to their methods keyed by annotations.
     */
    private final Map<Object, Map<String, Method>> keyRegistry = new HashMap<>();

    /**
     * Private constructor to prevent instantiation.
     */
    private KeyHandler() {}

    /**
     * Returns the KeyHandler.
     * @return singleton instance of KeyHandler.
     */
    public static KeyHandler getInstance() {
        return instance;
    }

    /**
     * Gets a list of all registered class instances that have methods accessible by keys.
     *
     * @return A {@link List} of registered class instances.
     */
    public List<Object> getInstances() {
        return Arrays.asList(this.keyRegistry.keySet());
    }

    /**
     * Registers a class instance for key-based method access.
     * <p>Automatically maps methods annotated with {@link Key} to their respective keys.</p>
     *
     * @param classInstance The instance of the class to register.
     */
    public void registerInstance(Object classInstance) {
        // Form a map of key+method to later be invoked
        Map<String, Method> keyedMethods = Arrays.stream(classInstance.getClass().getDeclaredMethods()) // Get all methods directly in this class type
                .filter(method -> method.isAnnotationPresent(Key.class)) // Skip all that are not keyed
                .collect(Collectors.toMap(
                        method -> method.getAnnotation(Key.class).value(), // Key: key as string
                        method -> method // Value: method instance
                ));

        // Submit all keyed methods to keyRegistry
        this.keyRegistry.put(classInstance, keyedMethods);
    }

    /**
     * Deregisters a class instance, removing it from key-based access.
     *
     * @param classInstance The instance of the class to deregister.
     */
    public void deregisterInstance(Object classInstance) {
        this.keyRegistry.remove(classInstance);
    }

    /**
     * Sets a value for a given key in the specified class instance.
     * <p>Finds the method associated with the key and invokes it with the given value.</p>
     *
     * @param classInstance The instance of the class where the value should be set.
     * @param key The key associated with the method to invoke.
     * @param value The value to set.
     * @throws IllegalArgumentException if the class instance is not registered or the key is not found.
     */
    public void setValue(Object classInstance, String key, String value) {
        if (this.keyRegistry.get(classInstance) == null) {
            throw new IllegalArgumentException("Invalid instance to set a value in, it may have been deregistered: " + classInstance);
        }

        // get method and if found...
        Optional.ofNullable(this.keyRegistry.get(classInstance).get(key)).ifPresentOrElse(method -> {
            // try to invoke Method
            try {
                method.invoke(classInstance, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException("Could not invoke the " + method + " function", e);
            }
        }, () -> {
            throw new IllegalArgumentException("Could not find method according to the key: " + key);
        });
    }

    /**
     * Gets a value from the specified class instance based on the key.
     * <p>Finds the method associated with the key and invokes it to retrieve the value.</p>
     *
     * @param classInstance The instance of the class to get the value from.
     * @param match The key associated with the method to invoke.
     * @return The result of the method invocation.
     * @throws IllegalArgumentException if the class instance is not registered or the key is not found.
     */
    public Object getValue(Object classInstance, String match) {
        if (this.keyRegistry.get(classInstance) == null) {
            throw new IllegalArgumentException("Invalid instance to set a value in, it may have been deregistered: " + classInstance);
        }

        Method method = this.keyRegistry.get(classInstance).get(match);

        Object result;
        
        try {
            result = method.invoke(classInstance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Could not get value from the value: " + match);
        }
        
        return result;
    }

    /**
     * Retrieves the class type associated with a given key.
     * <p>Searches through registered class instances to find which class has a method corresponding to the key.</p>
     *
     * @param key The key to search for.
     * @return The {@link Class} associated with the key.
     * @throws InvalidParameterException if the key cannot be found in any registered class instance.
     */
    public Class<?> getClassFromKey(String key) throws InvalidParameterException {
        Class<?> keyType = keyRegistry.keySet().stream() // get a stream of the keyregistry entries
            .filter(classInstance -> keyRegistry.get(classInstance).containsKey(key)) // filtering for the key
            .findFirst() // stop the stream if match is found
            .map(Object::getClass) // get the class type of the classInstance
            .orElse(null); // otherwise set keyType to null

        if (keyType != null) {
            return keyType;
        } else {
            throw new InvalidParameterException(key + " could not be found in the registry.");
        }
    }
}

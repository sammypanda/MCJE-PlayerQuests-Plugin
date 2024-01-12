package playerquests.utils;

import java.lang.reflect.InvocationTargetException; // report if a method accessed via reflection cannot invoke
import java.lang.reflect.Method; // holds instances of methods
import java.util.ArrayList; // handles a list of values
import java.util.Arrays; // utilities for array literals
import java.util.HashMap; // handles a map of values, important for this key-pair pattern {@see keyRegistry}
import java.util.List; // utilities for list literals
import java.util.Map; // utilities for map literals
import java.util.Optional; // handles if values are null
import java.util.stream.Collectors; // additional functional actions on stream types

import playerquests.annotations.Key; // the annotation used to define a key name

/**
 * Singleton for mapping a key string/name to a method anywhere in the plugin.
 * <pre>
 * Usage example:
 * <code>
 * {@literal @}Key("category.value")
 * </code>
 * </pre>
 * <ul>
 * <li>Used to set values for a class with just the key.</li>
 * </ul>
 */
public class KeyHandler {

    private static final KeyHandler instance = new KeyHandler(); // this class as a Singleton
    private final Map<Object, Map<String, Method>> keyRegistry = new HashMap<>(); // list of keys accessible on each class instance

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
     * Gets a list of registered class instances that can be accessed with keys.
     * @return List of registered class instances.
     */
    public List<Object> getInstances() {
        return new ArrayList<>(this.keyRegistry.keySet());
    }

    /**
     * Registers a class instance to be accessed by a key
     * <p>
     * Identifies the key-method association automatically by the annotations.
     * @param classInstance instance of the class to register.
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
     * Deregisters a class instance from being accessed by a key.
     * @param classInstance instance of the class to deregister.
     */
    public void deregisterInstance(Object classInstance) {
        this.keyRegistry.remove(classInstance);
    }

    /**
     * Sets the value for a given key.
     * @param classInstance instance of the class to set the value in.
     * @param key key for which the value should be set.
     * @param value value to set.
     */
    public void setValue(Object classInstance, String key, String value) {
        // get method and if found...
        Optional.ofNullable(this.keyRegistry.get(classInstance).get(key)).ifPresentOrElse(method -> {
            // try to invoke Method
            try {
                System.out.println("Invoking " + method + " in " + classInstance + " with value: " + value);
                method.invoke(classInstance, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException("Could not invoke the " + method + " function", e);
            }
        }, () -> {
            throw new IllegalArgumentException("Could not find method according to the key: " + key);
        });
    }
}

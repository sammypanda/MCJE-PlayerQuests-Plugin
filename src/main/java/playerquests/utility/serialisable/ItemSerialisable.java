package playerquests.utility.serialisable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import playerquests.utility.serialisable.data.ItemData;

public final class ItemSerialisable implements Serialisable {
    private final ItemData itemData;
    private final Map<String, String> properties;

    // Public from string - use builder for other construction
    public ItemSerialisable(String string) {
        // Convert to GENERIC
        if (!string.contains("[")) {
            this.itemData = ItemData.fromString(string);
            this.properties = Map.of("material", string);
            return;
        }

        // get ItemData base and key-value pairs
        String[] parts = string.split("\\[|\\]");
        Map<String, String> keyValues = Arrays.stream(parts[1].split(";"))
            .map(pair -> pair.split(":"))
            .collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));
        ItemData base;

        // convert from GENERIC
        if (parts[0].equalsIgnoreCase("GENERIC")) {
            String materialString = keyValues.get("material");
            Material material = Material.valueOf(materialString);
            keyValues.remove("material");
            base = ItemData.fromMaterial(material);
            keyValues = base.extractProperties(new ItemStack(material));
        } else {
            base = ItemData.fromString(parts[0]);
        }

        // set final ItemData base and key-value properties
        this.itemData = base;
        this.properties = keyValues;
    }

    // Private constructor - use builder
    private ItemSerialisable(ItemData itemData, Map<String, String> properties) {
        this.itemData = Objects.requireNonNull(itemData);
        this.properties = Collections.unmodifiableMap(new HashMap<>(properties));
    }

    // Serialisation
    @Override
    public String toString() {
        if (properties.isEmpty()) return itemData.name();
        return itemData.name() + properties.entrySet().stream()
            .map(e -> e.getKey() + ":" + e.getValue())
            .collect(Collectors.joining(";", "[", "]"));
    }

    // Deserialisation
    @Override
    public Serialisable fromString(String string) {
        return new ItemSerialisable(string);
    }

    // Bukkit conversion (out of)
    public ItemStack toItemStack() {
        return itemData.createItem(properties);
    }

    // Bukkit conversion (into)
    public static ItemSerialisable fromItemStack(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        ItemData data = ItemData.fromMaterial(item.getType());
        if (data == null) throw new IllegalArgumentException("Unsupported item type " + item.getType());
        return new ItemSerialisable(data, data.extractProperties(item));
    }

    // Builder pattern
    public static class Builder {
        private final ItemData itemData;
        private final Map<String, String> properties = new HashMap<>();

        public Builder(ItemData itemData) {
            this.itemData = itemData;
        }

        public Builder with(String key, String value) {
            properties.put(key, value);
            return this;
        }

        public ItemSerialisable build() {
            return new ItemSerialisable(itemData, properties);
        }
    }

    // Getters
    public ItemData getItemData() { return itemData; }
    public Map<String, String> getProperties() { return properties; }

    // Equality Checking
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ItemSerialisable other = (ItemSerialisable) obj;
        return Objects.equals(itemData, other.itemData) &&
               Objects.equals(properties, other.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemData, properties);
    }
}

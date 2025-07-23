package playerquests.utility.serialisable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import net.citizensnpcs.api.npc.NPC;
import playerquests.utility.serialisable.data.EntityData;

public class EntitySerialisable implements Serialisable {
    private final EntityData entityData;
    private final Map<String, String> properties;

    // Public from string - use builder for other construction
    public EntitySerialisable(String string) {
        // Convert Spigot EntityType to our GENERIC
        if (!string.contains("[")) {
            this.entityData = EntityData.GENERIC;

            // TODO: remove me vvv
            // Convert old format to curr
            if (string.contains("type:")) {
                this.properties = Map.of(EntityData.getEntityKey(), string.split(":")[1].split(",")[0]);
                return;
            }

            this.properties = Map.of(EntityData.getEntityKey(), string);
            return;
        }

        // get EntityData base and key-value pairs
        String[] parts = string.split("\\[|\\]");
        Map<String, String> keyValues = Arrays.stream(parts[1].split(";"))
            .map(pair -> pair.split(":"))
            .collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));
        String baseString = parts[0];
        String typeString = keyValues.get("entity");

        EntityData entityData;
        // convert from GENERIC (GENERIC[entity:HERE]), otherwise use special EntityData base string (HERE[key:value])
        if (typeString != null && ( ! typeString.isEmpty())) {
            entityData = EntityData.getEnum(typeString);
        } else {
            entityData = EntityData.getEnum(baseString);
        }

        // spawn entity to wash properties (wash properties meaning cycle them to remove fake/unused ones)
        World world = Bukkit.getServer().getWorlds().getFirst();
        Location location = new Location(world, 0, -100, 0); // hidden location, requires spawning in world
        NPC citizen = entityData.createEntity(keyValues, location);
        citizen.getEntity().setInvisible(true); // hide the entity

        // set final EntitySerialisable data
        this.entityData = entityData;
        this.properties = entityData.extractProperties(citizen.getEntity());
        
        // remove data collection entity
        citizen.destroy();
    }

    // Private constructor - use builder
    private EntitySerialisable(EntityData entityData, Map<String, String> properties) {
        this.entityData = Objects.requireNonNull(entityData);
        this.properties = Collections.unmodifiableMap(new HashMap<>(properties));
    }

    @Override
    public Serialisable fromString(String string) {
        return new EntitySerialisable(string);
    }

    public static EntitySerialisable fromEntity(Entity entity) {
        if (entity == null) return null;
        EntityData data = EntityData.fromEntity(entity);
        if (data == null) throw new IllegalArgumentException("Unsupported entity type " + entity.getType());
        return new EntitySerialisable(data, data.extractProperties(entity));
    }

    // Getters
    public EntityData getEntityData() { return entityData; }
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
        EntitySerialisable other = (EntitySerialisable) obj;
        return Objects.equals(entityData, other.entityData) &&
               Objects.equals(properties, other.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityData, properties);
    }

    public NPC spawn(Location location) {
        return this.getEntityData().createEntity(this.getProperties(), location);
    }

    // Serialisation
    @Override
    public String toString() {
        if (properties.isEmpty()) return entityData.name();
        return entityData.name() + properties.entrySet().stream()
            .map(e -> e.getKey() + ":" + e.getValue())
            .collect(Collectors.joining(";", "[", "]"));
    }
}

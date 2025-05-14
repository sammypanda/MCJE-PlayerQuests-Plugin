package playerquests.utility.serialisable;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pose;

public class EntitySerialisable implements Serialisable {

    EntityType entityType = EntityType.VILLAGER;

    Pose entityPose = Pose.STANDING;

    Cat.Type catVariant;

    public EntitySerialisable(String entityTypeString) {
        this.fromString(entityTypeString);
    }

    public EntitySerialisable(Entity entity) {
        this.entityType = entity.getType();
        this.entityPose = entity.getPose();

        // custom entries for each entity
        switch (this.entityType) {
            case CAT:
                Cat cat = (Cat) entity;
                this.catVariant = cat.getCatType();
                break;
            default:
                break;
        }
    }

    @Override
    public Serialisable fromString(String serialised) {
        if (serialised == null || serialised.isEmpty()) {
            throw new IllegalArgumentException("Serialized string cannot be null or empty");
        }

        // get map of key value pairs of the entity attributes
        Map<String, String> data = Arrays.stream(serialised.split(","))
            .map(keyvalue -> keyvalue.split(":", 2))
            .collect(Collectors.toMap(
                keyvalue -> keyvalue[0],
                keyvalue -> keyvalue.length > 1 ? keyvalue[1] : ""
            ));

        // Get and validate entity type
        String entityTypeString = data.get("type");
        if (entityTypeString == null) {
            throw new IllegalArgumentException("Missing entity type in serialized data");
        }

        // resolve entity type from string
        try {
            this.entityType = EntityType.valueOf(entityTypeString);
        } catch (IllegalArgumentException _e) {
            this.entityType = EntityType.VILLAGER;
        }

        // set pose
        this.entityPose = Pose.valueOf(data.getOrDefault("pose", "STANDING"));

        switch (this.entityType) {
            case CAT:
                String catVariantString = data.get("cat_variant");

                if (catVariantString != null) {
                    NamespacedKey catVariantKey = NamespacedKey.fromString(catVariantString.toLowerCase());
                    this.catVariant = Registry.CAT_VARIANT.get(catVariantKey);
                }
                break;
            default:
                break;
        }

        return this;
    }

    @Override
    public String toString() {
        return String.format("type:%s,pose:%s,cat_variant:%s",
            this.getEntityType(),
            this.getPose(),
            this.getCatVariant()
        );
    }

    public EntityType getEntityType() {
        return this.entityType;
    }

    public Pose getPose() {
        return this.entityPose;
    }

    public Cat.Type getCatVariant() {
        return this.catVariant;
    }

    public Entity spawn(Location location) {
        EntityType entityType = this.getEntityType();

        if ( entityType == null ) {
            throw new IllegalStateException("Entity type missing from entity serialisable data");
        }

        Entity entity = location.getWorld().spawnEntity(location, entityType);

        switch (entityType) {
            case CAT:
                Cat cat = (Cat) entity;
                Cat.Type catVariant = this.getCatVariant();

                if (catVariant != null) {
                    cat.setCatType(catVariant);
                }

                cat.setSitting(this.entityPose == Pose.SITTING);
                break;
            default:
                break;
        }

        return entity;
    }
}

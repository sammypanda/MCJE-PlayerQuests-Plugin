package playerquests.utility.serialisable;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pose;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Rabbit.Type;

public class EntitySerialisable implements Serialisable {

    private EntityType entityType;

    private Pose entityPose;

    private Cat.Type catVariant;

    private Type rabbitVariant;

    private DyeColor sheepColor;

    public EntitySerialisable(String entityTypeString) {
        this.fromString(entityTypeString);
    }

    public EntitySerialisable(Entity entity) {
        this.entityType = entity.getType();
        this.entityPose = entity.getPose();

        // NOTE: custom entries for each entity here
        switch (this.entityType) {
            case CAT:
                Cat cat = (Cat) entity;
                this.catVariant = cat.getCatType();
                break;
            case RABBIT:
                Rabbit rabbit = (Rabbit) entity;
                this.rabbitVariant = rabbit.getRabbitType();
                break;
            case SHEEP:
                Sheep sheep = (Sheep) entity;
                this.sheepColor = sheep.getColor();
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
            this.entityType = EntityType.valueOf(entityTypeString.toLowerCase());
        } catch (IllegalArgumentException _e) {
            this.entityType = EntityType.VILLAGER;
        }

        // set pose
        this.entityPose = Pose.valueOf(data.getOrDefault("pose", "STANDING"));

        switch (this.entityType) {
            case CAT:
                String catVariantString = data.get("cat_variant");
                NamespacedKey catVariantKey = NamespacedKey.fromString(catVariantString.toLowerCase());
                this.catVariant = Registry.CAT_VARIANT.get(catVariantKey);
                break;
            case RABBIT:
                String rabbitVariantString = data.get("rabbit_variant");
                this.rabbitVariant = Rabbit.Type.valueOf(rabbitVariantString.toUpperCase());
            case SHEEP:
                String sheepColorString = data.get("sheep_color");
                this.sheepColor = DyeColor.valueOf(sheepColorString.toUpperCase());
            default:
                break;
        }

        return this;
    }

    public Entity spawn(Location location) {
        EntityType entityType = this.getEntityType();

        if ( entityType == null ) {
            throw new IllegalStateException("Entity type missing from entity serialisable data");
        }

        Entity entity = location.getWorld().spawnEntity(location, entityType);

        // NOTE: applying unique attributes to spawned entity here
        switch (entityType) {
            case CAT:
                Cat cat = (Cat) entity;
                Cat.Type catVariant = this.getCatVariant();

                cat.setCatType(catVariant);
                cat.setSitting(this.entityPose == Pose.SITTING);
                break;
            case RABBIT:
                Rabbit rabbit = (Rabbit) entity;
                Rabbit.Type rabbitVariant = this.getRabbitVariant();

                rabbit.setRabbitType(rabbitVariant);
                break;
            case SHEEP:
                Sheep sheep = (Sheep) entity;
                DyeColor sheepColor = this.getSheepColor();

                sheep.setColor(sheepColor);
            default:
                break;
        }

        return entity;
    }

    @Override
    public String toString() {
        // NOTE: add new unique attributes here:
        return String.format("type:%s,pose:%s,cat_variant:%s,rabbit_variant:%s,sheep_color:%s",
            this.getEntityType(),
            this.getPose(),
            this.getCatVariant(),
            this.getRabbitVariant(),
            this.getSheepColor()
        );
    }

    public EntityType getEntityType() {
        if (this.entityType == null) {
            return EntityType.VILLAGER;
        }

        return this.entityType;
    }

    public Pose getPose() {
        if (this.entityPose == null) {
            return Pose.STANDING;
        }

        return this.entityPose;
    }

    public Cat.Type getCatVariant() {
        if (this.catVariant == null) {
            return Cat.Type.ALL_BLACK;
        }

        return this.catVariant;
    }

    public Rabbit.Type getRabbitVariant() {
        if (this.rabbitVariant == null) {
            return Rabbit.Type.BLACK;
        }

        return this.rabbitVariant;
    }

    public DyeColor getSheepColor() {
        if (this.sheepColor == null) {
            return DyeColor.BLACK;
        }

        return this.sheepColor;
    }
}

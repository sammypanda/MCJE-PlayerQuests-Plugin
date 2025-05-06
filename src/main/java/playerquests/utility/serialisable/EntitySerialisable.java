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
            case ACACIA_BOAT:
                break;
            case ACACIA_CHEST_BOAT:
                break;
            case ALLAY:
                break;
            case AREA_EFFECT_CLOUD:
                break;
            case ARMADILLO:
                break;
            case ARMOR_STAND:
                break;
            case ARROW:
                break;
            case AXOLOTL:
                break;
            case BAMBOO_CHEST_RAFT:
                break;
            case BAMBOO_RAFT:
                break;
            case BAT:
                break;
            case BEE:
                break;
            case BIRCH_BOAT:
                break;
            case BIRCH_CHEST_BOAT:
                break;
            case BLAZE:
                break;
            case BLOCK_DISPLAY:
                break;
            case BOGGED:
                break;
            case BREEZE:
                break;
            case BREEZE_WIND_CHARGE:
                break;
            case CAMEL:
                break;
            case CAVE_SPIDER:
                break;
            case CHERRY_BOAT:
                break;
            case CHERRY_CHEST_BOAT:
                break;
            case CHEST_MINECART:
                break;
            case CHICKEN:
                break;
            case COD:
                break;
            case COMMAND_BLOCK_MINECART:
                break;
            case COW:
                break;
            case CREAKING:
                break;
            case CREEPER:
                break;
            case DARK_OAK_BOAT:
                break;
            case DARK_OAK_CHEST_BOAT:
                break;
            case DOLPHIN:
                break;
            case DONKEY:
                break;
            case DRAGON_FIREBALL:
                break;
            case DROWNED:
                break;
            case EGG:
                break;
            case ELDER_GUARDIAN:
                break;
            case ENDERMAN:
                break;
            case ENDERMITE:
                break;
            case ENDER_DRAGON:
                break;
            case ENDER_PEARL:
                break;
            case END_CRYSTAL:
                break;
            case EVOKER:
                break;
            case EVOKER_FANGS:
                break;
            case EXPERIENCE_BOTTLE:
                break;
            case EXPERIENCE_ORB:
                break;
            case EYE_OF_ENDER:
                break;
            case FALLING_BLOCK:
                break;
            case FIREBALL:
                break;
            case FIREWORK_ROCKET:
                break;
            case FISHING_BOBBER:
                break;
            case FOX:
                break;
            case FROG:
                break;
            case FURNACE_MINECART:
                break;
            case GHAST:
                break;
            case GIANT:
                break;
            case GLOW_ITEM_FRAME:
                break;
            case GLOW_SQUID:
                break;
            case GOAT:
                break;
            case GUARDIAN:
                break;
            case HOGLIN:
                break;
            case HOPPER_MINECART:
                break;
            case HORSE:
                break;
            case HUSK:
                break;
            case ILLUSIONER:
                break;
            case INTERACTION:
                break;
            case IRON_GOLEM:
                break;
            case ITEM:
                break;
            case ITEM_DISPLAY:
                break;
            case ITEM_FRAME:
                break;
            case JUNGLE_BOAT:
                break;
            case JUNGLE_CHEST_BOAT:
                break;
            case LEASH_KNOT:
                break;
            case LIGHTNING_BOLT:
                break;
            case LLAMA:
                break;
            case LLAMA_SPIT:
                break;
            case MAGMA_CUBE:
                break;
            case MANGROVE_BOAT:
                break;
            case MANGROVE_CHEST_BOAT:
                break;
            case MARKER:
                break;
            case MINECART:
                break;
            case MOOSHROOM:
                break;
            case MULE:
                break;
            case OAK_BOAT:
                break;
            case OAK_CHEST_BOAT:
                break;
            case OCELOT:
                break;
            case OMINOUS_ITEM_SPAWNER:
                break;
            case PAINTING:
                break;
            case PALE_OAK_BOAT:
                break;
            case PALE_OAK_CHEST_BOAT:
                break;
            case PANDA:
                break;
            case PARROT:
                break;
            case PHANTOM:
                break;
            case PIG:
                break;
            case PIGLIN:
                break;
            case PIGLIN_BRUTE:
                break;
            case PILLAGER:
                break;
            case PLAYER:
                break;
            case POLAR_BEAR:
                break;
            case POTION:
                break;
            case PUFFERFISH:
                break;
            case RABBIT:
                break;
            case RAVAGER:
                break;
            case SALMON:
                break;
            case SHEEP:
                break;
            case SHULKER:
                break;
            case SHULKER_BULLET:
                break;
            case SILVERFISH:
                break;
            case SKELETON:
                break;
            case SKELETON_HORSE:
                break;
            case SLIME:
                break;
            case SMALL_FIREBALL:
                break;
            case SNIFFER:
                break;
            case SNOWBALL:
                break;
            case SNOW_GOLEM:
                break;
            case SPAWNER_MINECART:
                break;
            case SPECTRAL_ARROW:
                break;
            case SPIDER:
                break;
            case SPRUCE_BOAT:
                break;
            case SPRUCE_CHEST_BOAT:
                break;
            case SQUID:
                break;
            case STRAY:
                break;
            case STRIDER:
                break;
            case TADPOLE:
                break;
            case TEXT_DISPLAY:
                break;
            case TNT:
                break;
            case TNT_MINECART:
                break;
            case TRADER_LLAMA:
                break;
            case TRIDENT:
                break;
            case TROPICAL_FISH:
                break;
            case TURTLE:
                break;
            case UNKNOWN:
                break;
            case VEX:
                break;
            case VILLAGER:
                break;
            case VINDICATOR:
                break;
            case WANDERING_TRADER:
                break;
            case WARDEN:
                break;
            case WIND_CHARGE:
                break;
            case WITCH:
                break;
            case WITHER:
                break;
            case WITHER_SKELETON:
                break;
            case WITHER_SKULL:
                break;
            case WOLF:
                break;
            case ZOGLIN:
                break;
            case ZOMBIE:
                break;
            case ZOMBIE_HORSE:
                break;
            case ZOMBIE_VILLAGER:
                break;
            case ZOMBIFIED_PIGLIN:
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
            case ACACIA_BOAT:
                break;
            case ACACIA_CHEST_BOAT:
                break;
            case ALLAY:
                break;
            case AREA_EFFECT_CLOUD:
                break;
            case ARMADILLO:
                break;
            case ARMOR_STAND:
                break;
            case ARROW:
                break;
            case AXOLOTL:
                break;
            case BAMBOO_CHEST_RAFT:
                break;
            case BAMBOO_RAFT:
                break;
            case BAT:
                break;
            case BEE:
                break;
            case BIRCH_BOAT:
                break;
            case BIRCH_CHEST_BOAT:
                break;
            case BLAZE:
                break;
            case BLOCK_DISPLAY:
                break;
            case BOGGED:
                break;
            case BREEZE:
                break;
            case BREEZE_WIND_CHARGE:
                break;
            case CAMEL:
                break;
            case CAT:
                String catVariantString = data.get("cat_variant");

                if (catVariantString != null) {
                    NamespacedKey catVariantKey = NamespacedKey.fromString(catVariantString.toLowerCase());
                    this.catVariant = Registry.CAT_VARIANT.get(catVariantKey);
                }
                break;
            case CAVE_SPIDER:
                break;
            case CHERRY_BOAT:
                break;
            case CHERRY_CHEST_BOAT:
                break;
            case CHEST_MINECART:
                break;
            case CHICKEN:
                break;
            case COD:
                break;
            case COMMAND_BLOCK_MINECART:
                break;
            case COW:
                break;
            case CREAKING:
                break;
            case CREEPER:
                break;
            case DARK_OAK_BOAT:
                break;
            case DARK_OAK_CHEST_BOAT:
                break;
            case DOLPHIN:
                break;
            case DONKEY:
                break;
            case DRAGON_FIREBALL:
                break;
            case DROWNED:
                break;
            case EGG:
                break;
            case ELDER_GUARDIAN:
                break;
            case ENDERMAN:
                break;
            case ENDERMITE:
                break;
            case ENDER_DRAGON:
                break;
            case ENDER_PEARL:
                break;
            case END_CRYSTAL:
                break;
            case EVOKER:
                break;
            case EVOKER_FANGS:
                break;
            case EXPERIENCE_BOTTLE:
                break;
            case EXPERIENCE_ORB:
                break;
            case EYE_OF_ENDER:
                break;
            case FALLING_BLOCK:
                break;
            case FIREBALL:
                break;
            case FIREWORK_ROCKET:
                break;
            case FISHING_BOBBER:
                break;
            case FOX:
                break;
            case FROG:
                break;
            case FURNACE_MINECART:
                break;
            case GHAST:
                break;
            case GIANT:
                break;
            case GLOW_ITEM_FRAME:
                break;
            case GLOW_SQUID:
                break;
            case GOAT:
                break;
            case GUARDIAN:
                break;
            case HOGLIN:
                break;
            case HOPPER_MINECART:
                break;
            case HORSE:
                break;
            case HUSK:
                break;
            case ILLUSIONER:
                break;
            case INTERACTION:
                break;
            case IRON_GOLEM:
                break;
            case ITEM:
                break;
            case ITEM_DISPLAY:
                break;
            case ITEM_FRAME:
                break;
            case JUNGLE_BOAT:
                break;
            case JUNGLE_CHEST_BOAT:
                break;
            case LEASH_KNOT:
                break;
            case LIGHTNING_BOLT:
                break;
            case LLAMA:
                break;
            case LLAMA_SPIT:
                break;
            case MAGMA_CUBE:
                break;
            case MANGROVE_BOAT:
                break;
            case MANGROVE_CHEST_BOAT:
                break;
            case MARKER:
                break;
            case MINECART:
                break;
            case MOOSHROOM:
                break;
            case MULE:
                break;
            case OAK_BOAT:
                break;
            case OAK_CHEST_BOAT:
                break;
            case OCELOT:
                break;
            case OMINOUS_ITEM_SPAWNER:
                break;
            case PAINTING:
                break;
            case PALE_OAK_BOAT:
                break;
            case PALE_OAK_CHEST_BOAT:
                break;
            case PANDA:
                break;
            case PARROT:
                break;
            case PHANTOM:
                break;
            case PIG:
                break;
            case PIGLIN:
                break;
            case PIGLIN_BRUTE:
                break;
            case PILLAGER:
                break;
            case PLAYER:
                break;
            case POLAR_BEAR:
                break;
            case POTION:
                break;
            case PUFFERFISH:
                break;
            case RABBIT:
                break;
            case RAVAGER:
                break;
            case SALMON:
                break;
            case SHEEP:
                break;
            case SHULKER:
                break;
            case SHULKER_BULLET:
                break;
            case SILVERFISH:
                break;
            case SKELETON:
                break;
            case SKELETON_HORSE:
                break;
            case SLIME:
                break;
            case SMALL_FIREBALL:
                break;
            case SNIFFER:
                break;
            case SNOWBALL:
                break;
            case SNOW_GOLEM:
                break;
            case SPAWNER_MINECART:
                break;
            case SPECTRAL_ARROW:
                break;
            case SPIDER:
                break;
            case SPRUCE_BOAT:
                break;
            case SPRUCE_CHEST_BOAT:
                break;
            case SQUID:
                break;
            case STRAY:
                break;
            case STRIDER:
                break;
            case TADPOLE:
                break;
            case TEXT_DISPLAY:
                break;
            case TNT:
                break;
            case TNT_MINECART:
                break;
            case TRADER_LLAMA:
                break;
            case TRIDENT:
                break;
            case TROPICAL_FISH:
                break;
            case TURTLE:
                break;
            case UNKNOWN:
                break;
            case VEX:
                break;
            case VILLAGER:
                break;
            case VINDICATOR:
                break;
            case WANDERING_TRADER:
                break;
            case WARDEN:
                break;
            case WIND_CHARGE:
                break;
            case WITCH:
                break;
            case WITHER:
                break;
            case WITHER_SKELETON:
                break;
            case WITHER_SKULL:
                break;
            case WOLF:
                break;
            case ZOGLIN:
                break;
            case ZOMBIE:
                break;
            case ZOMBIE_HORSE:
                break;
            case ZOMBIE_VILLAGER:
                break;
            case ZOMBIFIED_PIGLIN:
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
            case ACACIA_BOAT:
                break;
            case ACACIA_CHEST_BOAT:
                break;
            case ALLAY:
                break;
            case AREA_EFFECT_CLOUD:
                break;
            case ARMADILLO:
                break;
            case ARMOR_STAND:
                break;
            case ARROW:
                break;
            case AXOLOTL:
                break;
            case BAMBOO_CHEST_RAFT:
                break;
            case BAMBOO_RAFT:
                break;
            case BAT:
                break;
            case BEE:
                break;
            case BIRCH_BOAT:
                break;
            case BIRCH_CHEST_BOAT:
                break;
            case BLAZE:
                break;
            case BLOCK_DISPLAY:
                break;
            case BOGGED:
                break;
            case BREEZE:
                break;
            case BREEZE_WIND_CHARGE:
                break;
            case CAMEL:
                break;
            case CAT:
                Cat cat = (Cat) entity;
                Cat.Type catVariant = this.getCatVariant();

                if (catVariant != null) {
                    cat.setCatType(catVariant);
                }

                cat.setSitting(this.entityPose == Pose.SITTING);
                break;
            case CAVE_SPIDER:
                break;
            case CHERRY_BOAT:
                break;
            case CHERRY_CHEST_BOAT:
                break;
            case CHEST_MINECART:
                break;
            case CHICKEN:
                break;
            case COD:
                break;
            case COMMAND_BLOCK_MINECART:
                break;
            case COW:
                break;
            case CREAKING:
                break;
            case CREEPER:
                break;
            case DARK_OAK_BOAT:
                break;
            case DARK_OAK_CHEST_BOAT:
                break;
            case DOLPHIN:
                break;
            case DONKEY:
                break;
            case DRAGON_FIREBALL:
                break;
            case DROWNED:
                break;
            case EGG:
                break;
            case ELDER_GUARDIAN:
                break;
            case ENDERMAN:
                break;
            case ENDERMITE:
                break;
            case ENDER_DRAGON:
                break;
            case ENDER_PEARL:
                break;
            case END_CRYSTAL:
                break;
            case EVOKER:
                break;
            case EVOKER_FANGS:
                break;
            case EXPERIENCE_BOTTLE:
                break;
            case EXPERIENCE_ORB:
                break;
            case EYE_OF_ENDER:
                break;
            case FALLING_BLOCK:
                break;
            case FIREBALL:
                break;
            case FIREWORK_ROCKET:
                break;
            case FISHING_BOBBER:
                break;
            case FOX:
                break;
            case FROG:
                break;
            case FURNACE_MINECART:
                break;
            case GHAST:
                break;
            case GIANT:
                break;
            case GLOW_ITEM_FRAME:
                break;
            case GLOW_SQUID:
                break;
            case GOAT:
                break;
            case GUARDIAN:
                break;
            case HOGLIN:
                break;
            case HOPPER_MINECART:
                break;
            case HORSE:
                break;
            case HUSK:
                break;
            case ILLUSIONER:
                break;
            case INTERACTION:
                break;
            case IRON_GOLEM:
                break;
            case ITEM:
                break;
            case ITEM_DISPLAY:
                break;
            case ITEM_FRAME:
                break;
            case JUNGLE_BOAT:
                break;
            case JUNGLE_CHEST_BOAT:
                break;
            case LEASH_KNOT:
                break;
            case LIGHTNING_BOLT:
                break;
            case LLAMA:
                break;
            case LLAMA_SPIT:
                break;
            case MAGMA_CUBE:
                break;
            case MANGROVE_BOAT:
                break;
            case MANGROVE_CHEST_BOAT:
                break;
            case MARKER:
                break;
            case MINECART:
                break;
            case MOOSHROOM:
                break;
            case MULE:
                break;
            case OAK_BOAT:
                break;
            case OAK_CHEST_BOAT:
                break;
            case OCELOT:
                break;
            case OMINOUS_ITEM_SPAWNER:
                break;
            case PAINTING:
                break;
            case PALE_OAK_BOAT:
                break;
            case PALE_OAK_CHEST_BOAT:
                break;
            case PANDA:
                break;
            case PARROT:
                break;
            case PHANTOM:
                break;
            case PIG:
                break;
            case PIGLIN:
                break;
            case PIGLIN_BRUTE:
                break;
            case PILLAGER:
                break;
            case PLAYER:
                break;
            case POLAR_BEAR:
                break;
            case POTION:
                break;
            case PUFFERFISH:
                break;
            case RABBIT:
                break;
            case RAVAGER:
                break;
            case SALMON:
                break;
            case SHEEP:
                break;
            case SHULKER:
                break;
            case SHULKER_BULLET:
                break;
            case SILVERFISH:
                break;
            case SKELETON:
                break;
            case SKELETON_HORSE:
                break;
            case SLIME:
                break;
            case SMALL_FIREBALL:
                break;
            case SNIFFER:
                break;
            case SNOWBALL:
                break;
            case SNOW_GOLEM:
                break;
            case SPAWNER_MINECART:
                break;
            case SPECTRAL_ARROW:
                break;
            case SPIDER:
                break;
            case SPRUCE_BOAT:
                break;
            case SPRUCE_CHEST_BOAT:
                break;
            case SQUID:
                break;
            case STRAY:
                break;
            case STRIDER:
                break;
            case TADPOLE:
                break;
            case TEXT_DISPLAY:
                break;
            case TNT:
                break;
            case TNT_MINECART:
                break;
            case TRADER_LLAMA:
                break;
            case TRIDENT:
                break;
            case TROPICAL_FISH:
                break;
            case TURTLE:
                break;
            case UNKNOWN:
                break;
            case VEX:
                break;
            case VILLAGER:
                break;
            case VINDICATOR:
                break;
            case WANDERING_TRADER:
                break;
            case WARDEN:
                break;
            case WIND_CHARGE:
                break;
            case WITCH:
                break;
            case WITHER:
                break;
            case WITHER_SKELETON:
                break;
            case WITHER_SKULL:
                break;
            case WOLF:
                break;
            case ZOGLIN:
                break;
            case ZOMBIE:
                break;
            case ZOMBIE_HORSE:
                break;
            case ZOMBIE_VILLAGER:
                break;
            case ZOMBIFIED_PIGLIN:
                break;
            default:
                break;
        }

        return entity;
    }
}

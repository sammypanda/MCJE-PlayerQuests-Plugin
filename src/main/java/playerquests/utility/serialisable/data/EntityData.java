package playerquests.utility.serialisable.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.text.WordUtils;
import org.bukkit.Location;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Chicken.Variant;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.ChickenVariantKeys;
import net.citizensnpcs.api.npc.NPC;
import net.kyori.adventure.key.Key;
import playerquests.utility.ChatUtils;
import playerquests.utility.ChatUtils.MessageStyle;
import playerquests.utility.ChatUtils.MessageTarget;
import playerquests.utility.ChatUtils.MessageType;
import playerquests.utility.singleton.PlayerQuests;

public enum EntityData {
    GENERIC {
        @Override
        public NPC createEntity(Map<String, String> properties, Location location) {
            EntityType entityType = EntityType.valueOf(properties.get(EntityData.getEntityKey()));
            NPC citizen = PlayerQuests.getInstance().getCitizensRegistry().createNPC(this.getEntityType(properties), "", location);
            if (!isAllowedGeneric(entityType)) {
                warnUnimplemented(entityType);
            }
            return basicEntity(citizen, properties);
        }

        @Override
        public Map<String, String> extractProperties(Entity entity) {
            return basicProperties(entity, Map.of(EntityData.getEntityKey(), entity.getType().name()));
        }

        @Override
        public String getName(Map<String, String> properties) {
            return formatText(properties.get(EntityData.getEntityKey()));
        }

        @Override
        public EntityType getEntityType(Map<String, String> properties) {
            return EntityType.fromName(properties.get(EntityData.getEntityKey()));
        }
    },
    CHICKEN {
        @Override
        public NPC createEntity(Map<String, String> properties, Location location) {
            NPC citizen = PlayerQuests.getInstance().getCitizensRegistry().createNPC(this.getEntityType(properties), "", location);
            Chicken entity = (Chicken) citizen.getEntity();

            // apply chicken variant
            Chicken.Variant variant = Optional.ofNullable(RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.CHICKEN_VARIANT)
                .get(ChickenVariantKeys.create(Key.key(
                    properties.getOrDefault("variant", "temperate"))
                ))).orElse(Variant.TEMPERATE);
            entity.setVariant(variant);

            // apply basic properties
            return basicEntity(citizen, properties);
        }

        @Override
        public Map<String, String> extractProperties(Entity entity) {
            Chicken chicken = (Chicken) entity;
            Chicken.Variant variant = chicken.getVariant();

            return basicProperties(entity, Map.of("variant", variant.getKey().asMinimalString()));
        }

        @Override
        public String getName(Map<String, String> properties) {
            return formatText(this.getEntityType(properties).name());
        }

        @Override
        public EntityType getEntityType(Map<String, String> properties) {
            return EntityType.CHICKEN;
        }
    },
    VILLAGER {
        @Override
        public NPC createEntity(Map<String, String> properties, Location location) {
            return PlayerQuests.getInstance().getCitizensRegistry().createNPC(this.getEntityType(properties), "", location);
        }

        @Override
        public Map<String, String> extractProperties(Entity entity) {
            return basicProperties(entity, Map.of()); // TODO: add special properties here
        }

        @Override
        public String getName(Map<String, String> properties) {
            return formatText(this.getEntityType(properties).name());
        }

        @Override
        public EntityType getEntityType(Map<String, String> properties) {
            return EntityType.VILLAGER;
        }
    };

    private static final Set<EntityType> ALLOWED_GENERIC_ENTITIES = new HashSet<>(Arrays.asList(
        EntityType.RABBIT,
        EntityType.WOLF
    ));

    // Bukkit EntityType mappings
    private static final Map<EntityType, EntityData> SPECIAL_MAPPINGS = new HashMap<>();
    static {
        SPECIAL_MAPPINGS.put(EntityType.CHICKEN, CHICKEN);
    }

    private static boolean isAllowedGeneric(EntityType entity) {
        return ALLOWED_GENERIC_ENTITIES.contains(entity);
    }

    Map<String, String> basicProperties(Entity entity, Map<String, String> properties) {
        return properties; // TODO: ADD THINGS LIKE IF ITS A BABY OR ADULT
    }

    String formatText(String string) {
        return WordUtils.capitalizeFully(
            string.replaceAll("_+", " ")
        );
    }

    private static final String entityTypeKey = "entity";

    /**
     * Adds basic entity properties to an entity.
     * Used to supplement creating repetitive "special mapping" entities.
     * @param citizen the NPC with custom changes already made (can be unmodified)
     * @param properties the current properties set (can be empty)
     * @return a modified entity
     */
    private static NPC basicEntity(NPC citizen, Map<String, String> properties) {
        return citizen; // TODO: implement application of basic entity properties
    }

    // Add these new members at the bottom of the class
    private static final Set<EntityType> warnedEntities = Collections.synchronizedSet(new HashSet<>());

    private static void warnUnimplemented(EntityType entity) {
        if (!warnedEntities.contains(entity)) {
            warnedEntities.add(entity);
            ChatUtils.message("Unimplemented EntityData detected: " + entity +
            ". Using generic fallback. Please report this message to sammypanda!")
                .target(MessageTarget.CONSOLE)
                .type(MessageType.WARN)
                .style(MessageStyle.SIMPLE)
                .send();
        }
    }

    public static EntityData getEnum(String string) {
        // First try to find matching ItemData (excluding GENERIC)
        Optional<EntityData> entityDataMatch = Arrays.stream(values())
            .filter(e -> !e.equals(GENERIC))
            .filter(e -> e.name().equalsIgnoreCase(string))
            .findFirst();
        if (entityDataMatch.isPresent()) {
            return entityDataMatch.get();
        }

        // No matches found - complain and return VILLAGER
        ChatUtils.message("Unknown entity type: " + string + ". Defaulting to VILLAGER. 💔")
            .target(MessageTarget.CONSOLE)
            .type(MessageType.ERROR)
            .style(MessageStyle.PRETTY)
            .send();
        return EntityData.VILLAGER;
    }

    public static EntityData fromEntity(Entity entity) {
        // 1. Return special handler if exists
        EntityData specialHandler = SPECIAL_MAPPINGS.get(entity.getType());
        if (specialHandler != null) return specialHandler;

        // 2. Return GENERIC (will warn if not allowed)
        return GENERIC;
    }

    public static String getEntityKey() {
        return entityTypeKey;
    }

    // Core interface methods
    public abstract NPC createEntity(Map<String, String> properties, Location location);
    public abstract Map<String, String> extractProperties(Entity entity);
    public abstract String getName(Map<String, String> properties);
    public abstract EntityType getEntityType(Map<String, String> properties);
}

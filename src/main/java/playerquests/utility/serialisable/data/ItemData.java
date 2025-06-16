package playerquests.utility.serialisable.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionType;


import playerquests.utility.ChatUtils;
import playerquests.utility.ChatUtils.MessageStyle;
import playerquests.utility.ChatUtils.MessageTarget;
import playerquests.utility.ChatUtils.MessageType;

public enum ItemData {
    // Generic handler
    GENERIC {
        @Override
        public ItemStack createItem(Map<String, String> properties) {
            Material material = Material.valueOf(properties.get("material"));
            if (!isAllowedGeneric(material)) {
                warnUnimplemented(material);
            }
            return new ItemStack(material);
        }

        @Override
        public Map<String, String> extractProperties(ItemStack item) {
            return Map.of("material", item.getType().name());
        }
    },

    POTION {
        @Override
        public ItemStack createItem(Map<String, String> properties) {
            ItemStack item = new ItemStack(Material.POTION);
            PotionMeta meta = (PotionMeta) item.getItemMeta();

            PotionType type = PotionType.valueOf(
                properties.getOrDefault("type", "WATER").toUpperCase()
            );
            boolean upgraded = Boolean.parseBoolean(properties.get("upgraded"));
            meta.setBasePotionType(type);

            item.setItemMeta(meta);
            return item;
        }

        @Override
        public Map<String, String> extractProperties(ItemStack item) {
            Map<String, String> props = new HashMap<>();

            // get potion meta if exists
            PotionMeta meta = (item != null && item.getItemMeta() instanceof PotionMeta)
                ? (PotionMeta) item.getItemMeta()
                : null;

            // get potion type or default to 0th potion type
            PotionType type = (meta != null && meta.getBasePotionType() != null)
                ? meta.getBasePotionType()
                : PotionType.values()[0];

            // set properties - type can't be null at this point
            props.put("type", type.name());

            return props;
        }
    },

    WOOL {
        @Override
        public ItemStack createItem(Map<String, String> properties) {
            String color = properties.getOrDefault("color", "WHITE");
            return new ItemStack(Material.valueOf(color + "_WOOL"));
        }

        @Override
        public Map<String, String> extractProperties(ItemStack item) {
            String color = item.getType().name().replace("_WOOL", "");
            return Map.of("color", color);
        }
    },

    PLAYER_HEAD {
        @Override
        public ItemStack createItem(Map<String, String> properties) {
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(properties.get("player")));
            item.setItemMeta(meta);
            return item;
        }

        @Override
        public Map<String, String> extractProperties(ItemStack item) {
            Map<String, String> props = new HashMap<>();

            // get head meta info if exists
            SkullMeta meta = (item != null && item.getItemMeta() instanceof SkullMeta)
                ? (SkullMeta) item.getItemMeta()
                : null;

            // get head data or default data
            String playerName = (meta != null && meta.hasOwner())
                ? meta.getOwningPlayer().getName()
                : "UNKNOWN";

            props.put("player", playerName);
            return props;
        }
    },

    SOUP {
        @Override
        public ItemStack createItem(Map<String, String> properties) {
            String flavour = properties.getOrDefault("flavour", "BEETROOT");
            return new ItemStack(Material.valueOf(flavour + "_SOUP"));
        }

        @Override
        public Map<String, String> extractProperties(ItemStack item) {
            String flavour = item.getType().name().replace("_SOUP", "");
            return Map.of("flavour", flavour);
        }
    },

    AIR {
		@Override
		public ItemStack createItem(Map<String, String> properties) {
		    return new ItemStack(Material.AIR);
		}

		@Override
		public Map<String, String> extractProperties(ItemStack item) {
		    return Map.of();
		}
	};

    // Explicit allowlist of materials that can use GENERIC without warnings
    private static final Set<Material> ALLOWED_GENERIC_MATERIALS = new HashSet<>(Arrays.asList(
        Material.DIAMOND,
        Material.EMERALD,
        Material.GOLD_INGOT,
        Material.IRON_INGOT,
        Material.COAL,
        Material.REDSTONE,
        Material.LAPIS_LAZULI,
        Material.QUARTZ,
        Material.STONE,
        Material.COBBLESTONE,
        Material.PAPER
    ));

    private static boolean isAllowedGeneric(Material material) {
        return ALLOWED_GENERIC_MATERIALS.contains(material);
    }

    // Bukkit material mappings
    private static final Map<Material, ItemData> SPECIAL_MAPPINGS = new HashMap<>();
    static {
        SPECIAL_MAPPINGS.put(Material.POTION, POTION);
        SPECIAL_MAPPINGS.put(Material.SPLASH_POTION, POTION);
        SPECIAL_MAPPINGS.put(Material.LINGERING_POTION, POTION);
        Arrays.stream(Material.values()).filter(m -> m.name().endsWith("_WOOL")).forEach(m -> SPECIAL_MAPPINGS.put(m, WOOL));
        SPECIAL_MAPPINGS.put(Material.PLAYER_HEAD, PLAYER_HEAD);
        Arrays.stream(Material.values()).filter(m -> m.name().endsWith("_SOUP")).forEach(m -> SPECIAL_MAPPINGS.put(m, SOUP));
    }

    // Add these new members at the bottom of the class
    private static final Set<Material> warnedMaterials = Collections.synchronizedSet(new HashSet<>());

    private static void warnUnimplemented(Material material) {
        if (!warnedMaterials.contains(material)) {
            warnedMaterials.add(material);
            ChatUtils.message("Unimplemented ItemData detected: " + material +
            ". Using generic fallback. Please report this message to sammypanda!")
                .target(MessageTarget.CONSOLE)
                .type(MessageType.WARN)
                .style(MessageStyle.SIMPLE)
                .send();
        }
    }

    public static ItemData fromMaterial(Material material) {
        // 1. Return special handler if exists
        ItemData specialHandler = SPECIAL_MAPPINGS.get(material);
        if (specialHandler != null) return specialHandler;

        // 2. Return GENERIC (will warn if not allowed)
        return GENERIC;
    }

    public static ItemData fromString(String string) {
        // 1. First try to find matching ItemData (excluding GENERIC)
        Optional<ItemData> itemDataMatch = Arrays.stream(values())
            .filter(e -> !e.equals(GENERIC))
            .filter(e -> e.name().equalsIgnoreCase(string))
            .findFirst();

        if (itemDataMatch.isPresent()) {
            return itemDataMatch.get();
        }

        // 2. Try to match with Material enum
        try {
            Material material = Material.valueOf(string.toUpperCase());
            return fromMaterial(material);
        } catch (IllegalArgumentException _e) {
            // Material not found, continue to fallback
        }

        // 3. No matches found - complain and return AIR
        ChatUtils.message("Unknown item type: " + string + ". Defaulting to AIR. 💔")
            .target(MessageTarget.CONSOLE)
            .type(MessageType.ERROR)
            .style(MessageStyle.PRETTY)
            .send();
        return ItemData.AIR;
    }

    // Core interface methods
    public abstract ItemStack createItem(Map<String, String> properties);
    public abstract Map<String, String> extractProperties(ItemStack item);
}

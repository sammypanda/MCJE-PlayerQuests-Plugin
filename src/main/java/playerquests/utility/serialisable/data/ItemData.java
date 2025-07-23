package playerquests.utility.serialisable.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionType;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.TrimMaterialKeys;
import io.papermc.paper.registry.keys.TrimPatternKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import playerquests.utility.ChatUtils;
import playerquests.utility.ChatUtils.MessageStyle;
import playerquests.utility.ChatUtils.MessageTarget;
import playerquests.utility.ChatUtils.MessageType;

public enum ItemData {
    // Generic handler
    GENERIC {
        @Override
        public ItemStack createItem(Map<String, String> properties) {
            Material material = Material.valueOf(properties.get(ItemData.getMaterialKey()));
            if (!isAllowedGeneric(material)) {
                warnUnimplemented(material);
            }
            return basicItem(new ItemStack(material), properties);
        }

        @Override
        public Map<String, String> extractProperties(ItemStack item) {
            return basicProperties(item, Map.of(ItemData.getMaterialKey(), item.getType().name()));
        }

        @Override
        public String getName(Map<String, String> properties) {
            return formatText(properties.get(ItemData.getMaterialKey()));
        }

        @Override
        protected boolean includes(Material m) {
            return true; // all* materials valid to apply
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
            meta.setBasePotionType(type);

            item.setItemMeta(meta);
            return basicItem(item, properties);
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

            return basicProperties(item, props);
        }

        @Override
        public String getName(Map<String, String> properties) {
            if ( ! properties.containsKey("type")) {
                return "Potion";
            }

            PotionType type = PotionType.valueOf(properties.get("type"));
            return formatText(type.toString() + " Potion");
        }

        @Override
        protected boolean includes(Material m) {
            return m.equals(Material.POTION);
        }
    },

    WOOL {
        private static String colorKey = "color";
        private static String woolSuffix = "_WOOL";

        @Override
        public ItemStack createItem(Map<String, String> properties) {
            String color = properties.getOrDefault(colorKey, "WHITE");
            return basicItem(new ItemStack(Material.valueOf(color + woolSuffix)), properties);
        }

        @Override
        public Map<String, String> extractProperties(ItemStack item) {
            String color = item.getType().name().replace(woolSuffix, "");
            return basicProperties(item, Map.of(colorKey, color));
        }

        @Override
        public String getName(Map<String, String> properties) {
            if ( ! properties.containsKey(colorKey)) {
                return "Wool";   
            }

            return formatText(properties.get(colorKey) + " Wool");
        }

        @Override
        protected boolean includes(Material m) {
            return m.name().endsWith(woolSuffix);
        }
    },

    PLAYER_HEAD {
        private static String playerKey = "player";

        @Override
        public ItemStack createItem(Map<String, String> properties) {
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(properties.get(playerKey)));
            item.setItemMeta(meta);
            return basicItem(item, properties);
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

            props.put(playerKey, playerName);
            return basicProperties(item, props);
        }

        @Override
        public String getName(Map<String, String> properties) {
            if ( ! properties.containsKey(playerKey)) {
                return "Player Head";
            }

            return formatText(properties.get(playerKey) + " Head");
        }

        @Override
        protected boolean includes(Material m) {
            return m.equals(Material.PLAYER_HEAD);
        }
    },

    SOUP {
        private static String soupSuffix = "_SOUP";

        @Override
        public ItemStack createItem(Map<String, String> properties) {
            String flavour = properties.getOrDefault("flavour", "BEETROOT");
            return basicItem(new ItemStack(Material.valueOf(flavour + soupSuffix)), properties);
        }

        @Override
        public Map<String, String> extractProperties(ItemStack item) {
            String flavour = item.getType().name().replace(soupSuffix, "");
            return basicProperties(item, Map.of("flavour", flavour));
        }

        @Override
        public String getName(Map<String, String> properties) {
            if ( ! properties.containsKey("flavour")) {
                return "Soup";
            }

            return formatText(properties.get("flavour") + " Soup");
        }

        @Override
        protected boolean includes(Material m) {
            return m.name().endsWith(soupSuffix);
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

        @Override
        public String getName(Map<String, String> properties) {
            return "Air";
        }

        @Override
        protected boolean includes(Material m) {
            return m.equals(Material.AIR);
        }
	};

    // Explicit allowlist of materials that can use GENERIC without warnings
    private static final Set<Material> ALLOWED_GENERIC_MATERIALS = new HashSet<>(Arrays.asList(
        Material.STONE,
        Material.GRANITE,
        Material.POLISHED_GRANITE,
        Material.DIORITE,
        Material.POLISHED_DIORITE,
        Material.ANDESITE,
        Material.POLISHED_ANDESITE,
        Material.DEEPSLATE,
        Material.COBBLED_DEEPSLATE,
        Material.POLISHED_DEEPSLATE,
        Material.CALCITE,
        Material.TUFF,
        Material.TUFF_SLAB,
        Material.TUFF_STAIRS,
        Material.TUFF_WALL,
        Material.CHISELED_TUFF,
        Material.POLISHED_TUFF,
        Material.POLISHED_TUFF_SLAB,
        Material.POLISHED_TUFF_STAIRS,
        Material.POLISHED_TUFF_WALL,
        Material.TUFF_BRICKS,
        Material.TUFF_BRICK_SLAB,
        Material.TUFF_BRICK_STAIRS,
        Material.TUFF_BRICK_WALL,
        Material.CHISELED_TUFF_BRICKS,
        Material.DRIPSTONE_BLOCK,
        Material.GRASS_BLOCK,
        Material.DIRT,
        Material.COARSE_DIRT,
        Material.PODZOL,
        Material.ROOTED_DIRT,
        Material.MUD,
        Material.CRIMSON_NYLIUM,
        Material.WARPED_NYLIUM,
        Material.COBBLESTONE,
        Material.OAK_PLANKS,
        Material.SPRUCE_PLANKS,
        Material.BIRCH_PLANKS,
        Material.JUNGLE_PLANKS,
        Material.ACACIA_PLANKS,
        Material.CHERRY_PLANKS,
        Material.DARK_OAK_PLANKS,
        Material.PALE_OAK_PLANKS,
        Material.MANGROVE_PLANKS,
        Material.BAMBOO_PLANKS,
        Material.CRIMSON_PLANKS,
        Material.WARPED_PLANKS,
        Material.BAMBOO_MOSAIC,
        Material.OAK_SAPLING,
        Material.SPRUCE_SAPLING,
        Material.BIRCH_SAPLING,
        Material.JUNGLE_SAPLING,
        Material.ACACIA_SAPLING,
        Material.CHERRY_SAPLING,
        Material.DARK_OAK_SAPLING,
        Material.PALE_OAK_SAPLING,
        Material.MANGROVE_PROPAGULE,
        Material.BEDROCK,
        Material.SAND,
        Material.SUSPICIOUS_SAND,
        Material.SUSPICIOUS_GRAVEL,
        Material.RED_SAND,
        Material.GRAVEL,
        Material.COAL_ORE,
        Material.DEEPSLATE_COAL_ORE,
        Material.IRON_ORE,
        Material.DEEPSLATE_IRON_ORE,
        Material.COPPER_ORE,
        Material.DEEPSLATE_COPPER_ORE,
        Material.GOLD_ORE,
        Material.DEEPSLATE_GOLD_ORE,
        Material.REDSTONE_ORE,
        Material.DEEPSLATE_REDSTONE_ORE,
        Material.EMERALD_ORE,
        Material.DEEPSLATE_EMERALD_ORE,
        Material.LAPIS_ORE,
        Material.DEEPSLATE_LAPIS_ORE,
        Material.DIAMOND_ORE,
        Material.DEEPSLATE_DIAMOND_ORE,
        Material.NETHER_GOLD_ORE,
        Material.NETHER_QUARTZ_ORE,
        Material.ANCIENT_DEBRIS,
        Material.COAL_BLOCK,
        Material.RAW_IRON_BLOCK,
        Material.RAW_COPPER_BLOCK,
        Material.RAW_GOLD_BLOCK,
        Material.HEAVY_CORE,
        Material.AMETHYST_BLOCK,
        Material.BUDDING_AMETHYST,
        Material.IRON_BLOCK,
        Material.COPPER_BLOCK,
        Material.GOLD_BLOCK,
        Material.DIAMOND_BLOCK,
        Material.NETHERITE_BLOCK,
        Material.EXPOSED_COPPER,
        Material.WEATHERED_COPPER,
        Material.OXIDIZED_COPPER,
        Material.CHISELED_COPPER,
        Material.EXPOSED_CHISELED_COPPER,
        Material.WEATHERED_CHISELED_COPPER,
        Material.OXIDIZED_CHISELED_COPPER,
        Material.CUT_COPPER,
        Material.EXPOSED_CUT_COPPER,
        Material.WEATHERED_CUT_COPPER,
        Material.OXIDIZED_CUT_COPPER,
        Material.CUT_COPPER_STAIRS,
        Material.EXPOSED_CUT_COPPER_STAIRS,
        Material.WEATHERED_CUT_COPPER_STAIRS,
        Material.OXIDIZED_CUT_COPPER_STAIRS,
        Material.CUT_COPPER_SLAB,
        Material.EXPOSED_CUT_COPPER_SLAB,
        Material.WEATHERED_CUT_COPPER_SLAB,
        Material.OXIDIZED_CUT_COPPER_SLAB,
        Material.WAXED_COPPER_BLOCK,
        Material.WAXED_EXPOSED_COPPER,
        Material.WAXED_WEATHERED_COPPER,
        Material.WAXED_OXIDIZED_COPPER,
        Material.WAXED_CHISELED_COPPER,
        Material.WAXED_EXPOSED_CHISELED_COPPER,
        Material.WAXED_WEATHERED_CHISELED_COPPER,
        Material.WAXED_OXIDIZED_CHISELED_COPPER,
        Material.WAXED_CUT_COPPER,
        Material.WAXED_EXPOSED_CUT_COPPER,
        Material.WAXED_WEATHERED_CUT_COPPER,
        Material.WAXED_OXIDIZED_CUT_COPPER,
        Material.WAXED_CUT_COPPER_STAIRS,
        Material.WAXED_EXPOSED_CUT_COPPER_STAIRS,
        Material.WAXED_WEATHERED_CUT_COPPER_STAIRS,
        Material.WAXED_OXIDIZED_CUT_COPPER_STAIRS,
        Material.WAXED_CUT_COPPER_SLAB,
        Material.WAXED_EXPOSED_CUT_COPPER_SLAB,
        Material.WAXED_WEATHERED_CUT_COPPER_SLAB,
        Material.WAXED_OXIDIZED_CUT_COPPER_SLAB,
        Material.OAK_LOG,
        Material.SPRUCE_LOG,
        Material.BIRCH_LOG,
        Material.JUNGLE_LOG,
        Material.ACACIA_LOG,
        Material.CHERRY_LOG,
        Material.PALE_OAK_LOG,
        Material.DARK_OAK_LOG,
        Material.MANGROVE_LOG,
        Material.MANGROVE_ROOTS,
        Material.MUDDY_MANGROVE_ROOTS,
        Material.CRIMSON_STEM,
        Material.WARPED_STEM,
        Material.BAMBOO_BLOCK,
        Material.STRIPPED_OAK_LOG,
        Material.STRIPPED_SPRUCE_LOG,
        Material.STRIPPED_BIRCH_LOG,
        Material.STRIPPED_JUNGLE_LOG,
        Material.STRIPPED_ACACIA_LOG,
        Material.STRIPPED_CHERRY_LOG,
        Material.STRIPPED_DARK_OAK_LOG,
        Material.STRIPPED_PALE_OAK_LOG,
        Material.STRIPPED_MANGROVE_LOG,
        Material.STRIPPED_CRIMSON_STEM,
        Material.STRIPPED_WARPED_STEM,
        Material.STRIPPED_OAK_WOOD,
        Material.STRIPPED_SPRUCE_WOOD,
        Material.STRIPPED_BIRCH_WOOD,
        Material.STRIPPED_JUNGLE_WOOD,
        Material.STRIPPED_ACACIA_WOOD,
        Material.STRIPPED_CHERRY_WOOD,
        Material.STRIPPED_DARK_OAK_WOOD,
        Material.STRIPPED_PALE_OAK_WOOD,
        Material.STRIPPED_MANGROVE_WOOD,
        Material.STRIPPED_CRIMSON_HYPHAE,
        Material.STRIPPED_WARPED_HYPHAE,
        Material.STRIPPED_BAMBOO_BLOCK,
        Material.OAK_WOOD,
        Material.SPRUCE_WOOD,
        Material.BIRCH_WOOD,
        Material.JUNGLE_WOOD,
        Material.ACACIA_WOOD,
        Material.CHERRY_WOOD,
        Material.PALE_OAK_WOOD,
        Material.DARK_OAK_WOOD,
        Material.MANGROVE_WOOD,
        Material.CRIMSON_HYPHAE,
        Material.WARPED_HYPHAE,
        Material.OAK_LEAVES,
        Material.SPRUCE_LEAVES,
        Material.BIRCH_LEAVES,
        Material.JUNGLE_LEAVES,
        Material.ACACIA_LEAVES,
        Material.CHERRY_LEAVES,
        Material.DARK_OAK_LEAVES,
        Material.PALE_OAK_LEAVES,
        Material.MANGROVE_LEAVES,
        Material.AZALEA_LEAVES,
        Material.FLOWERING_AZALEA_LEAVES,
        Material.SPONGE,
        Material.WET_SPONGE,
        Material.GLASS,
        Material.TINTED_GLASS,
        Material.LAPIS_BLOCK,
        Material.SANDSTONE,
        Material.CHISELED_SANDSTONE,
        Material.CUT_SANDSTONE,
        Material.COBWEB,
        Material.SHORT_GRASS,
        Material.FERN,
        Material.AZALEA,
        Material.FLOWERING_AZALEA,
        Material.DEAD_BUSH,
        Material.SEAGRASS,
        Material.SEA_PICKLE,
        Material.WHITE_WOOL,
        Material.ORANGE_WOOL,
        Material.MAGENTA_WOOL,
        Material.LIGHT_BLUE_WOOL,
        Material.YELLOW_WOOL,
        Material.LIME_WOOL,
        Material.PINK_WOOL,
        Material.GRAY_WOOL,
        Material.LIGHT_GRAY_WOOL,
        Material.CYAN_WOOL,
        Material.PURPLE_WOOL,
        Material.BLUE_WOOL,
        Material.BROWN_WOOL,
        Material.GREEN_WOOL,
        Material.RED_WOOL,
        Material.BLACK_WOOL,
        Material.DANDELION,
        Material.OPEN_EYEBLOSSOM,
        Material.CLOSED_EYEBLOSSOM,
        Material.POPPY,
        Material.BLUE_ORCHID,
        Material.ALLIUM,
        Material.AZURE_BLUET,
        Material.RED_TULIP,
        Material.ORANGE_TULIP,
        Material.WHITE_TULIP,
        Material.PINK_TULIP,
        Material.OXEYE_DAISY,
        Material.CORNFLOWER,
        Material.LILY_OF_THE_VALLEY,
        Material.WITHER_ROSE,
        Material.TORCHFLOWER,
        Material.PITCHER_PLANT,
        Material.SPORE_BLOSSOM,
        Material.BROWN_MUSHROOM,
        Material.RED_MUSHROOM,
        Material.CRIMSON_FUNGUS,
        Material.WARPED_FUNGUS,
        Material.CRIMSON_ROOTS,
        Material.WARPED_ROOTS,
        Material.NETHER_SPROUTS,
        Material.WEEPING_VINES,
        Material.TWISTING_VINES,
        Material.SUGAR_CANE,
        Material.KELP,
        Material.PINK_PETALS,
        Material.MOSS_CARPET,
        Material.MOSS_BLOCK,
        Material.PALE_MOSS_CARPET,
        Material.PALE_HANGING_MOSS,
        Material.PALE_MOSS_BLOCK,
        Material.HANGING_ROOTS,
        Material.BIG_DRIPLEAF,
        Material.SMALL_DRIPLEAF,
        Material.BAMBOO,
        Material.OAK_SLAB,
        Material.SPRUCE_SLAB,
        Material.BIRCH_SLAB,
        Material.JUNGLE_SLAB,
        Material.ACACIA_SLAB,
        Material.CHERRY_SLAB,
        Material.DARK_OAK_SLAB,
        Material.PALE_OAK_SLAB,
        Material.MANGROVE_SLAB,
        Material.BAMBOO_SLAB,
        Material.BAMBOO_MOSAIC_SLAB,
        Material.CRIMSON_SLAB,
        Material.WARPED_SLAB,
        Material.STONE_SLAB,
        Material.SMOOTH_STONE_SLAB,
        Material.SANDSTONE_SLAB,
        Material.CUT_SANDSTONE_SLAB,
        Material.PETRIFIED_OAK_SLAB,
        Material.COBBLESTONE_SLAB,
        Material.BRICK_SLAB,
        Material.STONE_BRICK_SLAB,
        Material.MUD_BRICK_SLAB,
        Material.NETHER_BRICK_SLAB,
        Material.QUARTZ_SLAB,
        Material.RED_SANDSTONE_SLAB,
        Material.CUT_RED_SANDSTONE_SLAB,
        Material.PURPUR_SLAB,
        Material.PRISMARINE_SLAB,
        Material.PRISMARINE_BRICK_SLAB,
        Material.DARK_PRISMARINE_SLAB,
        Material.SMOOTH_QUARTZ,
        Material.SMOOTH_RED_SANDSTONE,
        Material.SMOOTH_SANDSTONE,
        Material.SMOOTH_STONE,
        Material.BRICKS,
        Material.BOOKSHELF,
        Material.CHISELED_BOOKSHELF,
        Material.DECORATED_POT,
        Material.MOSSY_COBBLESTONE,
        Material.OBSIDIAN,
        Material.TORCH,
        Material.END_ROD,
        Material.CHORUS_PLANT,
        Material.CHORUS_FLOWER,
        Material.PURPUR_BLOCK,
        Material.PURPUR_PILLAR,
        Material.PURPUR_STAIRS,
        Material.SPAWNER,
        Material.CREAKING_HEART,
        Material.CHEST,
        Material.CRAFTING_TABLE,
        Material.FARMLAND,
        Material.FURNACE,
        Material.LADDER,
        Material.COBBLESTONE_STAIRS,
        Material.SNOW,
        Material.ICE,
        Material.SNOW_BLOCK,
        Material.CACTUS,
        Material.CLAY,
        Material.JUKEBOX,
        Material.OAK_FENCE,
        Material.SPRUCE_FENCE,
        Material.BIRCH_FENCE,
        Material.JUNGLE_FENCE,
        Material.ACACIA_FENCE,
        Material.CHERRY_FENCE,
        Material.DARK_OAK_FENCE,
        Material.PALE_OAK_FENCE,
        Material.MANGROVE_FENCE,
        Material.BAMBOO_FENCE,
        Material.CRIMSON_FENCE,
        Material.WARPED_FENCE,
        Material.PUMPKIN,
        Material.CARVED_PUMPKIN,
        Material.JACK_O_LANTERN,
        Material.NETHERRACK,
        Material.SOUL_SAND,
        Material.SOUL_SOIL,
        Material.BASALT,
        Material.POLISHED_BASALT,
        Material.SMOOTH_BASALT,
        Material.SOUL_TORCH,
        Material.GLOWSTONE,
        Material.INFESTED_STONE,
        Material.INFESTED_COBBLESTONE,
        Material.INFESTED_STONE_BRICKS,
        Material.INFESTED_MOSSY_STONE_BRICKS,
        Material.INFESTED_CRACKED_STONE_BRICKS,
        Material.INFESTED_CHISELED_STONE_BRICKS,
        Material.INFESTED_DEEPSLATE,
        Material.STONE_BRICKS,
        Material.MOSSY_STONE_BRICKS,
        Material.CRACKED_STONE_BRICKS,
        Material.CHISELED_STONE_BRICKS,
        Material.PACKED_MUD,
        Material.MUD_BRICKS,
        Material.DEEPSLATE_BRICKS,
        Material.CRACKED_DEEPSLATE_BRICKS,
        Material.DEEPSLATE_TILES,
        Material.CRACKED_DEEPSLATE_TILES,
        Material.CHISELED_DEEPSLATE,
        Material.REINFORCED_DEEPSLATE,
        Material.BROWN_MUSHROOM_BLOCK,
        Material.RED_MUSHROOM_BLOCK,
        Material.MUSHROOM_STEM,
        Material.IRON_BARS,
        Material.CHAIN,
        Material.GLASS_PANE,
        Material.MELON,
        Material.VINE,
        Material.GLOW_LICHEN,
        Material.RESIN_CLUMP,
        Material.RESIN_BLOCK,
        Material.RESIN_BRICKS,
        Material.RESIN_BRICK_STAIRS,
        Material.RESIN_BRICK_SLAB,
        Material.RESIN_BRICK_WALL,
        Material.CHISELED_RESIN_BRICKS,
        Material.BRICK_STAIRS,
        Material.STONE_BRICK_STAIRS,
        Material.MUD_BRICK_STAIRS,
        Material.MYCELIUM,
        Material.LILY_PAD,
        Material.NETHER_BRICKS,
        Material.CRACKED_NETHER_BRICKS,
        Material.CHISELED_NETHER_BRICKS,
        Material.NETHER_BRICK_FENCE,
        Material.NETHER_BRICK_STAIRS,
        Material.SCULK,
        Material.SCULK_VEIN,
        Material.SCULK_CATALYST,
        Material.SCULK_SHRIEKER,
        Material.ENCHANTING_TABLE,
        Material.END_PORTAL_FRAME,
        Material.END_STONE,
        Material.END_STONE_BRICKS,
        Material.DRAGON_EGG,
        Material.SANDSTONE_STAIRS,
        Material.ENDER_CHEST,
        Material.EMERALD_BLOCK,
        Material.OAK_STAIRS,
        Material.SPRUCE_STAIRS,
        Material.BIRCH_STAIRS,
        Material.JUNGLE_STAIRS,
        Material.ACACIA_STAIRS,
        Material.CHERRY_STAIRS,
        Material.DARK_OAK_STAIRS,
        Material.PALE_OAK_STAIRS,
        Material.MANGROVE_STAIRS,
        Material.BAMBOO_STAIRS,
        Material.BAMBOO_MOSAIC_STAIRS,
        Material.CRIMSON_STAIRS,
        Material.WARPED_STAIRS,
        Material.COMMAND_BLOCK,
        Material.BEACON,
        Material.COBBLESTONE_WALL,
        Material.MOSSY_COBBLESTONE_WALL,
        Material.BRICK_WALL,
        Material.PRISMARINE_WALL,
        Material.RED_SANDSTONE_WALL,
        Material.MOSSY_STONE_BRICK_WALL,
        Material.GRANITE_WALL,
        Material.STONE_BRICK_WALL,
        Material.MUD_BRICK_WALL,
        Material.NETHER_BRICK_WALL,
        Material.ANDESITE_WALL,
        Material.RED_NETHER_BRICK_WALL,
        Material.SANDSTONE_WALL,
        Material.END_STONE_BRICK_WALL,
        Material.DIORITE_WALL,
        Material.BLACKSTONE_WALL,
        Material.POLISHED_BLACKSTONE_WALL,
        Material.POLISHED_BLACKSTONE_BRICK_WALL,
        Material.COBBLED_DEEPSLATE_WALL,
        Material.POLISHED_DEEPSLATE_WALL,
        Material.DEEPSLATE_BRICK_WALL,
        Material.DEEPSLATE_TILE_WALL,
        Material.ANVIL,
        Material.CHIPPED_ANVIL,
        Material.DAMAGED_ANVIL,
        Material.CHISELED_QUARTZ_BLOCK,
        Material.QUARTZ_BLOCK,
        Material.QUARTZ_BRICKS,
        Material.QUARTZ_PILLAR,
        Material.QUARTZ_STAIRS,
        Material.WHITE_TERRACOTTA,
        Material.ORANGE_TERRACOTTA,
        Material.MAGENTA_TERRACOTTA,
        Material.LIGHT_BLUE_TERRACOTTA,
        Material.YELLOW_TERRACOTTA,
        Material.LIME_TERRACOTTA,
        Material.PINK_TERRACOTTA,
        Material.GRAY_TERRACOTTA,
        Material.LIGHT_GRAY_TERRACOTTA,
        Material.CYAN_TERRACOTTA,
        Material.PURPLE_TERRACOTTA,
        Material.BLUE_TERRACOTTA,
        Material.BROWN_TERRACOTTA,
        Material.GREEN_TERRACOTTA,
        Material.RED_TERRACOTTA,
        Material.BLACK_TERRACOTTA,
        Material.BARRIER,
        Material.LIGHT,
        Material.HAY_BLOCK,
        Material.WHITE_CARPET,
        Material.ORANGE_CARPET,
        Material.MAGENTA_CARPET,
        Material.LIGHT_BLUE_CARPET,
        Material.YELLOW_CARPET,
        Material.LIME_CARPET,
        Material.PINK_CARPET,
        Material.GRAY_CARPET,
        Material.LIGHT_GRAY_CARPET,
        Material.CYAN_CARPET,
        Material.PURPLE_CARPET,
        Material.BLUE_CARPET,
        Material.BROWN_CARPET,
        Material.GREEN_CARPET,
        Material.RED_CARPET,
        Material.BLACK_CARPET,
        Material.TERRACOTTA,
        Material.PACKED_ICE,
        Material.DIRT_PATH,
        Material.SUNFLOWER,
        Material.LILAC,
        Material.ROSE_BUSH,
        Material.PEONY,
        Material.TALL_GRASS,
        Material.LARGE_FERN,
        Material.WHITE_STAINED_GLASS,
        Material.ORANGE_STAINED_GLASS,
        Material.MAGENTA_STAINED_GLASS,
        Material.LIGHT_BLUE_STAINED_GLASS,
        Material.YELLOW_STAINED_GLASS,
        Material.LIME_STAINED_GLASS,
        Material.PINK_STAINED_GLASS,
        Material.GRAY_STAINED_GLASS,
        Material.LIGHT_GRAY_STAINED_GLASS,
        Material.CYAN_STAINED_GLASS,
        Material.PURPLE_STAINED_GLASS,
        Material.BLUE_STAINED_GLASS,
        Material.BROWN_STAINED_GLASS,
        Material.GREEN_STAINED_GLASS,
        Material.RED_STAINED_GLASS,
        Material.BLACK_STAINED_GLASS,
        Material.WHITE_STAINED_GLASS_PANE,
        Material.ORANGE_STAINED_GLASS_PANE,
        Material.MAGENTA_STAINED_GLASS_PANE,
        Material.LIGHT_BLUE_STAINED_GLASS_PANE,
        Material.YELLOW_STAINED_GLASS_PANE,
        Material.LIME_STAINED_GLASS_PANE,
        Material.PINK_STAINED_GLASS_PANE,
        Material.GRAY_STAINED_GLASS_PANE,
        Material.LIGHT_GRAY_STAINED_GLASS_PANE,
        Material.CYAN_STAINED_GLASS_PANE,
        Material.PURPLE_STAINED_GLASS_PANE,
        Material.BLUE_STAINED_GLASS_PANE,
        Material.BROWN_STAINED_GLASS_PANE,
        Material.GREEN_STAINED_GLASS_PANE,
        Material.RED_STAINED_GLASS_PANE,
        Material.BLACK_STAINED_GLASS_PANE,
        Material.PRISMARINE,
        Material.PRISMARINE_BRICKS,
        Material.DARK_PRISMARINE,
        Material.PRISMARINE_STAIRS,
        Material.PRISMARINE_BRICK_STAIRS,
        Material.DARK_PRISMARINE_STAIRS,
        Material.SEA_LANTERN,
        Material.RED_SANDSTONE,
        Material.CHISELED_RED_SANDSTONE,
        Material.CUT_RED_SANDSTONE,
        Material.RED_SANDSTONE_STAIRS,
        Material.REPEATING_COMMAND_BLOCK,
        Material.CHAIN_COMMAND_BLOCK,
        Material.MAGMA_BLOCK,
        Material.NETHER_WART_BLOCK,
        Material.WARPED_WART_BLOCK,
        Material.RED_NETHER_BRICKS,
        Material.BONE_BLOCK,
        Material.STRUCTURE_VOID,
        Material.SHULKER_BOX,
        Material.WHITE_SHULKER_BOX,
        Material.ORANGE_SHULKER_BOX,
        Material.MAGENTA_SHULKER_BOX,
        Material.LIGHT_BLUE_SHULKER_BOX,
        Material.YELLOW_SHULKER_BOX,
        Material.LIME_SHULKER_BOX,
        Material.PINK_SHULKER_BOX,
        Material.GRAY_SHULKER_BOX,
        Material.LIGHT_GRAY_SHULKER_BOX,
        Material.CYAN_SHULKER_BOX,
        Material.PURPLE_SHULKER_BOX,
        Material.BLUE_SHULKER_BOX,
        Material.BROWN_SHULKER_BOX,
        Material.GREEN_SHULKER_BOX,
        Material.RED_SHULKER_BOX,
        Material.BLACK_SHULKER_BOX,
        Material.WHITE_GLAZED_TERRACOTTA,
        Material.ORANGE_GLAZED_TERRACOTTA,
        Material.MAGENTA_GLAZED_TERRACOTTA,
        Material.LIGHT_BLUE_GLAZED_TERRACOTTA,
        Material.YELLOW_GLAZED_TERRACOTTA,
        Material.LIME_GLAZED_TERRACOTTA,
        Material.PINK_GLAZED_TERRACOTTA,
        Material.GRAY_GLAZED_TERRACOTTA,
        Material.LIGHT_GRAY_GLAZED_TERRACOTTA,
        Material.CYAN_GLAZED_TERRACOTTA,
        Material.PURPLE_GLAZED_TERRACOTTA,
        Material.BLUE_GLAZED_TERRACOTTA,
        Material.BROWN_GLAZED_TERRACOTTA,
        Material.GREEN_GLAZED_TERRACOTTA,
        Material.RED_GLAZED_TERRACOTTA,
        Material.BLACK_GLAZED_TERRACOTTA,
        Material.WHITE_CONCRETE,
        Material.ORANGE_CONCRETE,
        Material.MAGENTA_CONCRETE,
        Material.LIGHT_BLUE_CONCRETE,
        Material.YELLOW_CONCRETE,
        Material.LIME_CONCRETE,
        Material.PINK_CONCRETE,
        Material.GRAY_CONCRETE,
        Material.LIGHT_GRAY_CONCRETE,
        Material.CYAN_CONCRETE,
        Material.PURPLE_CONCRETE,
        Material.BLUE_CONCRETE,
        Material.BROWN_CONCRETE,
        Material.GREEN_CONCRETE,
        Material.RED_CONCRETE,
        Material.BLACK_CONCRETE,
        Material.WHITE_CONCRETE_POWDER,
        Material.ORANGE_CONCRETE_POWDER,
        Material.MAGENTA_CONCRETE_POWDER,
        Material.LIGHT_BLUE_CONCRETE_POWDER,
        Material.YELLOW_CONCRETE_POWDER,
        Material.LIME_CONCRETE_POWDER,
        Material.PINK_CONCRETE_POWDER,
        Material.GRAY_CONCRETE_POWDER,
        Material.LIGHT_GRAY_CONCRETE_POWDER,
        Material.CYAN_CONCRETE_POWDER,
        Material.PURPLE_CONCRETE_POWDER,
        Material.BLUE_CONCRETE_POWDER,
        Material.BROWN_CONCRETE_POWDER,
        Material.GREEN_CONCRETE_POWDER,
        Material.RED_CONCRETE_POWDER,
        Material.BLACK_CONCRETE_POWDER,
        Material.TURTLE_EGG,
        Material.SNIFFER_EGG,
        Material.DEAD_TUBE_CORAL_BLOCK,
        Material.DEAD_BRAIN_CORAL_BLOCK,
        Material.DEAD_BUBBLE_CORAL_BLOCK,
        Material.DEAD_FIRE_CORAL_BLOCK,
        Material.DEAD_HORN_CORAL_BLOCK,
        Material.TUBE_CORAL_BLOCK,
        Material.BRAIN_CORAL_BLOCK,
        Material.BUBBLE_CORAL_BLOCK,
        Material.FIRE_CORAL_BLOCK,
        Material.HORN_CORAL_BLOCK,
        Material.TUBE_CORAL,
        Material.BRAIN_CORAL,
        Material.BUBBLE_CORAL,
        Material.FIRE_CORAL,
        Material.HORN_CORAL,
        Material.DEAD_BRAIN_CORAL,
        Material.DEAD_BUBBLE_CORAL,
        Material.DEAD_FIRE_CORAL,
        Material.DEAD_HORN_CORAL,
        Material.DEAD_TUBE_CORAL,
        Material.TUBE_CORAL_FAN,
        Material.BRAIN_CORAL_FAN,
        Material.BUBBLE_CORAL_FAN,
        Material.FIRE_CORAL_FAN,
        Material.HORN_CORAL_FAN,
        Material.DEAD_TUBE_CORAL_FAN,
        Material.DEAD_BRAIN_CORAL_FAN,
        Material.DEAD_BUBBLE_CORAL_FAN,
        Material.DEAD_FIRE_CORAL_FAN,
        Material.DEAD_HORN_CORAL_FAN,
        Material.BLUE_ICE,
        Material.CONDUIT,
        Material.POLISHED_GRANITE_STAIRS,
        Material.SMOOTH_RED_SANDSTONE_STAIRS,
        Material.MOSSY_STONE_BRICK_STAIRS,
        Material.POLISHED_DIORITE_STAIRS,
        Material.MOSSY_COBBLESTONE_STAIRS,
        Material.END_STONE_BRICK_STAIRS,
        Material.STONE_STAIRS,
        Material.SMOOTH_SANDSTONE_STAIRS,
        Material.SMOOTH_QUARTZ_STAIRS,
        Material.GRANITE_STAIRS,
        Material.ANDESITE_STAIRS,
        Material.RED_NETHER_BRICK_STAIRS,
        Material.POLISHED_ANDESITE_STAIRS,
        Material.DIORITE_STAIRS,
        Material.COBBLED_DEEPSLATE_STAIRS,
        Material.POLISHED_DEEPSLATE_STAIRS,
        Material.DEEPSLATE_BRICK_STAIRS,
        Material.DEEPSLATE_TILE_STAIRS,
        Material.POLISHED_GRANITE_SLAB,
        Material.SMOOTH_RED_SANDSTONE_SLAB,
        Material.MOSSY_STONE_BRICK_SLAB,
        Material.POLISHED_DIORITE_SLAB,
        Material.MOSSY_COBBLESTONE_SLAB,
        Material.END_STONE_BRICK_SLAB,
        Material.SMOOTH_SANDSTONE_SLAB,
        Material.SMOOTH_QUARTZ_SLAB,
        Material.GRANITE_SLAB,
        Material.ANDESITE_SLAB,
        Material.RED_NETHER_BRICK_SLAB,
        Material.POLISHED_ANDESITE_SLAB,
        Material.DIORITE_SLAB,
        Material.COBBLED_DEEPSLATE_SLAB,
        Material.POLISHED_DEEPSLATE_SLAB,
        Material.DEEPSLATE_BRICK_SLAB,
        Material.DEEPSLATE_TILE_SLAB,
        Material.SCAFFOLDING,
        Material.REDSTONE,
        Material.REDSTONE_TORCH,
        Material.REDSTONE_BLOCK,
        Material.REPEATER,
        Material.COMPARATOR,
        Material.PISTON,
        Material.STICKY_PISTON,
        Material.SLIME_BLOCK,
        Material.HONEY_BLOCK,
        Material.OBSERVER,
        Material.HOPPER,
        Material.DISPENSER,
        Material.DROPPER,
        Material.LECTERN,
        Material.TARGET,
        Material.LEVER,
        Material.LIGHTNING_ROD,
        Material.DAYLIGHT_DETECTOR,
        Material.SCULK_SENSOR,
        Material.CALIBRATED_SCULK_SENSOR,
        Material.TRIPWIRE_HOOK,
        Material.TRAPPED_CHEST,
        Material.TNT,
        Material.REDSTONE_LAMP,
        Material.NOTE_BLOCK,
        Material.STONE_BUTTON,
        Material.POLISHED_BLACKSTONE_BUTTON,
        Material.OAK_BUTTON,
        Material.SPRUCE_BUTTON,
        Material.BIRCH_BUTTON,
        Material.JUNGLE_BUTTON,
        Material.ACACIA_BUTTON,
        Material.CHERRY_BUTTON,
        Material.DARK_OAK_BUTTON,
        Material.PALE_OAK_BUTTON,
        Material.MANGROVE_BUTTON,
        Material.BAMBOO_BUTTON,
        Material.CRIMSON_BUTTON,
        Material.WARPED_BUTTON,
        Material.STONE_PRESSURE_PLATE,
        Material.POLISHED_BLACKSTONE_PRESSURE_PLATE,
        Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
        Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
        Material.OAK_PRESSURE_PLATE,
        Material.SPRUCE_PRESSURE_PLATE,
        Material.BIRCH_PRESSURE_PLATE,
        Material.JUNGLE_PRESSURE_PLATE,
        Material.ACACIA_PRESSURE_PLATE,
        Material.CHERRY_PRESSURE_PLATE,
        Material.DARK_OAK_PRESSURE_PLATE,
        Material.PALE_OAK_PRESSURE_PLATE,
        Material.MANGROVE_PRESSURE_PLATE,
        Material.BAMBOO_PRESSURE_PLATE,
        Material.CRIMSON_PRESSURE_PLATE,
        Material.WARPED_PRESSURE_PLATE,
        Material.IRON_DOOR,
        Material.OAK_DOOR,
        Material.SPRUCE_DOOR,
        Material.BIRCH_DOOR,
        Material.JUNGLE_DOOR,
        Material.ACACIA_DOOR,
        Material.CHERRY_DOOR,
        Material.DARK_OAK_DOOR,
        Material.PALE_OAK_DOOR,
        Material.MANGROVE_DOOR,
        Material.BAMBOO_DOOR,
        Material.CRIMSON_DOOR,
        Material.WARPED_DOOR,
        Material.COPPER_DOOR,
        Material.EXPOSED_COPPER_DOOR,
        Material.WEATHERED_COPPER_DOOR,
        Material.OXIDIZED_COPPER_DOOR,
        Material.WAXED_COPPER_DOOR,
        Material.WAXED_EXPOSED_COPPER_DOOR,
        Material.WAXED_WEATHERED_COPPER_DOOR,
        Material.WAXED_OXIDIZED_COPPER_DOOR,
        Material.IRON_TRAPDOOR,
        Material.OAK_TRAPDOOR,
        Material.SPRUCE_TRAPDOOR,
        Material.BIRCH_TRAPDOOR,
        Material.JUNGLE_TRAPDOOR,
        Material.ACACIA_TRAPDOOR,
        Material.CHERRY_TRAPDOOR,
        Material.DARK_OAK_TRAPDOOR,
        Material.PALE_OAK_TRAPDOOR,
        Material.MANGROVE_TRAPDOOR,
        Material.BAMBOO_TRAPDOOR,
        Material.CRIMSON_TRAPDOOR,
        Material.WARPED_TRAPDOOR,
        Material.COPPER_TRAPDOOR,
        Material.EXPOSED_COPPER_TRAPDOOR,
        Material.WEATHERED_COPPER_TRAPDOOR,
        Material.OXIDIZED_COPPER_TRAPDOOR,
        Material.WAXED_COPPER_TRAPDOOR,
        Material.WAXED_EXPOSED_COPPER_TRAPDOOR,
        Material.WAXED_WEATHERED_COPPER_TRAPDOOR,
        Material.WAXED_OXIDIZED_COPPER_TRAPDOOR,
        Material.OAK_FENCE_GATE,
        Material.SPRUCE_FENCE_GATE,
        Material.BIRCH_FENCE_GATE,
        Material.JUNGLE_FENCE_GATE,
        Material.ACACIA_FENCE_GATE,
        Material.CHERRY_FENCE_GATE,
        Material.DARK_OAK_FENCE_GATE,
        Material.PALE_OAK_FENCE_GATE,
        Material.MANGROVE_FENCE_GATE,
        Material.BAMBOO_FENCE_GATE,
        Material.CRIMSON_FENCE_GATE,
        Material.WARPED_FENCE_GATE,
        Material.POWERED_RAIL,
        Material.DETECTOR_RAIL,
        Material.RAIL,
        Material.ACTIVATOR_RAIL,
        Material.SADDLE,
        Material.MINECART,
        Material.CHEST_MINECART,
        Material.FURNACE_MINECART,
        Material.TNT_MINECART,
        Material.HOPPER_MINECART,
        Material.CARROT_ON_A_STICK,
        Material.WARPED_FUNGUS_ON_A_STICK,
        Material.PHANTOM_MEMBRANE,
        Material.ELYTRA,
        Material.OAK_BOAT,
        Material.OAK_CHEST_BOAT,
        Material.SPRUCE_BOAT,
        Material.SPRUCE_CHEST_BOAT,
        Material.BIRCH_BOAT,
        Material.BIRCH_CHEST_BOAT,
        Material.JUNGLE_BOAT,
        Material.JUNGLE_CHEST_BOAT,
        Material.ACACIA_BOAT,
        Material.ACACIA_CHEST_BOAT,
        Material.CHERRY_BOAT,
        Material.CHERRY_CHEST_BOAT,
        Material.DARK_OAK_BOAT,
        Material.DARK_OAK_CHEST_BOAT,
        Material.PALE_OAK_BOAT,
        Material.PALE_OAK_CHEST_BOAT,
        Material.MANGROVE_BOAT,
        Material.MANGROVE_CHEST_BOAT,
        Material.BAMBOO_RAFT,
        Material.BAMBOO_CHEST_RAFT,
        Material.STRUCTURE_BLOCK,
        Material.JIGSAW,
        Material.TURTLE_HELMET,
        Material.TURTLE_SCUTE,
        Material.ARMADILLO_SCUTE,
        Material.WOLF_ARMOR,
        Material.FLINT_AND_STEEL,
        Material.BOWL,
        Material.APPLE,
        Material.BOW,
        Material.ARROW,
        Material.COAL,
        Material.CHARCOAL,
        Material.DIAMOND,
        Material.EMERALD,
        Material.LAPIS_LAZULI,
        Material.QUARTZ,
        Material.AMETHYST_SHARD,
        Material.RAW_IRON,
        Material.IRON_INGOT,
        Material.RAW_COPPER,
        Material.COPPER_INGOT,
        Material.RAW_GOLD,
        Material.GOLD_INGOT,
        Material.NETHERITE_INGOT,
        Material.NETHERITE_SCRAP,
        Material.WOODEN_SWORD,
        Material.WOODEN_SHOVEL,
        Material.WOODEN_PICKAXE,
        Material.WOODEN_AXE,
        Material.WOODEN_HOE,
        Material.STONE_SWORD,
        Material.STONE_SHOVEL,
        Material.STONE_PICKAXE,
        Material.STONE_AXE,
        Material.STONE_HOE,
        Material.GOLDEN_SWORD,
        Material.GOLDEN_SHOVEL,
        Material.GOLDEN_PICKAXE,
        Material.GOLDEN_AXE,
        Material.GOLDEN_HOE,
        Material.IRON_SWORD,
        Material.IRON_SHOVEL,
        Material.IRON_PICKAXE,
        Material.IRON_AXE,
        Material.IRON_HOE,
        Material.DIAMOND_SWORD,
        Material.DIAMOND_SHOVEL,
        Material.DIAMOND_PICKAXE,
        Material.DIAMOND_AXE,
        Material.DIAMOND_HOE,
        Material.NETHERITE_SWORD,
        Material.NETHERITE_SHOVEL,
        Material.NETHERITE_PICKAXE,
        Material.NETHERITE_AXE,
        Material.NETHERITE_HOE,
        Material.STICK,
        Material.MUSHROOM_STEW,
        Material.STRING,
        Material.FEATHER,
        Material.GUNPOWDER,
        Material.WHEAT_SEEDS,
        Material.WHEAT,
        Material.BREAD,
        Material.LEATHER_HELMET,
        Material.LEATHER_CHESTPLATE,
        Material.LEATHER_LEGGINGS,
        Material.LEATHER_BOOTS,
        Material.CHAINMAIL_HELMET,
        Material.CHAINMAIL_CHESTPLATE,
        Material.CHAINMAIL_LEGGINGS,
        Material.CHAINMAIL_BOOTS,
        Material.IRON_HELMET,
        Material.IRON_CHESTPLATE,
        Material.IRON_LEGGINGS,
        Material.IRON_BOOTS,
        Material.DIAMOND_HELMET,
        Material.DIAMOND_CHESTPLATE,
        Material.DIAMOND_LEGGINGS,
        Material.DIAMOND_BOOTS,
        Material.GOLDEN_HELMET,
        Material.GOLDEN_CHESTPLATE,
        Material.GOLDEN_LEGGINGS,
        Material.GOLDEN_BOOTS,
        Material.NETHERITE_HELMET,
        Material.NETHERITE_CHESTPLATE,
        Material.NETHERITE_LEGGINGS,
        Material.NETHERITE_BOOTS,
        Material.FLINT,
        Material.PORKCHOP,
        Material.COOKED_PORKCHOP,
        Material.PAINTING,
        Material.GOLDEN_APPLE,
        Material.ENCHANTED_GOLDEN_APPLE,
        Material.OAK_SIGN,
        Material.SPRUCE_SIGN,
        Material.BIRCH_SIGN,
        Material.JUNGLE_SIGN,
        Material.ACACIA_SIGN,
        Material.CHERRY_SIGN,
        Material.DARK_OAK_SIGN,
        Material.PALE_OAK_SIGN,
        Material.MANGROVE_SIGN,
        Material.BAMBOO_SIGN,
        Material.CRIMSON_SIGN,
        Material.WARPED_SIGN,
        Material.OAK_HANGING_SIGN,
        Material.SPRUCE_HANGING_SIGN,
        Material.BIRCH_HANGING_SIGN,
        Material.JUNGLE_HANGING_SIGN,
        Material.ACACIA_HANGING_SIGN,
        Material.CHERRY_HANGING_SIGN,
        Material.DARK_OAK_HANGING_SIGN,
        Material.PALE_OAK_HANGING_SIGN,
        Material.MANGROVE_HANGING_SIGN,
        Material.BAMBOO_HANGING_SIGN,
        Material.CRIMSON_HANGING_SIGN,
        Material.WARPED_HANGING_SIGN,
        Material.BUCKET,
        Material.WATER_BUCKET,
        Material.LAVA_BUCKET,
        Material.POWDER_SNOW_BUCKET,
        Material.SNOWBALL,
        Material.LEATHER,
        Material.ITEM_FRAME,
        Material.MILK_BUCKET,
        Material.PUFFERFISH_BUCKET,
        Material.SALMON_BUCKET,
        Material.COD_BUCKET,
        Material.TROPICAL_FISH_BUCKET,
        Material.AXOLOTL_BUCKET,
        Material.TADPOLE_BUCKET,
        Material.BRICK,
        Material.CLAY_BALL,
        Material.DRIED_KELP_BLOCK,
        Material.PAPER,
        Material.BOOK,
        Material.SLIME_BALL,
        Material.EGG,
        Material.COMPASS,
        Material.RECOVERY_COMPASS,
        Material.BUNDLE,
        Material.WHITE_BUNDLE,
        Material.ORANGE_BUNDLE,
        Material.MAGENTA_BUNDLE,
        Material.LIGHT_BLUE_BUNDLE,
        Material.YELLOW_BUNDLE,
        Material.LIME_BUNDLE,
        Material.PINK_BUNDLE,
        Material.GRAY_BUNDLE,
        Material.LIGHT_GRAY_BUNDLE,
        Material.CYAN_BUNDLE,
        Material.PURPLE_BUNDLE,
        Material.BLUE_BUNDLE,
        Material.BROWN_BUNDLE,
        Material.GREEN_BUNDLE,
        Material.RED_BUNDLE,
        Material.BLACK_BUNDLE,
        Material.FISHING_ROD,
        Material.CLOCK,
        Material.SPYGLASS,
        Material.GLOWSTONE_DUST,
        Material.COD,
        Material.SALMON,
        Material.TROPICAL_FISH,
        Material.PUFFERFISH,
        Material.COOKED_COD,
        Material.COOKED_SALMON,
        Material.INK_SAC,
        Material.GLOW_INK_SAC,
        Material.COCOA_BEANS,
        Material.WHITE_DYE,
        Material.ORANGE_DYE,
        Material.MAGENTA_DYE,
        Material.LIGHT_BLUE_DYE,
        Material.YELLOW_DYE,
        Material.LIME_DYE,
        Material.PINK_DYE,
        Material.GRAY_DYE,
        Material.LIGHT_GRAY_DYE,
        Material.CYAN_DYE,
        Material.PURPLE_DYE,
        Material.BLUE_DYE,
        Material.BROWN_DYE,
        Material.GREEN_DYE,
        Material.RED_DYE,
        Material.BLACK_DYE,
        Material.BONE_MEAL,
        Material.BONE,
        Material.SUGAR,
        Material.CAKE,
        Material.WHITE_BED,
        Material.ORANGE_BED,
        Material.MAGENTA_BED,
        Material.LIGHT_BLUE_BED,
        Material.YELLOW_BED,
        Material.LIME_BED,
        Material.PINK_BED,
        Material.GRAY_BED,
        Material.LIGHT_GRAY_BED,
        Material.CYAN_BED,
        Material.PURPLE_BED,
        Material.BLUE_BED,
        Material.BROWN_BED,
        Material.GREEN_BED,
        Material.RED_BED,
        Material.BLACK_BED,
        Material.COOKIE,
        Material.CRAFTER,
        Material.FILLED_MAP,
        Material.SHEARS,
        Material.MELON_SLICE,
        Material.DRIED_KELP,
        Material.PUMPKIN_SEEDS,
        Material.MELON_SEEDS,
        Material.BEEF,
        Material.COOKED_BEEF,
        Material.CHICKEN,
        Material.COOKED_CHICKEN,
        Material.ROTTEN_FLESH,
        Material.ENDER_PEARL,
        Material.BLAZE_ROD,
        Material.GHAST_TEAR,
        Material.GOLD_NUGGET,
        Material.NETHER_WART,
        Material.GLASS_BOTTLE,
        Material.POTION,
        Material.SPIDER_EYE,
        Material.FERMENTED_SPIDER_EYE,
        Material.BLAZE_POWDER,
        Material.MAGMA_CREAM,
        Material.BREWING_STAND,
        Material.CAULDRON,
        Material.ENDER_EYE,
        Material.GLISTERING_MELON_SLICE,
        Material.ARMADILLO_SPAWN_EGG,
        Material.ALLAY_SPAWN_EGG,
        Material.AXOLOTL_SPAWN_EGG,
        Material.BAT_SPAWN_EGG,
        Material.BEE_SPAWN_EGG,
        Material.BLAZE_SPAWN_EGG,
        Material.BOGGED_SPAWN_EGG,
        Material.BREEZE_SPAWN_EGG,
        Material.CAT_SPAWN_EGG,
        Material.CAMEL_SPAWN_EGG,
        Material.CAVE_SPIDER_SPAWN_EGG,
        Material.CHICKEN_SPAWN_EGG,
        Material.COD_SPAWN_EGG,
        Material.COW_SPAWN_EGG,
        Material.CREEPER_SPAWN_EGG,
        Material.DOLPHIN_SPAWN_EGG,
        Material.DONKEY_SPAWN_EGG,
        Material.DROWNED_SPAWN_EGG,
        Material.VILLAGER_SPAWN_EGG,
        Material.WRITABLE_BOOK,
        Material.WRITTEN_BOOK,
        Material.ENCHANTED_BOOK,
        Material.NAME_TAG
    ));

    private static String materialTypeKey = "material";

    private static boolean isAllowedGeneric(Material material) {
        return ALLOWED_GENERIC_MATERIALS.contains(material);
    }

    // Bukkit material mappings
    private static final Map<Material, ItemData> SPECIAL_MAPPINGS = new HashMap<>();
    static {
        SPECIAL_MAPPINGS.put(Material.POTION, POTION);
        SPECIAL_MAPPINGS.put(Material.SPLASH_POTION, POTION);
        SPECIAL_MAPPINGS.put(Material.LINGERING_POTION, POTION);
        Arrays.stream(Material.values()).filter(m -> WOOL.includes(m)).forEach(m -> SPECIAL_MAPPINGS.put(m, WOOL));
        SPECIAL_MAPPINGS.put(Material.PLAYER_HEAD, PLAYER_HEAD);
        Arrays.stream(Material.values()).filter(m -> SOUP.includes(m)).forEach(m -> SPECIAL_MAPPINGS.put(m, SOUP));
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

    protected abstract boolean includes(Material m);

    public static ItemData fromMaterial(Material material) {
        // 1. Return special handler if exists
        ItemData specialHandler = SPECIAL_MAPPINGS.get(material);
        if (specialHandler != null) return specialHandler;

        // 2. Return GENERIC (will warn if not allowed)
        return GENERIC;
    }

    public static ItemData getEnum(String string) {
        // First try to find matching ItemData (excluding GENERIC)
        Optional<ItemData> itemDataMatch = Arrays.stream(values())
            .filter(e -> !e.equals(GENERIC))
            .filter(e -> e.name().equalsIgnoreCase(string))
            .findFirst();
        if (itemDataMatch.isPresent()) {
            return itemDataMatch.get();
        }

        // No matches found - complain and return AIR
        ChatUtils.message("Unknown item type: " + string + ". Defaulting to AIR. 💔")
            .target(MessageTarget.CONSOLE)
            .type(MessageType.ERROR)
            .style(MessageStyle.PRETTY)
            .send();
        return ItemData.AIR;
    }

    String formatText(String string) {
        return WordUtils.capitalizeFully(
            string.replaceAll("_+", " ")
        );
    }

    /**
     * Adds basic item properties to an item.
     * Used to supplement creating "special mapping" ItemStacks.
     * @param itemStack the item with custom changes already made (can be unmodified)
     * @param properties the current properties set (can be empty)
     * @return a modified ItemStack
     */
    private static ItemStack basicItem(ItemStack itemStack, Map<String, String> properties) {
        // set enchantments
        if (properties.containsKey("enchantment")) { // TODO: also fix only supporting one enchantment datum
            String enchantmentString = properties.get("enchantment");
            List<String> enchantmentStringParts = List.of(enchantmentString.split("_"));
            
            // resolve enchantment level
            Integer level = Integer.parseInt(enchantmentStringParts.getLast());

            // resolve enchantment type
            String enchantmentType = String.join("_", enchantmentStringParts.removeLast());
            Enchantment enchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(EnchantmentKeys.create(Key.key(enchantmentType)));

            // apply enchantment
            itemStack.addEnchantment(enchantment, level);
        }

        // set nametag
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (properties.containsKey("nametag")) {
            String nametagString = properties.get("nametag");

            // apply nametag
            itemMeta.displayName(Component.text(nametagString));
            itemStack.setItemMeta(itemMeta);
        }

        // set trim
        if (properties.containsKey("trimPattern") && properties.containsKey("trimMaterial")) {
            String trimPatternString = properties.get("trimPattern").toLowerCase();
            String trimMaterialString = properties.get("trimMaterial").toLowerCase();
            ArmorMeta armorMeta = (ArmorMeta) itemMeta;

            // resolve trim
            TrimPattern trimPattern = RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_PATTERN).getOrThrow(TrimPatternKeys.create(Key.key(trimPatternString)));
            TrimMaterial trimMaterial = RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_MATERIAL).getOrThrow(TrimMaterialKeys.create(Key.key(trimMaterialString)));

            // apply trim
            armorMeta.setTrim(new ArmorTrim(trimMaterial, trimPattern));
            itemStack.setItemMeta(armorMeta);
        }

        return itemStack;
    }

    /**
     * Gets basic item properties from an item.
     * Used to supplement extracting properties from "special mapping" items.
     * @param itemStack the item to extract properties from
     * @return properties the existing properties already extracted (can be empty)
     */
    private static Map<String, String> basicProperties(ItemStack itemStack, Map<String, String> properties) {
        Map<String, String> newProperties = new HashMap<>(properties); // useful if properties is a Map (immutable)

        // add enchantment as properties
        Map<Enchantment, Integer> enchantments = itemStack.getEnchantments();
        enchantments.forEach((enchantment, level) -> {
            newProperties.put("enchantment", enchantment.getKey().asString()+"_"+String.valueOf(level)); // TODO: fix only supporting one enchantment datum
        });

        // add nametag as property
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta.hasDisplayName()) {
            newProperties.put("nametag", PlainTextComponentSerializer.plainText().serialize(itemStack.getItemMeta().displayName()));
        }

        // add trims as property
        List<Material> trimCapable = List.of(Material.NETHERITE_BOOTS, Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS);
        if (trimCapable.contains(itemStack.getType())) {
            ArmorMeta armorMeta = (ArmorMeta) itemMeta;
            if (armorMeta.hasTrim()) {
                ArmorTrim armorTrim = armorMeta.getTrim();
                String trimPatternString = RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_PATTERN).getKey(armorTrim.getPattern()).getKey();
                String trimMaterialString = RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_MATERIAL).getKey(armorTrim.getMaterial()).getKey();
                newProperties.put("trimPattern", trimPatternString);
                newProperties.put("trimMaterial", trimMaterialString);
            }
        }

        return newProperties;

    }

    public static String getMaterialKey() {
        return materialTypeKey;
    }

    // Core interface methods
    public abstract ItemStack createItem(Map<String, String> properties);
    public abstract Map<String, String> extractProperties(ItemStack item);
    public abstract String getName(Map<String, String> properties);
}

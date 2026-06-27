package org.yanbwe.searchcarefully;

import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.ArrayList;
import java.util.List;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // Configuration options for the search system
    public static ModConfigSpec.BooleanValue ENABLE_SEARCH_SYSTEM;
    public static ModConfigSpec.IntValue MAX_SEARCH_TIME_TICKS;
    public static ModConfigSpec.DoubleValue SEARCH_SPEED_MULTIPLIER;
    
    // 稀有度搜索时间配置（支持任意稀有度等级）
    // 格式: rarity:baseTime:randomTime[:soundId]
    public static ModConfigSpec.ConfigValue<List<? extends String>> RARITY_SEARCH_TIMES;
    
    // Custom loot table paths configuration
    public static ModConfigSpec.ConfigValue<List<? extends String>> CUSTOM_LOOT_TABLE_PATHS;
    
    // Chest path segments for dynamic matching
    public static ModConfigSpec.ConfigValue<List<? extends String>> CHEST_PATH_SEGMENTS;
    
    // Hotbar search configuration
    public static ModConfigSpec.BooleanValue ENABLE_HOTBAR_SEARCH;
    
    // Single slot search configuration
    public static ModConfigSpec.BooleanValue ENABLE_SINGLE_SLOT_SEARCH;
    
    // Single slot search time multiplier
    public static ModConfigSpec.BooleanValue SINGLE_SLOT_SEARCH_TIME_MULTIPLIER;
    
    // Mouse target search configuration
    public static ModConfigSpec.BooleanValue ENABLE_MOUSE_TARGET_SEARCH;
    public static ModConfigSpec.DoubleValue MOUSE_TARGET_SWITCH_DELAY;
    
    // Search progress sound configuration
    public static ModConfigSpec.BooleanValue ENABLE_SEARCH_PROGRESS_SOUND;
    
    // Mask rendering layer configuration
    public static ModConfigSpec.BooleanValue MASK_RENDER_ON_TOP;
    
    static {
        BUILDER.push("Search System Configuration");

        ENABLE_SEARCH_SYSTEM = BUILDER
                .comment("Enable the tactical search system")
                .define("enableSearchSystem", true);

        MAX_SEARCH_TIME_TICKS = BUILDER
                .comment("Maximum search time in ticks (20 ticks = 1 second)")
                .defineInRange("maxSearchTimeTicks", 200, 1, 1000);

        SEARCH_SPEED_MULTIPLIER = BUILDER
                .comment("Multiplier for search speed (higher values = faster search)")
                .defineInRange("searchSpeedMultiplier", 1.0, 0.1, 10.0);
        
        // Rarity search time configuration (supports any rarity level)
        RARITY_SEARCH_TIMES = BUILDER
                .comment("Search time configuration for each rarity level.",
                         "Format: rarity:baseTime:randomTime[:soundId]",
                         "  rarity: The rarity level (integer >= 1)",
                         "  baseTime: Base search time in ticks (integer >= 1)",
                         "  randomTime: Random time range in ticks (integer >= 0)",
                         "    Actual search time = baseTime +/- random(0, randomTime)",
                         "  soundId: (Optional) Sound event ID to play on completion.",
                         "    Format: namespace:path, e.g. 'minecraft:entity.player.levelup'",
                         "    Can reference any registered sound (vanilla, mod, or resource pack).",
                         "    If omitted or invalid, rarity is auto-mapped to the built-in completion sounds (1-7).",
                         "Examples:",
                         "  '1:10:10:searchcarefully:search_completion_rarity_1' - Rarity 1 with custom sound",
                         "  '8:200:20:minecraft:block.note_block.chime'       - Rarity 8 with vanilla sound",
                         "  '10:400:50'                                        - Rarity 10 with auto-mapped sound",
                         "",
                         "Default values (rarity 1-7 with their built-in completion sounds):")
                .defineListAllowEmpty(List.of("raritySearchTimes"),
                    () -> List.of(
                        "1:10:10:searchcarefully:search_completion_rarity_1",
                        "2:30:10:searchcarefully:search_completion_rarity_2",
                        "3:55:10:searchcarefully:search_completion_rarity_3",
                        "4:85:10:searchcarefully:search_completion_rarity_4",
                        "5:110:10:searchcarefully:search_completion_rarity_5",
                        "6:130:10:searchcarefully:search_completion_rarity_6",
                        "7:140:10:searchcarefully:search_completion_rarity_7"
                    ),
                    obj -> obj instanceof String s && s.matches("\\d+:\\d+:\\d+(:.*)?"));
        
        // Custom loot table paths configuration
        CUSTOM_LOOT_TABLE_PATHS = BUILDER
                .comment("Additional loot table paths to apply search times to (one path per line)",
                         "Example: ['modid:special_chest', 'anothermod:treasure_box']",
                         "Note: These are full resource locations, not just path prefixes")
                .defineListAllowEmpty(List.of("customLootTablePaths"), 
                                    ArrayList::new, 
                                    obj -> obj instanceof String s && !s.isEmpty());
        
        // Chest path segments for dynamic middle-path matching
        CHEST_PATH_SEGMENTS = BUILDER
                .comment("Path segments that indicate chest-type loot tables for middle-path matching",
                         "Example: ['chest', 'chests', 'treasure']",
                         "Used to match paths like 'structures/village/chest' or 'modid/special/chests'")
                .defineListAllowEmpty(List.of("chestPathSegments"),
                                    () -> List.of("chest", "chests"),
                                    obj -> obj instanceof String s && !s.isEmpty());
        
        // Hotbar search configuration
        ENABLE_HOTBAR_SEARCH = BUILDER
                .comment("Enable search system for hotbar slots (items in hotbar will be searched automatically)",
                         "Default: false")
                .define("enableHotbarSearch", false);
        
        // Single slot search configuration
        ENABLE_SINGLE_SLOT_SEARCH = BUILDER
                .comment("Enable single slot search mode (search items one by one in order)",
                         "When enabled, items will be searched one by one starting from the first slot",
                         "Default: false")
                .define("enableSingleSlotSearch", false);
        
        // Single slot search time multiplier
        SINGLE_SLOT_SEARCH_TIME_MULTIPLIER = BUILDER
                .comment("Apply 3x time multiplier when single slot search mode is enabled",
                         "Default: true")
                .define("singleSlotSearchTimeMultiplier", true);

        // Mouse target search configuration
        BUILDER.push("Mouse Target Search");
        
        ENABLE_MOUSE_TARGET_SEARCH = BUILDER
                .comment("Enable mouse-targeted search mode",
                         "When enabled, mouse cursor will target specific items for search",
                         "If mouse is not pointing at any item, automatic search continues",
                         "Default: true")
                .define("enableMouseTargetSearch", true);
        
        MOUSE_TARGET_SWITCH_DELAY = BUILDER
                .comment("Delay before switching to mouse target (in ticks)",
                         "Higher values prevent rapid switching when moving mouse quickly",
                         "Default: 3.0 ticks")
                .defineInRange("mouseTargetSwitchDelay", 3.0, 0.0, 20.0);
        
        BUILDER.pop();
        
        // Search progress sound configuration
        BUILDER.push("Search Progress Sound");
        
        ENABLE_SEARCH_PROGRESS_SOUND = BUILDER
                .comment("Enable playing sound during search progress",
                         "When enabled, a looping search progress sound will play while items are being searched",
                         "Default: true")
                .define("enableSearchProgressSound", true);
        
        BUILDER.pop();
        
        // Mask rendering layer configuration
        BUILDER.push("Mask Rendering");
        
        MASK_RENDER_ON_TOP = BUILDER
                .comment("Render search mask at the highest rendering layer",
                         "When enabled, the search mask will render on top of ALL screen elements including tooltips",
                         "This improves compatibility with mods that render custom elements between items and tooltips",
                         "Default: false")
                .define("maskRenderOnTop", false);
        
        BUILDER.pop();
        
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}

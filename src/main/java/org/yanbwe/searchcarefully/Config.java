package org.yanbwe.searchcarefully;

import net.minecraftforge.common.ForgeConfigSpec;
import java.util.ArrayList;
import java.util.List;

public class Config {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // Configuration options for the search system
    public static ForgeConfigSpec.BooleanValue ENABLE_SEARCH_SYSTEM;
    public static ForgeConfigSpec.IntValue MAX_SEARCH_TIME_TICKS;
    public static ForgeConfigSpec.DoubleValue SEARCH_SPEED_MULTIPLIER;
    
    // 各稀有度等级的基础搜索时间
    public static ForgeConfigSpec.IntValue[] RARITY_BASE_TIMES = new ForgeConfigSpec.IntValue[8]; // 索引0未使用，1-7对应稀有度
    
    // 各稀有度等级的独立随机时间增量
    public static ForgeConfigSpec.IntValue[] RARITY_RANDOM_TIMES = new ForgeConfigSpec.IntValue[8]; // 索引0未使用，1-7对应稀有度
    
    // Custom loot table paths configuration
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> CUSTOM_LOOT_TABLE_PATHS;
    
    // Chest path segments for dynamic matching
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> CHEST_PATH_SEGMENTS;
    
    // Hotbar search configuration
    public static ForgeConfigSpec.BooleanValue ENABLE_HOTBAR_SEARCH;
    
    // Single slot search configuration
    public static ForgeConfigSpec.BooleanValue ENABLE_SINGLE_SLOT_SEARCH;
    
    // Single slot search time multiplier
    public static ForgeConfigSpec.BooleanValue SINGLE_SLOT_SEARCH_TIME_MULTIPLIER;
    
    // Mouse target search configuration
    public static ForgeConfigSpec.BooleanValue ENABLE_MOUSE_TARGET_SEARCH;
    public static ForgeConfigSpec.DoubleValue MOUSE_TARGET_SWITCH_DELAY;
    
    // Search progress sound configuration
    public static ForgeConfigSpec.BooleanValue ENABLE_SEARCH_PROGRESS_SOUND;
    public static ForgeConfigSpec.DoubleValue SEARCH_PROGRESS_SOUND_INTERVAL;
    
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
        
        // Base search times for each rarity level
        RARITY_BASE_TIMES[1] = BUILDER
                .comment("Base search time for rarity 1 (in ticks)")
                .defineInRange("rarity1BaseTime", 10, 1, 1000);
        RARITY_BASE_TIMES[2] = BUILDER
                .comment("Base search time for rarity 2 (in ticks)")
                .defineInRange("rarity2BaseTime", 30, 1, 1000);
        RARITY_BASE_TIMES[3] = BUILDER
                .comment("Base search time for rarity 3 (in ticks)")
                .defineInRange("rarity3BaseTime", 55, 1, 1000);
        RARITY_BASE_TIMES[4] = BUILDER
                .comment("Base search time for rarity 4 (in ticks)")
                .defineInRange("rarity4BaseTime", 85, 1, 1000);
        RARITY_BASE_TIMES[5] = BUILDER
                .comment("Base search time for rarity 5 (in ticks)")
                .defineInRange("rarity5BaseTime", 110, 1, 1000);
        RARITY_BASE_TIMES[6] = BUILDER
                .comment("Base search time for rarity 6 (in ticks)")
                .defineInRange("rarity6BaseTime", 130, 1, 1000);
        RARITY_BASE_TIMES[7] = BUILDER
                .comment("Base search time for rarity 7 (in ticks)")
                .defineInRange("rarity7BaseTime", 140, 1, 1000);
        
        // Individual random time additions for each rarity level
        RARITY_RANDOM_TIMES[1] = BUILDER
                .comment("Random time addition for rarity 1 (in ticks, 0 = no randomness)")
                .defineInRange("rarity1RandomTime", 10, 0, 1000);
        RARITY_RANDOM_TIMES[2] = BUILDER
                .comment("Random time addition for rarity 2 (in ticks, 0 = no randomness)")
                .defineInRange("rarity2RandomTime", 10, 0, 1000);
        RARITY_RANDOM_TIMES[3] = BUILDER
                .comment("Random time addition for rarity 3 (in ticks, 0 = no randomness)")
                .defineInRange("rarity3RandomTime", 10, 0, 1000);
        RARITY_RANDOM_TIMES[4] = BUILDER
                .comment("Random time addition for rarity 4 (in ticks, 0 = no randomness)")
                .defineInRange("rarity4RandomTime", 10, 0, 1000);
        RARITY_RANDOM_TIMES[5] = BUILDER
                .comment("Random time addition for rarity 5 (in ticks, 0 = no randomness)")
                .defineInRange("rarity5RandomTime", 10, 0, 1000);
        RARITY_RANDOM_TIMES[6] = BUILDER
                .comment("Random time addition for rarity 6 (in ticks, 0 = no randomness)")
                .defineInRange("rarity6RandomTime", 10, 0, 1000);
        RARITY_RANDOM_TIMES[7] = BUILDER
                .comment("Random time addition for rarity 7 (in ticks, 0 = no randomness)")
                .defineInRange("rarity7RandomTime", 10, 0, 1000);
        
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
                                    () -> List.of("chest", "chests", "block"),
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
        
        // Search progress sound configuration
        BUILDER.push("Search Progress Sound");
        
        ENABLE_SEARCH_PROGRESS_SOUND = BUILDER
                .comment("Enable playing sound during search progress",
                         "When enabled, a sound will play at intervals while searching",
                         "Default: true")
                .define("enableSearchProgressSound", true);
        
        SEARCH_PROGRESS_SOUND_INTERVAL = BUILDER
                .comment("Interval between search progress sounds (in seconds)",
                         "Default: 0.5 seconds")
                .defineInRange("searchProgressSoundInterval", 0.5, 0.1, 10.0);
        
        BUILDER.pop();
        
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
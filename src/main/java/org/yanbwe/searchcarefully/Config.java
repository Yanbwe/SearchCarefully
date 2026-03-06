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

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
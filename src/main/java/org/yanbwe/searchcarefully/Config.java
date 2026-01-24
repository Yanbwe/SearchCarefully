package org.yanbwe.searchcarefully;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // Configuration options for the search system
    public static ForgeConfigSpec.BooleanValue ENABLE_SEARCH_SYSTEM;
    public static ForgeConfigSpec.IntValue MAX_SEARCH_TIME_TICKS;
    public static ForgeConfigSpec.DoubleValue SEARCH_SPEED_MULTIPLIER;
    
    // Base search times for each rarity level
    public static ForgeConfigSpec.IntValue[] RARITY_BASE_TIMES = new ForgeConfigSpec.IntValue[8]; // Index 0 unused, 1-7 for rarities
    
    // Individual random time additions for each rarity level
    public static ForgeConfigSpec.IntValue[] RARITY_RANDOM_TIMES = new ForgeConfigSpec.IntValue[8]; // Index 0 unused, 1-7 for rarities
    
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

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
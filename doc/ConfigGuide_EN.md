# SearchCarefully Mod Configuration Guide

## Configuration File Location

The configuration file is located at `.minecraft/config/searchcarefully-common.toml`

## Detailed Explanation of Configuration Options

### Search System Configuration

#### `enableSearchSystem` (Boolean, default: true)
- Function: Enable or disable the entire search system
- true: Enables the search function
- false: Disables the search function

#### `maxSearchTimeTicks` (integer, range: 1-1000, default: 200)
- Function: Sets the maximum search time (in game ticks)
- Note: 20 game ticks = 1 second
- Recommended values: Adjust according to server requirements

#### `searchSpeedMultiplier` (float, range: 0.1-10.0, default: 1.0)
- Function: Search speed multiplier
- 1.0: Normal speed
- Greater than 1.0: Increases search speed
- Less than 1.0: Reduces the search speed

### Rarity Base Search Time Configuration

#### `rarity1BaseTime` (integer, range: 1-1000, default: 10)
- Function: The base search time (game ticks) for items with rarity level 1
- Corresponds to approximately 0.5 seconds

#### `rarity2BaseTime` (integer, range: 1-1000, default: 40)
- Function: Base search time (game ticks) for items with rarity level 2
- Corresponds to approximately 2 seconds

#### `rarity3BaseTime` (integer, range: 1-1000, default: 60)
- Function: Base search time (game ticks) for items with rarity level 3
- corresponds to approximately 3 seconds

#### `rarity4BaseTime` (integer, range: 1-1000, default: 80)
- Function: Base search time (game ticks) for items with rarity level 4
- corresponds to approximately 4 seconds

#### `rarity5BaseTime` (integer, range: 1-1000, default: 100)
- Function: The base search time (game ticks) for items with a rarity of 5
- corresponds to approximately 5 seconds

#### `rarity6BaseTime` (integer, range: 1-1000, default: 120)
- Function: The base search time (game ticks) for items with a rarity of 6
- Corresponding to approximately 6 seconds

#### `rarity7BaseTime` (integer, range: 1-1000, default: 140)
- Function: Base search time (game ticks) for items with rarity level 7
- Corresponds to approximately 7 seconds

### Rarity Random Time Configuration

#### `rarity1RandomTime` (integer, range: 0-1000, default: 0)
- Function: Random time variation (game ticks) for items with rarity level 1
- 0: No random variation
- Greater than 0: The search time can fluctuate around the base time

#### `rarity2RandomTime` (integer, range: 0-1000, default: 0)
- Function: Random time variation (game ticks) for items with rarity level 2

#### `rarity3RandomTime` (integer, range: 0-1000, default: 0)
- Function: Random time variation (game ticks) for items with rarity level 3

#### `rarity4RandomTime` (integer, range: 0-1000, default: 0)
- Function: Random time variation (game ticks) for items with rarity level 4

#### `rarity5RandomTime` (integer, range: 0-1000, default: 0)
- Function: Random time variation (game ticks) for items with rarity level 5

#### `rarity6RandomTime` (integer, range: 0-1000, default: 0)
- Function: Random time variation (game ticks) for items with rarity level 6

#### `rarity7RandomTime` (integer, range: 0-1000, default: 0)
- Function: Random time variation (game ticks) for items with rarity level 7

### Custom Loot Table Paths Configuration

#### `customLootTablePaths` (String List, default: [])
- Function: Add additional loot table paths to apply search time mechanism
- Format: List of complete resource location identifiers
- Example:
  ```toml
  customLootTablePaths = [
      "modid:special_chest",
      "anothermod:treasure_box",
      "custommod:magic_container"
  ]
  ```
- Description:
  - These paths must be complete resource locations (namespace:path format)
  - Complements the standard chest/chests path mechanism
  - Suitable for mod loot tables using non-standard paths
  - Arbitrary number of custom paths can be added

#### `chestPathSegments` (String List, default: ["chest", "chests"])
- Function: Define path segments for middle-path matching
- Format: List of path segment strings
- Default value: `["chest", "chests"]`
- Example:
  ```toml
  chestPathSegments = ["chest", "chests", "treasure", "loot"]
  ```
- Description:
  - Used to match loot tables containing these segments anywhere in the path
  - For example matches: `structures/village/chest`, `modid/special/chests`, `dungeons/treasure_room`
  - Supports user-defined extensions to adapt to different mod naming conventions
  - Forms a complete matching system with prefix matching (`chest/`, `chests/`) and full path matching


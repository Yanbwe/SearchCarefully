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


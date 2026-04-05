# SearchCarefully API Documentation

**Mod Version**: 1201.3.0  
**Last Updated**: 2026-03-07

---

## Table of Contents

1. [Item Search Core](#item-search-core)
2. [Player Attributes & Buffs](#player-attributes--buffs)
3. [Configuration Management](#configuration-management)
4. [Network & Synchronization](#network--synchronization)
5. [Loot System](#loot-system)
6. [Sound & Feedback](#sound--feedback)
7. [Client Rendering](#client-rendering)
8. [Commands & Management](#commands--management)
9. [Data Formats](#data-formats)

---

## Item Search Core

### Creating and Setting Search Time

#### Adding Search Time to Items

```java
import org.yanbwe.searchcarefully.util.ItemStackHelper;

// Method 1: Directly set search time
ItemStack stack = new ItemStack(Items.DIAMOND);
ItemStackHelper.setRemainingSearchTime(stack, 100.0); 

// Method 2: Using placeholder items
import org.yanbwe.searchcarefully.item.SearchPlaceholderItem;
ItemStack placeholder = SearchPlaceholderItem.createPlaceholder(originalItem, searchTime);
```

#### Checking Search Status

```java
import org.yanbwe.searchcarefully.util.ItemStackHelper;

// Check if has search time
boolean hasTime = ItemStackHelper.hasRemainingSearchTime(stack);

// Get remaining time
double remaining = ItemStackHelper.getRemainingSearchTime(stack);

// Check if search is complete
boolean isComplete = ItemStackHelper.isSearchComplete(stack);
```

#### Processing Search Progress

```java
import org.yanbwe.searchcarefully.Searchcarefully;
import org.yanbwe.searchcarefully.util.ItemStackHelper;

// Reduce search time
double amount = 1.0; // Base reduction amount
double speed = Searchcarefully.getPlayerSearchSpeed(player);
double actualDecrement = amount * Config.SEARCH_SPEED_MULTIPLIER.get() * speed;

double newTime = ItemStackHelper.decrementSearchTime(stack, actualDecrement);

// Clean up when search is complete
if (newTime <= 0) {
    ItemStackHelper.completeSearch(stack);
}
```

#### Batch Operations

```java
import org.yanbwe.searchcarefully.util.ItemStackHelper;

// Find all items with search time
List<ItemStack> searchingItems = ItemStackHelper.findAllItemsWithSearchTime(inventoryItems);

// Clear all search tags
int clearedCount = ItemStackHelper.clearAllSearchTags(inventoryItems);
```

---

### Placeholder Item System

**Package Path**: `org.yanbwe.searchcarefully.item.SearchPlaceholderItem`

Placeholder items are used to replace original items during the search process.

#### API Usage

```java
// Check if it's a placeholder item
boolean isPlaceholder = SearchPlaceholderItem.isPlaceholder(stack);

// Get original item from placeholder
ItemStack original = SearchPlaceholderItem.getOriginalItem(placeholderStack);

// Create placeholder item
ItemStack placeholder = SearchPlaceholderItem.createPlaceholder(originalItem, searchTime);
```

#### NBT Structure

```nbt
{
  SearchTimeRemaining: 100.0,  // double type
  SearchItem: {                // Original item data
    id: "minecraft:diamond",
    Count: 1b,
    ...
  }
}
```

**Note**: Placeholder items currently only implement search functionality for placeholders and are not yet integrated into loot tables.

---

## Player Attributes & Buffs

### Getting Player Search Speed

```java
import org.yanbwe.searchcarefully.Searchcarefully;

// Get base attribute value
double baseSpeed = Searchcarefully.getPlayerSearchSpeed(player);
```

**Attribute Description**:
- Attribute Name: `search_speed`
- Range: 0.0 - 100.0
- Default Value: 1.0
- Network synchronization enabled

### Potion Effects

#### Search Speed Boost (`SearchSpeedBoostEffect`)

**Registered Object**: `Searchcarefully.SEARCH_SPEED_BOOST`

**Effect Calculation**:
```java
if (player.hasEffect(Searchcarefully.SEARCH_SPEED_BOOST.get())) {
    int amplifier = player.getEffect(Searchcarefully.SEARCH_SPEED_BOOST.get()).getAmplifier();
    // +20% search speed per level
    double multiplier = 1.0 + (amplifier + 1) * 0.2;
}
```

**Potion Recipes**:
- `SEARCH_SPEED_POTION_1` ~ `SEARCH_SPEED_POTION_5` (Level 1-5)
- Duration: 12000 ticks (10 minutes)

#### Search Speed Reduction (`SearchSpeedLessEffect`)

**Registered Object**: `Searchcarefully.SEARCH_SPEED_LESS`

**Potion Recipe**:
- `SEARCH_SPEED_LESS_POTION_2` (Level 2)
- Duration: 12000 ticks (10 minutes)

---

### Processing Search Progress

#### Items in Containers

```java
import org.yanbwe.searchcarefully.Searchcarefully;

// Automatically process search progress for specified slot in container
Searchcarefully.handleSearchProgress(player, slotIndex);
```

#### Hotbar Items

```java
import org.yanbwe.searchcarefully.Searchcarefully;

// Automatically process search progress for hotbar items
Searchcarefully.handleHotbarSearchProgress(player, hotbarSlotIndex);
```

**Note**: Requires configuration to be enabled (`Config.ENABLE_HOTBAR_SEARCH`)

---

## Configuration Management

### Reading Configuration Values

```java
import org.yanbwe.searchcarefully.Config;

// System switches
boolean enabled = Config.ENABLE_SEARCH_SYSTEM.get();
boolean hotbarEnabled = Config.ENABLE_HOTBAR_SEARCH.get();

// Search time
int maxTime = Config.MAX_SEARCH_TIME_TICKS.get();
double multiplier = Config.SEARCH_SPEED_MULTIPLIER.get();

// Search times for each rarity
int rarity3Time = Config.RARITY_BASE_TIMES[3].get() + 
                  Config.RARITY_RANDOM_TIMES[3].get();

// Custom loot tables
List<String> customTables = Config.CUSTOM_LOOT_TABLE_PATHS.get();
List<String> chestSegments = Config.CHEST_PATH_SEGMENTS.get();
```

### Configuration Options Description

| Configuration Option | Type | Default Value | Range | Description |
|---------------------|------|---------------|-------|-------------|
| `enableSearchSystem` | boolean | true | - | Whether to enable the search system |
| `maxSearchTimeTicks` | int | 200 | 1-1000 | Maximum search time (ticks) |
| `searchSpeedMultiplier` | double | 1.0 | 0.1-10.0 | Search speed multiplier |
| `rarity{1-7}BaseTime` | int | 10-140 | 1-1000 | Base time for each rarity |
| `rarity{1-7}RandomTime` | int | 10 | 0-1000 | Random time for each rarity |
| `customLootTablePaths` | List\<String\> | [] | - | Custom loot table paths |
| `chestPathSegments` | List\<String\> | ["chest", "chests", "block"] | - | Chest path matching segments |
| `enableHotbarSearch` | boolean | false | - | Whether to enable hotbar search |

---

## Network & Synchronization

### Initializing Network Handler

```java
import org.yanbwe.searchcarefully.network.NetworkHandler;

@SubscribeEvent
public void commonSetup(FMLCommonSetupEvent event) {
    NetworkHandler.registerMessages();
}
```

### Network Channel

```java
import org.yanbwe.searchcarefully.network.NetworkHandler;

// Send network packet
NetworkHandler.INSTANCE.send(...);

// Receive and process (implemented in SearchProgressPacket)
```

---

## Loot System

### Global Loot Modifier

**Class**: `AddSearchTimeLootModifier`  
**Package Path**: `org.yanbwe.searchcarefully.loot.AddSearchTimeLootModifier`

Automatically adds search time to items in loot chests.

#### JSON Configuration Example

```json
{
  "type": "searchcarefully:add_search_time",
  "conditions": [
    {
      "condition": "forge:loot_table_id",
      "loot_table_id": "minecraft:chests/simple_dungeon"
    }
  ]
}
```

#### How It Works

1. Define which loot tables should apply modifiers through JSON configuration files
2. Automatically add `SearchTimeRemaining` NBT tag to generated items
3. Calculate search time based on item rarity

---

## Sound & Feedback

### Playing Search Completion Sound

```java
import org.yanbwe.searchcarefully.sounds.SoundHandler;
import net.yanbwe.raritycore.registry.RarityRegistry;

// Get item rarity
ItemStack stack = ...;
int rarity = RarityRegistry.getNormalizedRarity(stack.getItem());

// Play sound effect
SoundHandler.playSearchCompletionSound(
    level,      // Game world
    x, y, z,    // Sound position
    rarity      // Rarity (1-7)
);
```

### Registered Sounds

| Sound Name | Rarity | Usage |
|-----------|--------|-------|
| `search_completion_rarity_1` | 1 | `Searchcarefully.RARITY_COMPLETION_SOUNDS[1].get()` |
| `search_completion_rarity_2` | 2 | `Searchcarefully.RARITY_COMPLETION_SOUNDS[2].get()` |
| ... | ... | ... |
| `search_completion_rarity_7` | 7 | `Searchcarefully.RARITY_COMPLETION_SOUNDS[7].get()` |

---

## Client Rendering

### Mixin Injection Points

The project uses Mixin to inject into Minecraft vanilla code for rendering:

| Mixin Class | Target | Function |
|------------|---------|----------|
| `ContainerScreenRenderMixin` | `ContainerScreen` | Render search progress in container interface |
| `SlotMixin` | `Slot` | Slot interaction logic |
| `SlotRenderMixin` | `SlotRenderer` | Slot rendering |
| `TooltipRenderMixin` | `GuiGraphics` | Tooltip rendering with search information |
| `GuiHotbarRenderMixin` | `Gui` | Hotbar rendering |
| `ChestMenuAccessor` | `ChestMenu` | Access chest menu internal fields |

### Rendering Related Classes

- **ClientOverlayRenderer**: Renders search progress overlay
- **ClientTickHandler**: Handles client tick events and search progress updates

---

## Utility Class API

### `ItemStackHelper`

**Package Path**: `org.yanbwe.searchcarefully.util.ItemStackHelper`

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `hasRemainingSearchTime` | `ItemStack stack` | `boolean` | Check if has search time |
| `getRemainingSearchTime` | `ItemStack stack` | `double` | Get remaining search time |
| `setRemainingSearchTime` | `ItemStack stack, double time` | `void` | Set search time |
| `decrementSearchTime` | `ItemStack stack, double amount` | `double` | Reduce search time |
| `completeSearch` | `ItemStack stack` | `void` | Complete search and clean NBT |
| `isSearchComplete` | `ItemStack stack` | `boolean` | Check if complete |
| `findAllItemsWithSearchTime` | `List<ItemStack> items` | `List<ItemStack>` | Find all items with search time |
| `clearAllSearchTags` | `List<ItemStack> items` | `int` | Clear all search tags |

### `SearchConstants`

**Package Path**: `org.yanbwe.searchcarefully.util.SearchConstants`

| Field/Method | Type | Description |
|-------------|------|-------------|
| `SEARCH_TIME_REMAINING` | `String` | NBT tag key name `"SearchTimeRemaining"` |
| `getSearchTimeByRarity` | `int rarity -> int` | Get search time by rarity |

---

## Data Formats

### NBT Tag Structure

**Search Time Item**:
```nbt
{
  SearchTimeRemaining: 100.0  # double type, remaining search time (ticks)
}
```

**Placeholder Item**:
```nbt
{
  SearchTimeRemaining: 100.0,  # double type
  SearchItem: {                # Original item data
    id: "minecraft:diamond",
    Count: 1b,
    ...
  }
}
```

### Network Packet Format

**SearchProgressPacket**:
```
[VarInt: slotIndex] [Double: remainingTime]
```

---

## ⚠️ Important Notes

**Thread Safety**: Most methods are not thread-safe, please call them on the main thread

---

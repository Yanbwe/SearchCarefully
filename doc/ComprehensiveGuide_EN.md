## Basic Concepts and Principles

SearchCarefully implements the search system through the following methods:

1. Modifies specific loot table paths through loot modifiers to execute the mod's functions.
2. In scripts, uses the API provided by the RarityCore mod to obtain the corrected item rarity level.
3. Adds a "SearchTimeRemaining" tag (type: int) to items based on their rarity and configuration to record remaining search time.
4. When an item with this tag appears in a GUI opened by a player, its tag value decreases by 1 per tick until it reaches 0, then the tag is removed.

## Adding Search Time to a Single Item

Let's understand through examples:

This is the NBT structure of 32 diamonds with 1000 ticks of search time:
```
(root)
- Count:32
- id:"minecraft:diamond"
- tag:{SearchTimeRemaining:1000}

```

NBT structure of the item:
Its SNBT is:  
`{Count:32,id:"minecraft:diamond",tag:{SearchTimeRemaining:1000}}`

Use the give command to grant this item:

`/give @p diamond{SearchTimeRemaining:1000} 32`

Check if the item in the player's hand has search time:

`/execute if data entity @p SelectedItem.tag.SearchTimeRemaining run say The item in hand has search time`

If you can understand the example, you should have mastered this content.

If you are a mod developer, you can use the mod's API to add search time more efficiently.

## Modifying Player Search Speed

This mod adds a Minecraft attribute `search_speed` for players.  
The unit is multiplier, default is 1.

This mod also adds two status effects:  
`Searchcarefully.SEARCH_SPEED_BOOST` and `Searchcarefully.SEARCH_SPEED_LESS`  
These are used to increase or decrease search speed respectively, with 20% per level.

You can modify this attribute using any method you prefer to easily change the player's search speed.

## Detailed Loot Table Configuration

The mod automatically recognizes loot tables with the following path formats to execute functions:

```
namespace:chests/loot_table         # Example: modid:chests/dungeon.json
namespace:chest/loot_table          # Example: modid:chest/treasure.json
```
It is recommended to use `chests` because this is the official path used by vanilla.

File structure example:
```
resources/
└── data/
    └── yourmod/
        └── loot_tables/
            ├── chest/                    # Will be recognized
            │   ├── village_chest.json
            │   ├── dungeon_chest.json
            │   └── buried_treasure.json
            ├── chests/                   # Will be recognized
            │   ├── end_city_treasure.json
            │   └── woodland_mansion.json
            └── custom/                 # Will NOT be recognized
                └── special_loot.json
```
By default, this mod uses middle-path matching, so the following loot table paths can also be recognized:
```
namespace:xxx/chests/xxx/loot_table   # Example: modid:ball/chests/water/treasure.json
```
If you really need the mod to match special paths, you need to modify the mod's configuration:

Configuration file: `.minecraft/config/searchcarefully-common.toml`

```
# Exactly match specific loot tables
customLootTablePaths = [
"yourmod:custom/special_loot",
"anothermod:unique/treasure_chest",
"mymod:rare/boss_loot"
]

# Segments for middle-path matching
chestPathSegments = [
"treasure",      
"cache",        
"container"      
]
```

## Fully Customizing Item Search Time in Loot Tables

First, you need to ensure that this loot table is not matched and modified by this mod.

Then, as shown in the content above, simply add tags to the items: (This is a loot table that will spawn a diamond sword with 114514 ticks of search time)
```
{
  "type": "minecraft:chest",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "minecraft:diamond_sword",
          "functions": [
            {
              "function": "minecraft:set_nbt",
              "tag": "{SearchTimeRemaining:114514}"
            }
          ]
        }
      ]
    }
  ]
}
```

## About Placeholder Items

Placeholder items are special items introduced in version `x.3.0`. Like the original search mechanism, when they appear in a GUI opened by a player, the value of their search time tag decreases.  
The difference is that placeholder items will take out the item stored in the tag and replace themselves when the search is complete.

NBT structure of placeholder items:
```nbt
{
  SearchTimeRemaining: 100.0,  // Search time
  SearchItem: {                // Original item data
    id: "minecraft:diamond",
    Count: 1b,
    ...
  }
}
```
You can use the mod's public method to quickly create placeholder items:  
`ItemStack placeholder = SearchPlaceholderItem.createPlaceholder(originalItem, searchTime)`

If you are not a mod developer, then when you manually fill in the NBT, please make sure the structure and content are correct and usable, otherwise the placeholder item will not replace itself when the search is complete.

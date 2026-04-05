## 基础概念与原理

仔细搜刮 通过以下方式实现搜索系统：

1. 通过 战利品修饰符 来修饰特定路径的战利品表，使其执行本模组的函数。
2. 在脚本中，通过 稀有度核心 模组提供的API获取修正后物品稀有度等级
3. 根据物品稀有度和配置来为物品添加"SearchTimeRemaining"标签(类型为int)，用来记录剩余搜索时间
4. 当拥有此标签的物品出现在玩家打开的GUI中时，使其标签数值每tick减少1，直到减少为0，然后删除标签。

## 为单个物品添加搜索时间

通过例子来理解吧：

这是32个拥有1000tick搜索时间的钻石的nbt结构：
```
(root)
- Count:32
- id:"minecraft:diamond"
- tag:{SearchTimeRemaining:1000}

```

物品的nbt结构
其snbt为：  
`{Count:32,id:"minecraft:diamond",tag:{SearchTimeRemaining:1000}}`

使用give指令基于该物品：

`/give @p diamond{SearchTimeRemaining:1000} 32`

检查玩家手上物品是否具有搜索时间：

`/execute if data entity @p SelectedItem.tag.SearchTimeRemaining run say 手上物品具有搜索时间`

如果你能看懂例子，那你应该掌握了此内容。

如果你是模组开发者，你可以通过本模组的API来更高效地添加搜索时间。

## 修改玩家搜索速度

本模组为玩家添加了一个minecraft属性`search_speed`。  
单位为倍，默认为1。  

本模组还添加了两个状态效果：  
`Searchcarefully.SEARCH_SPEED_BOOST`和`Searchcarefully.SEARCH_SPEED_LESS`  
分别用于提升或下降搜索速度，每级的效果为20%。

你可以用你喜欢的方式来修改这个属性，轻松改变玩家搜索速度。

## 战利品表配置详解

模组自动识别以下路径格式的战利品表使其执行函数：

```
命名空间:chests/战利品表         #例子：modid:chests/dungeon.json
命名空间:chest/战利品表          # 例子：modid:chest/treasure.json
```
推荐使用chests，因为这是官方使用的路径。

文件结构示例：
```
resources/
└── data/
└── yourmod/
└── loot_tables/
├── chest/                    #会识别
│   ├── village_chest.json
│   ├── dungeon_chest.json
│   └── buried_treasure.json
├── chests/                   #会识别
│   ├── end_city_treasure.json
│   └── woodland_mansion.json
└── custom/                 #不会识别
└── special_loot.json
```
本模组默认使用中间路径匹配，以下路径的战利品表也能被识别到：
```
命名空间:xxx/chests/xxx/战利品表   # 例子：modid:ball/chests/water/treasure.json
```
如果你是在需要本模组匹配特殊的路径，你需要修改本模组的配置：

配置文件：`.minecraft/config/searchcarefully-common.toml`

```
# 精确匹配特定战利品表
customLootTablePaths = [
"yourmod:custom/special_loot",
"anothermod:unique/treasure_chest",
"mymod:rare/boss_loot"
]

# 中间路径匹配的片段
chestPathSegments = [
"treasure",      
"cache",        
"container"      
]
```
## 完全自定义战利品表内的物品搜索时间

首先你需要保证此战利品表不会被本模组匹配到并修饰，

接着像上面内容一样，为物品打上tag即可：(这是一个会刷出一把带有114514tick搜索时间的钻石剑的战利品表)
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

## 关于占位物品

占位物品是在`x.3.0`版本推出的一种特殊的物品，和原有的搜索机制一样，出现在玩家打开的GUI中时，会减少其搜索时间tag的值。  
不同的是，占位物品会在搜索结束后取出tag内储存的物品，替换掉本身。

占位物品的nbt结构：
```nbt
{
  SearchTimeRemaining: 100.0,  // 搜索时间
  SearchItem: {                // 原始物品数据
    id: "minecraft:diamond",
    Count: 1b,
    ...
  }
}
```
你可以使用本模组的公共方法来快速创建占位物品：  
`ItemStack placeholder = SearchPlaceholderItem.createPlaceholder(originalItem, searchTime)`

如果你不是模组开发者，那么你手动填写nbt时，请务必保证结构和内容正确可用，否则会导致占位物品在搜索完成时不会替换自身。
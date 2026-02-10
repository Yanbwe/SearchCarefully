# SearchCarefully 模组配置指南

## 配置文件位置

配置文件位于 `.minecraft/config/searchcarefully-common.toml`

## 配置选项详解

### 搜索系统配置

#### `enableSearchSystem` (布尔值, 默认: true)
- 作用：启用或禁用整个搜索系统
- true：开启搜索功能
- false：关闭搜索功能

#### `maxSearchTimeTicks` (整数, 范围: 1-1000, 默认: 200)
- 作用：设置最大搜索时间（以游戏刻为单位）
- 注意：20 游戏刻 = 1 秒
- 建议值：根据服务器需求调整

#### `searchSpeedMultiplier` (浮点数, 范围: 0.1-10.0, 默认: 1.0)
- 作用：搜索速度倍数
- 1.0：正常速度
- 大于 1.0：加快搜索速度
- 小于 1.0：减慢搜索速度

### 稀有度基础搜索时间配置

#### `rarity1BaseTime` (整数, 范围: 1-1000, 默认: 10)
- 作用：稀有度1物品的基础搜索时间（游戏刻）
- 对应大约 0.5 秒

#### `rarity2BaseTime` (整数, 范围: 1-1000, 默认: 40)
- 作用：稀有度2物品的基础搜索时间（游戏刻）
- 对应大约 2 秒

#### `rarity3BaseTime` (整数, 范围: 1-1000, 默认: 60)
- 作用：稀有度3物品的基础搜索时间（游戏刻）
- 对应大约 3 秒

#### `rarity4BaseTime` (整数, 范围: 1-1000, 默认: 80)
- 作用：稀有度4物品的基础搜索时间（游戏刻）
- 对应大约 4 秒

#### `rarity5BaseTime` (整数, 范围: 1-1000, 默认: 100)
- 作用：稀有度5物品的基础搜索时间（游戏刻）
- 对应大约 5 秒

#### `rarity6BaseTime` (整数, 范围: 1-1000, 默认: 120)
- 作用：稀有度6物品的基础搜索时间（游戏刻）
- 对应大约 6 秒

#### `rarity7BaseTime` (整数, 范围: 1-1000, 默认: 140)
- 作用：稀有度7物品的基础搜索时间（游戏刻）
- 对应大约 7 秒

### 稀有度随机时间配置

#### `rarity1RandomTime` (整数, 范围: 0-1000, 默认: 0)
- 作用：稀有度1物品的随机时间变化（游戏刻）
- 0：无随机变化
- 大于 0：搜索时间可在基础时间上下波动

#### `rarity2RandomTime` (整数, 范围: 0-1000, 默认: 0)
- 作用：稀有度2物品的随机时间变化（游戏刻）

#### `rarity3RandomTime` (整数, 范围: 0-1000, 默认: 0)
- 作用：稀有度3物品的随机时间变化（游戏刻）

#### `rarity4RandomTime` (整数, 范围: 0-1000, 默认: 0)
- 作用：稀有度4物品的随机时间变化（游戏刻）

#### `rarity5RandomTime` (整数, 范围: 0-1000, 默认: 0)
- 作用：稀有度5物品的随机时间变化（游戏刻）

#### `rarity6RandomTime` (整数, 范围: 0-1000, 默认: 0)
- 作用：稀有度6物品的随机时间变化（游戏刻）

#### `rarity7RandomTime` (整数, 范围: 0-1000, 默认: 0)
- 作用：稀有度7物品的随机时间变化（游戏刻）

### 自定义战利品表路径配置

#### `customLootTablePaths` 
- 作用：添加额外的战利品表路径，为其应用搜索时间机制
- 格式：完整资源位置标识符列表
- 示例：
  ```toml
  customLootTablePaths = [
      "modid:special_chest",
      "anothermod:treasure_box",
      "custommod:magic_container"
  ]
  ```
- 说明：
  - 这些路径必须是完整的资源位置（namespace:path格式）
  - 适用于那些路径非常猎奇的模组战利品表
  - 可以添加任意数量的自定义路径

#### `chestPathSegments` 
- 作用：定义用于中间路径匹配的路径片段，为匹配的路径下的战利品表应用搜索时间机制
- 格式：路径片段字符串列表
- 默认值：`["chest", "chests"]`
- 示例：
  ```toml
  chestPathSegments = ["chest", "chests", "treasure", "loot"]
  ```
- 说明：
  - 用于匹配路径中间包含这些片段的战利品表
  - 如果你按照示例值写，会匹配：`structures/village/chest`、`modid/special/chests`、`dungeons/treasure_room`


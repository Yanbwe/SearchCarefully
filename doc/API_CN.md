# SearchCarefully API 文档

**模组版本**: 1201.3.0  
**最后更新**: 2026-03-07

---

## 目录

1. [物品搜索核心](#物品搜索核心)
2. [玩家属性与增益](#玩家属性与增益)
3. [配置管理](#配置管理)
4. [网络与同步](#网络与同步)
5. [战利品系统](#战利品系统)
6. [音效与反馈](#音效与反馈)
7. [客户端渲染](#客户端渲染)
8. [命令与管理](#命令与管理)
9. [数据格式](#数据格式)

---

## 物品搜索核心

### 创建和设置搜索时间

#### 为物品添加搜索时间

```java
import org.yanbwe.searchcarefully.util.ItemStackHelper;

// 方法 1: 直接设置搜索时间
ItemStack stack = new ItemStack(Items.DIAMOND);
ItemStackHelper.setRemainingSearchTime(stack, 100.0); 

// 方法 2: 使用占位物品
import org.yanbwe.searchcarefully.item.SearchPlaceholderItem;
ItemStack placeholder = SearchPlaceholderItem.createPlaceholder(originalItem, searchTime);
```

#### 检查搜索状态

```java
import org.yanbwe.searchcarefully.util.ItemStackHelper;

// 是否有搜索时间
boolean hasTime = ItemStackHelper.hasRemainingSearchTime(stack);

// 获取剩余时间
double remaining = ItemStackHelper.getRemainingSearchTime(stack);

// 是否完成搜索
boolean isComplete = ItemStackHelper.isSearchComplete(stack);
```

#### 处理搜索进度

```java
import org.yanbwe.searchcarefully.Searchcarefully;
import org.yanbwe.searchcarefully.util.ItemStackHelper;

// 减少搜索时间
double amount = 1.0; // 基础减少量
double speed = Searchcarefully.getPlayerSearchSpeed(player);
double actualDecrement = amount * Config.SEARCH_SPEED_MULTIPLIER.get() * speed;

double newTime = ItemStackHelper.decrementSearchTime(stack, actualDecrement);

// 搜索完成时清理
if (newTime <= 0) {
    ItemStackHelper.completeSearch(stack);
}
```

#### 批量操作

```java
import org.yanbwe.searchcarefully.util.ItemStackHelper;

// 查找所有有搜索时间的物品
List<ItemStack> searchingItems = ItemStackHelper.findAllItemsWithSearchTime(inventoryItems);

// 清除所有搜索标签
int clearedCount = ItemStackHelper.clearAllSearchTags(inventoryItems);
```

---

### 占位物品系统

**包路径**: `org.yanbwe.searchcarefully.item.SearchPlaceholderItem`

占位物品用于在搜索过程中替代原始物品。

#### API 使用

```java
// 检查是否为占位物品
boolean isPlaceholder = SearchPlaceholderItem.isPlaceholder(stack);

// 从占位物品获取原始物品
ItemStack original = SearchPlaceholderItem.getOriginalItem(placeholderStack);

// 创建占位物品
ItemStack placeholder = SearchPlaceholderItem.createPlaceholder(originalItem, searchTime);
```

#### NBT 结构

```nbt
{
  SearchTimeRemaining: 100.0,  // double 类型
  SearchItem: {                // 原始物品数据
    id: "minecraft:diamond",
    Count: 1b,
    ...
  }
}
```

**注意**: 占位物品目前仅实现占位物品的搜索功能，暂未集成到战利品表中。

---

## 玩家属性与增益

### 获取玩家搜索速度

```java
import org.yanbwe.searchcarefully.Searchcarefully;

// 获取基础属性值
double baseSpeed = Searchcarefully.getPlayerSearchSpeed(player);
```

**属性说明**:
- 属性名称：`search_speed`
- 范围：0.0 - 100.0
- 默认值：1.0
- 已启用网络同步

### 药水效果

#### 搜索速度提升 (`SearchSpeedBoostEffect`)

**注册对象**: `Searchcarefully.SEARCH_SPEED_BOOST`

**效果计算**:
```java
if (player.hasEffect(Searchcarefully.SEARCH_SPEED_BOOST.get())) {
    int amplifier = player.getEffect(Searchcarefully.SEARCH_SPEED_BOOST.get()).getAmplifier();
    // 每等级 +20% 搜索速度
    double multiplier = 1.0 + (amplifier + 1) * 0.2;
}
```

**药水配方**:
- `SEARCH_SPEED_POTION_1` ~ `SEARCH_SPEED_POTION_5` (等级 1-5)
- 持续时间：12000 ticks (10 分钟)

#### 搜索速度降低 (`SearchSpeedLessEffect`)

**注册对象**: `Searchcarefully.SEARCH_SPEED_LESS`

**药水配方**:
- `SEARCH_SPEED_LESS_POTION_2` (等级 2)
- 持续时间：12000 ticks (10 分钟)

---

### 处理搜索进度

#### 容器中的物品

```java
import org.yanbwe.searchcarefully.Searchcarefully;

// 自动处理容器中指定槽位的搜索进度
Searchcarefully.handleSearchProgress(player, slotIndex);
```

#### 热键栏物品

```java
import org.yanbwe.searchcarefully.Searchcarefully;

// 自动处理热键栏物品的搜索进度
Searchcarefully.handleHotbarSearchProgress(player, hotbarSlotIndex);
```

**注意**: 需要配置中启用 (`Config.ENABLE_HOTBAR_SEARCH`)

---

## 配置管理

### 读取配置值

```java
import org.yanbwe.searchcarefully.Config;

// 系统开关
boolean enabled = Config.ENABLE_SEARCH_SYSTEM.get();
boolean hotbarEnabled = Config.ENABLE_HOTBAR_SEARCH.get();

// 搜索时间
int maxTime = Config.MAX_SEARCH_TIME_TICKS.get();
double multiplier = Config.SEARCH_SPEED_MULTIPLIER.get();

// 各稀有度时间
int rarity3Time = Config.RARITY_BASE_TIMES[3].get() + 
                  Config.RARITY_RANDOM_TIMES[3].get();

// 自定义战利品表
List<String> customTables = Config.CUSTOM_LOOT_TABLE_PATHS.get();
List<String> chestSegments = Config.CHEST_PATH_SEGMENTS.get();
```

### 配置项说明

| 配置项 | 类型 | 默认值 | 范围   | 说明 |
|--------|------|--------|------|------|
| `enableSearchSystem` | boolean | true | -    | 是否启用搜索系统 |
| `maxSearchTimeTicks` | int | 200 | 1-1000 | 最大搜索时间 (tick) |
| `searchSpeedMultiplier` | double | 1.0 | 0~？  | 搜索速度倍率 |
| `rarity{1-7}BaseTime` | int | 10-140 | 1-1000 | 各稀有度基础时间 |
| `rarity{1-7}RandomTime` | int | 10 | 0-1000 | 各稀有度随机时间 |
| `customLootTablePaths` | List\<String\> | [] | -    | 自定义战利品表路径 |
| `chestPathSegments` | List\<String\> | ["chest", "chests", "block"] | -    | 箱子路径匹配段 |
| `enableHotbarSearch` | boolean | false | -    | 是否启用热键栏搜索 |

---

## 网络与同步

### 初始化网络处理器

```java
import org.yanbwe.searchcarefully.network.NetworkHandler;

@SubscribeEvent
public void commonSetup(FMLCommonSetupEvent event) {
    NetworkHandler.registerMessages();
}
```

### 网络通道

```java
import org.yanbwe.searchcarefully.network.NetworkHandler;

// 发送数据包
NetworkHandler.INSTANCE.send(...);

// 接收处理 (在 SearchProgressPacket 中实现)
```

---

## 战利品表系统

### 全局战利品修饰符

**类**: `AddSearchTimeLootModifier`  
**包路径**: `org.yanbwe.searchcarefully.loot.AddSearchTimeLootModifier`

自动为战利品箱中的物品添加搜索时间。

#### JSON 配置示例

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

#### 工作原理

1. 通过 JSON 配置文件定义哪些战利品表需要应用修饰符
2. 自动为生成的物品添加 `SearchTimeRemaining` NBT 标签
3. 根据物品稀有度计算搜索时间

---

## 音效与反馈

### 播放搜索完成音效

```java
import org.yanbwe.searchcarefully.sounds.SoundHandler;
import net.yanbwe.raritycore.registry.RarityRegistry;

// 获取物品稀有度
ItemStack stack = ...;
int rarity = RarityRegistry.getNormalizedRarity(stack.getItem());

// 播放音效
SoundHandler.playSearchCompletionSound(
    level,      // 游戏世界
    x, y, z,    // 音效位置
    rarity      // 稀有度 (1-7)
);
```

### 注册的音效

| 音效名称 | 稀有度 | 使用方式 |
|---------|-------|---------|
| `search_completion_rarity_1` | 1 | `Searchcarefully.RARITY_COMPLETION_SOUNDS[1].get()` |
| `search_completion_rarity_2` | 2 | `Searchcarefully.RARITY_COMPLETION_SOUNDS[2].get()` |
| ... | ... | ... |
| `search_completion_rarity_7` | 7 | `Searchcarefully.RARITY_COMPLETION_SOUNDS[7].get()` |

---

## 客户端渲染

### Mixin 注入点

项目使用 Mixin 注入到 Minecraft 原版代码实现渲染：

| Mixin 类 | 注入目标 | 功能 |
|---------|---------|------|
| `ContainerScreenRenderMixin` | `ContainerScreen` | 容器界面渲染搜索进度 |
| `SlotMixin` | `Slot` | 槽位交互逻辑 |
| `SlotRenderMixin` | `SlotRenderer` | 槽位渲染 |
| `TooltipRenderMixin` | `GuiGraphics` | 提示框渲染搜索信息 |
| `GuiHotbarRenderMixin` | `Gui` | 热键栏渲染 |
| `ChestMenuAccessor` | `ChestMenu` | 访问箱子菜单内部字段 |

### 渲染相关类

- **ClientOverlayRenderer**: 渲染搜索进度覆盖层
- **ClientTickHandler**: 处理客户端刻事件和搜索进度更新

---

## 工具类 API

### `ItemStackHelper`

**包路径**: `org.yanbwe.searchcarefully.util.ItemStackHelper`

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `hasRemainingSearchTime` | `ItemStack stack` | `boolean` | 检查是否有搜索时间 |
| `getRemainingSearchTime` | `ItemStack stack` | `double` | 获取剩余搜索时间 |
| `setRemainingSearchTime` | `ItemStack stack, double time` | `void` | 设置搜索时间 |
| `decrementSearchTime` | `ItemStack stack, double amount` | `double` | 减少搜索时间 |
| `completeSearch` | `ItemStack stack` | `void` | 完成搜索并清理 NBT |
| `isSearchComplete` | `ItemStack stack` | `boolean` | 检查是否完成 |
| `findAllItemsWithSearchTime` | `List<ItemStack> items` | `List<ItemStack>` | 查找所有有搜索时间的物品 |
| `clearAllSearchTags` | `List<ItemStack> items` | `int` | 清除所有搜索标签 |

### `SearchConstants`

**包路径**: `org.yanbwe.searchcarefully.util.SearchConstants`

| 字段/方法 | 类型 | 说明 |
|----------|------|------|
| `SEARCH_TIME_REMAINING` | `String` | NBT 标签键名 `"SearchTimeRemaining"` |
| `getSearchTimeByRarity` | `int rarity -> int` | 根据稀有度获取搜索时间 |

---

## 数据格式

### NBT 标签结构

**搜索时间物品**:
```nbt
{
  SearchTimeRemaining: 100.0  # double 类型，剩余搜索时间 (ticks)
}
```

**占位物品**:
```nbt
{
  SearchTimeRemaining: 100.0,  # double 类型
  SearchItem: {                # 原始物品数据
    id: "minecraft:diamond",
    Count: 1b,
    ...
  }
}
```

### 网络数据包格式

**SearchProgressPacket**:
```
[VarInt: slotIndex] [Double: remainingTime]
```

---

## ⚠️ 注意事项

**线程安全**: 大多数方法不是线程安全的，请在主线程调用

---

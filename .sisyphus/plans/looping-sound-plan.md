# 方案 C：循环音效系统实施计划

## 概述

将搜索进度音效从"间隔触发播放"模式改为"循环持续播放"模式。
搜索开始时启动循环音效，所有物品搜完（或关闭容器）时立即停止。

---

## 一、架构设计

### 数据流

```
服务端 (Server)                             客户端 (Client)
                                            
containerMenu 中有待搜物品                     
        │                                            
        ▼                                            
发送 StartLoopSoundPacket ──────────────→   SoundManager.play(SearchProgressLoopingSound)
        │                                             │
        ▼                                             ▼
每 tick 处理 SearchProgressPacket           音效持续循环 ↺
(SearchManager.handleSearchProgress)                  
        │                                            
        ▼                                            
所有物品搜完 / 容器关闭                              
        │                                            
发送 ClientboundStopSoundPacket ──────────→   SoundManager.stop() → 音效立即停止
```

### 关键设计决策

| 决策 | 选择 | 原因 |
|------|------|------|
| 停止音效方式 | 使用 Minecraft 内置 `ClientboundStopSoundPacket` | 无需自定义 packet，减少代码量 |
| 启动音效方式 | 自定义 `StartLoopSoundPacket` | 需要客户端创建 `SearchProgressLoopingSound` 实例 |
| 会话追踪 | 服务端 `Map<UUID, SearchSoundSession>` | 按玩家独立追踪，多人兼容 |
| sounds.json 修改 | `search_progress` 添加 `"stream": true` | 循环音效需要流式加载 |

---

## 二、变更清单

### 新增文件（4 个）

| # | 文件 | 分类 | 说明 |
|---|------|------|------|
| 1 | `client/SearchProgressLoopingSound.java` | 新增 | 客户端的循环音效实例类，实现 `TickableSoundInstance` |
| 2 | `client/SearchSoundManager.java` | 新增 | 客户端音效会话管理器，管理循环音效实例引用 |
| 3 | `network/StartLoopSoundPacket.java` | 新增 | Server→Client 数据包，触发客户端开始循环播放 |
| 4 | `manager/SearchSoundSessionManager.java` | 新增 | 服务端音效会话管理器，追踪每个玩家的音效状态 |

### 修改文件（5 个）

| # | 文件 | 改动规模 | 说明 |
|---|------|---------|------|
| 1 | `sounds.json` | ~2 行 | `search_progress` 添加 `"stream": true` |
| 2 | `network/NetworkHandler.java` | ~8 行 | 注册 `StartLoopSoundPacket` |
| 3 | `manager/SearchManager.java` | ~50 行 | 替换 `playSearchProgressSoundIfNeeded()` 为会话生命周期管理 |
| 4 | `Searchcarefully.java` | ~30 行 | 注册容器关闭事件监听 + 修改 `playSound` 调用方式 |
| 5 | `client/ClientTickHandler.java` | ~10 行 | 客户端关闭容器时通知服务端 |

### 无需修改的文件

| 文件 | 原因 |
|------|------|
| `Config.java` | 保留旧配置项保证向后兼容，`searchProgressSoundInterval` 在循环模式下被忽略 |
| `SearchCompletionSound.java` | 音效注册逻辑不变 |
| `SoundHandler.java` | 完成音效（completion sound）不受影响 |
| `ContainerSearchTracker.java` | 容器槽位跟踪逻辑不变 |
| `HybridSearchManager.java` | 搜索模式管理不变 |

---

## 三、详细实现

### 3.1 `sounds.json` 修改

```json
{
  "search_progress": {
    "sounds": [
      {
        "name": "searchcarefully:search_progress",
        "stream": true,        // ← false → true：循环音效需要流式加载
        "attenuation_distance": 16
      }
    ],
    "subtitle": "sound.searchcarefully.search_progress"
  }
}
```

### 3.2 新增：`client/SearchProgressLoopingSound.java`

```java
package org.yanbwe.searchcarefully.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

/**
 * 客户端搜索进度循环音效。
 * 在搜索过程中持续循环播放，直到收到停止指令。
 */
public class SearchProgressLoopingSound extends AbstractSoundInstance
        implements TickableSoundInstance {

    private boolean stopped = false;
    private final Player player;

    public SearchProgressLoopingSound(SoundEvent soundEvent, SoundSource source, Player player) {
        super(soundEvent, source, SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = true;             // 关键：标记为循环音效
        this.delay = 0;
        this.volume = 0.5F;
        this.pitch = 1.0F;
        this.attenuation = AttenuationType.LINEAR;
        this.relative = false;
        // 初始位置绑定到玩家
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    @Override
    public void tick() {
        if (player == null || !player.isAlive()) {
            stop();
            return;
        }
        // 每 tick 更新位置，跟随玩家移动
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    public void stop() {
        this.stopped = true;
        this.looping = false;
    }
}
```

### 3.3 新增：`client/SearchSoundManager.java`

```java
package org.yanbwe.searchcarefully.client;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.yanbwe.searchcarefully.sounds.SearchCompletionSound;

/**
 * 客户端搜索音效管理器
 * 维护当前活跃的循环音效引用，支持启动和停止
 */
public class SearchSoundManager {

    private static SearchProgressLoopingSound activeSound = null;

    /**
     * 启动搜索进度循环音效
     */
    public static void startSearchLoopSound(Player player) {
        // 先停止已有的音效，防止重复
        stopSearchLoopSound();

        SearchProgressLoopingSound sound = new SearchProgressLoopingSound(
            SearchCompletionSound.SEARCH_PROGRESS_SOUND_EVENT,
            SoundSource.BLOCKS,
            player
        );
        Minecraft.getInstance().getSoundManager().play(sound);
        activeSound = sound;
    }

    /**
     * 停止搜索进度循环音效
     */
    public static void stopSearchLoopSound() {
        if (activeSound != null) {
            activeSound.stop();
            activeSound = null;
        }
        // 兜底：通过 SoundManager 也停止一次
        Minecraft.getInstance().getSoundManager().stop(
            SearchCompletionSound.SEARCH_PROGRESS_SOUND_ID,
            SoundSource.BLOCKS
        );
    }
}
```

### 3.4 新增：`network/StartLoopSoundPacket.java`

```java
package org.yanbwe.searchcarefully.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.yanbwe.searchcarefully.client.SearchSoundManager;

import java.util.function.Supplier;

/**
 * 服务端→客户端：启动搜索进度循环音效
 */
public class StartLoopSoundPacket {

    public StartLoopSoundPacket() {}

    public StartLoopSoundPacket(FriendlyByteBuf buf) {}

    public void toBytes(FriendlyByteBuf buf) {}

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 仅在客户端执行
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                net.minecraft.client.Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    SearchSoundManager.startSearchLoopSound(mc.player);
                }
            });
        });
        ctx.get().setPacketHandled(true);
        return true;
    }
}
```

### 3.5 修改：`network/NetworkHandler.java`

```java
public static void registerMessages() {
    INSTANCE.registerMessage(
            packetId++,
            SearchProgressPacket.class,
            SearchProgressPacket::toBytes,
            SearchProgressPacket::new,
            SearchProgressPacket::handle
    );
    // 新增：注册循环音效启动包
    INSTANCE.registerMessage(
            packetId++,
            StartLoopSoundPacket.class,
            StartLoopSoundPacket::toBytes,
            StartLoopSoundPacket::new,
            StartLoopSoundPacket::handle
    );
}
```

### 3.6 新增：`manager/SearchSoundSessionManager.java`

```java
package org.yanbwe.searchcarefully.manager;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.sounds.SoundSource;
import org.yanbwe.searchcarefully.network.NetworkHandler;
import org.yanbwe.searchcarefully.network.StartLoopSoundPacket;
import org.yanbwe.searchcarefully.sounds.SearchCompletionSound;
import org.yanbwe.searchcarefully.util.ItemStackHelper;

import java.util.*;

/**
 * 服务端音效会话管理器
 * 追踪每个玩家的搜索音效播放状态，控制音效的启动和停止
 */
public class SearchSoundSessionManager {

    private static final Map<UUID, Boolean> playerSoundActive = new HashMap<>();

    /**
     * 检查玩家当前打开的容器（或快捷栏）中是否还有待搜物品
     */
    public static boolean hasAnySearchableItems(Player player) {
        if (player.containerMenu != null) {
            for (Slot slot : player.containerMenu.slots) {
                ItemStack stack = slot.getItem();
                if (stack.isEmpty()) continue;
                if (ItemStackHelper.hasRemainingSearchTime(stack)) {
                    double time = ItemStackHelper.getRemainingSearchTime(stack);
                    if (time > 0.0) return true;
                }
            }
        }

        // 如果开启快捷栏搜索，检查快捷栏
        if (org.yanbwe.searchcarefully.Config.ENABLE_HOTBAR_SEARCH.get()) {
            var inventory = player.getInventory();
            for (int i = 0; i < 9; i++) {
                ItemStack stack = inventory.getItem(i);
                if (stack.isEmpty()) continue;
                if (ItemStackHelper.hasRemainingSearchTime(stack)) {
                    double time = ItemStackHelper.getRemainingSearchTime(stack);
                    if (time > 0.0) return true;
                }
            }
        }

        return false;
    }

    /**
     * 更新音效播放状态。应在每次处理搜索进度后调用。
     * 
     * @param player 玩家
     */
    public static void updateSoundSession(Player player) {
        UUID uuid = player.getUUID();
        boolean currentlyPlaying = playerSoundActive.getOrDefault(uuid, false);
        boolean hasItems = hasAnySearchableItems(player);

        if (hasItems && !currentlyPlaying) {
            // 从无到有 → 启动循环音效
            startLoopSound(player);
            playerSoundActive.put(uuid, true);
        } else if (!hasItems && currentlyPlaying) {
            // 从有到无 → 停止循环音效
            stopLoopSound(player);
            playerSoundActive.put(uuid, false);
        }
        // hasItems && currentlyPlaying → 不变，继续播放
        // !hasItems && !currentlyPlaying → 不变
    }

    /**
     * 强制停止玩家的音效（如关闭容器时）
     */
    public static void forceStopSound(Player player) {
        UUID uuid = player.getUUID();
        if (playerSoundActive.getOrDefault(uuid, false)) {
            stopLoopSound(player);
            playerSoundActive.put(uuid, false);
        }
    }

    private static void startLoopSound(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.INSTANCE.send(
                new StartLoopSoundPacket(),
                serverPlayer.connection.getConnection(),
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> serverPlayer)
            );
        }
    }

    private static void stopLoopSound(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            // 使用 Minecraft 内置包停止音效——无需自定义 packet
            serverPlayer.connection.send(
                new ClientboundStopSoundPacket(
                    SearchCompletionSound.SEARCH_PROGRESS_SOUND_ID,
                    SoundSource.BLOCKS
                )
            );
        }
    }

    /**
     * 清理玩家会话（玩家退出时调用）
     */
    public static void removePlayer(UUID uuid) {
        playerSoundActive.remove(uuid);
    }

    /**
     * 重置所有状态（可用于 /reload 等场景）
     */
    public static void resetAll() {
        playerSoundActive.clear();
    }
}
```

### 3.7 修改：`manager/SearchManager.java`

需要做两处改动：

1. 在 `handleSearchProgress()` 和 `handleHotbarSearchProgress()` 的末尾添加会话更新调用
2. 移除原来的 `playSearchProgressSoundIfNeeded()` 方法

```java
// 在 handleSearchProgress 末尾添加：
public static void handleSearchProgress(Player player, int slotIndex) {
    // ... 现有逻辑不变 ...
    // 在方法末尾：
    SearchSoundSessionManager.updateSoundSession(player);
}

// 在 handleHotbarSearchProgress 末尾添加：
public static void handleHotbarSearchProgress(Player player, int hotbarSlotIndex) {
    // ... 现有逻辑不变 ...
    // 在方法末尾：
    SearchSoundSessionManager.updateSoundSession(player);
}

// 移除或注释掉以下方法：
// private static void playSearchProgressSoundIfNeeded(Player player) { ... }
// private static long lastSearchProgressSoundTick = 0;  // 不再需要
```

注意：`playCompletionEffect()` 和 `SoundHandler.playSearchCompletionSound()` 不受影响，保持不变。

### 3.8 修改：`Searchcarefully.java`

注册容器关闭事件，停止音效：

```java
// 在 Searchcarefully 构造函数中新增：
MinecraftForge.EVENT_BUS.addListener(this::onPlayerContainerClose);

// 新增方法：
@SubscribeEvent
public void onPlayerContainerClose(PlayerContainerEvent.Close event) {
    Player player = event.getEntity();
    if (!player.level().isClientSide()) {
        SearchSoundSessionManager.forceStopSound(player);
    }
}
```

需要新增 import：

```java
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import org.yanbwe.searchcarefully.manager.SearchSoundSessionManager;
```

### 3.9 修改：`client/ClientTickHandler.java`

当客户端打开容器界面时正常工作；当界面关闭时，向服务端发送一个信号来关闭音效。
实际上，容器关闭事件已经在 `Searchcarefully.java` 中通过 `PlayerContainerEvent.Close` 处理了，所以客户端不需要额外通知。

但需要确认：容器关闭时服务器会触发 `PlayerContainerEvent.Close` 吗？
- 是的！Forge 的 `PlayerContainerEvent.Close` 在玩家关闭任何容器时都会触发。
- 所以客户端侧不需要额外改动。

---

## 四、边界情况处理

| 场景 | 处理方式 |
|------|---------|
| 玩家关闭容器 | `PlayerContainerEvent.Close` → `forceStopSound()` |
| 玩家死亡 | `PlayerContainerEvent.Close` 会被触发（容器关闭）→ 音效停止 |
| 玩家退出游戏 | Forge 自动清理玩家数据，不强制处理但建议加 `PlayerEvent.PlayerLoggedOutEvent` |
| 玩家打开新容器 | 旧容器关闭事件会先触发 → 音效停止 → 新容器重新进入循环 |
| 快捷栏搜索（无容器） | `updateSoundSession` 同时检查 `containerMenu` 和快捷栏，均可触发 |
| 多人游戏 | 每个玩家独立 `playerSoundActive` 映射，互不干扰 |
| 配置热重载 | `SearchSoundSessionManager.resetAll()` 可用于 `/reload` 场景 |
| `/searchcarefully applysearch` 命令 | 命令执行的搜索会正常触发音效，因为 `SearchManager.handleSearchProgress` 仍被调用 |

---

## 五、向后兼容性

| 配置项 | 旧行为 | 新行为（循环模式） |
|--------|--------|-------------------|
| `enableSearchProgressSound` | 控制音效播放 | 不变——关闭则不启动循环 |
| `searchProgressSoundInterval` | 控制触发间隔 | **被忽略**（循环模式下不需要间隔）|

`searchProgressSoundInterval` 在循环模式下虽然被忽略，但保留在配置文件中以避免破坏现有玩家配置。

---

## 六、实施步骤（执行顺序）

```
Step 1: sounds.json   — 修改 stream: true
Step 2: SearchProgressLoopingSound.java  — 新增客户端循环音效类
Step 3: SearchSoundManager.java         — 新增客户端音效管理器
Step 4: StartLoopSoundPacket.java       — 新增网络包
Step 5: NetworkHandler.java             — 注册新包
Step 6: SearchSoundSessionManager.java  — 新增服务端会话管理器
Step 7: SearchManager.java              — 移除旧逻辑，集成新会话管理
Step 8: Searchcarefully.java            — 注册容器关闭事件
Step 9: Config.java（可选）             — 添加注释说明 interval 在循环模式下被忽略
Step 10: 验证                           — 编译测试 + 单人/多人场景验证
```

---

## 七、关键 API 参考

### Minecraft 原生类

| 类 | 用途 |
|----|------|
| `net.minecraft.client.resources.sounds.TickableSoundInstance` | 每 tick 可更新的音效实例接口 |
| `net.minecraft.client.resources.sounds.AbstractSoundInstance` | 音效实例基类 |
| `net.minecraft.client.sounds.SoundManager` | 客户端音效管理器（`play()` / `stop()`） |
| `net.minecraft.network.protocol.game.ClientboundStopSoundPacket` | 服务端→客户端：停止指定音效 |
| `net.minecraftforge.event.entity.player.PlayerContainerEvent.Close` | Forge 容器关闭事件 |

### 项目现有类（供参考）

| 类 | 用途 |
|----|------|
| `SearchCompletionSound.SEARCH_PROGRESS_SOUND_EVENT` | 搜索进度音效的 `SoundEvent` 实例 |
| `SearchCompletionSound.SEARCH_PROGRESS_SOUND_ID` | 搜索进度音效的 `ResourceLocation` |
| `NetworkHandler.INSTANCE` | Forge 网络通道实例 |
| `ItemStackHelper.hasRemainingSearchTime()` | 检查物品是否有搜索时间 |
| `Config.ENABLE_HOTBAR_SEARCH` | 快捷栏搜索开关 |

---

## 八、风险与注意事项

1. **音频文件质量**：用户的 `search_progress.ogg` 需要适合循环播放（结尾和开头无缝衔接），否则会有"咔哒"感。如果用户使用第三方音效包，质量参差不齐。

2. **音效跟随玩家**：循环音效绑定玩家位置，多人模式下每个玩家听到自己位置的音效。

3. **性能**：一个 `TickableSoundInstance` 每 tick 运行 `tick()` 方法，开销极小（只有位置更新和简单的条件检查）。

4. **配置兼容**：老玩家的 `searchProgressSoundInterval` 配置不会被删除，但循环模式下会被忽略，不会引发配置加载错误。

5. **测试范围**：
   - 单人模式搜索
   - 多人模式多个玩家同时搜索
   - 快速打开/关闭容器
   - 容器的物品在搜索中被其他玩家取走
   - 快捷栏搜索

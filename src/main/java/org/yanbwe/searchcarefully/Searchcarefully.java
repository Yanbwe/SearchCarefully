package org.yanbwe.searchcarefully;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import com.mojang.serialization.Codec;
import org.slf4j.Logger;
import org.yanbwe.raritycore.registry.RarityRegistry;
import org.yanbwe.searchcarefully.commands.ClearSearchTagsCommand;
import org.yanbwe.searchcarefully.loot.AddSearchTimeLootModifier;
import org.yanbwe.searchcarefully.network.NetworkHandler;
import org.yanbwe.searchcarefully.sounds.SearchCompletionSound;
import org.yanbwe.searchcarefully.util.SearchConstants;

// 此处的值应与META-INF/mods.toml文件中的条目匹配
@Mod(Searchcarefully.MODID)
public class Searchcarefully {

    // 在公共位置定义模组ID，供所有内容引用
    public static final String MODID = "searchcarefully";
    // 直接引用slf4j日志记录器
    private static final Logger LOGGER = LogUtils.getLogger();
    
    // 战利品函数注册
    // 音效注册
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    
    // 为每个稀有度注册音效
    public static final RegistryObject<SoundEvent>[] RARITY_COMPLETION_SOUNDS = new RegistryObject[8]; // 索引0未使用，1-7对应稀有度
    
    static {
        for (int i = 1; i <= 7; i++) {
            final int rarityIndex = i;
            RARITY_COMPLETION_SOUNDS[i] = SOUND_EVENTS.register("search_completion_rarity_" + i, 
                () -> SearchCompletionSound.SEARCH_COMPLETION_EVENTS[rarityIndex]);
        }
    }
    
    // 全局战利品修饰符注册
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);
    
    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ADD_SEARCH_TIME_LOOT_MODIFIER =
            GLOBAL_LOOT_MODIFIERS.register("add_search_time", () -> AddSearchTimeLootModifier.CODEC);

    public Searchcarefully() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 为模组加载注册commonSetup方法
        modEventBus.addListener(this::commonSetup);
        
        // 注册全局战利品修饰符
        GLOBAL_LOOT_MODIFIERS.register(modEventBus);
        
        // 注册音效
        SOUND_EVENTS.register(modEventBus);

        // 注册事件监听器以处理服务器和其他游戏事件
        MinecraftForge.EVENT_BUS.register(this);

        // 注册模组的ForgeConfigSpec，使Forge能够创建并加载配置文件
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // 初始化网络处理器
        NetworkHandler.registerMessages();
        
        LOGGER.info("SearchCarefully mod initialized - Global Loot Modifier registered");
        LOGGER.info("Modifier codec: {}", AddSearchTimeLootModifier.CODEC.toString());
    }

    // 你可以使用SubscribeEvent，让事件总线发现要调用的方法
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // 服务器启动时做一些事情
        LOGGER.info("HELLO from server starting");
    }
    
    // 注册自定义命令
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ClearSearchTagsCommand.register(event.getDispatcher());
        LOGGER.info("Registered SearchCarefully commands");
    }
    
    public static void handleSearchProgress(Player player, int slotIndex) {
        if (!Config.ENABLE_SEARCH_SYSTEM.get()) {
            return;
        }
        
        // 获取玩家正在交互的容器 - 直接检查玩家当前打开的容器
        if (player.containerMenu != null) {
            var slots = player.containerMenu.slots;
            if (slotIndex >= 0 && slotIndex < slots.size()) {
                var slot = slots.get(slotIndex);
                ItemStack stack = slot.getItem();
                
                if (stack.hasTag()) {
                    CompoundTag tag = stack.getTag();
                    if (tag.contains(SearchConstants.SEARCH_TIME_REMAINING)) {
                        int remainingTime = tag.getInt(SearchConstants.SEARCH_TIME_REMAINING);
                        
                        // 减少搜索时间，但遵守速度倍数和配置设置
                        double speed = Config.SEARCH_SPEED_MULTIPLIER.get();
                        // 使用速度乘数来决定每次减少多少，而不是总是减少1
                        int decrement = Math.max(1, (int) Math.ceil(speed)); // 至少减少1
                        
                        remainingTime = Math.max(0, remainingTime - decrement);
                        
                        tag.putInt(SearchConstants.SEARCH_TIME_REMAINING, remainingTime);
                        
                        // 用新的标签更新物品堆栈
                        slot.set(stack);
                        
                        // 如果搜索完成，播放音效并清理NBT标签
                        if (remainingTime <= 0) {
                            // 获取物品的稀有度
                            int rarity = RarityRegistry.getNormalizedRarity(stack.getItem());
                            
                            // 获取容器的物理位置以实现正确的3D空间音效
                            // 从容器的实际位置播放音效，而非玩家位置
                            double x = player.getX();
                            double y = player.getY();
                            double z = player.getZ();

                            // 物品搜索完成，根据稀有度播放音效
                            org.yanbwe.searchcarefully.sounds.SoundHandler.playSearchCompletionSound(
                                player.level(), x, y, z, rarity
                            );
                            
                            // 清除搜索时间NBT标签，使物品能够正常堆叠
                            tag.remove(SearchConstants.SEARCH_TIME_REMAINING);
                            
                            // 如果标签为空，完全移除标签以确保最佳兼容性
                            if (tag.isEmpty()) {
                                stack.setTag(null);
                            }
                            
                            // 用清理后的物品更新槽位
                            slot.set(stack);
                        }
                    }
                }
            }
        }
    }

    // 你可以使用EventBusSubscriber自动注册类中所有用@SubscribeEvent注解的静态方法
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // 一些客户端设置代码
            LOGGER.info("HELLO FROM CLIENT SETUP");
        }
    }
}
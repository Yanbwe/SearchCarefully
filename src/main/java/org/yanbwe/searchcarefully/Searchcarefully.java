package org.yanbwe.searchcarefully;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import org.yanbwe.searchcarefully.util.ItemStackHelper;
import org.yanbwe.searchcarefully.util.SearchConstants;
import org.yanbwe.searchcarefully.registry.ModItems;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;

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
    public static final RegistryObject<SoundEvent>[] RARITY_COMPLETION_SOUNDS = new RegistryObject[8]; // 索引 0 未使用，1-7 对应稀有度
        
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
        
    // 玩家属性注册
    public static final DeferredRegister<Attribute> ATTRIBUTES =
        DeferredRegister.create(ForgeRegistries.ATTRIBUTES, MODID);
        
    // 搜索速度属性：影响玩家搜索物品的效率
    public static final RegistryObject<Attribute> SEARCH_SPEED = 
        ATTRIBUTES.register("search_speed", () -> 
            new RangedAttribute("attribute.name.searchcarefully.search_speed", 
                               1.0D,   // 默认值
                               0.0D,   // 最小值
                               100.0D)  // 最大值
                               .setSyncable(true) // 启用网络同步
        );
    
    // 物品注册
    public static final DeferredRegister<Item> ITEMS = ModItems.ITEMS;
    
    // 药水效果注册
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
        DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);
        
    public static final RegistryObject<MobEffect> SEARCH_SPEED_BOOST = 
        MOB_EFFECTS.register("search_speed_boost", () -> new org.yanbwe.searchcarefully.effects.SearchSpeedBoostEffect());
    
    // 搜索速度降低效果
    public static final RegistryObject<MobEffect> SEARCH_SPEED_LESS = 
        MOB_EFFECTS.register("search_speed_less", () -> new org.yanbwe.searchcarefully.effects.SearchSpeedLessEffect());
    
    // 药水配方注册（Potion）
    public static final DeferredRegister<Potion> POTIONS =
        DeferredRegister.create(ForgeRegistries.POTIONS, MODID);
    
    // 搜索速度提升药水配方（等级 I-V）
    public static final RegistryObject<Potion> SEARCH_SPEED_POTION_1 = POTIONS.register("search_speed_boost_1",
        () -> new Potion(new MobEffectInstance(SEARCH_SPEED_BOOST.get(), 12000, 0)));
    
    public static final RegistryObject<Potion> SEARCH_SPEED_POTION_2 = POTIONS.register("search_speed_boost_2",
        () -> new Potion(new MobEffectInstance(SEARCH_SPEED_BOOST.get(), 12000, 1)));
    
    public static final RegistryObject<Potion> SEARCH_SPEED_POTION_3 = POTIONS.register("search_speed_boost_3",
        () -> new Potion(new MobEffectInstance(SEARCH_SPEED_BOOST.get(), 12000, 2)));
    
    public static final RegistryObject<Potion> SEARCH_SPEED_POTION_4 = POTIONS.register("search_speed_boost_4",
        () -> new Potion(new MobEffectInstance(SEARCH_SPEED_BOOST.get(), 12000, 3)));
    
    public static final RegistryObject<Potion> SEARCH_SPEED_POTION_5 = POTIONS.register("search_speed_boost_5",
        () -> new Potion(new MobEffectInstance(SEARCH_SPEED_BOOST.get(), 12000, 4)));
    
    // 搜索速度降低药水配方（等级 II）
    public static final RegistryObject<Potion> SEARCH_SPEED_LESS_POTION_2 = POTIONS.register("search_speed_less_2",
        () -> new Potion(new MobEffectInstance(SEARCH_SPEED_LESS.get(), 12000, 1)));
    
    // 不需要注册药水物品，直接使用原版的 minecraft:potion
    
    // 创造模式标签页注册（必须在药水物品之后定义）
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
        
    public static final RegistryObject<CreativeModeTab> SEARCHCAREFULLY_TAB = CREATIVE_MODE_TABS.register("searchcarefully_tab", () ->
        CreativeModeTab.builder()
            .title((Component.translatable("itemGroup.searchcarefully.searchcarefully_tab")))
            .icon(() -> new ItemStack(Items.COMPASS)) // 使用指南针作为图标
            .displayItems((parameters, output) -> {
                // 添加占位物品
                output.accept(new ItemStack(ModItems.SEARCH_PLACEHOLDER.get()));
                
                // 添加药水到标签页（使用原版 minecraft:potion + NBT）
                output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), SEARCH_SPEED_POTION_1.get()));
                output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), SEARCH_SPEED_POTION_2.get()));
                output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), SEARCH_SPEED_POTION_3.get()));
                output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), SEARCH_SPEED_POTION_4.get()));
                output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), SEARCH_SPEED_POTION_5.get()));
                output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), SEARCH_SPEED_LESS_POTION_2.get()));
            })
            .build()
    );

    public Searchcarefully() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
    
        // 为模组加载注册 commonSetup 方法
        modEventBus.addListener(this::commonSetup);
            
        // 注册物品
        ITEMS.register(modEventBus);
            
        // 注册属性
        ATTRIBUTES.register(modEventBus);
            
        // 注册创造模式标签页
        CREATIVE_MODE_TABS.register(modEventBus);
            
        // 注册药水效果
        MOB_EFFECTS.register(modEventBus);
            
        // 注册药水配方
        POTIONS.register(modEventBus);
            
        // 注册全局战利品修饰符
        GLOBAL_LOOT_MODIFIERS.register(modEventBus);
            
        // 注册音效
        SOUND_EVENTS.register(modEventBus);
    
        // 注册事件监听器以处理服务器和其他游戏事件
        MinecraftForge.EVENT_BUS.register(this);
    
        // 注册模组的 ForgeConfigSpec，使 Forge 能够创建并加载配置文件
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // 初始化网络处理器
        NetworkHandler.registerMessages();
        
        LOGGER.info("SearchCarefully mod initialized - Global Loot Modifier registered");
        LOGGER.info("Modifier codec: {}", AddSearchTimeLootModifier.CODEC.toString());
        LOGGER.info("Player search speed attribute registered");
        LOGGER.info("Search speed potions registered");
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
    
    /**
     * 获取玩家的搜索速度属性值
     * 
     * @param player 玩家实体
     * @return 搜索速度倍率（默认 1.0）
     */
    public static double getPlayerSearchSpeed(Player player) {
        AttributeInstance attr = player.getAttribute(SEARCH_SPEED.get());
        if (attr != null) {
            return attr.getValue();
        }
        return 1.0; // 默认值
    }
    
    /**
     * MOD 事件总线监听器 - 处理实体属性修改
     */
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        
        /**
         * 为所有生物实体添加搜索速度属性
         * 确保属性能正确应用到玩家和其他生物身上
         */
        @SubscribeEvent
        public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
            for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
                if (!event.has(entityType, SEARCH_SPEED.get())) {
                    event.add(entityType, SEARCH_SPEED.get());
                }
            }
            LOGGER.info("已为所有实体类型注册搜索速度属性");
        }
    }
    
    /**
     * 处理热键栏物品的搜索进度
     * 
     * @param player 玩家实体
     * @param hotbarSlotIndex 热键栏槽位索引（0-8）
     */
    public static void handleHotbarSearchProgress(Player player, int hotbarSlotIndex) {
        if (!Config.ENABLE_SEARCH_SYSTEM.get() || !Config.ENABLE_HOTBAR_SEARCH.get()) {
            return;
        }
        
        // 检查热键栏索引范围
        if (hotbarSlotIndex < 0 || hotbarSlotIndex > 8) {
            return;
        }
        
        // 获取玩家的物品栏
        var inventory = player.getInventory();
        ItemStack stack = inventory.getItem(hotbarSlotIndex);
        
        // 检查是否是占位物品
        if (org.yanbwe.searchcarefully.item.SearchPlaceholderItem.isPlaceholder(stack)) {
            handleHotbarPlaceholderSearch(player, hotbarSlotIndex, stack);
        }
        // 使用封装的工具方法检查和减少搜索时间（原有逻辑）
        else if (ItemStackHelper.hasRemainingSearchTime(stack)) {
            // 计算减少量并更新搜索时间
            double configSpeed = Config.SEARCH_SPEED_MULTIPLIER.get();
            double playerSearchSpeed = getPlayerSearchSpeed(player);
            
            // 实际减少量 = 基础值 × 配置倍率 × 玩家属性
            double baseDecrement = 1.0;
            double actualDecrement = baseDecrement * configSpeed * playerSearchSpeed;
            
            // 确保至少减少一个很小的值（避免除零或负数）
            actualDecrement = Math.max(0.1, actualDecrement);
            
            double remainingTime = ItemStackHelper.decrementSearchTime(stack, actualDecrement);
            
            // 如果搜索完成，播放音效并清理 NBT 标签
            if (remainingTime <= 0.0) {
                // 获取物品的稀有度
                int rarity = RarityRegistry.getNormalizedRarity(stack.getItem());
                
                // 使用玩家位置实现正确的 3D 空间音效
                double x = player.getX();
                double y = player.getY();
                double z = player.getZ();

                // 物品搜索完成，根据稀有度播放音效
                org.yanbwe.searchcarefully.sounds.SoundHandler.playSearchCompletionSound(
                    player.level(), x, y, z, rarity
                );
                
                // 使用封装的工具方法完成搜索
                ItemStackHelper.completeSearch(stack);
            }
        }
    }
    
    /**
     * 处理热键栏占位物品的搜索进度
     */
    private static void handleHotbarPlaceholderSearch(Player player, int hotbarSlotIndex, ItemStack placeholderStack) {
        if (!placeholderStack.hasTag() || !placeholderStack.getTag().contains("SearchTimeRemaining")) {
            return;
        }
        
        // 计算减少量并更新搜索时间
        double configSpeed = Config.SEARCH_SPEED_MULTIPLIER.get();
        double playerSearchSpeed = getPlayerSearchSpeed(player);
        
        // 实际减少量 = 基础值 × 配置倍率 × 玩家属性
        double baseDecrement = 1.0;
        double actualDecrement = baseDecrement * configSpeed * playerSearchSpeed;
        
        // 确保至少减少一个很小的值（避免除零或负数）
        actualDecrement = Math.max(0.1, actualDecrement);
        
        // 获取并减少剩余时间
        double currentTime = placeholderStack.getTag().getDouble("SearchTimeRemaining");
        double newTime = Math.max(0.0, currentTime - actualDecrement);
        placeholderStack.getTag().putDouble("SearchTimeRemaining", newTime);
        
        // 如果搜索完成，转换为原物品
        if (newTime <= 0.0) {
            // 获取原始物品
            ItemStack originalItem = org.yanbwe.searchcarefully.item.SearchPlaceholderItem.getOriginalItem(placeholderStack);
            
            if (!originalItem.isEmpty()) {
                // 从原始物品获取稀有度（而不是占位物品）
                int rarity = RarityRegistry.getNormalizedRarity(originalItem.getItem());
                
                // 使用玩家位置实现正确的 3D 空间音效
                double x = player.getX();
                double y = player.getY();
                double z = player.getZ();
                
                // 播放搜索完成音效
                org.yanbwe.searchcarefully.sounds.SoundHandler.playSearchCompletionSound(
                    player.level(), x, y, z, rarity
                );
                
                // 替换为原物品
                var inventory = player.getInventory();
                inventory.setItem(hotbarSlotIndex, originalItem);
            }
        }
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
                    
                // 检查是否是占位物品
                if (org.yanbwe.searchcarefully.item.SearchPlaceholderItem.isPlaceholder(stack)) {
                    handlePlaceholderSearch(player, slot, stack);
                }
                // 使用封装的工具方法检查和减少搜索时间（原有逻辑）
                else if (ItemStackHelper.hasRemainingSearchTime(stack)) {
                    // 计算减少量并更新搜索时间
                    double configSpeed = Config.SEARCH_SPEED_MULTIPLIER.get();
                    double playerSearchSpeed = getPlayerSearchSpeed(player);
                    
                    // 实际减少量 = 基础值 × 配置倍率 × 玩家属性
                    double baseDecrement = 1.0;
                    double actualDecrement = baseDecrement * configSpeed * playerSearchSpeed;
                    
                    // 确保至少减少一个很小的值（避免除零或负数）
                    actualDecrement = Math.max(0.1, actualDecrement);
                    
                    double remainingTime = ItemStackHelper.decrementSearchTime(stack, actualDecrement);
                    
                    // 更新槽位中的物品
                    slot.set(stack);
                        
                    // 如果搜索完成，播放音效并清理 NBT 标签
                    if (remainingTime <= 0.0) {
                        // 获取物品的稀有度
                        int rarity = RarityRegistry.getNormalizedRarity(stack.getItem());
                            
                        // 获取容器的物理位置以实现正确的 3D 空间音效
                        double x = player.getX();
                        double y = player.getY();
                        double z = player.getZ();
    
                        // 物品搜索完成，根据稀有度播放音效
                        org.yanbwe.searchcarefully.sounds.SoundHandler.playSearchCompletionSound(
                            player.level(), x, y, z, rarity
                        );
                            
                        // 使用封装的工具方法完成搜索
                        ItemStackHelper.completeSearch(stack);
                            
                        // 用清理后的物品更新槽位
                        slot.set(stack);
                    }
                }
            }
        }
    }
    
    /**
     * 处理占位物品的搜索进度
     */
    private static void handlePlaceholderSearch(Player player, net.minecraft.world.inventory.Slot slot, ItemStack placeholderStack) {
        if (!placeholderStack.hasTag() || !placeholderStack.getTag().contains("SearchTimeRemaining")) {
            return;
        }
        
        // 计算减少量并更新搜索时间
        double configSpeed = Config.SEARCH_SPEED_MULTIPLIER.get();
        double playerSearchSpeed = getPlayerSearchSpeed(player);
        
        // 实际减少量 = 基础值 × 配置倍率 × 玩家属性
        double baseDecrement = 1.0;
        double actualDecrement = baseDecrement * configSpeed * playerSearchSpeed;
        
        // 确保至少减少一个很小的值（避免除零或负数）
        actualDecrement = Math.max(0.1, actualDecrement);
        
        // 获取并减少剩余时间
        double currentTime = placeholderStack.getTag().getDouble("SearchTimeRemaining");
        double newTime = Math.max(0.0, currentTime - actualDecrement);
        placeholderStack.getTag().putDouble("SearchTimeRemaining", newTime);
        
        // 更新槽位中的物品
        slot.set(placeholderStack);
        
        // 如果搜索完成，转换为原物品
        if (newTime <= 0.0) {
            // 获取原始物品
            ItemStack originalItem = org.yanbwe.searchcarefully.item.SearchPlaceholderItem.getOriginalItem(placeholderStack);
            
            if (!originalItem.isEmpty()) {
                // 从原始物品获取稀有度（而不是占位物品）
                int rarity = RarityRegistry.getNormalizedRarity(originalItem.getItem());
                
                // 获取容器的物理位置以实现正确的 3D 空间音效
                double x = player.getX();
                double y = player.getY();
                double z = player.getZ();
                
                // 播放搜索完成音效
                org.yanbwe.searchcarefully.sounds.SoundHandler.playSearchCompletionSound(
                    player.level(), x, y, z, rarity
                );
                
                // 替换为原物品
                slot.set(originalItem);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }
}
package org.yanbwe.searchcarefully;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.GameRules;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;
import org.yanbwe.searchcarefully.commands.ApplySearchCommand;
import org.yanbwe.searchcarefully.commands.ClearSearchTagsCommand;
import org.yanbwe.searchcarefully.effects.SearchSpeedBoostEffect;
import org.yanbwe.searchcarefully.effects.SearchSpeedLessEffect;
import org.yanbwe.searchcarefully.loot.AddSearchTimeLootModifier;
import org.yanbwe.searchcarefully.network.NetworkHandler;
import org.yanbwe.searchcarefully.network.SearchProgressPacket;
import org.yanbwe.searchcarefully.registry.ModItems;
import org.yanbwe.searchcarefully.sounds.SearchCompletionSound;

// 此处的值应与META-INF/mods.toml文件中的条目匹配
@Mod(Searchcarefully.MODID)
public class Searchcarefully {

    // 在公共位置定义模组ID，供所有内容引用
    public static final String MODID = "searchcarefully";
    
    // 直接引用日志记录器
    public static final Logger LOGGER = LogUtils.getLogger();
    
    // 游戏规则
    public static final GameRules.Key<GameRules.BooleanValue> SEARCH_LOOT_MODIFIER_GAMERULE = 
        GameRules.register("searchcarefully:loot_modifier", GameRules.Category.DROPS, GameRules.BooleanValue.create(true));
    
    // 注册表
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MODID);
    
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);
    
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, MODID);
    
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, MODID);
    
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(BuiltInRegistries.POTION, MODID);
    
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, MODID);
    
    // 属性
    public static final DeferredHolder<Attribute, Attribute> SEARCH_SPEED = ATTRIBUTES.register("search_speed", 
            () -> new RangedAttribute("attribute.name.searchcarefully.search_speed", 1.0, 0.0, 10.0).setSyncable(true));
    
    // 状态效果
    public static final DeferredHolder<MobEffect, MobEffect> SEARCH_SPEED_BOOST = MOB_EFFECTS.register("search_speed_boost", 
            SearchSpeedBoostEffect::new);
    
    public static final DeferredHolder<MobEffect, MobEffect> SEARCH_SPEED_LESS = MOB_EFFECTS.register("search_speed_less", 
            SearchSpeedLessEffect::new);
    
    // 药水
    public static final DeferredHolder<Potion, Potion> SEARCH_SPEED_BOOST_1 = POTIONS.register("search_speed_boost_1", 
            () -> new Potion(new MobEffectInstance(SEARCH_SPEED_BOOST, 3600)));
    
    public static final DeferredHolder<Potion, Potion> SEARCH_SPEED_BOOST_2 = POTIONS.register("search_speed_boost_2", 
            () -> new Potion(new MobEffectInstance(SEARCH_SPEED_BOOST, 1800, 1)));
    
    public static final DeferredHolder<Potion, Potion> SEARCH_SPEED_BOOST_3 = POTIONS.register("search_speed_boost_3", 
            () -> new Potion(new MobEffectInstance(SEARCH_SPEED_BOOST, 900, 2)));
    
    public static final DeferredHolder<Potion, Potion> SEARCH_SPEED_BOOST_4 = POTIONS.register("search_speed_boost_4", 
            () -> new Potion(new MobEffectInstance(SEARCH_SPEED_BOOST, 450, 3)));
    
    public static final DeferredHolder<Potion, Potion> SEARCH_SPEED_BOOST_5 = POTIONS.register("search_speed_boost_5", 
            () -> new Potion(new MobEffectInstance(SEARCH_SPEED_BOOST, 225, 4)));
    
    public static final DeferredHolder<Potion, Potion> SEARCH_SPEED_LESS_2 = POTIONS.register("search_speed_less_2", 
            () -> new Potion(new MobEffectInstance(SEARCH_SPEED_LESS, 1800, 1)));
    
    // 创造模式物品栏标签
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SEARCHCAREFULLY_TAB = CREATIVE_MODE_TABS.register("searchcarefully_tab", 
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.searchcarefully.searchcarefully_tab"))
                    .icon(() -> new ItemStack(ModItems.SEARCH_PLACEHOLDER.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.SEARCH_PLACEHOLDER.get());
                    })
                    .build());
    
    // 全局战利品修饰符
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<AddSearchTimeLootModifier>> ADD_SEARCH_TIME_LOOT_MODIFIER = 
            GLOBAL_LOOT_MODIFIERS.register("add_search_time", AddSearchTimeLootModifier.CODEC);
    
    public Searchcarefully(IEventBus modEventBus, ModContainer modContainer) {
        // 注册所有注册表
        ModItems.ITEMS.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);
        GLOBAL_LOOT_MODIFIERS.register(modEventBus);
        ATTRIBUTES.register(modEventBus);
        MOB_EFFECTS.register(modEventBus);
        POTIONS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // 注册配置
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // 注册事件监听器
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onRegisterPayloadHandlers);
        modEventBus.addListener(this::onEntityAttributeModification);

        // 命令需要注册到NeoForge.EVENT_BUS
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }
    
    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("SearchCarefully mod initialized");
    }
    
    @SubscribeEvent
    public void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MODID);
        registrar.playToServer(
                SearchProgressPacket.TYPE,
                SearchProgressPacket.STREAM_CODEC,
                (payload, context) -> payload.handle(context)
        );
    }
    
    private void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, SEARCH_SPEED);
    }
    
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // 服务器启动时执行的操作
    }
    
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ApplySearchCommand.register(event.getDispatcher());
        ClearSearchTagsCommand.register(event.getDispatcher());
    }
}
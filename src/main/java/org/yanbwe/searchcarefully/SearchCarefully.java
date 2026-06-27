package org.yanbwe.searchcarefully;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;
import org.yanbwe.searchcarefully.effects.SearchSpeedBoostEffect;
import org.yanbwe.searchcarefully.effects.SearchSpeedLessEffect;
import org.yanbwe.searchcarefully.commands.ApplySearchCommand;
import org.yanbwe.searchcarefully.commands.ClearSearchTagsCommand;
import org.yanbwe.searchcarefully.loot.AddSearchTimeLootModifier;
import org.yanbwe.searchcarefully.manager.SearchSoundSessionManager;
import org.yanbwe.searchcarefully.registry.ModItems;

@Mod(SearchCarefully.MODID)
public class SearchCarefully {

    public static final String MODID = "searchcarefully";
    private static final Logger LOGGER = LogUtils.getLogger();

    // =====================================================================
    // DeferredRegister — Sound Events
    // =====================================================================

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, MODID);

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<SoundEvent, SoundEvent>[] RARITY_COMPLETION_SOUNDS = new DeferredHolder[8];

    static {
        for (int i = 1; i <= 7; i++) {
            final int index = i;
            RARITY_COMPLETION_SOUNDS[i] = SOUND_EVENTS.register(
                    "search_completion_rarity_" + index,
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(MODID, "search_completion_rarity_" + index)));
        }
    }

    // =====================================================================
    // DeferredRegister — Items (declared in registry/ModItems)
    // =====================================================================

    public static final DeferredRegister<Item> ITEMS = ModItems.ITEMS;

    // =====================================================================
    // DeferredRegister — Attributes
    // =====================================================================

    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, MODID);

    public static final DeferredHolder<Attribute, Attribute> SEARCH_SPEED =
            ATTRIBUTES.register("search_speed", () ->
                    new RangedAttribute("attribute.name.searchcarefully.search_speed",
                            1.0D, 0.0D, 100.0D)
                            .setSyncable(true));

    // =====================================================================
    // DeferredRegister — Mob Effects
    // =====================================================================

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, MODID);

    public static final DeferredHolder<MobEffect, MobEffect> SEARCH_SPEED_BOOST =
            MOB_EFFECTS.register("search_speed_boost", SearchSpeedBoostEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> SEARCH_SPEED_LESS =
            MOB_EFFECTS.register("search_speed_less", SearchSpeedLessEffect::new);

    // =====================================================================
    // DeferredRegister — Potions
    // =====================================================================

    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(Registries.POTION, MODID);

    private static final int POTION_DURATION = 12000;

    public static final DeferredHolder<Potion, Potion> SEARCH_SPEED_POTION_1 =
            POTIONS.register("search_speed_boost_1",
                    () -> new Potion(new MobEffectInstance(SEARCH_SPEED_BOOST, POTION_DURATION, 0)));

    public static final DeferredHolder<Potion, Potion> SEARCH_SPEED_POTION_2 =
            POTIONS.register("search_speed_boost_2",
                    () -> new Potion(new MobEffectInstance(SEARCH_SPEED_BOOST, POTION_DURATION, 1)));

    public static final DeferredHolder<Potion, Potion> SEARCH_SPEED_POTION_3 =
            POTIONS.register("search_speed_boost_3",
                    () -> new Potion(new MobEffectInstance(SEARCH_SPEED_BOOST, POTION_DURATION, 2)));

    public static final DeferredHolder<Potion, Potion> SEARCH_SPEED_POTION_4 =
            POTIONS.register("search_speed_boost_4",
                    () -> new Potion(new MobEffectInstance(SEARCH_SPEED_BOOST, POTION_DURATION, 3)));

    public static final DeferredHolder<Potion, Potion> SEARCH_SPEED_POTION_5 =
            POTIONS.register("search_speed_boost_5",
                    () -> new Potion(new MobEffectInstance(SEARCH_SPEED_BOOST, POTION_DURATION, 4)));

    public static final DeferredHolder<Potion, Potion> SEARCH_SPEED_LESS_POTION_2 =
            POTIONS.register("search_speed_less_2",
                    () -> new Potion(new MobEffectInstance(SEARCH_SPEED_LESS, POTION_DURATION, 1)));

    // =====================================================================
    // DeferredRegister — Creative Mode Tab
    // =====================================================================

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SEARCHCAREFULLY_TAB =
            CREATIVE_MODE_TABS.register("searchcarefully_tab", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.searchcarefully.searchcarefully_tab"))
                            .icon(() -> new ItemStack(Items.COMPASS))
                        .displayItems((parameters, output) -> {
                            output.accept(new ItemStack(ModItems.SEARCH_PLACEHOLDER.get()));
                            output.accept(PotionContents.createItemStack(Items.POTION, SEARCH_SPEED_POTION_1));
                            output.accept(PotionContents.createItemStack(Items.POTION, SEARCH_SPEED_POTION_2));
                            output.accept(PotionContents.createItemStack(Items.POTION, SEARCH_SPEED_POTION_3));
                            output.accept(PotionContents.createItemStack(Items.POTION, SEARCH_SPEED_POTION_4));
                            output.accept(PotionContents.createItemStack(Items.POTION, SEARCH_SPEED_POTION_5));
                            output.accept(PotionContents.createItemStack(Items.POTION, SEARCH_SPEED_LESS_POTION_2));
                        })
                            .build());

    // =====================================================================
    // Game Rules
    // =====================================================================

    public static final GameRules.Key<GameRules.BooleanValue> SEARCH_LOOT_MODIFIER_GAMERULE =
            GameRules.register("searchcarefully:loot_modifier", GameRules.Category.MISC,
                    GameRules.BooleanValue.create(true));

    // TODO: Phase 6 — GLOBAL_LOOT_MODIFIERS DeferredRegister + ADD_SEARCH_TIME_LOOT_MODIFIER
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<? extends IGlobalLootModifier>>
            ADD_SEARCH_TIME_LOOT_MODIFIER =
            GLOBAL_LOOT_MODIFIERS.register("add_search_time", () -> AddSearchTimeLootModifier.CODEC);

    // =====================================================================
    // Constructor
    // =====================================================================

    public SearchCarefully(IEventBus modEventBus, ModContainer modContainer) {
        // Register all DeferredRegisters to the mod event bus
        ITEMS.register(modEventBus);
        ATTRIBUTES.register(modEventBus);
        MOB_EFFECTS.register(modEventBus);
        POTIONS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);
        GLOBAL_LOOT_MODIFIERS.register(modEventBus);

        // Mod lifecycle
        modEventBus.addListener(this::commonSetup);

        // Game events (commands, container, player)
        NeoForge.EVENT_BUS.register(this);

        // Register config
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    // =====================================================================
    // Common Setup
    // =====================================================================

    private void commonSetup(final FMLCommonSetupEvent event) {
        // NetworkHandler auto-registers via @EventBusSubscriber — no explicit call needed
        LOGGER.info("SearchCarefully mod initialized");
        LOGGER.info("Player search speed attribute registered");
        LOGGER.info("Search speed potions registered");
    }

    // =====================================================================
    // Game Event Handlers (server + player)
    // =====================================================================

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ClearSearchTagsCommand.register(event.getDispatcher());
        ApplySearchCommand.register(event.getDispatcher());
        LOGGER.info("Registered SearchCarefully commands");
    }

    @SubscribeEvent
    public void onPlayerContainerClose(PlayerContainerEvent.Close event) {
        SearchSoundSessionManager.forceStopSound(event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        SearchSoundSessionManager.removePlayer(event.getEntity().getUUID());
    }

    // =====================================================================
    // Mod Bus Event Handlers (static inner class)
    // =====================================================================

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {

        @SubscribeEvent
        public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
            for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
                if (!event.has(entityType, SEARCH_SPEED.get())) {
                    event.add(entityType, SEARCH_SPEED.get());
                }
            }
            LOGGER.info("Registered search speed attribute for all entity types");
        }
    }
}

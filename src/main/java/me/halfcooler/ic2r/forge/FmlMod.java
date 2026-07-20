package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.event.EventHandler;
import me.halfcooler.ic2r.core.init.IC2RClientConfig;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.loot.Ic2rLootNbtProviderTypes;
import me.halfcooler.ic2r.core.ref.Ic2rArmorMaterials;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.ref.Ic2rFluids;
import me.halfcooler.ic2r.forge.ref.Ic2rSoundEventsForge;
import me.halfcooler.ic2r.integration.ae2.Ic2rAe2Plugin;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod("ic2r")
public final class FmlMod {

    private static final AtomicInteger loadState = new AtomicInteger();

    public static FmlMod instance;

    private final ModContainer modContainer;

    private List<Runnable> toRunAfterRegistryInit = new ArrayList<>();

    public FmlMod(IEventBus modEventBus, ModContainer modContainer) {
        instance = this;
        this.modContainer = modContainer;
        // W3.2: install platform SPI before common code may use PlatformServices
        ForgePlatformServices.install();
        // Force class-loading of *Blocks definition files so DeferredRegister entries
        // exist before BLOCKS DeferredRegister processes during RegisterEvent.
        Ic2rBlocks.init();
        // MOB_EFFECT RegisterEvent fires BEFORE BLOCK. Status effects must be queued
        // here (mod constructor), never inside onInitEarly / RegisterEvent.BLOCK.
        EnvProxyForge.queueCoreStatusEffects();
        Ic2rArmorMaterials.REGISTRY.register(modEventBus);
        EnvProxyForge.BLOCKS.register(modEventBus);
        modEventBus.register(this);
        EnvProxyForge.blockEntityRegistry.register(modEventBus);
        EnvProxyForge.creativeTabRegistry.register(modEventBus);
        EnvProxyForge.entityRegistry.register(modEventBus);
        EnvProxyForge.screenHandlerRegistry.register(modEventBus);
        EnvProxyForge.statusEffectRegistry.register(modEventBus);
        EnvProxyForge.foliagePlacerRegistry.register(modEventBus);
        EnvProxyForge.recipeTypeRegistry.register(modEventBus);
        EnvProxyForge.recipeSerializerRegistry.register(modEventBus);
        EnvFluidHandlerForge.fluidRegistry.register(modEventBus);
        EnvFluidHandlerForge.fluidTypeRegistry.register(modEventBus);
        Ic2rLootModifier.lootModifiersRegistry.register(modEventBus);
        // W1.7: SoundEvent category fully DeferredRegister + RegistryObject
        Ic2rSoundEventsForge.register(modEventBus);
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.register(new ClientModEventHandlerForge());
            modContainer.registerConfig(ModConfig.Type.CLIENT, IC2RClientConfig.SPEC, "ic2r/ic2r-client.toml");
            // Enable the Mods list "Config" button via NeoForge's built-in ConfigurationScreen.
            // Without IConfigScreenFactory the button stays disabled even though configs are registered.
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
        Ic2rFluids.init();
        // All IC2R configs live under config/ic2r/
        modContainer.registerConfig(ModConfig.Type.COMMON, IC2RConfig.SPEC, "ic2r/ic2r-common.toml");
        // UU matter costs: config/ic2r/ic2r-uu-matter.toml (loaded by IC2RUuMatterConfig, not ModConfigSpec)
        modEventBus.addListener(Ic2rCapabilities::register);
        modEventBus.addListener(this::registerPayloads);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playBidirectional(Ic2rRawPayload.TYPE, Ic2rRawPayload.STREAM_CODEC, ForgeNetworkHandler::handle);
    }

    @SubscribeEvent
    public void load(FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.register(new EventHandlerForge());
        NeoForge.EVENT_BUS.register(new Ic2rAe2Plugin.ForgeEventHandler());
        if (FMLEnvironment.dist.isClient()) {
            NeoForge.EVENT_BUS.register(new ClientEventHandlerForge());
        }
        if (!loadState.compareAndSet(1, 2)) {
            throw new IllegalStateException();
        }
        EventHandler.onInit();
    }

    @SubscribeEvent
    public void init(FMLLoadCompleteEvent event) {
        if (!loadState.compareAndSet(2, 3)) {
            throw new IllegalStateException();
        }
        Ic2rSoundEventsForge.wireCoreFields();
        EventHandler.onInitLate();
    }

    @SubscribeEvent
    public void registerFluidTypes(RegisterEvent event) {
        if (event.getRegistryKey() == NeoForgeRegistries.Keys.FLUID_TYPES) {
            EnvFluidHandlerForge.registerPendingFluidTypes();
        }
    }

    @SubscribeEvent
    public void registerLootNbtProviders(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.LOOT_NBT_PROVIDER_TYPE) {
            Ic2rLootNbtProviderTypes.init();
        }
    }

    @SubscribeEvent
    public void registerFluids(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.FLUID) {
            EnvFluidHandlerForge.registerPendingFluids();
        }
    }

    @SubscribeEvent
    public void registerItems(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.ITEM) {
            EnvProxyForge.registerPendingItems();
        }
    }

    @SubscribeEvent
    public void registerBlocks(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.BLOCK) {
            if (!loadState.compareAndSet(0, 1)) {
                throw new IllegalStateException();
            }
            EventHandler.onInitEarly();
        }
    }

    /**
     * After every registry finishes mod registration, alias pre-20.1.40 {@code ic2:*} ids
     * to current {@code ic2r:*} entries (same path). Replaces removed {@code MissingMappingsEvent}.
     * <p>
     * Priority {@link EventPriority#LOWEST} so DeferredRegister / pending item and fluid hooks
     * have already populated the registry for this event.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void registerLegacyRegistryAliases(RegisterEvent event) {
        LegacyRegistryRemap.apply(event.getRegistry());
    }

    @SubscribeEvent
    public void registerGameEvents(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.SOUND_EVENT) {
            EventHandler.onInitGameEvents();
        }
    }

    @SubscribeEvent
    public void registerLate(RegisterEvent event) {
        // Run post-registry hooks once registries are largely populated (fluid types is late enough).
        if (event.getRegistryKey() == NeoForgeRegistries.Keys.FLUID_TYPES) {
            if (this.toRunAfterRegistryInit != null) {
                for (Runnable runnable : this.toRunAfterRegistryInit) {
                    runnable.run();
                }
                this.toRunAfterRegistryInit = null;
            }
        }
    }

    void runAfterRegistryInit(Runnable runnable) {
        if (loadState.get() > 1) {
            runnable.run();
        } else {
            this.toRunAfterRegistryInit.add(runnable);
        }
    }

    @SubscribeEvent
    public void registerFeatures(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.CONFIGURED_FEATURE) {
            for (EnvProxyForge.ConfiguredFeatureRegistration<?, ?> reg : EnvProxyForge.configuredFeatureRegistrations) {
                ConfiguredFeature<?, ?> cf = createConfiguredFeature(reg);
                event.register(Registries.CONFIGURED_FEATURE, reg.id(), () -> cf);
            }
        }
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> ConfiguredFeature<FC, ?> createConfiguredFeature(EnvProxyForge.ConfiguredFeatureRegistration<FC, F> reg) {
        return new ConfiguredFeature<>(reg.feature(), reg.config());
    }
}

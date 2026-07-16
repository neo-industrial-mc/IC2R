package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.event.EventHandler;
import me.halfcooler.ic2r.core.init.IC2RClientConfig;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.network.NetworkManager;
import me.halfcooler.ic2r.core.loot.Ic2rLootNbtProviderTypes;
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
import net.neoforged.fml.IExtensionPoint.DisplayTest;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.fml.ModContainer;

@Mod("ic2r")
public final class FmlMod {

    private static final AtomicInteger loadState = new AtomicInteger();

    public static FmlMod instance;

    private final FMLJavaModLoadingContext ctx;

    private List<Runnable> toRunAfterRegistryInit = new ArrayList<>();

    public FmlMod(FMLJavaModLoadingContext ctx, ModContainer modContainer) {
        instance = this;
        this.ctx = ctx;
        // W3.2: install platform SPI before common code may use PlatformServices
        ForgePlatformServices.install();
        IEventBus modEventBus = this.ctx.getModEventBus();
        // Force class-loading of *Blocks definition files so RegistryObject entries
        // exist before BLOCKS DeferredRegister processes during RegisterEvent.
        Ic2rBlocks.init();
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
            this.ctx.registerConfig(ModConfig.Type.CLIENT, IC2RClientConfig.SPEC, "ic2r/ic2r-client.toml");
        }
        Ic2rFluids.init();
        // All IC2R configs live under config/ic2r/
        this.ctx.registerConfig(ModConfig.Type.COMMON, IC2RConfig.SPEC, "ic2r/ic2r-common.toml");
        // UU matter costs: config/ic2r/ic2r-uu-matter.toml (loaded by IC2RUuMatterConfig, not ModConfigSpec)
        modEventBus.addListener(NanoSaberCapabilities::register);
    }

    @SubscribeEvent
    public void load(FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.register(new EventHandlerForge());
        NeoForge.EVENT_BUS.register(new Ic2rAe2Plugin.ForgeEventHandler());
        if (FMLEnvironment.dist.isClient()) {
            NeoForge.EVENT_BUS.register(new ClientEventHandlerForge());
        }
        NetworkRegistry.newEventChannel(NetworkManager.channelId, () -> "0", v -> true, v -> true).registerObject(new ForgeNetworkHandler());
        this.ctx.registerExtensionPoint(DisplayTest.class, () -> new DisplayTest(() -> "OH, NO!", (in, net) -> true));
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

    @SubscribeEvent
    public void registerGameEvents(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.SOUND_EVENT) {
            EventHandler.onInitGameEvents();
        }
    }

    @SubscribeEvent
    public void registerLate(RegisterEvent event) {
        if (event.getRegistryKey() == ForgeRegistries.Keys.HOLDER_SET_TYPES) {
            for (Runnable runnable : this.toRunAfterRegistryInit) {
                runnable.run();
            }
            this.toRunAfterRegistryInit = null;
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

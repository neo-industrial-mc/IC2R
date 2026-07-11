package ic2.forge;

import ic2.core.event.EventHandler;
import ic2.core.init.IC2ClientConfig;
import ic2.core.init.IC2Config;
import ic2.core.loot.Ic2LootNbtProviderTypes;
import ic2.core.ref.Ic2Fluids;
import ic2.integration.ae2.Ic2Ae2Plugin;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod("ic2")
public final class FmlMod {

  private static final AtomicInteger loadState = new AtomicInteger();

  public static FmlMod instance;

  private final ModContainer modContainer;

  private List<Runnable> toRunAfterRegistryInit = new ArrayList<>();

  public FmlMod(IEventBus modEventBus, ModContainer modContainer) {
    instance = this;
    this.modContainer = modContainer;
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
    Ic2LootModifier.lootModifiersRegistry.register(modEventBus);
    if (FMLEnvironment.dist.isClient()) {
      modEventBus.register(new ClientModEventHandlerForge());
      this.modContainer.registerConfig(ModConfig.Type.CLIENT, IC2ClientConfig.SPEC);
    }
    Ic2Fluids.init();
    this.modContainer.registerConfig(ModConfig.Type.COMMON, IC2Config.SPEC);
    modEventBus.addListener(Ic2Capabilities::register);
    modEventBus.addListener(Ic2Network::register);
  }

  @SubscribeEvent
  public void load(FMLCommonSetupEvent event) {
    NeoForge.EVENT_BUS.register(new EventHandlerForge());
    NeoForge.EVENT_BUS.register(new Ic2Ae2Plugin.ForgeEventHandler());
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
    if (this.toRunAfterRegistryInit != null) {
      for (Runnable runnable : this.toRunAfterRegistryInit) {
        runnable.run();
      }
      this.toRunAfterRegistryInit = null;
    }
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
      Ic2LootNbtProviderTypes.init();
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
  public void registerStatusEffects(RegisterEvent event) {
    // MOB_EFFECT registers before BLOCK, so potions must be created here rather than in
    // onInitEarly.
    if (event.getRegistryKey() == Registries.MOB_EFFECT) {
      ic2.core.init.BlocksItems.initPotions();
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
      for (EnvProxyForge.ConfiguredFeatureRegistration<?, ?> reg :
          EnvProxyForge.configuredFeatureRegistrations) {
        ConfiguredFeature<?, ?> cf = createConfiguredFeature(reg);
        event.register(Registries.CONFIGURED_FEATURE, reg.id(), () -> cf);
      }
    }
  }

  private static <FC extends FeatureConfiguration, F extends Feature<FC>>
      ConfiguredFeature<FC, ?> createConfiguredFeature(
          EnvProxyForge.ConfiguredFeatureRegistration<FC, F> reg) {
    return new ConfiguredFeature<>(reg.feature(), reg.config());
  }
}

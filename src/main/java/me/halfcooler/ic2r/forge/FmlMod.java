package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.event.EventHandler;
import me.halfcooler.ic2r.core.init.IC2RClientConfig;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.init.IC2RUuScanConfig;
import me.halfcooler.ic2r.core.network.NetworkManager;
import me.halfcooler.ic2r.core.loot.Ic2rLootNbtProviderTypes;
import me.halfcooler.ic2r.core.ref.Ic2rFluids;
import me.halfcooler.ic2r.core.ref.Ic2rSoundEvents;
import me.halfcooler.ic2r.integration.ae2.Ic2rAe2Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint.DisplayTest;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod("ic2r")
public final class FmlMod
{
	private static final AtomicInteger loadState = new AtomicInteger();
	public static FmlMod instance;
	private final FMLJavaModLoadingContext ctx;
	private List<Runnable> toRunAfterRegistryInit = new ArrayList<>();

	public FmlMod(FMLJavaModLoadingContext ctx)
	{
		instance = this;
		this.ctx = ctx;
		// W3.2: install platform SPI before common code may use PlatformServices
		ForgePlatformServices.install();
		IEventBus modEventBus = this.ctx.getModEventBus();
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
		Ic2rSoundEvents.register(modEventBus);
		if (FMLEnvironment.dist.isClient())
		{
			modEventBus.register(new ClientModEventHandlerForge());
			this.ctx.registerConfig(ModConfig.Type.CLIENT, IC2RClientConfig.SPEC);
		}

		Ic2rFluids.init();
		this.ctx.registerConfig(ModConfig.Type.COMMON, IC2RConfig.SPEC);
		this.ctx.registerConfig(ModConfig.Type.COMMON, IC2RUuScanConfig.SPEC, "ic2r-uu-scan-values.toml");
		modEventBus.addListener(NanoSaberCapabilities::register);
	}
	
	@SubscribeEvent
	public void load(FMLCommonSetupEvent event)
	{
		MinecraftForge.EVENT_BUS.register(new EventHandlerForge());
		MinecraftForge.EVENT_BUS.register(new Ic2rAe2Plugin.ForgeEventHandler());
		if (FMLEnvironment.dist.isClient())
		{
			MinecraftForge.EVENT_BUS.register(new ClientEventHandlerForge());
		}

		NetworkRegistry.newEventChannel(NetworkManager.channelId, () -> "0", v -> true, v -> true).registerObject(new ForgeNetworkHandler());
		this.ctx
			.registerExtensionPoint(
				DisplayTest.class,
				() -> new DisplayTest(
					() -> "OH, NO!",
					(in, net) -> true
				)
			);
		if (!loadState.compareAndSet(1, 2))
		{
			throw new IllegalStateException();
		}

		EventHandler.onInit();
	}

	@SubscribeEvent
	public void init(FMLLoadCompleteEvent event)
	{
		if (!loadState.compareAndSet(2, 3))
		{
			throw new IllegalStateException();
		}

		EventHandler.onInitLate();
	}

	@SubscribeEvent
	public void registerFluidTypes(RegisterEvent event)
	{
		if (event.getRegistryKey() == ForgeRegistries.Keys.FLUID_TYPES)
		{
			EnvFluidHandlerForge.registerPendingFluidTypes();
		}
	}

	@SubscribeEvent
	public void registerLootNbtProviders(RegisterEvent event)
	{
		if (event.getRegistryKey() == Registries.LOOT_NBT_PROVIDER_TYPE)
		{
			Ic2rLootNbtProviderTypes.init();
		}
	}

	@SubscribeEvent
	public void registerFluids(RegisterEvent event)
	{
		if (event.getRegistryKey() == Registries.FLUID)
		{
			EnvFluidHandlerForge.registerPendingFluids();
		}
	}

	@SubscribeEvent
	public void registerItems(RegisterEvent event)
	{
		if (event.getRegistryKey() == Registries.ITEM)
		{
			EnvProxyForge.registerPendingItems();
		}
	}

	@SubscribeEvent
	public void registerBlocks(RegisterEvent event)
	{
		if (event.getRegistryKey() == Registries.BLOCK)
		{
			if (!loadState.compareAndSet(0, 1))
			{
				throw new IllegalStateException();
			}

			EventHandler.onInitEarly();
		}
	}

	@SubscribeEvent
	public void registerGameEvents(RegisterEvent event)
	{
		if (event.getRegistryKey() == Registries.SOUND_EVENT)
		{
			EventHandler.onInitGameEvents();
		}
	}

	@SubscribeEvent
	public void registerLate(RegisterEvent event)
	{
		if (event.getRegistryKey() == ForgeRegistries.Keys.HOLDER_SET_TYPES)
		{
			for (Runnable runnable : this.toRunAfterRegistryInit)
			{
				runnable.run();
			}

			this.toRunAfterRegistryInit = null;
		}
	}

	void runAfterRegistryInit(Runnable runnable)
	{
		if (loadState.get() > 1)
		{
			runnable.run();
		} else
		{
			this.toRunAfterRegistryInit.add(runnable);
		}
	}

	@SubscribeEvent
	public void registerFeatures(RegisterEvent event)
	{
		if (event.getRegistryKey() == Registries.CONFIGURED_FEATURE)
		{
			for (EnvProxyForge.ConfiguredFeatureRegistration<?, ?> reg : EnvProxyForge.configuredFeatureRegistrations)
			{
				ConfiguredFeature<?, ?> cf = createConfiguredFeature(reg);
				event.register(Registries.CONFIGURED_FEATURE, reg.id(), () -> cf);
			}
		}
	}

	private static <FC extends FeatureConfiguration, F extends Feature<FC>> ConfiguredFeature<FC, ?> createConfiguredFeature(EnvProxyForge.ConfiguredFeatureRegistration<FC, F> reg)
	{
		return new ConfiguredFeature<>(reg.feature(), reg.config());
	}
}

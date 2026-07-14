package me.halfcooler.ic2r.core.proxy;

import me.halfcooler.ic2r.api.tile.IRotorProvider;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.block.generator.gui.GuiSolarGenerator;
import me.halfcooler.ic2r.core.block.heatgenerator.gui.GuiElectricHeatGenerator;
import me.halfcooler.ic2r.core.block.heatgenerator.gui.GuiFluidHeatGenerator;
import me.halfcooler.ic2r.core.block.heatgenerator.gui.GuiRTHeatGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.gui.GuiElectricKineticGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.gui.GuiSteamKineticGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.gui.GuiStirlingKineticGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.gui.GuiWaterKineticGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.gui.GuiWindKineticGenerator;
import me.halfcooler.ic2r.core.block.machine.gui.GuiAdvMiner;
import me.halfcooler.ic2r.core.block.machine.gui.GuiBatchCrafter;
import me.halfcooler.ic2r.core.block.machine.gui.GuiCanner;
import me.halfcooler.ic2r.core.block.machine.gui.GuiChunkLoader;
import me.halfcooler.ic2r.core.block.machine.gui.GuiCondenser;
import me.halfcooler.ic2r.core.block.machine.gui.GuiCropHarvester;
import me.halfcooler.ic2r.core.block.machine.gui.GuiCropmatron;
import me.halfcooler.ic2r.core.block.machine.gui.GuiElectrolyzer;
import me.halfcooler.ic2r.core.block.machine.gui.GuiFermenter;
import me.halfcooler.ic2r.core.block.machine.gui.GuiFluidBottler;
import me.halfcooler.ic2r.core.block.machine.gui.GuiFluidDistributor;
import me.halfcooler.ic2r.core.block.machine.gui.GuiFluidRegulator;
import me.halfcooler.ic2r.core.block.machine.gui.GuiIndustrialWorkbench;
import me.halfcooler.ic2r.core.block.machine.gui.GuiItemBuffer;
import me.halfcooler.ic2r.core.block.machine.gui.GuiLiquidHeatExchanger;
import me.halfcooler.ic2r.core.block.machine.gui.GuiMagnetizer;
import me.halfcooler.ic2r.core.block.machine.gui.GuiMatter;
import me.halfcooler.ic2r.core.block.machine.gui.GuiMetalFormer;
import me.halfcooler.ic2r.core.block.machine.gui.GuiMiner;
import me.halfcooler.ic2r.core.block.machine.gui.GuiPatternStorage;
import me.halfcooler.ic2r.core.block.machine.gui.GuiReplicator;
import me.halfcooler.ic2r.core.block.machine.gui.GuiScanner;
import me.halfcooler.ic2r.core.block.machine.gui.GuiSolarDestiller;
import me.halfcooler.ic2r.core.block.machine.gui.GuiSortingMachine;
import me.halfcooler.ic2r.core.block.machine.gui.GuiSteamGenerator;
import me.halfcooler.ic2r.core.block.machine.gui.GuiWeightedFluidDistributor;
import me.halfcooler.ic2r.core.block.machine.gui.GuiWeightedItemDistributor;
import me.halfcooler.ic2r.core.block.personal.GuiEnergyOMatClosed;
import me.halfcooler.ic2r.core.block.personal.GuiEnergyOMatOpen;
import me.halfcooler.ic2r.core.block.personal.GuiTradeOMatClosed;
import me.halfcooler.ic2r.core.block.personal.GuiTradeOMatOpen;
import me.halfcooler.ic2r.core.block.reactor.gui.GuiNuclearReactor;
import me.halfcooler.ic2r.core.block.renderer.KineticGeneratorRenderer;
import me.halfcooler.ic2r.core.block.storage.box.GuiStorageBox;
import me.halfcooler.ic2r.core.block.wiring.GuiChargePadBlock;
import me.halfcooler.ic2r.core.block.wiring.GuiElectricBlock;
import me.halfcooler.ic2r.core.block.wiring.GuiTransformer;
import me.halfcooler.ic2r.core.entity.render.BoatEntityRenderer;
import me.halfcooler.ic2r.core.entity.render.ExplosiveBlockRenderer;
import me.halfcooler.ic2r.core.entity.render.LaserBulletEntityRenderer;
import me.halfcooler.ic2r.core.gui.code.CodeGuiSampleScreen;
import me.halfcooler.ic2r.core.gui.dynamic.DynamicGui;
import me.halfcooler.ic2r.core.item.tool.GuiContainmentbox;
import me.halfcooler.ic2r.core.item.tool.GuiMiningFilter;
import me.halfcooler.ic2r.core.item.tool.GuiToolMeter;
import me.halfcooler.ic2r.core.item.tool.GuiToolScanner;
import me.halfcooler.ic2r.core.item.tool.GuiCropAnalyzer;
import me.halfcooler.ic2r.core.item.tool.GuiToolbox;
import me.halfcooler.ic2r.core.item.upgrade.AdvancedUpgradeScreenFactory;
import me.halfcooler.ic2r.core.item.upgrade.HandHeldOre;
import me.halfcooler.ic2r.core.item.upgrade.HandHeldValueConfig;
import me.halfcooler.ic2r.core.fluid.FluidHandler;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidItem;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.ref.Ic2rEntities;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.sound.SoundManager;
import me.halfcooler.ic2r.core.sound.SoundManagerClient;
import me.halfcooler.ic2r.core.util.Keyboard;
import me.halfcooler.ic2r.core.util.KeyboardClient;
import me.halfcooler.ic2r.core.util.Util;

import java.io.File;
import java.util.Objects;

import me.halfcooler.ic2r.forge.ClientEnvProxyForge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class SideProxyClient implements SideProxy
{
	public static final ClientEnvProxy envProxy = new ClientEnvProxyForge();
	public static final Minecraft mc = Minecraft.getInstance();
	public static final Keyboard keyboard = new KeyboardClient();
	private static final SoundManager soundManager = new SoundManagerClient();

	@Override
	public void preInit()
	{
		envProxy.registerScreen(Ic2rScreenHandlers.DYNAMIC_BE, DynamicGui::create);
		envProxy.registerScreen(Ic2rScreenHandlers.DYNAMIC_ITEM, DynamicGui::create);
		envProxy.registerScreen(Ic2rScreenHandlers.ELECTRIC_HEAT_GENERATOR, GuiElectricHeatGenerator::new);
		envProxy.registerScreen(Ic2rScreenHandlers.FLUID_HEAT_GENERATOR, GuiFluidHeatGenerator::new);
		envProxy.registerScreen(Ic2rScreenHandlers.RT_HEAT_GENERATOR, GuiRTHeatGenerator::new);
		envProxy.registerScreen(Ic2rScreenHandlers.ELECTRIC_KINETIC_GENERATOR, GuiElectricKineticGenerator::new);
		envProxy.registerScreen(Ic2rScreenHandlers.STEAM_KINETIC_GENERATOR, GuiSteamKineticGenerator::new);
		envProxy.registerScreen(Ic2rScreenHandlers.STIRLING_KINETIC_GENERATOR, GuiStirlingKineticGenerator::new);
		envProxy.registerScreen(Ic2rScreenHandlers.WATER_KINETIC_GENERATOR, GuiWaterKineticGenerator::new);
		envProxy.registerScreen(Ic2rScreenHandlers.WIND_KINETIC_GENERATOR, GuiWindKineticGenerator::new);
		envProxy.registerScreen(Ic2rScreenHandlers.NUCLEAR_REACTOR, GuiNuclearReactor::new);
		envProxy.registerScreen(Ic2rScreenHandlers.CONDENSER, GuiCondenser::new);
		envProxy.registerScreen(Ic2rScreenHandlers.FLUID_BOTTLER, GuiFluidBottler::new);
		envProxy.registerScreen(Ic2rScreenHandlers.FLUID_DISTRIBUTOR, GuiFluidDistributor::new);
		envProxy.registerScreen(Ic2rScreenHandlers.FLUID_REGULATOR, GuiFluidRegulator::new);
		envProxy.registerScreen(Ic2rScreenHandlers.LIQUID_HEAT_EXCHANGER, GuiLiquidHeatExchanger::new);
		envProxy.registerScreen(Ic2rScreenHandlers.SOLAR_DISTILLER, GuiSolarDestiller::new);
		envProxy.registerScreen(Ic2rScreenHandlers.STEAM_GENERATOR, GuiSteamGenerator::new);
		envProxy.registerScreen(Ic2rScreenHandlers.ITEM_BUFFER, GuiItemBuffer::new);
		envProxy.registerScreen(Ic2rScreenHandlers.MAGNETIZER, GuiMagnetizer::new);
		envProxy.registerScreen(Ic2rScreenHandlers.SORTING_MACHINE, GuiSortingMachine::new);
		envProxy.registerScreen(Ic2rScreenHandlers.CANNER, GuiCanner::new);
		envProxy.registerScreen(Ic2rScreenHandlers.FERMENTER, GuiFermenter::new);
		envProxy.registerScreen(Ic2rScreenHandlers.METAL_FORMER, GuiMetalFormer::new);
		envProxy.registerScreen(Ic2rScreenHandlers.ADVANCED_MINER, GuiAdvMiner::new);
		envProxy.registerScreen(Ic2rScreenHandlers.CROP_HARVESTER, GuiCropHarvester::new);
		envProxy.registerScreen(Ic2rScreenHandlers.CROPMATRON, GuiCropmatron::new);
		envProxy.registerScreen(Ic2rScreenHandlers.MINER, GuiMiner::new);
		envProxy.registerScreen(Ic2rScreenHandlers.MATTER_GENERATOR, GuiMatter::new);
		envProxy.registerScreen(Ic2rScreenHandlers.PATTERN_STORAGE, GuiPatternStorage::new);
		envProxy.registerScreen(Ic2rScreenHandlers.REPLICATOR, GuiReplicator::new);
		envProxy.registerScreen(Ic2rScreenHandlers.UU_SCANNER, GuiScanner::new);
		envProxy.registerScreen(Ic2rScreenHandlers.ENERGY_O_MAT_CLOSED, GuiEnergyOMatClosed::new);
		envProxy.registerScreen(Ic2rScreenHandlers.ENERGY_O_MAT_OPEN, GuiEnergyOMatOpen::new);
		envProxy.registerScreen(Ic2rScreenHandlers.TRADE_O_MAT_CLOSED, GuiTradeOMatClosed::new);
		envProxy.registerScreen(Ic2rScreenHandlers.TRADE_O_MAT_OPEN, GuiTradeOMatOpen::new);
		envProxy.registerScreen(Ic2rScreenHandlers.CHARGEPAD, GuiChargePadBlock::new);
		envProxy.registerScreen(Ic2rScreenHandlers.ENERGY_STORAGE, GuiElectricBlock::new);
		envProxy.registerScreen(Ic2rScreenHandlers.ELECTROLYZER, GuiElectrolyzer::new);
		envProxy.registerScreen(Ic2rScreenHandlers.TRANSFORMER, GuiTransformer::new);
		envProxy.registerScreen(Ic2rScreenHandlers.CHUNK_LOADER, GuiChunkLoader::new);
		envProxy.registerScreen(Ic2rScreenHandlers.WEIGHTED_FLUID_DISTRIBUTOR, GuiWeightedFluidDistributor::new);
		envProxy.registerScreen(Ic2rScreenHandlers.WEIGHTED_ITEM_DISTRIBUTOR, GuiWeightedItemDistributor::new);
		envProxy.registerScreen(Ic2rScreenHandlers.INDUSTRIAL_WORKBENCH, GuiIndustrialWorkbench::new);
		envProxy.registerScreen(Ic2rScreenHandlers.BATCH_CRAFTER, GuiBatchCrafter::new);
		envProxy.registerScreen(Ic2rScreenHandlers.STORAGE_BOX, GuiStorageBox::new);
		envProxy.registerScreen(Ic2rScreenHandlers.ADVANCED_UPGRADE, new AdvancedUpgradeScreenFactory());
		envProxy.registerScreen(Ic2rScreenHandlers.ADVANCED_UPGRADE_EDIT_ORE, HandHeldOre.GuiEditOre::new);
		envProxy.registerScreen(Ic2rScreenHandlers.ADVANCED_UPGRADE_VALUE_CONFIG, HandHeldValueConfig.GuiValueConfig::new);
		envProxy.registerScreen(Ic2rScreenHandlers.SCANNER, GuiToolScanner::new);
		envProxy.registerScreen(Ic2rScreenHandlers.MINING_FILTER, GuiMiningFilter::new);
		envProxy.registerScreen(Ic2rScreenHandlers.TOOL_BOX, GuiToolbox::new);
		envProxy.registerScreen(Ic2rScreenHandlers.CONTAINMENT_BOX, GuiContainmentbox::new);
		envProxy.registerScreen(Ic2rScreenHandlers.METER, GuiToolMeter::new);
		envProxy.registerScreen(Ic2rScreenHandlers.CROP_ANALYZER, GuiCropAnalyzer::new);
		envProxy.registerScreen(Ic2rScreenHandlers.SOLAR_GENERATOR, GuiSolarGenerator::new);
		// W2.4 sample: pure-code Menu/Screen (no guidef); not opened from a block by default
		envProxy.registerScreen(Ic2rScreenHandlers.CODE_GUI_SAMPLE, CodeGuiSampleScreen::new);
		envProxy.registerColorProvider((state, world, post, tintIndex) -> 6723908, Ic2rBlocks.RUBBER_LEAVES);
		envProxy.registerColorProvider((var1, var2) -> 6723908, Ic2rItems.RUBBER_LEAVES);
		envProxy.registerColorProvider(SideProxyClient::getFluidCellTintColor,
			Ic2rItems.FACADE_CELL,
			Ic2rItems.CREOSOTE_CELL,
			Ic2rItems.HEAVY_WATER_CELL,
			Ic2rItems.HOT_WATER_CELL,
			Ic2rItems.HYDROGEN_CELL,
			Ic2rItems.OXYGEN_CELL
		);
		envProxy.registerBlockLayer(RenderType.cutout(), Ic2rBlocks.DYNAMITE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.FOAM);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.REINFORCED_GLASS);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.REINFORCED_DOOR);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.COPPER_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.INSULATED_COPPER_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.TIN_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.INSULATED_TIN_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.IRON_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.INSULATED_IRON_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.DOUBLE_INSULATED_IRON_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.TRIPLE_INSULATED_IRON_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.GOLD_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.INSULATED_GOLD_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.DOUBLE_INSULATED_GOLD_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.GLASS_FIBRE_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.DETECTOR_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.SPLITTER_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.CROP_STICK);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.WEED_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.WHEAT_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.CARROTS_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.POTATO_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.BEETROOTS_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.PUMPKIN_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.MELON_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.DANDELION_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.POPPY_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.BLACKTHORN_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.TULIP_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.CYAZINT_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.VENOMILIA_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.REED_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.STICKY_REED_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.COCOA_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.FLAX_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.RED_MUSHROOM_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.BROWN_MUSHROOM_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.NETHER_WART_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.TERRA_WART_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.OAK_SAPLING_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.SPRUCE_SAPLING_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.BIRCH_SAPLING_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.JUNGLE_SAPLING_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.ACACIA_SAPLING_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.DARK_OAK_SAPLING_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.FERRU_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.CYPRIUM_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.STAGNIUM_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.PLUMBISCUS_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.AURELIA_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.SHINING_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.RED_WHEAT_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.COFFEE_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.HOPS_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.EATING_PLANT_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.BLAZEREED_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.BOBS_YER_UNCLE_RANKS_BERRIES_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.CORIUM_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.CORPSE_PLANT_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.CREEPER_WEED_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.DIAREED_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.EGG_PLANT_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.ENDER_BLOSSOM_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.MEAT_ROSE_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.MILK_WART_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.OIL_BERRIES_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.SLIME_PLANT_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.SPIDERNIP_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.TEARSTALKS_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2rBlocks.WITHEREED_CROP);
		this.registerRotorProvider(Ic2rBlockEntities.WIND_KINETIC_GENERATOR);
		this.registerRotorProvider(Ic2rBlockEntities.WATER_KINETIC_GENERATOR);
		this.registerRotorProvider(Ic2rBlockEntities.WIND_GENERATOR);
		this.registerRotorProvider(Ic2rBlockEntities.WATER_GENERATOR);
		envProxy.registerEntityRenderer(Ic2rEntities.ITNT, ExplosiveBlockRenderer::new);
		envProxy.registerEntityRenderer(Ic2rEntities.NUKE, ExplosiveBlockRenderer::new);
		envProxy.registerEntityRenderer(Ic2rEntities.DYNAMITE, context -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(context));
		envProxy.registerEntityRenderer(Ic2rEntities.STICKY_DYNAMITE, context -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(context));
		envProxy.registerEntityRenderer(Ic2rEntities.LASER_BULLET, LaserBulletEntityRenderer::new);
		envProxy.registerEntityRenderer(Ic2rEntities.RUBBER_BOAT, context -> new BoatEntityRenderer(context, false, "ic2r"));
		envProxy.registerEntityRenderer(Ic2rEntities.ELECTRIC_BOAT, context -> new BoatEntityRenderer(context, false, "ic2r"));
		envProxy.registerEntityRenderer(Ic2rEntities.CARBON_BOAT, context -> new BoatEntityRenderer(context, false, "ic2r"));
		envProxy.registerBlockEntityRenderer(Ic2rBlockEntities.SIGN, SignRenderer::new);
	}

	@Override
	public void onPostInit()
	{
	}

	@Override
	public boolean isSimulating()
	{
		return !this.isRendering();
	}

	@Override
	public SoundManager getSoundManager()
	{
		return soundManager;
	}

	@Override
	public Keyboard getKeyboard()
	{
		return keyboard;
	}

	@Override
	public boolean isRendering()
	{
		return Minecraft.getInstance().isSameThread();
	}

	@Override
	public void requestTick(boolean simulating, Runnable runnable)
	{
		if (simulating)
		{
			MinecraftServer server = mc.getSingleplayerServer();
			if (server == null)
			{
				throw new IllegalStateException("server unavailable");
			}

			server.execute(runnable);
		} else
		{
			mc.execute(runnable);
		}
	}

	@Override
	public void onServerAvailable(MinecraftServer server)
	{
	}

	@Override
	public void displayError(String error, Object... args)
	{
		SideProxyServer.displayError0(error, args);
	}

	@Override
	public void displayError(Exception e, String error, Object... args)
	{
		SideProxyServer.displayError(this, e, error, args);
	}

	@Override
	public Player getPlayerInstance()
	{
		return mc.player;
	}

	@Override
	public Level getWorld(MinecraftServer server, ResourceLocation dimId)
	{
		if (server == null)
		{
			Level ret = mc.level;
			if (ret != null && dimId.equals(Util.getDimId(ret)))
			{
				return ret;
			}
		} else
		{
			for (Level world : server.getAllLevels())
			{
				if (dimId.equals(Util.getDimId(world)))
				{
					return world;
				}
			}
		}

		return null;
	}

	@Override
	public Level getPlayerWorld()
	{
		return mc.level;
	}

	@Override
	public RecipeManager getRecipeManager()
	{
		MinecraftServer server = mc.getSingleplayerServer();
		return server != null ? server.getRecipeManager() : Objects.requireNonNull(mc.getConnection()).getRecipeManager();
	}

	@Override
	public File getMinecraftDir()
	{
		return envProxy.getMinecraftDir();
	}

	@Deprecated
	@Override
	public void playSoundSp(SoundEvent soundEvent, SoundSource soundCategory, float volume, float pitch)
	{
		IC2R.soundManager.playOnce(soundEvent, soundCategory, volume, pitch, this.getPlayerInstance());
	}

	@Override
	public void playSoundOnce(Entity entity, SoundEvent soundEvent, float volume, float pitch)
	{
		entity.playSound(soundEvent, volume, pitch);
	}

	@Override
	public void messagePlayer(Player player, String translatable, Object... args)
	{
		if (player == null) player = mc.player;
		if (player != null)
			player.displayClientMessage(Component.translatable(translatable, args.length > 0 ? SideProxyServer.getMessageComponents(args) : new Object[0]), false);
	}

	public void messagePlayer(Player player, Component translatable)
	{
		messagePlayer(player, translatable.getString());
	}

	@Override
	public <T extends BlockEntity & IRotorProvider> void registerRotorProvider(BlockEntityType<T> type)
	{
		envProxy.registerBer(type, KineticGeneratorRenderer::new);
	}

	private static int getFluidCellTintColor(ItemStack stack, int tintIndex)
	{
		if (tintIndex != 1 || !(stack.getItem() instanceof Ic2rFluidItem fluidItem))
		{
			return -1;
		}

		Ic2rFluidStack fluidStack = fluidItem.getFluidStack(stack);
		return fluidStack != null && !fluidStack.isEmpty() ? FluidHandler.getColor(fluidStack.getFluid()) : -1;
	}

}

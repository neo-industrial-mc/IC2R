package me.halfcooler.ic2r.core.ref;

import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.generator.container.ContainerSolarGenerator;
import me.halfcooler.ic2r.core.block.heatgenerator.container.ContainerElectricHeatGenerator;
import me.halfcooler.ic2r.core.block.heatgenerator.container.ContainerFluidHeatGenerator;
import me.halfcooler.ic2r.core.block.heatgenerator.container.ContainerRTHeatGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.container.ContainerElectricKineticGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.container.ContainerSteamKineticGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.container.ContainerStirlingKineticGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.container.ContainerWaterKineticGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.container.ContainerWindKineticGenerator;
import me.halfcooler.ic2r.core.block.machine.container.ContainerAdvMiner;
import me.halfcooler.ic2r.core.block.machine.container.ContainerBatchCrafter;
import me.halfcooler.ic2r.core.block.machine.container.ContainerCanner;
import me.halfcooler.ic2r.core.block.machine.container.ContainerChunkLoader;
import me.halfcooler.ic2r.core.block.machine.container.ContainerCondenser;
import me.halfcooler.ic2r.core.block.machine.container.ContainerCropHarvester;
import me.halfcooler.ic2r.core.block.machine.container.ContainerCropmatron;
import me.halfcooler.ic2r.core.block.machine.container.ContainerElectrolyzer;
import me.halfcooler.ic2r.core.block.machine.container.ContainerFermenter;
import me.halfcooler.ic2r.core.block.machine.container.ContainerFluidBottler;
import me.halfcooler.ic2r.core.block.machine.container.ContainerFluidDistributor;
import me.halfcooler.ic2r.core.block.machine.container.ContainerFluidRegulator;
import me.halfcooler.ic2r.core.block.machine.container.ContainerIndustrialWorkbench;
import me.halfcooler.ic2r.core.block.machine.container.ContainerItemBuffer;
import me.halfcooler.ic2r.core.block.machine.container.ContainerLiquidHeatExchanger;
import me.halfcooler.ic2r.core.block.machine.container.ContainerMagnetizer;
import me.halfcooler.ic2r.core.block.machine.container.ContainerMatter;
import me.halfcooler.ic2r.core.block.machine.container.ContainerMetalFormer;
import me.halfcooler.ic2r.core.block.machine.container.ContainerMiner;
import me.halfcooler.ic2r.core.block.machine.container.ContainerPatternStorage;
import me.halfcooler.ic2r.core.block.machine.container.ContainerReplicator;
import me.halfcooler.ic2r.core.block.machine.container.ContainerScanner;
import me.halfcooler.ic2r.core.block.machine.container.ContainerSolarDistiller;
import me.halfcooler.ic2r.core.block.machine.container.ContainerSortingMachine;
import me.halfcooler.ic2r.core.block.machine.container.ContainerSteamGenerator;
import me.halfcooler.ic2r.core.block.machine.container.ContainerWeightedFluidDistributor;
import me.halfcooler.ic2r.core.block.machine.container.ContainerWeightedItemDistributor;
import me.halfcooler.ic2r.core.block.personal.ContainerEnergyOMatClosed;
import me.halfcooler.ic2r.core.block.personal.ContainerEnergyOMatOpen;
import me.halfcooler.ic2r.core.block.personal.ContainerTradeOMatClosed;
import me.halfcooler.ic2r.core.block.personal.ContainerTradeOMatOpen;
import me.halfcooler.ic2r.core.block.reactor.container.ContainerNuclearReactor;
import me.halfcooler.ic2r.core.block.storage.box.ContainerStorageBox;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityInventory;
import me.halfcooler.ic2r.core.block.wiring.ContainerChargepadBlock;
import me.halfcooler.ic2r.core.block.wiring.ContainerElectricBlock;
import me.halfcooler.ic2r.core.block.wiring.ContainerTransformer;
import me.halfcooler.ic2r.core.gui.code.CodeGuiSampleMenu;
import me.halfcooler.ic2r.core.gui.dynamic.DynamicContainer;
import me.halfcooler.ic2r.core.item.IHandHeldInventory;
import me.halfcooler.ic2r.core.item.IHandHeldSubInventory;
import me.halfcooler.ic2r.core.item.tool.ContainerContainmentbox;
import me.halfcooler.ic2r.core.item.tool.ContainerMeter;
import me.halfcooler.ic2r.core.item.tool.ContainerMiningFilter;
import me.halfcooler.ic2r.core.item.tool.ContainerToolScanner;
import me.halfcooler.ic2r.core.item.tool.ContainerToolbox;
import me.halfcooler.ic2r.core.item.tool.ContainerAnalyzer;
import me.halfcooler.ic2r.core.item.tool.HandHeldInventory;
import me.halfcooler.ic2r.core.item.upgrade.HandHeldAdvancedUpgrade;
import me.halfcooler.ic2r.core.item.upgrade.HandHeldOre;
import me.halfcooler.ic2r.core.item.upgrade.HandHeldValueConfig;
import me.halfcooler.ic2r.core.network.DataEncoder;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.proxy.EnvProxy;
import me.halfcooler.ic2r.core.util.Util;

import java.io.IOException;
import java.util.function.BiFunction;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class Ic2rScreenHandlers
{
	private static final EnvProxy.ExtendedClientScreenHandlerFactory<ContainerBase<?>> MANAGED_CLIENT_BE_HANDLER = createManagedBeClientHandler();
	public static final MenuType<DynamicContainer<TileEntityInventory>> DYNAMIC_BE = registerManagedBe("dynamic_be");
	public static final MenuType<ContainerSolarGenerator> SOLAR_GENERATOR = registerManagedBe("solar_generator");
	public static final MenuType<ContainerElectricHeatGenerator> ELECTRIC_HEAT_GENERATOR = registerManagedBe("electric_heat_generator");
	public static final MenuType<ContainerFluidHeatGenerator> FLUID_HEAT_GENERATOR = registerManagedBe("fluid_heat_generator");
	public static final MenuType<ContainerRTHeatGenerator> RT_HEAT_GENERATOR = registerManagedBe("rt_heat_generator");
	public static final MenuType<ContainerElectricKineticGenerator> ELECTRIC_KINETIC_GENERATOR = registerManagedBe("electric_kinetic_generator");
	public static final MenuType<ContainerSteamKineticGenerator> STEAM_KINETIC_GENERATOR = registerManagedBe("steam_kinetic_generator");
	public static final MenuType<ContainerStirlingKineticGenerator> STIRLING_KINETIC_GENERATOR = registerManagedBe("stirling_kinetic_generator");
	public static final MenuType<ContainerWaterKineticGenerator> WATER_KINETIC_GENERATOR = registerManagedBe("water_kinetic_generator");
	public static final MenuType<ContainerWindKineticGenerator> WIND_KINETIC_GENERATOR = registerManagedBe("wind_kinetic_generator");
	public static final MenuType<ContainerNuclearReactor> NUCLEAR_REACTOR = registerManagedBe("nuclear_reactor");
	public static final MenuType<ContainerCondenser> CONDENSER = registerManagedBe("condenser");
	public static final MenuType<ContainerFluidBottler> FLUID_BOTTLER = registerManagedBe("fluid_bottler");
	public static final MenuType<ContainerFluidDistributor> FLUID_DISTRIBUTOR = registerManagedBe("fluid_distributor");
	public static final MenuType<ContainerFluidRegulator> FLUID_REGULATOR = registerManagedBe("fluid_regulator");
	public static final MenuType<ContainerLiquidHeatExchanger> LIQUID_HEAT_EXCHANGER = registerManagedBe("liquid_heat_exchanger");
	public static final MenuType<ContainerSolarDistiller> SOLAR_DISTILLER = registerManagedBe("solar_distiller");
	public static final MenuType<ContainerSteamGenerator> STEAM_GENERATOR = registerManagedBe("steam_generator");
	public static final MenuType<ContainerItemBuffer> ITEM_BUFFER = registerManagedBe("item_buffer");
	public static final MenuType<ContainerMagnetizer> MAGNETIZER = registerManagedBe("magnetizer");
	public static final MenuType<ContainerSortingMachine> SORTING_MACHINE = registerManagedBe("sorting_machine");
	public static final MenuType<ContainerCanner> CANNER = registerManagedBe("canner");
	public static final MenuType<ContainerFermenter> FERMENTER = registerManagedBe("fermenter");
	public static final MenuType<ContainerMetalFormer> METAL_FORMER = registerManagedBe("metal_former");
	public static final MenuType<ContainerAdvMiner> ADVANCED_MINER = registerManagedBe("advanced_miner");
	public static final MenuType<ContainerCropHarvester> CROP_HARVESTER = registerManagedBe("crop_harvester");
	public static final MenuType<ContainerCropmatron> CROPMATRON = registerManagedBe("cropmatron");
	public static final MenuType<ContainerMiner> MINER = registerManagedBe("miner");
	public static final MenuType<ContainerMatter> MATTER_GENERATOR = registerManagedBe("matter_generator");
	public static final MenuType<ContainerPatternStorage> PATTERN_STORAGE = registerManagedBe("pattern_storage");
	public static final MenuType<ContainerReplicator> REPLICATOR = registerManagedBe("replicator");
	public static final MenuType<ContainerScanner> UU_SCANNER = registerManagedBe("uu_scanner");
	public static final MenuType<ContainerEnergyOMatClosed> ENERGY_O_MAT_CLOSED = registerManagedBe("energy_o_mat_closed");
	public static final MenuType<ContainerEnergyOMatOpen> ENERGY_O_MAT_OPEN = registerManagedBe("energy_o_mat_open");
	public static final MenuType<ContainerTradeOMatClosed> TRADE_O_MAT_CLOSED = registerManagedBe("trade_o_mat_closed");
	public static final MenuType<ContainerTradeOMatOpen> TRADE_O_MAT_OPEN = registerManagedBe("trade_o_mat_open");
	public static final MenuType<ContainerChargepadBlock> CHARGEPAD = registerManagedBe("chargepad");
	public static final MenuType<ContainerElectricBlock> ENERGY_STORAGE = registerManagedBe("energy_storage");
	public static final MenuType<ContainerElectrolyzer> ELECTROLYZER = registerManagedBe("electrolyzer");
	public static final MenuType<ContainerTransformer> TRANSFORMER = registerManagedBe("transformer");
	public static final MenuType<ContainerChunkLoader> CHUNK_LOADER = registerManagedBe("chunk_loader");
	public static final MenuType<ContainerWeightedFluidDistributor> WEIGHTED_FLUID_DISTRIBUTOR = registerManagedBe("weighted_fluid_distributor");
	public static final MenuType<ContainerWeightedItemDistributor> WEIGHTED_ITEM_DISTRIBUTOR = registerManagedBe("weighted_item_distributor");
	public static final MenuType<ContainerIndustrialWorkbench> INDUSTRIAL_WORKBENCH = registerManagedBe("industrial_workbench");
	public static final MenuType<ContainerBatchCrafter> BATCH_CRAFTER = registerManagedBe("batch_crafter");
	/** G2.3: all storage box tiers (wood/iron/bronze/steel/iridium) share one pure-code Menu. */
	public static final MenuType<ContainerStorageBox> STORAGE_BOX = registerManagedBe("storage_box");
	private static final EnvProxy.ExtendedClientScreenHandlerFactory<ContainerBase<?>> MANAGED_CLIENT_ITEM_HANDLER = createManagedItemClientHandler();
	public static final MenuType<DynamicContainer<HandHeldInventory>> DYNAMIC_ITEM = registerManagedItem("dynamic_item");
	public static final MenuType<DynamicContainer<HandHeldAdvancedUpgrade>> ADVANCED_UPGRADE = registerManagedItem("advanced_upgrade");
	public static final MenuType<HandHeldOre.ContainerEditOre> ADVANCED_UPGRADE_EDIT_ORE = registerManagedItem("advanced_upgrade/edit_ore");
	public static final MenuType<HandHeldValueConfig.ContainerValueConfig> ADVANCED_UPGRADE_VALUE_CONFIG = registerManagedItem("advanced_upgrade/value_config");
	public static final MenuType<ContainerToolScanner> SCANNER = registerManagedItem("scanner");
	public static final MenuType<ContainerMeter> METER = registerManagedItem("meter");
	public static final MenuType<ContainerToolbox> TOOL_BOX = registerManagedItem("tool_box");
	public static final MenuType<ContainerContainmentbox> CONTAINMENT_BOX = registerManagedItem("containment_box");
	public static final MenuType<ContainerAnalyzer> CROP_ANALYZER = registerManagedItem("crop_analyzer");
	public static final MenuType<ContainerMiningFilter> MINING_FILTER = registerManagedItem("mining_filter");

	/**
	 * W2.4 pure-code GUI sample (no guidef XML, no real block). See {@link CodeGuiSampleMenu}
	 * and {@code docs/spec/gui_modernization.md}. Not opened in-game by default.
	 */
	public static final MenuType<CodeGuiSampleMenu> CODE_GUI_SAMPLE = register("code_gui_sample", CodeGuiSampleMenu::create);

	public static void init()
	{
	}

	private static <T extends AbstractContainerMenu> MenuType<T> register(String name, BiFunction<Integer, Inventory, T> factory)
	{
		return IC2R.envProxy.registerScreenHandler(IC2R.getIdentifier(name), factory);
	}

	private static <T extends AbstractContainerMenu> MenuType<T> registerExtended(String name, EnvProxy.ExtendedClientScreenHandlerFactory<T> factory)
	{
		return IC2R.envProxy.registerExtendedScreenHandler(IC2R.getIdentifier(name), factory);
	}

	public static <T extends AbstractContainerMenu> MenuType<T> registerManagedBe(String name)
	{
		return (MenuType<T>) registerExtended(name, MANAGED_CLIENT_BE_HANDLER);
	}

	private static <T extends AbstractContainerMenu> MenuType<T> registerManagedItem(String name)
	{
		return (MenuType<T>) registerExtended(name, MANAGED_CLIENT_ITEM_HANDLER);
	}

	private static EnvProxy.ExtendedClientScreenHandlerFactory<ContainerBase<?>> createManagedBeClientHandler()
	{
		return (syncId, inventory, data) ->
		{
			GrowingBuffer buffer = GrowingBuffer.wrap(data);

			try
			{
				BlockEntity be = DataEncoder.getValue(DataEncoder.decode(buffer, DataEncoder.EncodedType.TileEntity), null);
				return !(be instanceof IHasGui provider) ? null : provider.createClientScreenHandler(syncId, inventory, buffer);
			} catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		};
	}

	public static void writeManagedBeData(BlockEntity be, GrowingBuffer buffer) throws IOException
	{
		DataEncoder.encode(buffer, be, false);
	}

	private static EnvProxy.ExtendedClientScreenHandlerFactory<ContainerBase<?>> createManagedItemClientHandler()
	{
		return (syncId, inventory, data) ->
		{
			GrowingBuffer buffer = GrowingBuffer.wrap(data);
			int currentItemPosition = buffer.readInt();
			boolean subGui = buffer.readBoolean();
			int subGuiId = subGui ? buffer.readVarInt() : 0;
			Player player = IC2R.sideProxy.getPlayerInstance();
			InteractionHand hand;
			ItemStack currentItem;
			if (currentItemPosition < 0)
			{
				int handOrdinal = -currentItemPosition;
				if (handOrdinal >= Util.HANDS.length)
				{
					return null;
				}

				hand = Util.HANDS[handOrdinal];
				currentItem = player.getItemInHand(hand);
			} else
			{
				if (currentItemPosition != player.getInventory().selected)
				{
					return null;
				}

				hand = InteractionHand.MAIN_HAND;
				currentItem = player.getInventory().getSelected();
			}

			if (!(currentItem.getItem() instanceof IHandHeldInventory item))
			{
				return null;
			} else
			{
				IHasGui provider;
				if (subGui && item instanceof IHandHeldSubInventory)
				{
					provider = ((IHandHeldSubInventory) item).getSubInventory(player, hand, currentItem, subGuiId);
				} else
				{
					provider = item.getInventory(player, hand, currentItem);
				}

				return provider.createClientScreenHandler(syncId, inventory, buffer);
			}
		};
	}

	public static void writeManagedItemData(Player player, InteractionHand hand, Integer subGuiId, GrowingBuffer buffer)
	{
		Item item = player.getItemInHand(hand).getItem();
		assert item instanceof IHandHeldInventory;
		int slot;
		if (hand == InteractionHand.MAIN_HAND)
		{
			slot = player.getInventory().selected;
		} else
		{
			slot = -hand.ordinal();
		}

		buffer.writeInt(slot);
		boolean subInv = subGuiId != null && item instanceof IHandHeldSubInventory;
		buffer.writeBoolean(subInv);
		if (subInv)
		{
			buffer.writeVarInt(subGuiId);
		}
	}
}

package me.halfcooler.ic2r.core.ref.blocks;

import me.halfcooler.ic2r.core.block.steam.TileEntityCokeKiln;
import me.halfcooler.ic2r.core.block.steam.TileEntityCokeKilnGrate;
import me.halfcooler.ic2r.core.block.steam.TileEntityCokeKilnHatch;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityAdvMiner;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityBatchCrafter;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityBlastFurnace;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityBlockCutter;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityCanner;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityCentrifuge;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityChunkLoader;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityCompressor;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityCondenser;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityCropHarvester;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityCropmatron;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityElectricFurnace;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityElectrolyzer;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityExtractor;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityFermenter;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityFluidBottler;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityFluidDistributor;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityFluidRegulator;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityInduction;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityIndustrialWorkbench;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityIronFurnace;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityItemBuffer;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityLiquidHeatExchanger;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityMacerator;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityMagnetizer;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityMatter;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityMetalFormer;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityMiner;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityOreWashing;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityPatternStorage;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityPump;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityRecycler;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityReplicator;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityScanner;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntitySolarDistiller;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntitySolidCanner;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntitySortingMachine;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntitySteamGenerator;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntitySteamRepressurizer;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityTank;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityTeleporter;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityTerra;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityTesla;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityWeightedFluidDistributor;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityWeightedItemDistributor;
import me.halfcooler.ic2r.core.block.personal.TileEntityEnergyOMat;
import me.halfcooler.ic2r.core.block.personal.TileEntityPersonalChest;
import me.halfcooler.ic2r.core.block.personal.TileEntityTradeOMat;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntityBlock;
import me.halfcooler.ic2r.core.util.Util;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;


import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;
import me.halfcooler.ic2r.forge.EnvProxyForge;

/** Domain block registrations: processing, fluid, logistics, personal, and utility machines */
public final class Ic2rBlocksMachines
{
	private Ic2rBlocksMachines()
	{
	}

	public static final RegistryObject<Block> CONDENSER = EnvProxyForge.BLOCKS.register("condenser", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityCondenser.class, true, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final RegistryObject<Block> FLUID_BOTTLER = EnvProxyForge.BLOCKS.register("fluid_bottler", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityFluidBottler.class, true, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final RegistryObject<Block> FLUID_DISTRIBUTOR = EnvProxyForge.BLOCKS.register("fluid_distributor", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityFluidDistributor.class, true, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.allFacings, true));
	public static final RegistryObject<Block> FLUID_REGULATOR = EnvProxyForge.BLOCKS.register("fluid_regulator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityFluidRegulator.class, true, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.allFacings, true));
	public static final RegistryObject<Block> LIQUID_HEAT_EXCHANGER = EnvProxyForge.BLOCKS.register("liquid_heat_exchanger", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityLiquidHeatExchanger.class, true, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.allFacings, true));
	public static final RegistryObject<Block> PUMP = EnvProxyForge.BLOCKS.register("pump", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityPump.class, true, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.allFacings, true));
	public static final RegistryObject<Block> SOLAR_DISTILLER = EnvProxyForge.BLOCKS.register("solar_distiller", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntitySolarDistiller.class, false, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.noFacings, false));
	public static final RegistryObject<Block> STEAM_GENERATOR = EnvProxyForge.BLOCKS.register("steam_generator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntitySteamGenerator.class, true, Ic2rTileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, true));
	public static final RegistryObject<Block> ITEM_BUFFER = EnvProxyForge.BLOCKS.register("item_buffer", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityItemBuffer.class, false, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.noFacings, false));
	public static final RegistryObject<Block> MAGNETIZER = EnvProxyForge.BLOCKS.register("magnetizer", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityMagnetizer.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, true));
	public static final RegistryObject<Block> SORTING_MACHINE = EnvProxyForge.BLOCKS.register("sorting_machine", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntitySortingMachine.class, false, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.onlyNorth, false));
	public static final RegistryObject<Block> TELEPORTER = EnvProxyForge.BLOCKS.register("teleporter", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityTeleporter.class, true, Ic2rTileEntityBlock.DefaultDrop.AdvMachine, Util.noFacings, false));
	public static final RegistryObject<Block> TERRAFORMER = EnvProxyForge.BLOCKS.register("terraformer", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityTerra.class, true, Ic2rTileEntityBlock.DefaultDrop.AdvMachine, Util.noFacings, false));
	public static final RegistryObject<Block> TESLA_COIL = EnvProxyForge.BLOCKS.register("tesla_coil", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityTesla.class, false, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final RegistryObject<Block> CANNER = EnvProxyForge.BLOCKS.register("canner", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityCanner.class, true, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final RegistryObject<Ic2rTileEntityBlock> COMPRESSOR = EnvProxyForge.BLOCKS.register("compressor", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityCompressor.class, true, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final RegistryObject<Block> ELECTRIC_FURNACE = EnvProxyForge.BLOCKS.register("electric_furnace", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityElectricFurnace.class, true, Ic2rTileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, true));
	public static final RegistryObject<Ic2rTileEntityBlock> EXTRACTOR = EnvProxyForge.BLOCKS.register("extractor", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityExtractor.class, true, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final RegistryObject<Block> IRON_FURNACE = EnvProxyForge.BLOCKS.register("iron_furnace", () -> Ic2rTileEntityBlock.create(Properties.of().strength(5.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityIronFurnace.class, true, Ic2rTileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, false));
	public static final RegistryObject<Ic2rTileEntityBlock> MACERATOR = EnvProxyForge.BLOCKS.register("macerator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityMacerator.class, true, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final RegistryObject<Block> RECYCLER = EnvProxyForge.BLOCKS.register("recycler", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityRecycler.class, true, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final RegistryObject<Block> SOLID_CANNER = EnvProxyForge.BLOCKS.register("solid_canner", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntitySolidCanner.class, true, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final RegistryObject<Ic2rTileEntityBlock> BLAST_FURNACE = EnvProxyForge.BLOCKS.register("blast_furnace", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityBlastFurnace.class, true, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.allFacings, true));
	public static final RegistryObject<Ic2rTileEntityBlock> BLOCK_CUTTER = EnvProxyForge.BLOCKS.register("block_cutter", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityBlockCutter.class, true, Ic2rTileEntityBlock.DefaultDrop.AdvMachine, Util.horizontalFacings, true));
	public static final RegistryObject<Ic2rTileEntityBlock> CENTRIFUGE = EnvProxyForge.BLOCKS.register("centrifuge", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityCentrifuge.class, true, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final RegistryObject<Block> FERMENTER = EnvProxyForge.BLOCKS.register("fermenter", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityFermenter.class, true, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.allFacings, true));
	public static final RegistryObject<Block> INDUCTION_FURNACE = EnvProxyForge.BLOCKS.register("induction_furnace", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityInduction.class, true, Ic2rTileEntityBlock.DefaultDrop.AdvMachine, Util.horizontalFacings, true));
	public static final RegistryObject<Ic2rTileEntityBlock> METAL_FORMER = EnvProxyForge.BLOCKS.register("metal_former", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityMetalFormer.class, true, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final RegistryObject<Ic2rTileEntityBlock> ORE_WASHING_PLANT = EnvProxyForge.BLOCKS.register("ore_washing_plant", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityOreWashing.class, true, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final RegistryObject<Block> ADVANCED_MINER = EnvProxyForge.BLOCKS.register("advanced_miner", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityAdvMiner.class, true, Ic2rTileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, true));
	public static final RegistryObject<Block> CROP_HARVESTER = EnvProxyForge.BLOCKS.register("crop_harvester", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityCropHarvester.class, false, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final RegistryObject<Block> CROPMATRON = EnvProxyForge.BLOCKS.register("cropmatron", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityCropmatron.class, true, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final RegistryObject<Block> MINER = EnvProxyForge.BLOCKS.register("miner", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityMiner.class, true, Ic2rTileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, true));
	public static final RegistryObject<Block> MATTER_GENERATOR = EnvProxyForge.BLOCKS.register("matter_generator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityMatter.class, true, Ic2rTileEntityBlock.DefaultDrop.AdvMachine, Util.horizontalFacings, true));
	public static final RegistryObject<Block> PATTERN_STORAGE = EnvProxyForge.BLOCKS.register("pattern_storage", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityPatternStorage.class, false, Ic2rTileEntityBlock.DefaultDrop.AdvMachine, Util.horizontalFacings, true));
	public static final RegistryObject<Block> REPLICATOR = EnvProxyForge.BLOCKS.register("replicator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityReplicator.class, true, Ic2rTileEntityBlock.DefaultDrop.AdvMachine, Util.horizontalFacings, true));
	public static final RegistryObject<Block> UU_SCANNER = EnvProxyForge.BLOCKS.register("uu_scanner", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityScanner.class, true, Ic2rTileEntityBlock.DefaultDrop.AdvMachine, Util.horizontalFacings, true));
	public static final RegistryObject<Block> ENERGY_O_MAT = EnvProxyForge.BLOCKS.register("energy_o_mat", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(-1.0F, 3600000.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityEnergyOMat.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.allFacings, false));
	public static final RegistryObject<Block> PERSONAL_CHEST = EnvProxyForge.BLOCKS.register("personal_chest", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(-1.0F, 3600000.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityPersonalChest.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, false));
	public static final RegistryObject<Block> TRADE_O_MAT = EnvProxyForge.BLOCKS.register("trade_o_mat", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(-1.0F, 3600000.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityTradeOMat.class, true, Ic2rTileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, false));
	public static final RegistryObject<Block> ELECTROLYZER = EnvProxyForge.BLOCKS.register("electrolyzer", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityElectrolyzer.class, true, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final RegistryObject<Block> TANK = EnvProxyForge.BLOCKS.register("tank", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityTank.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, true));
	public static final RegistryObject<Block> CHUNK_LOADER = EnvProxyForge.BLOCKS.register("chunk_loader", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityChunkLoader.class, true, Ic2rTileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, true));
	public static final RegistryObject<Block> STEAM_REPRESSURIZER = EnvProxyForge.BLOCKS.register("steam_repressurizer", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntitySteamRepressurizer.class, true, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.noFacings, true));
	public static final RegistryObject<Block> WEIGHTED_FLUID_DISTRIBUTOR = EnvProxyForge.BLOCKS.register("weighted_fluid_distributor", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityWeightedFluidDistributor.class, false, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.allFacings, true));
	public static final RegistryObject<Block> WEIGHTED_ITEM_DISTRIBUTOR = EnvProxyForge.BLOCKS.register("weighted_item_distributor", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityWeightedItemDistributor.class, false, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.allFacings, true));
	public static final RegistryObject<Block> INDUSTRIAL_WORKBENCH = EnvProxyForge.BLOCKS.register("industrial_workbench", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityIndustrialWorkbench.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, true));
	public static final RegistryObject<Block> BATCH_CRAFTER = EnvProxyForge.BLOCKS.register("batch_crafter", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityBatchCrafter.class, true, Ic2rTileEntityBlock.DefaultDrop.AdvMachine, Util.horizontalFacings, true));
	public static final RegistryObject<Block> COKE_KILN = EnvProxyForge.BLOCKS.register("coke_kiln", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.STONE), TileEntityCokeKiln.class, true, Ic2rTileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, true));
	public static final RegistryObject<Block> COKE_KILN_HATCH = EnvProxyForge.BLOCKS.register("coke_kiln_hatch", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.STONE), TileEntityCokeKilnHatch.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.allFacings, true));
	public static final RegistryObject<Block> COKE_KILN_GRATE = EnvProxyForge.BLOCKS.register("coke_kiln_grate", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.STONE), TileEntityCokeKilnGrate.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.allFacings, true));
}

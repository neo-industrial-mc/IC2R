package me.halfcooler.ic2r.core.ref;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.block.generator.tileentity.TileEntityCreativeGenerator;
import me.halfcooler.ic2r.core.block.generator.tileentity.TileEntityGenerator;
import me.halfcooler.ic2r.core.block.generator.tileentity.TileEntityGeoGenerator;
import me.halfcooler.ic2r.core.block.generator.tileentity.TileEntityKineticGenerator;
import me.halfcooler.ic2r.core.block.generator.tileentity.TileEntityRTGenerator;
import me.halfcooler.ic2r.core.block.generator.tileentity.TileEntitySemifluidGenerator;
import me.halfcooler.ic2r.core.block.generator.tileentity.TileEntitySolarGenerator;
import me.halfcooler.ic2r.core.block.generator.tileentity.TileEntityStirlingGenerator;
import me.halfcooler.ic2r.core.block.generator.tileentity.TileEntityWaterGenerator;
import me.halfcooler.ic2r.core.block.generator.tileentity.TileEntityWindGenerator;
import me.halfcooler.ic2r.core.block.heatgenerator.tileentity.TileEntityElectricHeatGenerator;
import me.halfcooler.ic2r.core.block.steam.TileEntityCokeKiln;
import me.halfcooler.ic2r.core.block.steam.TileEntityCokeKilnGrate;
import me.halfcooler.ic2r.core.block.steam.TileEntityCokeKilnHatch;
import me.halfcooler.ic2r.core.block.heatgenerator.tileentity.TileEntityFluidHeatGenerator;
import me.halfcooler.ic2r.core.block.heatgenerator.tileentity.TileEntityRTHeatGenerator;
import me.halfcooler.ic2r.core.block.heatgenerator.tileentity.TileEntitySolidHeatGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.tileentity.TileEntityElectricKineticGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.tileentity.TileEntityManualKineticGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.tileentity.TileEntitySteamKineticGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.tileentity.TileEntityStirlingKineticGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.tileentity.TileEntityWaterKineticGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.tileentity.TileEntityWindKineticGenerator;
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
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityITnt;
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
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityNuke;
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
import me.halfcooler.ic2r.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import me.halfcooler.ic2r.core.block.reactor.tileentity.TileEntityRCI_LZH;
import me.halfcooler.ic2r.core.block.reactor.tileentity.TileEntityRCI_RSH;
import me.halfcooler.ic2r.core.block.reactor.tileentity.TileEntityReactorAccessHatch;
import me.halfcooler.ic2r.core.block.reactor.tileentity.TileEntityReactorChamberElectric;
import me.halfcooler.ic2r.core.block.reactor.tileentity.TileEntityReactorFluidPort;
import me.halfcooler.ic2r.core.block.reactor.tileentity.TileEntityReactorRedstonePort;
import me.halfcooler.ic2r.core.block.storage.box.TileEntityBronzeStorageBox;
import me.halfcooler.ic2r.core.block.storage.box.TileEntityIridiumStorageBox;
import me.halfcooler.ic2r.core.block.storage.box.TileEntityIronStorageBox;
import me.halfcooler.ic2r.core.block.storage.box.TileEntitySteelStorageBox;
import me.halfcooler.ic2r.core.block.storage.box.TileEntityWoodenStorageBox;
import me.halfcooler.ic2r.core.block.storage.tank.TileEntityBronzeTank;
import me.halfcooler.ic2r.core.block.storage.tank.TileEntityIridiumTank;
import me.halfcooler.ic2r.core.block.storage.tank.TileEntityIronTank;
import me.halfcooler.ic2r.core.block.storage.tank.TileEntitySteelTank;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rSignBlockEntity;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityWall;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityChargePadBatBox;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityChargePadCESU;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityLuminator;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityChargePadMFE;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityChargePadMFSU;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityElectricBatBox;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityElectricCESU;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityElectricMFE;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityElectricMFSU;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityTransformerEV;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityTransformerHV;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityTransformerLV;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityTransformerMV;
import me.halfcooler.ic2r.core.crop.TileEntityCrop;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class Ic2rBlockEntities
{
	private static Map<String, BlockEntityType<?>> blockEntityTypeMap;
	public static final BlockEntityType<Ic2rSignBlockEntity> SIGN = register("sign", Ic2rSignBlockEntity::new, Ic2rBlocks.RUBBER_SIGN.get(), Ic2rBlocks.RUBBER_WALL_SIGN.get());	public static final BlockEntityType<TileEntityWall> WALL = register("wall", TileEntityWall::new, Ic2rBlocks.OBSCURED_WALL.get());
	public static final BlockEntityType<TileEntityCrop> CROP_STICK = register("crop_stick", TileEntityCrop::new, Ic2rBlocks.CROP_STICK.get());	public static final BlockEntityType<TileEntityITnt> ITNT = register("itnt", TileEntityITnt::new, Ic2rBlocks.ITNT.get());
	public static final BlockEntityType<TileEntityCrop> WEED_CROP = register("weed_crop", TileEntityCrop::new, Ic2rBlocks.WEED_CROP.get());	public static final BlockEntityType<TileEntityNuke> NUKE = register("nuke", TileEntityNuke::new, Ic2rBlocks.NUKE.get());
	public static final BlockEntityType<TileEntityCrop> WHEAT_CROP = register("wheat_crop", TileEntityCrop::new, Ic2rBlocks.WHEAT_CROP.get());	public static final BlockEntityType<TileEntityGenerator> GENERATOR = register("generator", TileEntityGenerator::new, Ic2rBlocks.GENERATOR.get());
	public static final BlockEntityType<TileEntityCrop> CARROTS_CROP = register("carrots_crop", TileEntityCrop::new, Ic2rBlocks.CARROTS_CROP.get());	public static final BlockEntityType<TileEntityGeoGenerator> GEO_GENERATOR = register("geo_generator", TileEntityGeoGenerator::new, Ic2rBlocks.GEO_GENERATOR.get());
	public static final BlockEntityType<TileEntityCrop> POTATO_CROP = register("potato_crop", TileEntityCrop::new, Ic2rBlocks.POTATO_CROP.get());	public static final BlockEntityType<TileEntityKineticGenerator> KINETIC_GENERATOR = register("kinetic_generator", TileEntityKineticGenerator::new, Ic2rBlocks.KINETIC_GENERATOR.get());
	public static final BlockEntityType<TileEntityCrop> BEETROOTS_CROP = register("beetroots_crop", TileEntityCrop::new, Ic2rBlocks.BEETROOTS_CROP.get());	public static final BlockEntityType<TileEntityRTGenerator> RT_GENERATOR = register("rt_generator", TileEntityRTGenerator::new, Ic2rBlocks.RT_GENERATOR.get());
	public static final BlockEntityType<TileEntityCrop> PUMPKIN_CROP = register("pumpkin_crop", TileEntityCrop::new, Ic2rBlocks.PUMPKIN_CROP.get());	public static final BlockEntityType<TileEntitySemifluidGenerator> SEMIFLUID_GENERATOR = register("semifluid_generator", TileEntitySemifluidGenerator::new, Ic2rBlocks.SEMIFLUID_GENERATOR.get());
	public static final BlockEntityType<TileEntityCrop> MELON_CROP = register("melon_crop", TileEntityCrop::new, Ic2rBlocks.MELON_CROP.get());	public static final BlockEntityType<TileEntitySolarGenerator> SOLAR_GENERATOR = register("solar_generator", TileEntitySolarGenerator::new, Ic2rBlocks.SOLAR_GENERATOR.get());
	public static final BlockEntityType<TileEntityCrop> DANDELION_CROP = register("dandelion_crop", TileEntityCrop::new, Ic2rBlocks.DANDELION_CROP.get());	public static final BlockEntityType<TileEntityStirlingGenerator> STIRLING_GENERATOR = register("stirling_generator", TileEntityStirlingGenerator::new, Ic2rBlocks.STIRLING_GENERATOR.get());
	public static final BlockEntityType<TileEntityCrop> POPPY_CROP = register("poppy_crop", TileEntityCrop::new, Ic2rBlocks.POPPY_CROP.get());	public static final BlockEntityType<TileEntityWaterGenerator> WATER_GENERATOR = register("water_generator", TileEntityWaterGenerator::new, Ic2rBlocks.WATER_GENERATOR.get());
	public static final BlockEntityType<TileEntityCrop> BLACKTHORN_CROP = register("blackthorn_crop", TileEntityCrop::new, Ic2rBlocks.BLACKTHORN_CROP.get());	public static final BlockEntityType<TileEntityWindGenerator> WIND_GENERATOR = register("wind_generator", TileEntityWindGenerator::new, Ic2rBlocks.WIND_GENERATOR.get());
	public static final BlockEntityType<TileEntityCrop> TULIP_CROP = register("tulip_crop", TileEntityCrop::new, Ic2rBlocks.TULIP_CROP.get());	public static final BlockEntityType<TileEntityElectricHeatGenerator> ELECTRIC_HEAT_GENERATOR = register("electric_heat_generator", TileEntityElectricHeatGenerator::new, Ic2rBlocks.ELECTRIC_HEAT_GENERATOR.get());
	public static final BlockEntityType<TileEntityCrop> CYAZINT_CROP = register("cyazint_crop", TileEntityCrop::new, Ic2rBlocks.CYAZINT_CROP.get());	public static final BlockEntityType<TileEntityFluidHeatGenerator> FLUID_HEAT_GENERATOR = register("fluid_heat_generator", TileEntityFluidHeatGenerator::new, Ic2rBlocks.FLUID_HEAT_GENERATOR.get());
	public static final BlockEntityType<TileEntityCrop> VENOMILIA_CROP = register("venomilia_crop", TileEntityCrop::new, Ic2rBlocks.VENOMILIA_CROP.get());	public static final BlockEntityType<TileEntityRTHeatGenerator> RT_HEAT_GENERATOR = register("rt_heat_generator", TileEntityRTHeatGenerator::new, Ic2rBlocks.RT_HEAT_GENERATOR.get());
	public static final BlockEntityType<TileEntityCrop> REED_CROP = register("reed_crop", TileEntityCrop::new, Ic2rBlocks.REED_CROP.get());	public static final BlockEntityType<TileEntitySolidHeatGenerator> SOLID_HEAT_GENERATOR = register("solid_heat_generator", TileEntitySolidHeatGenerator::new, Ic2rBlocks.SOLID_HEAT_GENERATOR.get());
	public static final BlockEntityType<TileEntityCrop> STICKY_REED_CROP = register("sticky_reed_crop", TileEntityCrop::new, Ic2rBlocks.STICKY_REED_CROP.get());	public static final BlockEntityType<TileEntityElectricKineticGenerator> ELECTRIC_KINETIC_GENERATOR = register("electric_kinetic_generator", TileEntityElectricKineticGenerator::new, Ic2rBlocks.ELECTRIC_KINETIC_GENERATOR.get());
	public static final BlockEntityType<TileEntityCrop> COCOA_CROP = register("cocoa_crop", TileEntityCrop::new, Ic2rBlocks.COCOA_CROP.get());	public static final BlockEntityType<TileEntityManualKineticGenerator> MANUAL_KINETIC_GENERATOR = register("manual_kinetic_generator", TileEntityManualKineticGenerator::new, Ic2rBlocks.MANUAL_KINETIC_GENERATOR.get());
	public static final BlockEntityType<TileEntityCrop> FLAX_CROP = register("flax_crop", TileEntityCrop::new, Ic2rBlocks.FLAX_CROP.get());	public static final BlockEntityType<TileEntitySteamKineticGenerator> STEAM_KINETIC_GENERATOR = register("steam_kinetic_generator", TileEntitySteamKineticGenerator::new, Ic2rBlocks.STEAM_KINETIC_GENERATOR.get());
	public static final BlockEntityType<TileEntityCrop> RED_MUSHROOM_CROP = register("red_mushroom_crop", TileEntityCrop::new, Ic2rBlocks.RED_MUSHROOM_CROP.get());	public static final BlockEntityType<TileEntityStirlingKineticGenerator> STIRLING_KINETIC_GENERATOR = register("stirling_kinetic_generator", TileEntityStirlingKineticGenerator::new, Ic2rBlocks.STIRLING_KINETIC_GENERATOR.get());
	public static final BlockEntityType<TileEntityCrop> BROWN_MUSHROOM_CROP = register("brown_mushroom_crop", TileEntityCrop::new, Ic2rBlocks.BROWN_MUSHROOM_CROP.get());	public static final BlockEntityType<TileEntityWaterKineticGenerator> WATER_KINETIC_GENERATOR = register("water_kinetic_generator", TileEntityWaterKineticGenerator::new, Ic2rBlocks.WATER_KINETIC_GENERATOR.get());
	public static final BlockEntityType<TileEntityCrop> NETHER_WART_CROP = register("nether_wart_crop", TileEntityCrop::new, Ic2rBlocks.NETHER_WART_CROP.get());	public static final BlockEntityType<TileEntityWindKineticGenerator> WIND_KINETIC_GENERATOR = register("wind_kinetic_generator", TileEntityWindKineticGenerator::new, Ic2rBlocks.WIND_KINETIC_GENERATOR.get());
	public static final BlockEntityType<TileEntityCrop> TERRA_WART_CROP = register("terra_wart_crop", TileEntityCrop::new, Ic2rBlocks.TERRA_WART_CROP.get());	public static final BlockEntityType<TileEntityNuclearReactorElectric> NUCLEAR_REACTOR = register("nuclear_reactor", TileEntityNuclearReactorElectric::new, Ic2rBlocks.NUCLEAR_REACTOR.get());
	public static final BlockEntityType<TileEntityCrop> OAK_SAPLING_CROP = register("oak_sapling_crop", TileEntityCrop::new, Ic2rBlocks.OAK_SAPLING_CROP.get());	public static final BlockEntityType<TileEntityReactorAccessHatch> REACTOR_ACCESS_HATCH = register("reactor_access_hatch", TileEntityReactorAccessHatch::new, Ic2rBlocks.REACTOR_ACCESS_HATCH.get());
	public static final BlockEntityType<TileEntityCrop> SPRUCE_SAPLING_CROP = register("spruce_sapling_crop", TileEntityCrop::new, Ic2rBlocks.SPRUCE_SAPLING_CROP.get());	public static final BlockEntityType<TileEntityReactorChamberElectric> REACTOR_CHAMBER = register("reactor_chamber", TileEntityReactorChamberElectric::new, Ic2rBlocks.REACTOR_CHAMBER.get());
	public static final BlockEntityType<TileEntityCrop> BIRCH_SAPLING_CROP = register("birch_sapling_crop", TileEntityCrop::new, Ic2rBlocks.BIRCH_SAPLING_CROP.get());	public static final BlockEntityType<TileEntityReactorFluidPort> REACTOR_FLUID_PORT = register("reactor_fluid_port", TileEntityReactorFluidPort::new, Ic2rBlocks.REACTOR_FLUID_PORT.get());
	public static final BlockEntityType<TileEntityCrop> JUNGLE_SAPLING_CROP = register("jungle_sapling_crop", TileEntityCrop::new, Ic2rBlocks.JUNGLE_SAPLING_CROP.get());	public static final BlockEntityType<TileEntityReactorRedstonePort> REACTOR_REDSTONE_PORT = register("reactor_redstone_port", TileEntityReactorRedstonePort::new, Ic2rBlocks.REACTOR_REDSTONE_PORT.get());
	public static final BlockEntityType<TileEntityCrop> ACACIA_SAPLING_CROP = register("acacia_sapling_crop", TileEntityCrop::new, Ic2rBlocks.ACACIA_SAPLING_CROP.get());	public static final BlockEntityType<TileEntityCondenser> CONDENSER = register("condenser", TileEntityCondenser::new, Ic2rBlocks.CONDENSER.get());
	public static final BlockEntityType<TileEntityCrop> DARK_OAK_SAPLING_CROP = register("dark_oak_sapling_crop", TileEntityCrop::new, Ic2rBlocks.DARK_OAK_SAPLING_CROP.get());	public static final BlockEntityType<TileEntityFluidBottler> FLUID_BOTTLER = register("fluid_bottler", TileEntityFluidBottler::new, Ic2rBlocks.FLUID_BOTTLER.get());
	public static final BlockEntityType<TileEntityCrop> FERRU_CROP = register("ferru_crop", TileEntityCrop::new, Ic2rBlocks.FERRU_CROP.get());	public static final BlockEntityType<TileEntityFluidDistributor> FLUID_DISTRIBUTOR = register("fluid_distributor", TileEntityFluidDistributor::new, Ic2rBlocks.FLUID_DISTRIBUTOR.get());
	public static final BlockEntityType<TileEntityCrop> CYPRIUM_CROP = register("cyprium_crop", TileEntityCrop::new, Ic2rBlocks.CYPRIUM_CROP.get());	public static final BlockEntityType<TileEntityFluidRegulator> FLUID_REGULATOR = register("fluid_regulator", TileEntityFluidRegulator::new, Ic2rBlocks.FLUID_REGULATOR.get());
	public static final BlockEntityType<TileEntityCrop> STAGNIUM_CROP = register("stagnium_crop", TileEntityCrop::new, Ic2rBlocks.STAGNIUM_CROP.get());	public static final BlockEntityType<TileEntityLiquidHeatExchanger> LIQUID_HEAT_EXCHANGER = register("liquid_heat_exchanger", TileEntityLiquidHeatExchanger::new, Ic2rBlocks.LIQUID_HEAT_EXCHANGER.get());
	public static final BlockEntityType<TileEntityCrop> PLUMBISCUS_CROP = register("plumbiscus_crop", TileEntityCrop::new, Ic2rBlocks.PLUMBISCUS_CROP.get());	public static final BlockEntityType<TileEntityPump> PUMP = register("pump", TileEntityPump::new, Ic2rBlocks.PUMP.get());
	public static final BlockEntityType<TileEntityCrop> AURELIA_CROP = register("aurelia_crop", TileEntityCrop::new, Ic2rBlocks.AURELIA_CROP.get());	public static final BlockEntityType<TileEntitySolarDistiller> SOLAR_DISTILLER = register("solar_distiller", TileEntitySolarDistiller::new, Ic2rBlocks.SOLAR_DISTILLER.get());
	public static final BlockEntityType<TileEntityCrop> SHINING_CROP = register("shining_crop", TileEntityCrop::new, Ic2rBlocks.SHINING_CROP.get());	public static final BlockEntityType<TileEntitySteamGenerator> STEAM_GENERATOR = register("steam_generator", TileEntitySteamGenerator::new, Ic2rBlocks.STEAM_GENERATOR.get());
	public static final BlockEntityType<TileEntityCrop> RED_WHEAT_CROP = register("red_wheat_crop", TileEntityCrop::new, Ic2rBlocks.RED_WHEAT_CROP.get());	public static final BlockEntityType<TileEntityItemBuffer> ITEM_BUFFER = register("item_buffer", TileEntityItemBuffer::new, Ic2rBlocks.ITEM_BUFFER.get());
	public static final BlockEntityType<TileEntityCrop> COFFEE_CROP = register("coffee_crop", TileEntityCrop::new, Ic2rBlocks.COFFEE_CROP.get());	public static final BlockEntityType<TileEntityMagnetizer> MAGNETIZER = register("magnetizer", TileEntityMagnetizer::new, Ic2rBlocks.MAGNETIZER.get());
	public static final BlockEntityType<TileEntityCrop> HOPS_CROP = register("hops_crop", TileEntityCrop::new, Ic2rBlocks.HOPS_CROP.get());	public static final BlockEntityType<TileEntitySortingMachine> SORTING_MACHINE = register("sorting_machine", TileEntitySortingMachine::new, Ic2rBlocks.SORTING_MACHINE.get());
	public static final BlockEntityType<TileEntityCrop> EATING_PLANT_CROP = register("eating_plant_crop", TileEntityCrop::new, Ic2rBlocks.EATING_PLANT_CROP.get());	public static final BlockEntityType<TileEntityTeleporter> TELEPORTER = register("teleporter", TileEntityTeleporter::new, Ic2rBlocks.TELEPORTER.get());
	public static final BlockEntityType<TileEntityCrop> BLAZEREED_CROP = register("blazereed_crop", TileEntityCrop::new, Ic2rBlocks.BLAZEREED_CROP.get());	public static final BlockEntityType<TileEntityTerra> TERRAFORMER = register("terraformer", TileEntityTerra::new, Ic2rBlocks.TERRAFORMER.get());
	public static final BlockEntityType<TileEntityCrop> BOBS_YER_UNCLE_RANKS_BERRIES_CROP = register("bobs_yer_uncle_ranks_berries_crop", TileEntityCrop::new, Ic2rBlocks.BOBS_YER_UNCLE_RANKS_BERRIES_CROP.get());	public static final BlockEntityType<TileEntityTesla> TESLA_COIL = register("tesla_coil", TileEntityTesla::new, Ic2rBlocks.TESLA_COIL.get());
	public static final BlockEntityType<TileEntityCrop> CORIUM_CROP = register("corium_crop", TileEntityCrop::new, Ic2rBlocks.CORIUM_CROP.get());	public static final BlockEntityType<TileEntityLuminator> LUMINATOR_FLAT = register("luminator", TileEntityLuminator::new, Ic2rBlocks.LUMINATOR_FLAT.get());
	public static final BlockEntityType<TileEntityCrop> CORPSE_PLANT_CROP = register("corpse_plant_crop", TileEntityCrop::new, Ic2rBlocks.CORPSE_PLANT_CROP.get());	public static final BlockEntityType<TileEntityCanner> CANNER = register("canner", TileEntityCanner::new, Ic2rBlocks.CANNER.get());
	public static final BlockEntityType<TileEntityCrop> CREEPER_WEED_CROP = register("creeper_weed_crop", TileEntityCrop::new, Ic2rBlocks.CREEPER_WEED_CROP.get());	public static final BlockEntityType<TileEntityCompressor> COMPRESSOR = register("compressor", TileEntityCompressor::new, Ic2rBlocks.COMPRESSOR.get());
	public static final BlockEntityType<TileEntityCrop> DIAREED_CROP = register("diareed_crop", TileEntityCrop::new, Ic2rBlocks.DIAREED_CROP.get());	public static final BlockEntityType<TileEntityElectricFurnace> ELECTRIC_FURNACE = register("electric_furnace", TileEntityElectricFurnace::new, Ic2rBlocks.ELECTRIC_FURNACE.get());
	public static final BlockEntityType<TileEntityCrop> EGG_PLANT_CROP = register("egg_plant_crop", TileEntityCrop::new, Ic2rBlocks.EGG_PLANT_CROP.get());	public static final BlockEntityType<TileEntityExtractor> EXTRACTOR = register("extractor", TileEntityExtractor::new, Ic2rBlocks.EXTRACTOR.get());
	public static final BlockEntityType<TileEntityCrop> ENDER_BLOSSOM_CROP = register("ender_blossom_crop", TileEntityCrop::new, Ic2rBlocks.ENDER_BLOSSOM_CROP.get());	public static final BlockEntityType<TileEntityIronFurnace> IRON_FURNACE = register("iron_furnace", TileEntityIronFurnace::new, Ic2rBlocks.IRON_FURNACE.get());
	public static final BlockEntityType<TileEntityCrop> MEAT_ROSE_CROP = register("meat_rose_crop", TileEntityCrop::new, Ic2rBlocks.MEAT_ROSE_CROP.get());	public static final BlockEntityType<TileEntityMacerator> MACERATOR = register("macerator", TileEntityMacerator::new, Ic2rBlocks.MACERATOR.get());
	public static final BlockEntityType<TileEntityCrop> MILK_WART_CROP = register("milk_wart_crop", TileEntityCrop::new, Ic2rBlocks.MILK_WART_CROP.get());	public static final BlockEntityType<TileEntityRecycler> RECYCLER = register("recycler", TileEntityRecycler::new, Ic2rBlocks.RECYCLER.get());
	public static final BlockEntityType<TileEntityCrop> OIL_BERRIES_CROP = register("oil_berries_crop", TileEntityCrop::new, Ic2rBlocks.OIL_BERRIES_CROP.get());	public static final BlockEntityType<TileEntitySolidCanner> SOLID_CANNER = register("solid_canner", TileEntitySolidCanner::new, Ic2rBlocks.SOLID_CANNER.get());
	public static final BlockEntityType<TileEntityCrop> SLIME_PLANT_CROP = register("slime_plant_crop", TileEntityCrop::new, Ic2rBlocks.SLIME_PLANT_CROP.get());	public static final BlockEntityType<TileEntityBlastFurnace> BLAST_FURNACE = register("blast_furnace", TileEntityBlastFurnace::new, Ic2rBlocks.BLAST_FURNACE.get());
	public static final BlockEntityType<TileEntityCrop> SPIDERNIP_CROP = register("spidernip_crop", TileEntityCrop::new, Ic2rBlocks.SPIDERNIP_CROP.get());	public static final BlockEntityType<TileEntityBlockCutter> BLOCK_CUTTER = register("block_cutter", TileEntityBlockCutter::new, Ic2rBlocks.BLOCK_CUTTER.get());
	public static final BlockEntityType<TileEntityCrop> TEARSTALKS_CROP = register("tearstalks_crop", TileEntityCrop::new, Ic2rBlocks.TEARSTALKS_CROP.get());	public static final BlockEntityType<TileEntityCentrifuge> CENTRIFUGE = register("centrifuge", TileEntityCentrifuge::new, Ic2rBlocks.CENTRIFUGE.get());
	public static final BlockEntityType<TileEntityCrop> WITHEREED_CROP = register("withereed_crop", TileEntityCrop::new, Ic2rBlocks.WITHEREED_CROP.get());	public static final BlockEntityType<TileEntityFermenter> FERMENTER = register("fermenter", TileEntityFermenter::new, Ic2rBlocks.FERMENTER.get());

	public static void init()
	{
	}	public static final BlockEntityType<TileEntityInduction> INDUCTION_FURNACE = register("induction_furnace", TileEntityInduction::new, Ic2rBlocks.INDUCTION_FURNACE.get());

	private static <T extends BlockEntity> BlockEntityType<T> register(String name, BiFunction<BlockPos, BlockState, T> factory, Block... blocks)
	{
		if (blockEntityTypeMap == null)
		{
			blockEntityTypeMap = new HashMap<>();
		}

		ResourceLocation identifier = IC2R.getIdentifier(name);
		BlockEntityType<T> blockEntityType = IC2R.envProxy.registerBlockEntity(identifier, factory, blocks);
		blockEntityTypeMap.put(identifier.toString(), blockEntityType);
		return blockEntityType;
	}	public static final BlockEntityType<TileEntityMetalFormer> METAL_FORMER = register("metal_former", TileEntityMetalFormer::new, Ic2rBlocks.METAL_FORMER.get());

	public static BlockEntityType<?> get(ResourceLocation identifier)
	{
		return blockEntityTypeMap.get(identifier.toString());
	}	public static final BlockEntityType<TileEntityOreWashing> ORE_WASHING_PLANT = register("ore_washing_plant", TileEntityOreWashing::new, Ic2rBlocks.ORE_WASHING_PLANT.get());
	public static final BlockEntityType<TileEntityAdvMiner> ADVANCED_MINER = register("advanced_miner", TileEntityAdvMiner::new, Ic2rBlocks.ADVANCED_MINER.get());
	public static final BlockEntityType<TileEntityCropHarvester> CROP_HARVESTER = register("crop_harvester", TileEntityCropHarvester::new, Ic2rBlocks.CROP_HARVESTER.get());
	public static final BlockEntityType<TileEntityCropmatron> CROPMATRON = register("cropmatron", TileEntityCropmatron::new, Ic2rBlocks.CROPMATRON.get());
	public static final BlockEntityType<TileEntityMiner> MINER = register("miner", TileEntityMiner::new, Ic2rBlocks.MINER.get());
	public static final BlockEntityType<TileEntityMatter> MATTER_GENERATOR = register("matter_generator", TileEntityMatter::new, Ic2rBlocks.MATTER_GENERATOR.get());
	public static final BlockEntityType<TileEntityPatternStorage> PATTERN_STORAGE = register("pattern_storage", TileEntityPatternStorage::new, Ic2rBlocks.PATTERN_STORAGE.get());
	public static final BlockEntityType<TileEntityReplicator> REPLICATOR = register("replicator", TileEntityReplicator::new, Ic2rBlocks.REPLICATOR.get());
	public static final BlockEntityType<TileEntityScanner> UU_SCANNER = register("uu_scanner", TileEntityScanner::new, Ic2rBlocks.UU_SCANNER.get());
	public static final BlockEntityType<TileEntityEnergyOMat> ENERGY_O_MAT = register("energy_o_mat", TileEntityEnergyOMat::new, Ic2rBlocks.ENERGY_O_MAT.get());
	public static final BlockEntityType<TileEntityPersonalChest> PERSONAL_CHEST = register("personal_chest", TileEntityPersonalChest::new, Ic2rBlocks.PERSONAL_CHEST.get());
	public static final BlockEntityType<TileEntityTradeOMat> TRADE_O_MAT = register("trade_o_mat", TileEntityTradeOMat::new, Ic2rBlocks.TRADE_O_MAT.get());
	public static final BlockEntityType<TileEntityChargePadBatBox> BATBOX_CHARGEPAD = register("batbox_chargepad", TileEntityChargePadBatBox::new, Ic2rBlocks.BATBOX_CHARGEPAD.get());
	public static final BlockEntityType<TileEntityChargePadCESU> CESU_CHARGEPAD = register("cesu_chargepad", TileEntityChargePadCESU::new, Ic2rBlocks.CESU_CHARGEPAD.get());
	public static final BlockEntityType<TileEntityChargePadMFE> MFE_CHARGEPAD = register("mfe_chargepad", TileEntityChargePadMFE::new, Ic2rBlocks.MFE_CHARGEPAD.get());
	public static final BlockEntityType<TileEntityChargePadMFSU> MFSU_CHARGEPAD = register("mfsu_chargepad", TileEntityChargePadMFSU::new, Ic2rBlocks.MFSU_CHARGEPAD.get());
	public static final BlockEntityType<TileEntityElectricBatBox> BATBOX = register("batbox", TileEntityElectricBatBox::new, Ic2rBlocks.BATBOX.get());
	public static final BlockEntityType<TileEntityElectricCESU> CESU = register("cesu", TileEntityElectricCESU::new, Ic2rBlocks.CESU.get());
	public static final BlockEntityType<TileEntityElectricMFE> MFE = register("mfe", TileEntityElectricMFE::new, Ic2rBlocks.MFE.get());
	public static final BlockEntityType<TileEntityElectricMFSU> MFSU = register("mfsu", TileEntityElectricMFSU::new, Ic2rBlocks.MFSU.get());
	public static final BlockEntityType<TileEntityElectrolyzer> ELECTROLYZER = register("electrolyzer", TileEntityElectrolyzer::new, Ic2rBlocks.ELECTROLYZER.get());
	public static final BlockEntityType<TileEntityTransformerLV> LV_TRANSFORMER = register("lv_transformer", TileEntityTransformerLV::new, Ic2rBlocks.LV_TRANSFORMER.get());
	public static final BlockEntityType<TileEntityTransformerMV> MV_TRANSFORMER = register("mv_transformer", TileEntityTransformerMV::new, Ic2rBlocks.MV_TRANSFORMER.get());
	public static final BlockEntityType<TileEntityTransformerHV> HV_TRANSFORMER = register("hv_transformer", TileEntityTransformerHV::new, Ic2rBlocks.HV_TRANSFORMER.get());
	public static final BlockEntityType<TileEntityTransformerEV> EV_TRANSFORMER = register("ev_transformer", TileEntityTransformerEV::new, Ic2rBlocks.EV_TRANSFORMER.get());
	public static final BlockEntityType<TileEntityTank> TANK = register("tank", TileEntityTank::new, Ic2rBlocks.TANK.get());
	public static final BlockEntityType<TileEntityChunkLoader> CHUNK_LOADER = register("chunk_loader", TileEntityChunkLoader::new, Ic2rBlocks.CHUNK_LOADER.get());
	public static final BlockEntityType<TileEntityCreativeGenerator> CREATIVE_GENERATOR = register("creative_generator", TileEntityCreativeGenerator::new, Ic2rBlocks.CREATIVE_GENERATOR.get());
	public static final BlockEntityType<TileEntitySteamRepressurizer> STEAM_REPRESSURIZER = register("steam_repressurizer", TileEntitySteamRepressurizer::new, Ic2rBlocks.STEAM_REPRESSURIZER.get());
	public static final BlockEntityType<TileEntityWeightedFluidDistributor> WEIGHTED_FLUID_DISTRIBUTOR = register("weighted_fluid_distributor", TileEntityWeightedFluidDistributor::new, Ic2rBlocks.WEIGHTED_FLUID_DISTRIBUTOR.get());
	public static final BlockEntityType<TileEntityWeightedItemDistributor> WEIGHTED_ITEM_DISTRIBUTOR = register("weighted_item_distributor", TileEntityWeightedItemDistributor::new, Ic2rBlocks.WEIGHTED_ITEM_DISTRIBUTOR.get());
	public static final BlockEntityType<TileEntityRCI_RSH> RCI_RSH = register("rci_rsh", TileEntityRCI_RSH::new, Ic2rBlocks.RCI_RSH.get());
	public static final BlockEntityType<TileEntityRCI_LZH> RCI_LZH = register("rci_lzh", TileEntityRCI_LZH::new, Ic2rBlocks.RCI_LZH.get());





















































	public static final BlockEntityType<TileEntityIndustrialWorkbench> INDUSTRIAL_WORKBENCH = register("industrial_workbench", TileEntityIndustrialWorkbench::new, Ic2rBlocks.INDUSTRIAL_WORKBENCH.get());
	public static final BlockEntityType<TileEntityBatchCrafter> BATCH_CRAFTER = register("batch_crafter", TileEntityBatchCrafter::new, Ic2rBlocks.BATCH_CRAFTER.get());
	public static final BlockEntityType<TileEntityWoodenStorageBox> WOODEN_STORAGE_BOX = register("wooden_storage_box", TileEntityWoodenStorageBox::new, Ic2rBlocks.WOODEN_STORAGE_BOX.get());
	public static final BlockEntityType<TileEntityIronStorageBox> IRON_STORAGE_BOX = register("iron_storage_box", TileEntityIronStorageBox::new, Ic2rBlocks.IRON_STORAGE_BOX.get());
	public static final BlockEntityType<TileEntityBronzeStorageBox> BRONZE_STORAGE_BOX = register("bronze_storage_box", TileEntityBronzeStorageBox::new, Ic2rBlocks.BRONZE_STORAGE_BOX.get());
	public static final BlockEntityType<TileEntitySteelStorageBox> STEEL_STORAGE_BOX = register("steel_storage_box", TileEntitySteelStorageBox::new, Ic2rBlocks.STEEL_STORAGE_BOX.get());
	public static final BlockEntityType<TileEntityIridiumStorageBox> IRIDIUM_STORAGE_BOX = register("iridium_storage_box", TileEntityIridiumStorageBox::new, Ic2rBlocks.IRIDIUM_STORAGE_BOX.get());
	public static final BlockEntityType<TileEntityBronzeTank> BRONZE_TANK = register("bronze_tank", TileEntityBronzeTank::new, Ic2rBlocks.BRONZE_TANK.get());
	public static final BlockEntityType<TileEntityIronTank> IRON_TANK = register("iron_tank", TileEntityIronTank::new, Ic2rBlocks.IRON_TANK.get());
	public static final BlockEntityType<TileEntitySteelTank> STEEL_TANK = register("steel_tank", TileEntitySteelTank::new, Ic2rBlocks.STEEL_TANK.get());
	public static final BlockEntityType<TileEntityIridiumTank> IRIDIUM_TANK = register("iridium_tank", TileEntityIridiumTank::new, Ic2rBlocks.IRIDIUM_TANK.get());
	public static final BlockEntityType<TileEntityCokeKiln> COKE_KILN = register("coke_kiln", TileEntityCokeKiln::new, Ic2rBlocks.COKE_KILN.get());
	public static final BlockEntityType<TileEntityCokeKilnHatch> COKE_KILN_HATCH = register("coke_kiln_hatch", TileEntityCokeKilnHatch::new, Ic2rBlocks.COKE_KILN_HATCH.get());
	public static final BlockEntityType<TileEntityCokeKilnGrate> COKE_KILN_GRATE = register("coke_kiln_grate", TileEntityCokeKilnGrate::new, Ic2rBlocks.COKE_KILN_GRATE.get());







}

package me.halfcooler.ic2r.core.ref;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.block.misc.RubberLogBlock;
import me.halfcooler.ic2r.core.block.misc.RubberWoodBlock;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntityBlock;
import me.halfcooler.ic2r.core.block.wiring.DetectorFoamCableBlock;
import me.halfcooler.ic2r.core.block.wiring.FoamCableBlock;
import me.halfcooler.ic2r.core.block.wiring.SplitterFoamCableBlock;
import me.halfcooler.ic2r.core.ref.blocks.Ic2rBlocksBuilding;
import me.halfcooler.ic2r.core.ref.blocks.Ic2rBlocksCrops;
import me.halfcooler.ic2r.core.ref.blocks.Ic2rBlocksGenerators;
import me.halfcooler.ic2r.core.ref.blocks.Ic2rBlocksMachines;
import me.halfcooler.ic2r.core.ref.blocks.Ic2rBlocksReactor;
import me.halfcooler.ic2r.core.ref.blocks.Ic2rBlocksResources;
import me.halfcooler.ic2r.core.ref.blocks.Ic2rBlocksStorage;
import me.halfcooler.ic2r.core.ref.blocks.Ic2rBlocksWiring;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

/**
 * Block registration facade. Domain implementations live under {@code ref.blocks}.
 * External code should keep using {@code Ic2rBlocks.XXX} field access.
 */
public final class Ic2rBlocks
{
	private Ic2rBlocks()
	{
	}

	public static final RegistryObject<Block> LEAD_ORE = Ic2rBlocksResources.LEAD_ORE;
	public static final RegistryObject<Block> TIN_ORE = Ic2rBlocksResources.TIN_ORE;
	public static final RegistryObject<Block> URANIUM_ORE = Ic2rBlocksResources.URANIUM_ORE;
	public static final RegistryObject<Block> DEEPSLATE_LEAD_ORE = Ic2rBlocksResources.DEEPSLATE_LEAD_ORE;
	public static final RegistryObject<Block> DEEPSLATE_TIN_ORE = Ic2rBlocksResources.DEEPSLATE_TIN_ORE;
	public static final RegistryObject<Block> DEEPSLATE_URANIUM_ORE = Ic2rBlocksResources.DEEPSLATE_URANIUM_ORE;
	public static final RegistryObject<Block> RAW_LEAD_BLOCK = Ic2rBlocksResources.RAW_LEAD_BLOCK;
	public static final RegistryObject<Block> RAW_TIN_BLOCK = Ic2rBlocksResources.RAW_TIN_BLOCK;
	public static final RegistryObject<Block> RAW_URANIUM_BLOCK = Ic2rBlocksResources.RAW_URANIUM_BLOCK;
	public static final RegistryObject<Block> BRONZE_BLOCK = Ic2rBlocksResources.BRONZE_BLOCK;
	public static final RegistryObject<Block> LEAD_BLOCK = Ic2rBlocksResources.LEAD_BLOCK;
	public static final RegistryObject<Block> STEEL_BLOCK = Ic2rBlocksResources.STEEL_BLOCK;
	public static final RegistryObject<Block> TIN_BLOCK = Ic2rBlocksResources.TIN_BLOCK;
	public static final RegistryObject<Block> URANIUM_BLOCK = Ic2rBlocksResources.URANIUM_BLOCK;
	public static final RegistryObject<Block> REINFORCED_STONE = Ic2rBlocksResources.REINFORCED_STONE;
	public static final RegistryObject<Block> REFRACTORY_BRICKS = Ic2rBlocksResources.REFRACTORY_BRICKS;
	public static final RegistryObject<Block> MACHINE = Ic2rBlocksResources.MACHINE;
	public static final RegistryObject<Block> ADVANCED_MACHINE = Ic2rBlocksResources.ADVANCED_MACHINE;
	public static final RegistryObject<Block> REACTOR_VESSEL = Ic2rBlocksResources.REACTOR_VESSEL;
	public static final RegistryObject<Block> SILVER_BLOCK = Ic2rBlocksResources.SILVER_BLOCK;
	public static final RegistryObject<LeavesBlock> RUBBER_LEAVES = Ic2rBlocksBuilding.RUBBER_LEAVES;
	public static final RegistryObject<RubberLogBlock> RUBBER_LOG = Ic2rBlocksBuilding.RUBBER_LOG;
	public static final RegistryObject<RotatedPillarBlock> STRIPPED_RUBBER_LOG = Ic2rBlocksBuilding.STRIPPED_RUBBER_LOG;
	public static final RegistryObject<RubberWoodBlock> RUBBER_WOOD = Ic2rBlocksBuilding.RUBBER_WOOD;
	public static final RegistryObject<Block> STRIPPED_RUBBER_WOOD = Ic2rBlocksBuilding.STRIPPED_RUBBER_WOOD;
	public static final RegistryObject<Block> RUBBER_SAPLING = Ic2rBlocksBuilding.RUBBER_SAPLING;
	public static final RegistryObject<Block> RUBBER_PLANKS = Ic2rBlocksBuilding.RUBBER_PLANKS;
	public static final RegistryObject<Block> RUBBER_BUTTON = Ic2rBlocksBuilding.RUBBER_BUTTON;
	public static final RegistryObject<Block> RUBBER_DOOR = Ic2rBlocksBuilding.RUBBER_DOOR;
	public static final RegistryObject<Block> RUBBER_FENCE = Ic2rBlocksBuilding.RUBBER_FENCE;
	public static final RegistryObject<Block> RUBBER_FENCE_GATE = Ic2rBlocksBuilding.RUBBER_FENCE_GATE;
	public static final RegistryObject<Block> RUBBER_PRESSURE_PLATE = Ic2rBlocksBuilding.RUBBER_PRESSURE_PLATE;
	public static final RegistryObject<Block> RUBBER_SIGN = Ic2rBlocksBuilding.RUBBER_SIGN;
	public static final RegistryObject<Block> RUBBER_WALL_SIGN = Ic2rBlocksBuilding.RUBBER_WALL_SIGN;
	public static final RegistryObject<Block> RUBBER_SLAB = Ic2rBlocksBuilding.RUBBER_SLAB;
	public static final RegistryObject<Block> RUBBER_STAIRS = Ic2rBlocksBuilding.RUBBER_STAIRS;
	public static final RegistryObject<Block> RUBBER_TRAPDOOR = Ic2rBlocksBuilding.RUBBER_TRAPDOOR;
	public static final RegistryObject<Block> IRON_FENCE = Ic2rBlocksBuilding.IRON_FENCE;
	public static final RegistryObject<Block> RESIN_SHEET = Ic2rBlocksBuilding.RESIN_SHEET;
	public static final RegistryObject<Block> RUBBER_SHEET = Ic2rBlocksBuilding.RUBBER_SHEET;
	public static final RegistryObject<Block> WOOL_SHEET = Ic2rBlocksBuilding.WOOL_SHEET;
	public static final RegistryObject<Block> REINFORCED_GLASS = Ic2rBlocksBuilding.REINFORCED_GLASS;
	public static final RegistryObject<Block> FOAM = Ic2rBlocksBuilding.FOAM;
	public static final RegistryObject<Block> MINING_PIPE = Ic2rBlocksBuilding.MINING_PIPE;
	public static final RegistryObject<Block> MINING_PIPE_TIP = Ic2rBlocksBuilding.MINING_PIPE_TIP;
	public static final RegistryObject<Block> REINFORCED_DOOR = Ic2rBlocksBuilding.REINFORCED_DOOR;
	public static final RegistryObject<Block> ITNT = Ic2rBlocksBuilding.ITNT;
	public static final RegistryObject<Block> NUKE = Ic2rBlocksBuilding.NUKE;
	public static final RegistryObject<Block> DYNAMITE = Ic2rBlocksBuilding.DYNAMITE;
	public static final RegistryObject<Block> GENERATOR = Ic2rBlocksGenerators.GENERATOR;
	public static final RegistryObject<Block> GEO_GENERATOR = Ic2rBlocksGenerators.GEO_GENERATOR;
	public static final RegistryObject<Block> KINETIC_GENERATOR = Ic2rBlocksGenerators.KINETIC_GENERATOR;
	public static final RegistryObject<Block> RT_GENERATOR = Ic2rBlocksGenerators.RT_GENERATOR;
	public static final RegistryObject<Block> SEMIFLUID_GENERATOR = Ic2rBlocksGenerators.SEMIFLUID_GENERATOR;
	public static final RegistryObject<Block> SOLAR_GENERATOR = Ic2rBlocksGenerators.SOLAR_GENERATOR;
	public static final RegistryObject<Block> STIRLING_GENERATOR = Ic2rBlocksGenerators.STIRLING_GENERATOR;
	public static final RegistryObject<Block> WATER_GENERATOR = Ic2rBlocksGenerators.WATER_GENERATOR;
	public static final RegistryObject<Block> WIND_GENERATOR = Ic2rBlocksGenerators.WIND_GENERATOR;
	public static final RegistryObject<Block> ELECTRIC_HEAT_GENERATOR = Ic2rBlocksGenerators.ELECTRIC_HEAT_GENERATOR;
	public static final RegistryObject<Block> FLUID_HEAT_GENERATOR = Ic2rBlocksGenerators.FLUID_HEAT_GENERATOR;
	public static final RegistryObject<Block> RT_HEAT_GENERATOR = Ic2rBlocksGenerators.RT_HEAT_GENERATOR;
	public static final RegistryObject<Block> SOLID_HEAT_GENERATOR = Ic2rBlocksGenerators.SOLID_HEAT_GENERATOR;
	public static final RegistryObject<Block> ELECTRIC_KINETIC_GENERATOR = Ic2rBlocksGenerators.ELECTRIC_KINETIC_GENERATOR;
	public static final RegistryObject<Block> MANUAL_KINETIC_GENERATOR = Ic2rBlocksGenerators.MANUAL_KINETIC_GENERATOR;
	public static final RegistryObject<Block> STEAM_KINETIC_GENERATOR = Ic2rBlocksGenerators.STEAM_KINETIC_GENERATOR;
	public static final RegistryObject<Block> STIRLING_KINETIC_GENERATOR = Ic2rBlocksGenerators.STIRLING_KINETIC_GENERATOR;
	public static final RegistryObject<Block> WATER_KINETIC_GENERATOR = Ic2rBlocksGenerators.WATER_KINETIC_GENERATOR;
	public static final RegistryObject<Block> WIND_KINETIC_GENERATOR = Ic2rBlocksGenerators.WIND_KINETIC_GENERATOR;
	public static final RegistryObject<Block> NUCLEAR_REACTOR = Ic2rBlocksReactor.NUCLEAR_REACTOR;
	public static final RegistryObject<Block> REACTOR_ACCESS_HATCH = Ic2rBlocksReactor.REACTOR_ACCESS_HATCH;
	public static final RegistryObject<Block> REACTOR_CHAMBER = Ic2rBlocksReactor.REACTOR_CHAMBER;
	public static final RegistryObject<Block> REACTOR_FLUID_PORT = Ic2rBlocksReactor.REACTOR_FLUID_PORT;
	public static final RegistryObject<Block> REACTOR_REDSTONE_PORT = Ic2rBlocksReactor.REACTOR_REDSTONE_PORT;
	public static final RegistryObject<Block> CONDENSER = Ic2rBlocksMachines.CONDENSER;
	public static final RegistryObject<Block> FLUID_BOTTLER = Ic2rBlocksMachines.FLUID_BOTTLER;
	public static final RegistryObject<Block> FLUID_DISTRIBUTOR = Ic2rBlocksMachines.FLUID_DISTRIBUTOR;
	public static final RegistryObject<Block> FLUID_REGULATOR = Ic2rBlocksMachines.FLUID_REGULATOR;
	public static final RegistryObject<Block> LIQUID_HEAT_EXCHANGER = Ic2rBlocksMachines.LIQUID_HEAT_EXCHANGER;
	public static final RegistryObject<Block> PUMP = Ic2rBlocksMachines.PUMP;
	public static final RegistryObject<Block> SOLAR_DISTILLER = Ic2rBlocksMachines.SOLAR_DISTILLER;
	public static final RegistryObject<Block> STEAM_GENERATOR = Ic2rBlocksMachines.STEAM_GENERATOR;
	public static final RegistryObject<Block> ITEM_BUFFER = Ic2rBlocksMachines.ITEM_BUFFER;
	public static final RegistryObject<Block> MAGNETIZER = Ic2rBlocksMachines.MAGNETIZER;
	public static final RegistryObject<Block> SORTING_MACHINE = Ic2rBlocksMachines.SORTING_MACHINE;
	public static final RegistryObject<Block> TELEPORTER = Ic2rBlocksMachines.TELEPORTER;
	public static final RegistryObject<Block> TERRAFORMER = Ic2rBlocksMachines.TERRAFORMER;
	public static final RegistryObject<Block> TESLA_COIL = Ic2rBlocksMachines.TESLA_COIL;
	public static final RegistryObject<Block> LUMINATOR_FLAT = Ic2rBlocksWiring.LUMINATOR_FLAT;
	public static final RegistryObject<Block> CANNER = Ic2rBlocksMachines.CANNER;
	public static final RegistryObject<Ic2rTileEntityBlock> COMPRESSOR = Ic2rBlocksMachines.COMPRESSOR;
	public static final RegistryObject<Block> ELECTRIC_FURNACE = Ic2rBlocksMachines.ELECTRIC_FURNACE;
	public static final RegistryObject<Ic2rTileEntityBlock> EXTRACTOR = Ic2rBlocksMachines.EXTRACTOR;
	public static final RegistryObject<Block> IRON_FURNACE = Ic2rBlocksMachines.IRON_FURNACE;
	public static final RegistryObject<Ic2rTileEntityBlock> MACERATOR = Ic2rBlocksMachines.MACERATOR;
	public static final RegistryObject<Block> RECYCLER = Ic2rBlocksMachines.RECYCLER;
	public static final RegistryObject<Block> SOLID_CANNER = Ic2rBlocksMachines.SOLID_CANNER;
	public static final RegistryObject<Ic2rTileEntityBlock> BLAST_FURNACE = Ic2rBlocksMachines.BLAST_FURNACE;
	public static final RegistryObject<Ic2rTileEntityBlock> BLOCK_CUTTER = Ic2rBlocksMachines.BLOCK_CUTTER;
	public static final RegistryObject<Ic2rTileEntityBlock> CENTRIFUGE = Ic2rBlocksMachines.CENTRIFUGE;
	public static final RegistryObject<Block> FERMENTER = Ic2rBlocksMachines.FERMENTER;
	public static final RegistryObject<Block> INDUCTION_FURNACE = Ic2rBlocksMachines.INDUCTION_FURNACE;
	public static final RegistryObject<Ic2rTileEntityBlock> METAL_FORMER = Ic2rBlocksMachines.METAL_FORMER;
	public static final RegistryObject<Ic2rTileEntityBlock> ORE_WASHING_PLANT = Ic2rBlocksMachines.ORE_WASHING_PLANT;
	public static final RegistryObject<Block> ADVANCED_MINER = Ic2rBlocksMachines.ADVANCED_MINER;
	public static final RegistryObject<Block> CROP_HARVESTER = Ic2rBlocksMachines.CROP_HARVESTER;
	public static final RegistryObject<Block> CROPMATRON = Ic2rBlocksMachines.CROPMATRON;
	public static final RegistryObject<Block> MINER = Ic2rBlocksMachines.MINER;
	public static final RegistryObject<Block> MATTER_GENERATOR = Ic2rBlocksMachines.MATTER_GENERATOR;
	public static final RegistryObject<Block> PATTERN_STORAGE = Ic2rBlocksMachines.PATTERN_STORAGE;
	public static final RegistryObject<Block> REPLICATOR = Ic2rBlocksMachines.REPLICATOR;
	public static final RegistryObject<Block> UU_SCANNER = Ic2rBlocksMachines.UU_SCANNER;
	public static final RegistryObject<Block> ENERGY_O_MAT = Ic2rBlocksMachines.ENERGY_O_MAT;
	public static final RegistryObject<Block> PERSONAL_CHEST = Ic2rBlocksMachines.PERSONAL_CHEST;
	public static final RegistryObject<Block> TRADE_O_MAT = Ic2rBlocksMachines.TRADE_O_MAT;
	public static final RegistryObject<FoamCableBlock> GLASS_FIBRE_FOAM_CABLE = Ic2rBlocksWiring.GLASS_FIBRE_FOAM_CABLE;
	public static final RegistryObject<Block> GLASS_FIBRE_CABLE = Ic2rBlocksWiring.GLASS_FIBRE_CABLE;
	public static final RegistryObject<Block> BATBOX_CHARGEPAD = Ic2rBlocksWiring.BATBOX_CHARGEPAD;
	public static final RegistryObject<Block> CESU_CHARGEPAD = Ic2rBlocksWiring.CESU_CHARGEPAD;
	public static final RegistryObject<Block> MFE_CHARGEPAD = Ic2rBlocksWiring.MFE_CHARGEPAD;
	public static final RegistryObject<Block> MFSU_CHARGEPAD = Ic2rBlocksWiring.MFSU_CHARGEPAD;
	public static final RegistryObject<Block> BATBOX = Ic2rBlocksWiring.BATBOX;
	public static final RegistryObject<Block> CESU = Ic2rBlocksWiring.CESU;
	public static final RegistryObject<Block> MFE = Ic2rBlocksWiring.MFE;
	public static final RegistryObject<Block> MFSU = Ic2rBlocksWiring.MFSU;
	public static final RegistryObject<Block> ELECTROLYZER = Ic2rBlocksMachines.ELECTROLYZER;
	public static final RegistryObject<Block> LV_TRANSFORMER = Ic2rBlocksWiring.LV_TRANSFORMER;
	public static final RegistryObject<Block> MV_TRANSFORMER = Ic2rBlocksWiring.MV_TRANSFORMER;
	public static final RegistryObject<Block> HV_TRANSFORMER = Ic2rBlocksWiring.HV_TRANSFORMER;
	public static final RegistryObject<Block> EV_TRANSFORMER = Ic2rBlocksWiring.EV_TRANSFORMER;
	public static final RegistryObject<Block> TANK = Ic2rBlocksMachines.TANK;
	public static final RegistryObject<Block> CHUNK_LOADER = Ic2rBlocksMachines.CHUNK_LOADER;
	public static final RegistryObject<Block> CREATIVE_GENERATOR = Ic2rBlocksGenerators.CREATIVE_GENERATOR;
	public static final RegistryObject<Block> STEAM_REPRESSURIZER = Ic2rBlocksMachines.STEAM_REPRESSURIZER;
	public static final RegistryObject<Block> WEIGHTED_FLUID_DISTRIBUTOR = Ic2rBlocksMachines.WEIGHTED_FLUID_DISTRIBUTOR;
	public static final RegistryObject<Block> WEIGHTED_ITEM_DISTRIBUTOR = Ic2rBlocksMachines.WEIGHTED_ITEM_DISTRIBUTOR;
	public static final RegistryObject<Block> RCI_RSH = Ic2rBlocksReactor.RCI_RSH;
	public static final RegistryObject<Block> RCI_LZH = Ic2rBlocksReactor.RCI_LZH;
	public static final RegistryObject<Block> INDUSTRIAL_WORKBENCH = Ic2rBlocksMachines.INDUSTRIAL_WORKBENCH;
	public static final RegistryObject<Block> BATCH_CRAFTER = Ic2rBlocksMachines.BATCH_CRAFTER;
	public static final RegistryObject<Block> WOODEN_STORAGE_BOX = Ic2rBlocksStorage.WOODEN_STORAGE_BOX;
	public static final RegistryObject<Block> IRON_STORAGE_BOX = Ic2rBlocksStorage.IRON_STORAGE_BOX;
	public static final RegistryObject<Block> BRONZE_STORAGE_BOX = Ic2rBlocksStorage.BRONZE_STORAGE_BOX;
	public static final RegistryObject<Block> STEEL_STORAGE_BOX = Ic2rBlocksStorage.STEEL_STORAGE_BOX;
	public static final RegistryObject<Block> IRIDIUM_STORAGE_BOX = Ic2rBlocksStorage.IRIDIUM_STORAGE_BOX;
	public static final RegistryObject<Block> BRONZE_TANK = Ic2rBlocksStorage.BRONZE_TANK;
	public static final RegistryObject<Block> IRON_TANK = Ic2rBlocksStorage.IRON_TANK;
	public static final RegistryObject<Block> STEEL_TANK = Ic2rBlocksStorage.STEEL_TANK;
	public static final RegistryObject<Block> IRIDIUM_TANK = Ic2rBlocksStorage.IRIDIUM_TANK;
	public static final RegistryObject<Block> COKE_KILN = Ic2rBlocksMachines.COKE_KILN;
	public static final RegistryObject<Block> COKE_KILN_HATCH = Ic2rBlocksMachines.COKE_KILN_HATCH;
	public static final RegistryObject<Block> COKE_KILN_GRATE = Ic2rBlocksMachines.COKE_KILN_GRATE;
	public static final RegistryObject<Block> WHITE_WALL = Ic2rBlocksBuilding.WHITE_WALL;
	public static final RegistryObject<Block> ORANGE_WALL = Ic2rBlocksBuilding.ORANGE_WALL;
	public static final RegistryObject<Block> MAGENTA_WALL = Ic2rBlocksBuilding.MAGENTA_WALL;
	public static final RegistryObject<Block> LIGHT_BLUE_WALL = Ic2rBlocksBuilding.LIGHT_BLUE_WALL;
	public static final RegistryObject<Block> YELLOW_WALL = Ic2rBlocksBuilding.YELLOW_WALL;
	public static final RegistryObject<Block> LIME_WALL = Ic2rBlocksBuilding.LIME_WALL;
	public static final RegistryObject<Block> PINK_WALL = Ic2rBlocksBuilding.PINK_WALL;
	public static final RegistryObject<Block> GRAY_WALL = Ic2rBlocksBuilding.GRAY_WALL;
	public static final RegistryObject<Block> LIGHT_GRAY_WALL = Ic2rBlocksBuilding.LIGHT_GRAY_WALL;
	public static final RegistryObject<Block> CYAN_WALL = Ic2rBlocksBuilding.CYAN_WALL;
	public static final RegistryObject<Block> PURPLE_WALL = Ic2rBlocksBuilding.PURPLE_WALL;
	public static final RegistryObject<Block> BLUE_WALL = Ic2rBlocksBuilding.BLUE_WALL;
	public static final RegistryObject<Block> BROWN_WALL = Ic2rBlocksBuilding.BROWN_WALL;
	public static final RegistryObject<Block> GREEN_WALL = Ic2rBlocksBuilding.GREEN_WALL;
	public static final RegistryObject<Block> RED_WALL = Ic2rBlocksBuilding.RED_WALL;
	public static final RegistryObject<Block> BLACK_WALL = Ic2rBlocksBuilding.BLACK_WALL;
	public static final RegistryObject<Block> OBSCURED_WALL = Ic2rBlocksBuilding.OBSCURED_WALL;
	public static final RegistryObject<FoamCableBlock> COPPER_FOAM_CABLE = Ic2rBlocksWiring.COPPER_FOAM_CABLE;
	public static final RegistryObject<Block> COPPER_CABLE = Ic2rBlocksWiring.COPPER_CABLE;
	public static final RegistryObject<FoamCableBlock> GOLD_FOAM_CABLE = Ic2rBlocksWiring.GOLD_FOAM_CABLE;
	public static final RegistryObject<Block> GOLD_CABLE = Ic2rBlocksWiring.GOLD_CABLE;
	public static final RegistryObject<FoamCableBlock> IRON_FOAM_CABLE = Ic2rBlocksWiring.IRON_FOAM_CABLE;
	public static final RegistryObject<Block> IRON_CABLE = Ic2rBlocksWiring.IRON_CABLE;
	public static final RegistryObject<FoamCableBlock> TIN_FOAM_CABLE = Ic2rBlocksWiring.TIN_FOAM_CABLE;
	public static final RegistryObject<Block> TIN_CABLE = Ic2rBlocksWiring.TIN_CABLE;
	public static final RegistryObject<DetectorFoamCableBlock> DETECTOR_FOAM_CABLE = Ic2rBlocksWiring.DETECTOR_FOAM_CABLE;
	public static final RegistryObject<Block> DETECTOR_CABLE = Ic2rBlocksWiring.DETECTOR_CABLE;
	public static final RegistryObject<SplitterFoamCableBlock> SPLITTER_FOAM_CABLE = Ic2rBlocksWiring.SPLITTER_FOAM_CABLE;
	public static final RegistryObject<Block> SPLITTER_CABLE = Ic2rBlocksWiring.SPLITTER_CABLE;
	public static final RegistryObject<FoamCableBlock> INSULATED_COPPER_FOAM_CABLE = Ic2rBlocksWiring.INSULATED_COPPER_FOAM_CABLE;
	public static final RegistryObject<Block> INSULATED_COPPER_CABLE = Ic2rBlocksWiring.INSULATED_COPPER_CABLE;
	public static final RegistryObject<FoamCableBlock> INSULATED_GOLD_FOAM_CABLE = Ic2rBlocksWiring.INSULATED_GOLD_FOAM_CABLE;
	public static final RegistryObject<Block> INSULATED_GOLD_CABLE = Ic2rBlocksWiring.INSULATED_GOLD_CABLE;
	public static final RegistryObject<FoamCableBlock> DOUBLE_INSULATED_GOLD_FOAM_CABLE = Ic2rBlocksWiring.DOUBLE_INSULATED_GOLD_FOAM_CABLE;
	public static final RegistryObject<Block> DOUBLE_INSULATED_GOLD_CABLE = Ic2rBlocksWiring.DOUBLE_INSULATED_GOLD_CABLE;
	public static final RegistryObject<FoamCableBlock> INSULATED_IRON_FOAM_CABLE = Ic2rBlocksWiring.INSULATED_IRON_FOAM_CABLE;
	public static final RegistryObject<Block> INSULATED_IRON_CABLE = Ic2rBlocksWiring.INSULATED_IRON_CABLE;
	public static final RegistryObject<FoamCableBlock> DOUBLE_INSULATED_IRON_FOAM_CABLE = Ic2rBlocksWiring.DOUBLE_INSULATED_IRON_FOAM_CABLE;
	public static final RegistryObject<Block> DOUBLE_INSULATED_IRON_CABLE = Ic2rBlocksWiring.DOUBLE_INSULATED_IRON_CABLE;
	public static final RegistryObject<FoamCableBlock> TRIPLE_INSULATED_IRON_FOAM_CABLE = Ic2rBlocksWiring.TRIPLE_INSULATED_IRON_FOAM_CABLE;
	public static final RegistryObject<Block> TRIPLE_INSULATED_IRON_CABLE = Ic2rBlocksWiring.TRIPLE_INSULATED_IRON_CABLE;
	public static final RegistryObject<FoamCableBlock> INSULATED_TIN_FOAM_CABLE = Ic2rBlocksWiring.INSULATED_TIN_FOAM_CABLE;
	public static final RegistryObject<Block> INSULATED_TIN_CABLE = Ic2rBlocksWiring.INSULATED_TIN_CABLE;
	public static final RegistryObject<Block> CROP_STICK = Ic2rBlocksCrops.CROP_STICK;
	public static final RegistryObject<Block> WEED_CROP = Ic2rBlocksCrops.WEED_CROP;
	public static final RegistryObject<Block> WHEAT_CROP = Ic2rBlocksCrops.WHEAT_CROP;
	public static final RegistryObject<Block> CARROTS_CROP = Ic2rBlocksCrops.CARROTS_CROP;
	public static final RegistryObject<Block> POTATO_CROP = Ic2rBlocksCrops.POTATO_CROP;
	public static final RegistryObject<Block> BEETROOTS_CROP = Ic2rBlocksCrops.BEETROOTS_CROP;
	public static final RegistryObject<Block> PUMPKIN_CROP = Ic2rBlocksCrops.PUMPKIN_CROP;
	public static final RegistryObject<Block> MELON_CROP = Ic2rBlocksCrops.MELON_CROP;
	public static final RegistryObject<Block> DANDELION_CROP = Ic2rBlocksCrops.DANDELION_CROP;
	public static final RegistryObject<Block> POPPY_CROP = Ic2rBlocksCrops.POPPY_CROP;
	public static final RegistryObject<Block> BLACKTHORN_CROP = Ic2rBlocksCrops.BLACKTHORN_CROP;
	public static final RegistryObject<Block> TULIP_CROP = Ic2rBlocksCrops.TULIP_CROP;
	public static final RegistryObject<Block> CYAZINT_CROP = Ic2rBlocksCrops.CYAZINT_CROP;
	public static final RegistryObject<Block> VENOMILIA_CROP = Ic2rBlocksCrops.VENOMILIA_CROP;
	public static final RegistryObject<Block> REED_CROP = Ic2rBlocksCrops.REED_CROP;
	public static final RegistryObject<Block> STICKY_REED_CROP = Ic2rBlocksCrops.STICKY_REED_CROP;
	public static final RegistryObject<Block> COCOA_CROP = Ic2rBlocksCrops.COCOA_CROP;
	public static final RegistryObject<Block> FLAX_CROP = Ic2rBlocksCrops.FLAX_CROP;
	public static final RegistryObject<Block> RED_MUSHROOM_CROP = Ic2rBlocksCrops.RED_MUSHROOM_CROP;
	public static final RegistryObject<Block> BROWN_MUSHROOM_CROP = Ic2rBlocksCrops.BROWN_MUSHROOM_CROP;
	public static final RegistryObject<Block> NETHER_WART_CROP = Ic2rBlocksCrops.NETHER_WART_CROP;
	public static final RegistryObject<Block> TERRA_WART_CROP = Ic2rBlocksCrops.TERRA_WART_CROP;
	public static final RegistryObject<Block> OAK_SAPLING_CROP = Ic2rBlocksCrops.OAK_SAPLING_CROP;
	public static final RegistryObject<Block> SPRUCE_SAPLING_CROP = Ic2rBlocksCrops.SPRUCE_SAPLING_CROP;
	public static final RegistryObject<Block> BIRCH_SAPLING_CROP = Ic2rBlocksCrops.BIRCH_SAPLING_CROP;
	public static final RegistryObject<Block> JUNGLE_SAPLING_CROP = Ic2rBlocksCrops.JUNGLE_SAPLING_CROP;
	public static final RegistryObject<Block> ACACIA_SAPLING_CROP = Ic2rBlocksCrops.ACACIA_SAPLING_CROP;
	public static final RegistryObject<Block> DARK_OAK_SAPLING_CROP = Ic2rBlocksCrops.DARK_OAK_SAPLING_CROP;
	public static final RegistryObject<Block> FERRU_CROP = Ic2rBlocksCrops.FERRU_CROP;
	public static final RegistryObject<Block> CYPRIUM_CROP = Ic2rBlocksCrops.CYPRIUM_CROP;
	public static final RegistryObject<Block> STAGNIUM_CROP = Ic2rBlocksCrops.STAGNIUM_CROP;
	public static final RegistryObject<Block> PLUMBISCUS_CROP = Ic2rBlocksCrops.PLUMBISCUS_CROP;
	public static final RegistryObject<Block> AURELIA_CROP = Ic2rBlocksCrops.AURELIA_CROP;
	public static final RegistryObject<Block> SHINING_CROP = Ic2rBlocksCrops.SHINING_CROP;
	public static final RegistryObject<Block> RED_WHEAT_CROP = Ic2rBlocksCrops.RED_WHEAT_CROP;
	public static final RegistryObject<Block> COFFEE_CROP = Ic2rBlocksCrops.COFFEE_CROP;
	public static final RegistryObject<Block> HOPS_CROP = Ic2rBlocksCrops.HOPS_CROP;
	public static final RegistryObject<Block> EATING_PLANT_CROP = Ic2rBlocksCrops.EATING_PLANT_CROP;
	public static final RegistryObject<Block> BLAZEREED_CROP = Ic2rBlocksCrops.BLAZEREED_CROP;
	public static final RegistryObject<Block> BOBS_YER_UNCLE_RANKS_BERRIES_CROP = Ic2rBlocksCrops.BOBS_YER_UNCLE_RANKS_BERRIES_CROP;
	public static final RegistryObject<Block> CORIUM_CROP = Ic2rBlocksCrops.CORIUM_CROP;
	public static final RegistryObject<Block> CORPSE_PLANT_CROP = Ic2rBlocksCrops.CORPSE_PLANT_CROP;
	public static final RegistryObject<Block> CREEPER_WEED_CROP = Ic2rBlocksCrops.CREEPER_WEED_CROP;
	public static final RegistryObject<Block> DIAREED_CROP = Ic2rBlocksCrops.DIAREED_CROP;
	public static final RegistryObject<Block> EGG_PLANT_CROP = Ic2rBlocksCrops.EGG_PLANT_CROP;
	public static final RegistryObject<Block> ENDER_BLOSSOM_CROP = Ic2rBlocksCrops.ENDER_BLOSSOM_CROP;
	public static final RegistryObject<Block> MEAT_ROSE_CROP = Ic2rBlocksCrops.MEAT_ROSE_CROP;
	public static final RegistryObject<Block> MILK_WART_CROP = Ic2rBlocksCrops.MILK_WART_CROP;
	public static final RegistryObject<Block> OIL_BERRIES_CROP = Ic2rBlocksCrops.OIL_BERRIES_CROP;
	public static final RegistryObject<Block> SLIME_PLANT_CROP = Ic2rBlocksCrops.SLIME_PLANT_CROP;
	public static final RegistryObject<Block> SPIDERNIP_CROP = Ic2rBlocksCrops.SPIDERNIP_CROP;
	public static final RegistryObject<Block> TEARSTALKS_CROP = Ic2rBlocksCrops.TEARSTALKS_CROP;
	public static final RegistryObject<Block> WITHEREED_CROP = Ic2rBlocksCrops.WITHEREED_CROP;

	public static void init()
	{
	}


	public static boolean never(BlockState state, BlockGetter world, BlockPos pos)
	{
		return false;
	}

	public static Boolean never(BlockState state, BlockGetter world, BlockPos pos, EntityType<?> type)
	{
		return false;
	}

	public static Boolean canSpawnOnLeaves(BlockState state, BlockGetter world, BlockPos pos, EntityType<?> type)
	{
		return type == EntityType.OCELOT || type == EntityType.PARROT;
	}
}

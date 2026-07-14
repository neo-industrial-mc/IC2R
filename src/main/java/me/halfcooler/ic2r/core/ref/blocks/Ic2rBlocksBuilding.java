package me.halfcooler.ic2r.core.ref.blocks;

import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.ref.Ic2rSignType;

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
import me.halfcooler.ic2r.core.block.steam.BlockRefractoryBricks;
import me.halfcooler.ic2r.core.block.steam.TileEntityCokeKiln;
import me.halfcooler.ic2r.core.block.steam.TileEntityCokeKilnGrate;
import me.halfcooler.ic2r.core.block.steam.TileEntityCokeKilnHatch;
import me.halfcooler.ic2r.core.block.heatgenerator.tileentity.TileEntityFluidHeatGenerator;
import me.halfcooler.ic2r.core.block.heatgenerator.tileentity.TileEntityRTHeatGenerator;
import me.halfcooler.ic2r.core.block.heatgenerator.tileentity.TileEntitySolidHeatGenerator;
import me.halfcooler.ic2r.core.block.inherit.Ic2rFenceBlock;
import me.halfcooler.ic2r.core.block.inherit.Ic2rGlassBlock;
import me.halfcooler.ic2r.core.block.inherit.Ic2rSheetBlock;
import me.halfcooler.ic2r.core.block.inherit.Ic2rSignBlock;
import me.halfcooler.ic2r.core.block.inherit.Ic2rWallSignBlock;
import me.halfcooler.ic2r.core.block.kineticgenerator.tileentity.TileEntityElectricKineticGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.tileentity.TileEntityManualKineticGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.tileentity.TileEntitySteamKineticGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.tileentity.TileEntityStirlingKineticGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.tileentity.TileEntityWaterKineticGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.tileentity.TileEntityWindKineticGenerator;
import me.halfcooler.ic2r.core.block.BlockDynamite;
import me.halfcooler.ic2r.core.block.machine.MiningPipeBlock;
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
import me.halfcooler.ic2r.core.block.misc.FoamBlock;
import me.halfcooler.ic2r.core.block.misc.RubberLogBlock;
import me.halfcooler.ic2r.core.block.misc.RubberWoodBlock;
import me.halfcooler.ic2r.core.block.misc.WallBlock;
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
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntityBlock;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityWall;
import me.halfcooler.ic2r.core.block.wiring.CableBlock;
import me.halfcooler.ic2r.core.block.wiring.CableType;
import me.halfcooler.ic2r.core.block.wiring.DetectorCableBlock;
import me.halfcooler.ic2r.core.block.wiring.DetectorFoamCableBlock;
import me.halfcooler.ic2r.core.block.wiring.FoamCableBlock;
import me.halfcooler.ic2r.core.block.wiring.SplitterCableBlock;
import me.halfcooler.ic2r.core.block.wiring.SplitterFoamCableBlock;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityChargePadBatBox;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityLuminator;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityChargePadCESU;
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
import me.halfcooler.ic2r.core.crop.Ic2rCropType;
import me.halfcooler.ic2r.core.crop.TileEntityCrop;
import me.halfcooler.ic2r.core.util.Util;
import net.minecraft.core.BlockPos;

import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.PressurePlateBlock.Sensitivity;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;


import net.minecraft.world.level.material.MapColor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.NotNull;
import net.minecraftforge.registries.RegistryObject;
import me.halfcooler.ic2r.forge.EnvProxyForge;

/** Domain block registrations: rubber wood, sheets, walls, foam, explosives, building blocks */
public final class Ic2rBlocksBuilding
{
	private Ic2rBlocksBuilding()
	{
	}

	public static final RegistryObject<LeavesBlock> RUBBER_LEAVES = EnvProxyForge.BLOCKS.register("rubber_leaves", () -> new LeavesBlock(Properties.of().strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion().isValidSpawn(Ic2rBlocks::canSpawnOnLeaves).isSuffocating(Ic2rBlocks::never).isViewBlocking(Ic2rBlocks::never)));
	public static final RegistryObject<RubberLogBlock> RUBBER_LOG = EnvProxyForge.BLOCKS.register("rubber_log", () -> new RubberLogBlock(Properties.of().mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Axis.Y ? MapColor.PODZOL : MapColor.COLOR_BROWN).randomTicks().strength(2.0F, 3.0f).sound(SoundType.WOOD)));
	public static final RegistryObject<RotatedPillarBlock> STRIPPED_RUBBER_LOG = EnvProxyForge.BLOCKS.register("stripped_rubber_log", () -> new RotatedPillarBlock(Properties.of().mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Axis.Y ? MapColor.PODZOL : MapColor.COLOR_BROWN).strength(2.0F, 3.0f).sound(SoundType.WOOD)));
	public static final RegistryObject<RubberWoodBlock> RUBBER_WOOD = EnvProxyForge.BLOCKS.register("rubber_wood", () -> new RubberWoodBlock(Properties.of().mapColor(MapColor.COLOR_BROWN).strength(2.0F, 3.0f).sound(SoundType.WOOD)));
	public static final RegistryObject<Block> STRIPPED_RUBBER_WOOD = EnvProxyForge.BLOCKS.register("stripped_rubber_wood", () -> new Block(Properties.of().mapColor(MapColor.PODZOL).strength(2.0F, 3.0f).sound(SoundType.WOOD)));
	public static final RegistryObject<Block> RUBBER_SAPLING = EnvProxyForge.BLOCKS.register("rubber_sapling", () -> new SaplingBlock(new AbstractTreeGrower()
	{
		protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(@NotNull RandomSource random, boolean bees)
		{
			return ResourceKey.create(Registries.CONFIGURED_FEATURE, IC2R.getIdentifier("rubber_tree"));
		}
	}, Properties.of().noCollission().randomTicks().instabreak().sound(SoundType.GRASS))
	{
	});
	public static final RegistryObject<Block> RUBBER_PLANKS = EnvProxyForge.BLOCKS.register("rubber_planks", () -> new Block(Properties.of().mapColor(MapColor.PODZOL).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
	public static final RegistryObject<Block> RUBBER_BUTTON = EnvProxyForge.BLOCKS.register("rubber_button", () -> new ButtonBlock(Properties.of().noCollission().strength(0.5F).sound(SoundType.WOOD), BlockSetType.OAK, 30, false)
	{
	});
	public static final RegistryObject<Block> RUBBER_DOOR = EnvProxyForge.BLOCKS.register("rubber_door", () -> new DoorBlock(Properties.of().mapColor(MapColor.PODZOL).strength(3.0F).sound(SoundType.WOOD).noOcclusion(), BlockSetType.OAK)
	{
	});
	public static final RegistryObject<Block> RUBBER_FENCE = EnvProxyForge.BLOCKS.register("rubber_fence", () -> new FenceBlock(Properties.of().mapColor(MapColor.PODZOL).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
	public static final RegistryObject<Block> RUBBER_FENCE_GATE = EnvProxyForge.BLOCKS.register("rubber_fence_gate", () -> new FenceGateBlock(Properties.of().mapColor(MapColor.PODZOL).strength(2.0F, 3.0F).sound(SoundType.WOOD), WoodType.OAK));
	public static final RegistryObject<Block> RUBBER_PRESSURE_PLATE = EnvProxyForge.BLOCKS.register("rubber_pressure_plate", () -> new PressurePlateBlock(Sensitivity.EVERYTHING, Properties.of().mapColor(MapColor.PODZOL).noCollission().strength(0.5F).sound(SoundType.WOOD), BlockSetType.OAK)
	{
	});
	public static final RegistryObject<Block> RUBBER_SIGN = EnvProxyForge.BLOCKS.register("rubber_sign", () -> new Ic2rSignBlock(Properties.of().mapColor(MapColor.PODZOL).noCollission().strength(1.0F).sound(SoundType.WOOD), Ic2rSignType.RUBBER));
	public static final RegistryObject<Block> RUBBER_WALL_SIGN = EnvProxyForge.BLOCKS.register("rubber_wall_sign", () -> new Ic2rWallSignBlock(Properties.of().mapColor(MapColor.PODZOL).noCollission().strength(1.0F).sound(SoundType.WOOD).lootFrom(BuiltInRegistries.BLOCK.getResourceKey(RUBBER_SIGN.get()).flatMap(BuiltInRegistries.BLOCK::getHolder).orElseThrow()), Ic2rSignType.RUBBER));
	public static final RegistryObject<Block> RUBBER_SLAB = EnvProxyForge.BLOCKS.register("rubber_slab", () -> new SlabBlock(Properties.of().mapColor(MapColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
	public static final RegistryObject<Block> RUBBER_STAIRS = EnvProxyForge.BLOCKS.register("rubber_stairs", () -> new StairBlock(() -> RUBBER_PLANKS.get().defaultBlockState(), Properties.of().mapColor(MapColor.PODZOL).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	{
	});
	public static final RegistryObject<Block> RUBBER_TRAPDOOR = EnvProxyForge.BLOCKS.register("rubber_trapdoor", () -> new TrapDoorBlock(Properties.of().mapColor(MapColor.PODZOL).strength(3.0F).sound(SoundType.WOOD).noOcclusion().isValidSpawn(Ic2rBlocks::never), BlockSetType.OAK)
	{
	});
	public static final RegistryObject<Block> IRON_FENCE = EnvProxyForge.BLOCKS.register("iron_fence", () -> new Ic2rFenceBlock(Properties.of().strength(5.0F, 10.0F), true));
	public static final RegistryObject<Block> RESIN_SHEET = EnvProxyForge.BLOCKS.register("resin_sheet", () -> new Ic2rSheetBlock(Properties.of().strength(1.6F, 0.5F)));
	public static final RegistryObject<Block> RUBBER_SHEET = EnvProxyForge.BLOCKS.register("rubber_sheet", () -> new Ic2rSheetBlock(Properties.of().strength(0.8F, 2.0F)));
	public static final RegistryObject<Block> WOOL_SHEET = EnvProxyForge.BLOCKS.register("wool_sheet", () -> new Ic2rSheetBlock(Properties.of().strength(0.8F, 0.8F)));
	public static final RegistryObject<Block> REINFORCED_GLASS = EnvProxyForge.BLOCKS.register("reinforced_glass", () -> new Ic2rGlassBlock(Properties.of().noOcclusion().strength(5.0F, 180.0F).sound(SoundType.GLASS).isValidSpawn((state, world, pos, type) -> false)));
	public static final RegistryObject<Block> FOAM = EnvProxyForge.BLOCKS.register("foam", () -> new FoamBlock(Properties.of().noOcclusion().strength(0.01F, 10.0F).randomTicks().sound(SoundType.WOOL)));
	public static final RegistryObject<Block> MINING_PIPE = EnvProxyForge.BLOCKS.register("mining_pipe", () -> new MiningPipeBlock(Properties.of().strength(6.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
	public static final RegistryObject<Block> MINING_PIPE_TIP = EnvProxyForge.BLOCKS.register("mining_pipe_tip", () -> new Block(Properties.of().strength(6.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
	public static final RegistryObject<Block> REINFORCED_DOOR = EnvProxyForge.BLOCKS.register("reinforced_door", () -> new DoorBlock(Properties.of().strength(50.0F, 150.0F).sound(SoundType.METAL), BlockSetType.IRON)
	{
	});
	public static final RegistryObject<Block> ITNT = EnvProxyForge.BLOCKS.register("itnt", () -> Ic2rTileEntityBlock.create(Properties.of().strength(0.0F, 0.0F).sound(SoundType.GRASS), TileEntityITnt.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false));
	public static final RegistryObject<Block> NUKE = EnvProxyForge.BLOCKS.register("nuke", () -> Ic2rTileEntityBlock.create(Properties.of().strength(0.0F, 0.0F).sound(SoundType.GRASS), TileEntityNuke.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false));
	public static final RegistryObject<Block> DYNAMITE = EnvProxyForge.BLOCKS.register("dynamite", () -> new BlockDynamite());
	private static final Properties wallSettings = Properties.of().strength(3.0F, 30.0F).requiresCorrectToolForDrops().sound(SoundType.STONE);
	public static final RegistryObject<Block> WHITE_WALL = EnvProxyForge.BLOCKS.register("white_wall", () -> new WallBlock(wallSettings, DyeColor.WHITE));
	public static final RegistryObject<Block> ORANGE_WALL = EnvProxyForge.BLOCKS.register("orange_wall", () -> new WallBlock(wallSettings, DyeColor.ORANGE));
	public static final RegistryObject<Block> MAGENTA_WALL = EnvProxyForge.BLOCKS.register("magenta_wall", () -> new WallBlock(wallSettings, DyeColor.MAGENTA));
	public static final RegistryObject<Block> LIGHT_BLUE_WALL = EnvProxyForge.BLOCKS.register("light_blue_wall", () -> new WallBlock(wallSettings, DyeColor.LIGHT_BLUE));
	public static final RegistryObject<Block> YELLOW_WALL = EnvProxyForge.BLOCKS.register("yellow_wall", () -> new WallBlock(wallSettings, DyeColor.YELLOW));
	public static final RegistryObject<Block> LIME_WALL = EnvProxyForge.BLOCKS.register("lime_wall", () -> new WallBlock(wallSettings, DyeColor.LIME));
	public static final RegistryObject<Block> PINK_WALL = EnvProxyForge.BLOCKS.register("pink_wall", () -> new WallBlock(wallSettings, DyeColor.PINK));
	public static final RegistryObject<Block> GRAY_WALL = EnvProxyForge.BLOCKS.register("gray_wall", () -> new WallBlock(wallSettings, DyeColor.GRAY));
	public static final RegistryObject<Block> LIGHT_GRAY_WALL = EnvProxyForge.BLOCKS.register("light_gray_wall", () -> new WallBlock(wallSettings, DyeColor.LIGHT_GRAY));
	public static final RegistryObject<Block> CYAN_WALL = EnvProxyForge.BLOCKS.register("cyan_wall", () -> new WallBlock(wallSettings, DyeColor.CYAN));
	public static final RegistryObject<Block> PURPLE_WALL = EnvProxyForge.BLOCKS.register("purple_wall", () -> new WallBlock(wallSettings, DyeColor.PURPLE));
	public static final RegistryObject<Block> BLUE_WALL = EnvProxyForge.BLOCKS.register("blue_wall", () -> new WallBlock(wallSettings, DyeColor.BLUE));
	public static final RegistryObject<Block> BROWN_WALL = EnvProxyForge.BLOCKS.register("brown_wall", () -> new WallBlock(wallSettings, DyeColor.BROWN));
	public static final RegistryObject<Block> GREEN_WALL = EnvProxyForge.BLOCKS.register("green_wall", () -> new WallBlock(wallSettings, DyeColor.GREEN));
	public static final RegistryObject<Block> RED_WALL = EnvProxyForge.BLOCKS.register("red_wall", () -> new WallBlock(wallSettings, DyeColor.RED));
	public static final RegistryObject<Block> BLACK_WALL = EnvProxyForge.BLOCKS.register("black_wall", () -> new WallBlock(wallSettings, DyeColor.BLACK));
	public static final RegistryObject<Block> OBSCURED_WALL = EnvProxyForge.BLOCKS.register("obscured_wall", () -> Ic2rTileEntityBlock.create(wallSettings, TileEntityWall.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false));

}

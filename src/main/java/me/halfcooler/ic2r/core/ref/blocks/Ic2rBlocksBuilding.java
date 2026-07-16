package me.halfcooler.ic2r.core.ref.blocks;

import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.ref.Ic2rSignType;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.block.inherit.Ic2rFenceBlock;
import me.halfcooler.ic2r.core.block.inherit.Ic2rGlassBlock;
import me.halfcooler.ic2r.core.block.inherit.Ic2rSheetBlock;
import me.halfcooler.ic2r.core.block.inherit.Ic2rSignBlock;
import me.halfcooler.ic2r.core.block.inherit.Ic2rWallSignBlock;
import me.halfcooler.ic2r.core.block.BlockDynamite;
import me.halfcooler.ic2r.core.block.machine.MiningPipeBlock;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityITnt;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityNuke;
import me.halfcooler.ic2r.core.block.misc.FoamBlock;
import me.halfcooler.ic2r.core.block.misc.RubberLogBlock;
import me.halfcooler.ic2r.core.block.misc.RubberWoodBlock;
import me.halfcooler.ic2r.core.block.misc.WallBlock;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntityBlock;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityWall;
import me.halfcooler.ic2r.core.util.Util;

import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.*;

import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;


import net.minecraft.world.level.material.MapColor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.NotNull;
import me.halfcooler.ic2r.forge.EnvProxyForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Domain block registrations: rubber wood, sheets, walls, foam, explosives, building blocks */
public final class Ic2rBlocksBuilding
{
	private Ic2rBlocksBuilding()
	{
	}

	public static final DeferredHolder<Block, LeavesBlock> RUBBER_LEAVES = EnvProxyForge.BLOCKS.register("rubber_leaves", () -> new LeavesBlock(Properties.of().strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion().isValidSpawn(Ic2rBlocks::canSpawnOnLeaves).isSuffocating(Ic2rBlocks::never).isViewBlocking(Ic2rBlocks::never)));
	public static final DeferredHolder<Block, RubberLogBlock> RUBBER_LOG = EnvProxyForge.BLOCKS.register("rubber_log", () -> new RubberLogBlock(Properties.of().mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Axis.Y ? MapColor.PODZOL : MapColor.COLOR_BROWN).randomTicks().strength(2.0F, 3.0f).sound(SoundType.WOOD)));
	public static final DeferredHolder<Block, RotatedPillarBlock> STRIPPED_RUBBER_LOG = EnvProxyForge.BLOCKS.register("stripped_rubber_log", () -> new RotatedPillarBlock(Properties.of().mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Axis.Y ? MapColor.PODZOL : MapColor.COLOR_BROWN).strength(2.0F, 3.0f).sound(SoundType.WOOD)));
	public static final DeferredHolder<Block, RubberWoodBlock> RUBBER_WOOD = EnvProxyForge.BLOCKS.register("rubber_wood", () -> new RubberWoodBlock(Properties.of().mapColor(MapColor.COLOR_BROWN).strength(2.0F, 3.0f).sound(SoundType.WOOD)));
	public static final DeferredHolder<Block, Block> STRIPPED_RUBBER_WOOD = EnvProxyForge.BLOCKS.register("stripped_rubber_wood", () -> new Block(Properties.of().mapColor(MapColor.PODZOL).strength(2.0F, 3.0f).sound(SoundType.WOOD)));
	private static final TreeGrower RUBBER_TREE_GROWER = new TreeGrower(
		"ic2r_rubber",
		java.util.Optional.empty(),
		java.util.Optional.of(ResourceKey.create(Registries.CONFIGURED_FEATURE, IC2R.getIdentifier("rubber_tree"))),
		java.util.Optional.empty()
	);
	public static final DeferredHolder<Block, Block> RUBBER_SAPLING = EnvProxyForge.BLOCKS.register("rubber_sapling", () -> new SaplingBlock(RUBBER_TREE_GROWER, Properties.of().noCollission().randomTicks().instabreak().sound(SoundType.GRASS)));
	public static final DeferredHolder<Block, Block> RUBBER_PLANKS = EnvProxyForge.BLOCKS.register("rubber_planks", () -> new Block(Properties.of().mapColor(MapColor.PODZOL).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
	public static final DeferredHolder<Block, Block> RUBBER_BUTTON = EnvProxyForge.BLOCKS.register("rubber_button", () -> new ButtonBlock(BlockSetType.OAK, 30, Properties.of().noCollission().strength(0.5F).sound(SoundType.WOOD)));
	public static final DeferredHolder<Block, Block> RUBBER_DOOR = EnvProxyForge.BLOCKS.register("rubber_door", () -> new DoorBlock(BlockSetType.OAK, Properties.of().mapColor(MapColor.PODZOL).strength(3.0F).sound(SoundType.WOOD).noOcclusion()));
	public static final DeferredHolder<Block, Block> RUBBER_FENCE = EnvProxyForge.BLOCKS.register("rubber_fence", () -> new FenceBlock(Properties.of().mapColor(MapColor.PODZOL).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
	public static final DeferredHolder<Block, Block> RUBBER_FENCE_GATE = EnvProxyForge.BLOCKS.register("rubber_fence_gate", () -> new FenceGateBlock(WoodType.OAK, Properties.of().mapColor(MapColor.PODZOL).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
	public static final DeferredHolder<Block, Block> RUBBER_PRESSURE_PLATE = EnvProxyForge.BLOCKS.register("rubber_pressure_plate", () -> new PressurePlateBlock(BlockSetType.OAK, Properties.of().mapColor(MapColor.PODZOL).noCollission().strength(0.5F).sound(SoundType.WOOD)));
	public static final DeferredHolder<Block, Block> RUBBER_SIGN = EnvProxyForge.BLOCKS.register("rubber_sign", () -> new Ic2rSignBlock(Properties.of().mapColor(MapColor.PODZOL).noCollission().strength(1.0F).sound(SoundType.WOOD), Ic2rSignType.RUBBER));
	public static final DeferredHolder<Block, Block> RUBBER_WALL_SIGN = EnvProxyForge.BLOCKS.register("rubber_wall_sign", () -> new Ic2rWallSignBlock(Properties.of().mapColor(MapColor.PODZOL).noCollission().strength(1.0F).sound(SoundType.WOOD).lootFrom(RUBBER_SIGN), Ic2rSignType.RUBBER));
	public static final DeferredHolder<Block, Block> RUBBER_SLAB = EnvProxyForge.BLOCKS.register("rubber_slab", () -> new SlabBlock(Properties.of().mapColor(MapColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
	public static final DeferredHolder<Block, Block> RUBBER_STAIRS = EnvProxyForge.BLOCKS.register("rubber_stairs", () -> new StairBlock(RUBBER_PLANKS.get().defaultBlockState(), Properties.of().mapColor(MapColor.PODZOL).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
	public static final DeferredHolder<Block, Block> RUBBER_TRAPDOOR = EnvProxyForge.BLOCKS.register("rubber_trapdoor", () -> new TrapDoorBlock(BlockSetType.OAK, Properties.of().mapColor(MapColor.PODZOL).strength(3.0F).sound(SoundType.WOOD).noOcclusion().isValidSpawn(Ic2rBlocks::never)));
	public static final DeferredHolder<Block, Block> IRON_FENCE = EnvProxyForge.BLOCKS.register("iron_fence", () -> new Ic2rFenceBlock(Properties.of().strength(5.0F, 10.0F), true));
	public static final DeferredHolder<Block, Block> RESIN_SHEET = EnvProxyForge.BLOCKS.register("resin_sheet", () -> new Ic2rSheetBlock(Properties.of().strength(1.6F, 0.5F)));
	public static final DeferredHolder<Block, Block> RUBBER_SHEET = EnvProxyForge.BLOCKS.register("rubber_sheet", () -> new Ic2rSheetBlock(Properties.of().strength(0.8F, 2.0F)));
	public static final DeferredHolder<Block, Block> WOOL_SHEET = EnvProxyForge.BLOCKS.register("wool_sheet", () -> new Ic2rSheetBlock(Properties.of().strength(0.8F, 0.8F)));
	public static final DeferredHolder<Block, Block> REINFORCED_GLASS = EnvProxyForge.BLOCKS.register("reinforced_glass", () -> new Ic2rGlassBlock(Properties.of().noOcclusion().strength(5.0F, 180.0F).sound(SoundType.GLASS).isValidSpawn((state, world, pos, type) -> false)));
	public static final DeferredHolder<Block, Block> FOAM = EnvProxyForge.BLOCKS.register("foam", () -> new FoamBlock(Properties.of().noOcclusion().strength(0.01F, 10.0F).randomTicks().sound(SoundType.WOOL)));
	public static final DeferredHolder<Block, Block> MINING_PIPE = EnvProxyForge.BLOCKS.register("mining_pipe", () -> new MiningPipeBlock(Properties.of().strength(6.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
	public static final DeferredHolder<Block, Block> MINING_PIPE_TIP = EnvProxyForge.BLOCKS.register("mining_pipe_tip", () -> new Block(Properties.of().strength(6.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
	public static final DeferredHolder<Block, Block> REINFORCED_DOOR = EnvProxyForge.BLOCKS.register("reinforced_door", () -> new DoorBlock(BlockSetType.IRON, Properties.of().strength(50.0F, 150.0F).sound(SoundType.METAL)));
	public static final DeferredHolder<Block, Block> ITNT = EnvProxyForge.BLOCKS.register("itnt", () -> Ic2rTileEntityBlock.create(Properties.of().strength(0.0F, 0.0F).sound(SoundType.GRASS), TileEntityITnt.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false));
	public static final DeferredHolder<Block, Block> NUKE = EnvProxyForge.BLOCKS.register("nuke", () -> Ic2rTileEntityBlock.create(Properties.of().strength(0.0F, 0.0F).sound(SoundType.GRASS), TileEntityNuke.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false));
	public static final DeferredHolder<Block, Block> DYNAMITE = EnvProxyForge.BLOCKS.register("dynamite", () -> new BlockDynamite());
	private static final Properties wallSettings = Properties.of().strength(3.0F, 30.0F).requiresCorrectToolForDrops().sound(SoundType.STONE);
	public static final DeferredHolder<Block, Block> WHITE_WALL = EnvProxyForge.BLOCKS.register("white_wall", () -> new WallBlock(wallSettings, DyeColor.WHITE));
	public static final DeferredHolder<Block, Block> ORANGE_WALL = EnvProxyForge.BLOCKS.register("orange_wall", () -> new WallBlock(wallSettings, DyeColor.ORANGE));
	public static final DeferredHolder<Block, Block> MAGENTA_WALL = EnvProxyForge.BLOCKS.register("magenta_wall", () -> new WallBlock(wallSettings, DyeColor.MAGENTA));
	public static final DeferredHolder<Block, Block> LIGHT_BLUE_WALL = EnvProxyForge.BLOCKS.register("light_blue_wall", () -> new WallBlock(wallSettings, DyeColor.LIGHT_BLUE));
	public static final DeferredHolder<Block, Block> YELLOW_WALL = EnvProxyForge.BLOCKS.register("yellow_wall", () -> new WallBlock(wallSettings, DyeColor.YELLOW));
	public static final DeferredHolder<Block, Block> LIME_WALL = EnvProxyForge.BLOCKS.register("lime_wall", () -> new WallBlock(wallSettings, DyeColor.LIME));
	public static final DeferredHolder<Block, Block> PINK_WALL = EnvProxyForge.BLOCKS.register("pink_wall", () -> new WallBlock(wallSettings, DyeColor.PINK));
	public static final DeferredHolder<Block, Block> GRAY_WALL = EnvProxyForge.BLOCKS.register("gray_wall", () -> new WallBlock(wallSettings, DyeColor.GRAY));
	public static final DeferredHolder<Block, Block> LIGHT_GRAY_WALL = EnvProxyForge.BLOCKS.register("light_gray_wall", () -> new WallBlock(wallSettings, DyeColor.LIGHT_GRAY));
	public static final DeferredHolder<Block, Block> CYAN_WALL = EnvProxyForge.BLOCKS.register("cyan_wall", () -> new WallBlock(wallSettings, DyeColor.CYAN));
	public static final DeferredHolder<Block, Block> PURPLE_WALL = EnvProxyForge.BLOCKS.register("purple_wall", () -> new WallBlock(wallSettings, DyeColor.PURPLE));
	public static final DeferredHolder<Block, Block> BLUE_WALL = EnvProxyForge.BLOCKS.register("blue_wall", () -> new WallBlock(wallSettings, DyeColor.BLUE));
	public static final DeferredHolder<Block, Block> BROWN_WALL = EnvProxyForge.BLOCKS.register("brown_wall", () -> new WallBlock(wallSettings, DyeColor.BROWN));
	public static final DeferredHolder<Block, Block> GREEN_WALL = EnvProxyForge.BLOCKS.register("green_wall", () -> new WallBlock(wallSettings, DyeColor.GREEN));
	public static final DeferredHolder<Block, Block> RED_WALL = EnvProxyForge.BLOCKS.register("red_wall", () -> new WallBlock(wallSettings, DyeColor.RED));
	public static final DeferredHolder<Block, Block> BLACK_WALL = EnvProxyForge.BLOCKS.register("black_wall", () -> new WallBlock(wallSettings, DyeColor.BLACK));
	public static final DeferredHolder<Block, Block> OBSCURED_WALL = EnvProxyForge.BLOCKS.register("obscured_wall", () -> Ic2rTileEntityBlock.create(wallSettings, TileEntityWall.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false));

}

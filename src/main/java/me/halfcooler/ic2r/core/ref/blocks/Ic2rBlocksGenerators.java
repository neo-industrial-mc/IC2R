package me.halfcooler.ic2r.core.ref.blocks;

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
import me.halfcooler.ic2r.core.block.heatgenerator.tileentity.TileEntityFluidHeatGenerator;
import me.halfcooler.ic2r.core.block.heatgenerator.tileentity.TileEntityRTHeatGenerator;
import me.halfcooler.ic2r.core.block.heatgenerator.tileentity.TileEntitySolidHeatGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.tileentity.TileEntityElectricKineticGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.tileentity.TileEntityManualKineticGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.tileentity.TileEntitySteamKineticGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.tileentity.TileEntityStirlingKineticGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.tileentity.TileEntityWaterKineticGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.tileentity.TileEntityWindKineticGenerator;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntityBlock;
import me.halfcooler.ic2r.core.util.Util;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;


import net.minecraft.world.level.material.MapColor;
import me.halfcooler.ic2r.forge.EnvProxyForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Domain block registrations: EU / heat / kinetic generators */
public final class Ic2rBlocksGenerators
{
	private Ic2rBlocksGenerators()
	{
	}

	public static final DeferredHolder<Block, Block> GENERATOR = EnvProxyForge.BLOCKS.register("generator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityGenerator.class, true, Ic2rTileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, true));
	public static final DeferredHolder<Block, Block> GEO_GENERATOR = EnvProxyForge.BLOCKS.register("geo_generator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityGeoGenerator.class, true, Ic2rTileEntityBlock.DefaultDrop.Generator, Util.horizontalFacings, true));
	public static final DeferredHolder<Block, Block> KINETIC_GENERATOR = EnvProxyForge.BLOCKS.register("kinetic_generator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityKineticGenerator.class, true, Ic2rTileEntityBlock.DefaultDrop.Generator, Util.allFacings, true));
	public static final DeferredHolder<Block, Block> RT_GENERATOR = EnvProxyForge.BLOCKS.register("rt_generator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityRTGenerator.class, true, Ic2rTileEntityBlock.DefaultDrop.Generator, Util.noFacings, false));
	public static final DeferredHolder<Block, Block> SEMIFLUID_GENERATOR = EnvProxyForge.BLOCKS.register("semifluid_generator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntitySemifluidGenerator.class, true, Ic2rTileEntityBlock.DefaultDrop.Generator, Util.horizontalFacings, true));
	public static final DeferredHolder<Block, Block> SOLAR_GENERATOR = EnvProxyForge.BLOCKS.register("solar_generator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntitySolarGenerator.class, true, Ic2rTileEntityBlock.DefaultDrop.Generator, Util.horizontalFacings, true));
	public static final DeferredHolder<Block, Block> STIRLING_GENERATOR = EnvProxyForge.BLOCKS.register("stirling_generator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityStirlingGenerator.class, true, Ic2rTileEntityBlock.DefaultDrop.Generator, Util.allFacings, true));
	public static final DeferredHolder<Block, Block> WATER_GENERATOR = EnvProxyForge.BLOCKS.register("water_generator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityWaterGenerator.class, true, Ic2rTileEntityBlock.DefaultDrop.Generator, Util.horizontalFacings, true));
	public static final DeferredHolder<Block, Block> WIND_GENERATOR = EnvProxyForge.BLOCKS.register("wind_generator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityWindGenerator.class, true, Ic2rTileEntityBlock.DefaultDrop.Generator, Util.horizontalFacings, true));
	public static final DeferredHolder<Block, Block> ELECTRIC_HEAT_GENERATOR = EnvProxyForge.BLOCKS.register("electric_heat_generator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityElectricHeatGenerator.class, true, Ic2rTileEntityBlock.DefaultDrop.Self, Util.allFacings, true));
	public static final DeferredHolder<Block, Block> FLUID_HEAT_GENERATOR = EnvProxyForge.BLOCKS.register("fluid_heat_generator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityFluidHeatGenerator.class, true, Ic2rTileEntityBlock.DefaultDrop.Self, Util.allFacings, true));
	public static final DeferredHolder<Block, Block> RT_HEAT_GENERATOR = EnvProxyForge.BLOCKS.register("rt_heat_generator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityRTHeatGenerator.class, true, Ic2rTileEntityBlock.DefaultDrop.Self, Util.allFacings, true));
	public static final DeferredHolder<Block, Block> SOLID_HEAT_GENERATOR = EnvProxyForge.BLOCKS.register("solid_heat_generator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntitySolidHeatGenerator.class, true, Ic2rTileEntityBlock.DefaultDrop.Self, Util.allFacings, true));
	public static final DeferredHolder<Block, Block> ELECTRIC_KINETIC_GENERATOR = EnvProxyForge.BLOCKS.register("electric_kinetic_generator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityElectricKineticGenerator.class, true, Ic2rTileEntityBlock.DefaultDrop.Self, Util.allFacings, true));
	public static final DeferredHolder<Block, Block> MANUAL_KINETIC_GENERATOR = EnvProxyForge.BLOCKS.register("manual_kinetic_generator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityManualKineticGenerator.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.allFacings, true));
	public static final DeferredHolder<Block, Block> STEAM_KINETIC_GENERATOR = EnvProxyForge.BLOCKS.register("steam_kinetic_generator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntitySteamKineticGenerator.class, true, Ic2rTileEntityBlock.DefaultDrop.Self, Util.allFacings, true));
	public static final DeferredHolder<Block, Block> STIRLING_KINETIC_GENERATOR = EnvProxyForge.BLOCKS.register("stirling_kinetic_generator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityStirlingKineticGenerator.class, true, Ic2rTileEntityBlock.DefaultDrop.Self, Util.allFacings, true));
	public static final DeferredHolder<Block, Block> WATER_KINETIC_GENERATOR = EnvProxyForge.BLOCKS.register("water_kinetic_generator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityWaterKineticGenerator.class, true, Ic2rTileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, true));
	public static final DeferredHolder<Block, Block> WIND_KINETIC_GENERATOR = EnvProxyForge.BLOCKS.register("wind_kinetic_generator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityWindKineticGenerator.class, true, Ic2rTileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, true));
	public static final DeferredHolder<Block, Block> CREATIVE_GENERATOR = EnvProxyForge.BLOCKS.register("creative_generator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(-1.0F, Float.POSITIVE_INFINITY).sound(SoundType.METAL), TileEntityCreativeGenerator.class, true, Ic2rTileEntityBlock.DefaultDrop.None, Util.noFacings, false));
}

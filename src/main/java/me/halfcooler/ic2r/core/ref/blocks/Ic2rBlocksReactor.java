package me.halfcooler.ic2r.core.ref.blocks;

import me.halfcooler.ic2r.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import me.halfcooler.ic2r.core.block.reactor.tileentity.TileEntityRCI_LZH;
import me.halfcooler.ic2r.core.block.reactor.tileentity.TileEntityRCI_RSH;
import me.halfcooler.ic2r.core.block.reactor.tileentity.TileEntityReactorAccessHatch;
import me.halfcooler.ic2r.core.block.reactor.tileentity.TileEntityReactorChamberElectric;
import me.halfcooler.ic2r.core.block.reactor.tileentity.TileEntityReactorFluidPort;
import me.halfcooler.ic2r.core.block.reactor.tileentity.TileEntityReactorRedstonePort;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntityBlock;
import me.halfcooler.ic2r.core.util.Util;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;


import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;
import me.halfcooler.ic2r.forge.EnvProxyForge;

/** Domain block registrations: nuclear reactor and RCI blocks */
public final class Ic2rBlocksReactor
{
	private Ic2rBlocksReactor()
	{
	}

	public static final RegistryObject<Block> NUCLEAR_REACTOR = EnvProxyForge.BLOCKS.register("nuclear_reactor", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityNuclearReactorElectric.class, true, Ic2rTileEntityBlock.DefaultDrop.Generator, Util.horizontalFacings, true));
	public static final RegistryObject<Block> REACTOR_ACCESS_HATCH = EnvProxyForge.BLOCKS.register("reactor_access_hatch", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(40.0F, 90.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityReactorAccessHatch.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.onlyNorth, false));
	public static final RegistryObject<Block> REACTOR_CHAMBER = EnvProxyForge.BLOCKS.register("reactor_chamber", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityReactorChamberElectric.class, false, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.onlyNorth, true));
	public static final RegistryObject<Block> REACTOR_FLUID_PORT = EnvProxyForge.BLOCKS.register("reactor_fluid_port", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(40.0F, 90.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityReactorFluidPort.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.onlyNorth, false));
	public static final RegistryObject<Block> REACTOR_REDSTONE_PORT = EnvProxyForge.BLOCKS.register("reactor_redstone_port", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(40.0F, 90.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityReactorRedstonePort.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.onlyNorth, false));
	public static final RegistryObject<Block> RCI_RSH = EnvProxyForge.BLOCKS.register("rci_rsh", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityRCI_RSH.class, true, Ic2rTileEntityBlock.DefaultDrop.AdvMachine, Util.allFacings, true));
	public static final RegistryObject<Block> RCI_LZH = EnvProxyForge.BLOCKS.register("rci_lzh", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityRCI_LZH.class, true, Ic2rTileEntityBlock.DefaultDrop.AdvMachine, Util.allFacings, true));
}

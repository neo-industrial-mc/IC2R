package me.halfcooler.ic2r.core.ref.blocks;

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
import me.halfcooler.ic2r.core.util.Util;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;


import net.minecraft.world.level.material.MapColor;
import me.halfcooler.ic2r.forge.EnvProxyForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Domain block registrations: storage boxes and metal tanks */
public final class Ic2rBlocksStorage
{
	private Ic2rBlocksStorage()
	{
	}

	public static final DeferredHolder<Block, Block> WOODEN_STORAGE_BOX = EnvProxyForge.BLOCKS.register("wooden_storage_box", () -> Ic2rTileEntityBlock.create(Properties.of().strength(1.0F, 10.0F).sound(SoundType.WOOD), TileEntityWoodenStorageBox.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false));
	public static final DeferredHolder<Block, Block> IRON_STORAGE_BOX = EnvProxyForge.BLOCKS.register("iron_storage_box", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(1.0F, 15.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityIronStorageBox.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false));
	public static final DeferredHolder<Block, Block> BRONZE_STORAGE_BOX = EnvProxyForge.BLOCKS.register("bronze_storage_box", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(1.0F, 15.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityBronzeStorageBox.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false));
	public static final DeferredHolder<Block, Block> STEEL_STORAGE_BOX = EnvProxyForge.BLOCKS.register("steel_storage_box", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 20.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntitySteelStorageBox.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false));
	public static final DeferredHolder<Block, Block> IRIDIUM_STORAGE_BOX = EnvProxyForge.BLOCKS.register("iridium_storage_box", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(3.0F, 100.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityIridiumStorageBox.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false));
	public static final DeferredHolder<Block, Block> BRONZE_TANK = EnvProxyForge.BLOCKS.register("bronze_tank", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(3.0F, 15.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityBronzeTank.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false));
	public static final DeferredHolder<Block, Block> IRON_TANK = EnvProxyForge.BLOCKS.register("iron_tank", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(3.0F, 15.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityIronTank.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false));
	public static final DeferredHolder<Block, Block> STEEL_TANK = EnvProxyForge.BLOCKS.register("steel_tank", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(4.0F, 20.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntitySteelTank.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false));
	public static final DeferredHolder<Block, Block> IRIDIUM_TANK = EnvProxyForge.BLOCKS.register("iridium_tank", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(5.0F, 100.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityIridiumTank.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false));
}

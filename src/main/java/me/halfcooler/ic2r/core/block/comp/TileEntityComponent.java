package me.halfcooler.ic2r.core.block.comp;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntity;
import me.halfcooler.ic2r.core.network.GrowingBuffer;

import java.io.DataInput;
import java.io.IOException;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;

public abstract class TileEntityComponent
{
	protected final Ic2rTileEntity parent;

	public TileEntityComponent(Ic2rTileEntity parent)
	{
		this.parent = parent;
	}

	public Ic2rTileEntity getParent()
	{
		return this.parent;
	}

	public void readFromNbt(CompoundTag nbt)
	{
	}

	public CompoundTag writeToNbt()
	{
		return null;
	}

	public void onLoaded()
	{
	}

	public void onUnloaded()
	{
	}

	public void onNeighborChange(Block srcBlock, BlockPos srcPos)
	{
	}

	public void onContainerUpdate(ServerPlayer player)
	{
	}

	public void onNetworkUpdate(DataInput is) throws IOException
	{
	}

	public boolean enableWorldTick()
	{
		return false;
	}

	public void onWorldTick()
	{
	}

	protected void setNetworkUpdate(ServerPlayer player, GrowingBuffer data)
	{
		IC2R.network.get(true).sendComponentUpdate(this.parent, Components.getId(this.getClass()), player, data);
	}
}

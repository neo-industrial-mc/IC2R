package me.halfcooler.ic2r.core.block.generator.tileentity;

import me.halfcooler.ic2r.api.energy.EnergyNet;
import me.halfcooler.ic2r.api.energy.tile.IEnergyAcceptor;
import me.halfcooler.ic2r.api.energy.tile.IMultiEnergySource;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntity;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityCreativeGenerator extends Ic2rTileEntity implements IMultiEnergySource
{
	public TileEntityCreativeGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.CREATIVE_GENERATOR, pos, state);
	}

	@Override
	public double getOfferedEnergy()
	{
		return Double.POSITIVE_INFINITY;
	}

	@Override
	public void drawEnergy(double amount)
	{
	}

	@Override
	public int getSourceTier()
	{
		return 1;
	}

	@Override
	public boolean emitsEnergyTo(IEnergyAcceptor receiver, Direction side)
	{
		return true;
	}

	@Override
	public boolean sendMultipleEnergyPackets()
	{
		return true;
	}

	@Override
	public int getMultipleEnergyPacketAmount()
	{
		return 10;
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (!this.getLevel().isClientSide)
		{
			EnergyNet.instance.addBlockEntityTile(this);
		}
	}

	@Override
	protected void onUnloaded()
	{
		if (!this.getLevel().isClientSide)
		{
			EnergyNet.instance.removeTile(this);
		}

		super.onUnloaded();
	}
}

package me.halfcooler.ic2r.core.block.generator.tileentity;

import me.halfcooler.ic2r.api.energy.tile.IHeatSource;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityStirlingGenerator extends TileEntityConversionGenerator
{
	private final double productionpeerheat = 0.5F * IC2RConfig.balance.energy.generator.stirling.get().floatValue();
	protected IHeatSource source;

	public TileEntityStirlingGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.STIRLING_GENERATOR, pos, state);
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		this.updateSource();
	}

	@Override
	protected void setFacing(Level world, Direction facing)
	{
		super.setFacing(world, facing);
		this.updateSource();
	}

	@Override
	protected void onNeighborChange(Block neighbor, BlockPos neighborPos)
	{
		super.onNeighborChange(neighbor, neighborPos);
		if (this.getBlockPos().relative(this.getFacing()).equals(neighborPos))
		{
			this.updateSource();
		}
	}

	protected void updateSource()
	{
		BlockEntity te = this.level.getBlockEntity(this.worldPosition.relative(this.getFacing()));
		if (te instanceof IHeatSource heatSource && !te.isRemoved())
		{
			this.source = heatSource;
		} else
		{
			this.source = null;
		}
	}

	@Override
	protected int getEnergyAvailable()
	{
		if (this.source == null)
		{
			return 0;
		}

		assert !((BlockEntity) this.source).isRemoved();
		return this.source.drawHeat(this.getFacing().getOpposite(), this.source.getConnectionBandwidth(this.getFacing().getOpposite()), true);
	}

	@Override
	protected void drawEnergyAvailable(int amount)
	{
		if (this.source != null)
		{
			assert !((BlockEntity) this.source).isRemoved();
			this.source.drawHeat(this.getFacing().getOpposite(), amount, false);
		} else
		{
			assert false;
		}
	}

	@Override
	protected double getMultiplier()
	{
		return this.productionpeerheat;
	}
}

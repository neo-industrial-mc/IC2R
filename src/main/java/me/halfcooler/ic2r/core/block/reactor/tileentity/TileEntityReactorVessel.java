package me.halfcooler.ic2r.core.block.reactor.tileentity;

import me.halfcooler.ic2r.api.reactor.IReactorChamber;
import me.halfcooler.ic2r.core.block.comp.FluidReactorLookup;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityReactorVessel extends Ic2rTileEntity implements IReactorChamber
{
	protected final FluidReactorLookup lookup = this.addComponent(new FluidReactorLookup(this));

	protected TileEntityReactorVessel(BlockEntityType<? extends TileEntityReactorVessel> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	public TileEntityNuclearReactorElectric getReactorInstance()
	{
		return this.lookup.getReactor();
	}

	@Override
	public boolean isWall()
	{
		return true;
	}
}

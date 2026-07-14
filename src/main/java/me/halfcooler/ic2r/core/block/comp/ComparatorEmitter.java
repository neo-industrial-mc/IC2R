package me.halfcooler.ic2r.core.block.comp;

import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntity;

public class ComparatorEmitter extends BasicRedstoneComponent
{
	public ComparatorEmitter(Ic2rTileEntity parent)
	{
		super(parent);
	}

	@Override
	public void onChange()
	{
		this.parent.getLevel().updateNeighbourForOutputSignal(this.parent.getBlockPos(), this.parent.getBlockState().getBlock());
	}
}

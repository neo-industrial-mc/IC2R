package me.halfcooler.ic2r.core.block.comp;

import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntity;

public class RedstoneEmitter extends BasicRedstoneComponent
{
	public RedstoneEmitter(Ic2rTileEntity parent)
	{
		super(parent);
	}

	@Override
	public void onChange()
	{
		this.parent.getLevel().updateNeighborsAt(this.parent.getBlockPos(), this.parent.getBlockState().getBlock());
	}
}

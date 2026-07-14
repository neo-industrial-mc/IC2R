package me.halfcooler.ic2r.core.item;

import net.minecraft.world.level.block.Block;

public class BlockItemEnergyStorage extends ItemBlockIc2r
{
	public final int maxEnergy;

	public BlockItemEnergyStorage(Block block, Properties properties, int maxEnergy)
	{
		super(block, properties);
		this.maxEnergy = maxEnergy;
	}
}

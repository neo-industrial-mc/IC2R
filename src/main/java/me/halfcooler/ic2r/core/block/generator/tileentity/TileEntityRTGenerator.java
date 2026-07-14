package me.halfcooler.ic2r.core.block.generator.tileentity;

import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumable;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableItemStack;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityRTGenerator extends TileEntityBaseGenerator
{
	public final InvSlotConsumable fuelSlot = new InvSlotConsumableItemStack(this, "fuel", 6, new ItemStack(Ic2rItems.RTG_PELLET));

	public TileEntityRTGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.RT_GENERATOR, pos, state, Math.round(16.0F * IC2RConfig.balance.energy.generator.radioisotope.get().floatValue()), 1, 20000);
		this.fuelSlot.setStackSizeLimit(1);
	}

	@Override
	public boolean gainEnergy()
	{
		int counter = 0;

		for (int i = 0; i < this.fuelSlot.size(); i++)
		{
			if (!this.fuelSlot.isEmpty(i))
			{
				counter++;
			}
		}

		if (counter == 0)
		{
			return false;
		}

		this.energy.addEnergy(Math.pow(2.0, counter - 1) * IC2RConfig.balance.energy.generator.radioisotope.get().floatValue());
		return true;
	}

	@Override
	public boolean gainFuel()
	{
		return false;
	}

	@Override
	public boolean needsFuel()
	{
		return false;
	}

	@Override
	protected boolean delayActiveUpdate()
	{
		return true;
	}
}

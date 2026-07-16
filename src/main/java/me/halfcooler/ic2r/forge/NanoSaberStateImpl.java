package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.api.item.INanoSaberState;
import me.halfcooler.ic2r.core.util.StackUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

final class NanoSaberStateImpl implements INanoSaberState
{
	private static final String NBT_ACTIVE = "active";

	private final ItemStack stack;
	private int energyTick;

	NanoSaberStateImpl(ItemStack stack)
	{
		this.stack = stack;
	}

	@Override
	public boolean isActive()
	{
		CompoundTag nbt = StackUtil.getTag(stack);
		return nbt != null && nbt.getBoolean(NBT_ACTIVE);
	}

	@Override
	public void setActive(boolean active)
	{
		StackUtil.getOrCreateNbtData(this.stack).putBoolean(NBT_ACTIVE, active);
	}

	@Override
	public int getEnergyTick()
	{
		return this.energyTick;
	}

	@Override
	public void setEnergyTick(int energyTick)
	{
		this.energyTick = energyTick;
	}
}
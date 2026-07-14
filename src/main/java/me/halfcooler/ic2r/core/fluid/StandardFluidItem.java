package me.halfcooler.ic2r.core.fluid;

import me.halfcooler.ic2r.core.util.StackUtil;
import me.halfcooler.ic2r.core.util.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.Mutable;

public interface StandardFluidItem extends Ic2rFluidItem
{
	static Ic2rFluidStack getFs(ItemStack stack)
	{
		CompoundTag nbt = stack.getTag();
		if (nbt == null)
		{
			return null;
		} else
		{
			return !nbt.contains("Fluid", 10) ? null : Ic2rFluidStack.read(nbt.getCompound("Fluid"));
		}
	}

	static void setFs(ItemStack stack, Ic2rFluidStack fs)
	{
		CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);

		if (fs != null && !fs.isEmpty())
		{
			CompoundTag fsNbt = new CompoundTag();
			fs.toNbt(fsNbt);
			nbt.put("Fluid", fsNbt);
		} else
		{
			nbt.remove("Fluid");
		}
	}

	default boolean canDrain(ItemStack stack)
	{
		return true;
	}

	default boolean canFill(ItemStack stack, Ic2rFluidStack fs)
	{
		return true;
	}

	@Override
	default Ic2rFluidStack getFluidStack(ItemStack stack)
	{
		Ic2rFluidStack fs = getFs(stack);
		return fs != null && !fs.isEmpty() ? fs : Ic2rFluidStack.EMPTY;
	}

	@Override
	default Ic2rFluidStack drainMb(ItemStack stack, int amount, boolean simulate, Mutable<ItemStack> newStack)
	{
		if (newStack != null)
		{
			newStack.setValue(stack);
		}

		if (amount < 0)
		{
			throw new IllegalArgumentException("negative amount");
		}

		if (amount == 0)
		{
			return Ic2rFluidStack.EMPTY;
		}

		if (stack.getCount() != 1)
		{
			throw new IllegalArgumentException("invalid stack size");
		}

		if (!this.canDrain(stack))
		{
			return Ic2rFluidStack.EMPTY;
		}

		Ic2rFluidStack fs = getFs(stack);
		if (fs != null && !fs.isEmpty())
		{
			if (fs.getAmountMb() <= amount)
			{
				amount = fs.getAmountMb();
				if (!simulate)
				{
					setFs(stack, null);
					this.updateDamage(stack, 0);
				}
			} else
			{
				if (!simulate)
				{
					fs.decreaseMb(amount);
					setFs(stack, fs);
					this.updateDamage(stack, fs.getAmountMb());
				}

				fs.setAmountMb(amount);
			}

			if (newStack != null)
			{
				newStack.setValue(stack);
			}

			return fs;
		} else
		{
			return Ic2rFluidStack.EMPTY;
		}
	}

	@Override
	default int drainMb(ItemStack stack, Ic2rFluidStack drainFs, boolean simulate, Mutable<ItemStack> newStack)
	{
		if (newStack != null)
		{
			newStack.setValue(stack);
		}

		if (drainFs == null)
		{
			throw new IllegalArgumentException("invalid drain medium");
		}

		if (drainFs.isEmpty())
		{
			return 0;
		}

		if (stack.getCount() != 1)
		{
			throw new IllegalArgumentException("invalid stack size");
		}

		if (!this.canDrain(stack))
		{
			return 0;
		}

		Ic2rFluidStack fs = getFs(stack);
		if (fs != null && !fs.isEmpty() && fs.hasExactFluid(drainFs))
		{
			int amount = drainFs.getAmountMb();
			if (fs.getAmountMb() <= amount)
			{
				amount = fs.getAmountMb();
				if (!simulate)
				{
					setFs(stack, null);
					this.updateDamage(stack, 0);
				}
			} else if (!simulate)
			{
				fs.decreaseMb(amount);
				setFs(stack, fs);
				this.updateDamage(stack, fs.getAmountMb());
			}

			if (newStack != null)
			{
				newStack.setValue(stack);
			}

			return amount;
		} else
		{
			return 0;
		}
	}

	@Override
	default int fillMb(ItemStack stack, Ic2rFluidStack fillFs, boolean simulate, Mutable<ItemStack> newStack)
	{
		if (newStack != null)
		{
			newStack.setValue(stack);
		}

		if (fillFs == null)
		{
			throw new IllegalArgumentException("invalid fill medium");
		}

		if (fillFs.isEmpty())
		{
			return 0;
		}

		if (stack.getCount() != 1)
		{
			throw new IllegalArgumentException("invalid stack size");
		}

		if (!this.canFill(stack, fillFs))
		{
			return 0;
		}

		int capacity = this.getCapacityMb(stack);
		if (capacity <= 0)
		{
			return 0;
		}

		int amount = fillFs.getAmountMb();
		Ic2rFluidStack fs = getFs(stack);
		if (fs == null || fs.isEmpty() && !fs.hasExactFluid(fillFs))
		{
			amount = Math.min(amount, capacity);
			if (!simulate)
			{
				fs = fillFs.copyWithAmountMb(amount);
				setFs(stack, fs);
				this.updateDamage(stack, amount);
			}
		} else
		{
			if (!fs.hasExactFluid(fillFs))
			{
				return 0;
			}

			amount = Math.min(amount, capacity - fs.getAmountMb());
			if (!simulate)
			{
				fs.increaseMb(amount);
				setFs(stack, fs);
				this.updateDamage(stack, fs.getAmountMb());
			}
		}

		if (newStack != null)
		{
			newStack.setValue(stack);
		}

		return amount;
	}

	default void updateDamage(ItemStack stack, int amount)
	{
		if (stack.isDamageableItem())
		{
			int maxDmg = stack.getMaxDamage();
			if (maxDmg > 2)
			{
				stack.setDamageValue(maxDmg - 1 - (int) Util.map(amount, this.getCapacityMb(stack), maxDmg - 2));
			}
		}
	}
}

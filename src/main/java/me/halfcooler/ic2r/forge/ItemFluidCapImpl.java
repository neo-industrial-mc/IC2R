package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.fluid.Ic2rFluidItem;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.apache.commons.lang3.mutable.Mutable;
import org.jetbrains.annotations.NotNull;

final class ItemFluidCapImpl implements ICapabilityProvider, IFluidHandlerItem, NonNullSupplier<IFluidHandlerItem>, Mutable<ItemStack>
{
	private ItemStack stack;

	public ItemFluidCapImpl(ItemStack stack)
	{
		this.stack = stack;
	}

	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, Direction facing)
	{
		return capability == ForgeCapabilities.FLUID_HANDLER_ITEM ? (LazyOptional<T>) LazyOptional.of(this) : LazyOptional.empty();
	}

	@Override
	public int getTanks()
	{
		return 1;
	}

	@Override
	public int getTankCapacity(int tank)
	{
		if (tank != 0)
		{
			return 0;
		}

		Ic2rFluidItem parent = (Ic2rFluidItem) this.stack.getItem();
		return parent.getCapacityMb(this.stack);
	}

	@Override
	public @NotNull FluidStack getFluidInTank(int tank)
	{
		if (tank != 0)
		{
			return FluidStack.EMPTY;
		}

		Ic2rFluidItem parent = (Ic2rFluidItem) this.stack.getItem();
		Ic2rFluidStack fs = parent.drainMb(this.stack, Integer.MAX_VALUE, true, null);
		return EnvFluidHandlerForge.getForgeFs(fs);
	}

	@Override
	public boolean isFluidValid(int tank, @NotNull FluidStack fs)
	{
		return tank == 0;
	}

	@Override
	public @NotNull FluidStack drain(int amount, IFluidHandler.FluidAction action)
	{
		if (amount > 0 && this.stack.getCount() == 1)
		{
			Ic2rFluidItem parent = (Ic2rFluidItem) this.stack.getItem();
			return EnvFluidHandlerForge.getForgeFs(parent.drainMb(this.stack, amount, action.simulate(), this));
		} else
		{
			return FluidStack.EMPTY;
		}
	}

	@Override
	public @NotNull FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action)
	{
		if (resource != null && !resource.isEmpty() && this.stack.getCount() == 1)
		{
			Ic2rFluidItem parent = (Ic2rFluidItem) this.stack.getItem();
			int amount = parent.drainMb(this.stack, new Ic2rFluidStackImpl(resource), action.simulate(), this);
			if (amount <= 0)
			{
				return FluidStack.EMPTY;
			}

			resource = resource.copy();
			resource.setAmount(amount);
			return resource;
		} else
		{
			return FluidStack.EMPTY;
		}
	}

	@Override
	public int fill(FluidStack resource, IFluidHandler.FluidAction action)
	{
		if (resource != null && !resource.isEmpty() && this.stack.getCount() == 1)
		{
			Ic2rFluidItem parent = (Ic2rFluidItem) this.stack.getItem();
			return parent.fillMb(this.stack, new Ic2rFluidStackImpl(resource), action.simulate(), this);
		} else
		{
			return 0;
		}
	}

	@Override
	public @NotNull ItemStack getContainer()
	{
		return this.stack;
	}

	public @NotNull IFluidHandlerItem get()
	{
		return this;
	}

	public ItemStack getValue()
	{
		return this.stack;
	}

	public void setValue(ItemStack value)
	{
		this.stack = value;
	}
}

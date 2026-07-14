package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.fluid.FluidTankInfo;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidBlock;
import me.halfcooler.ic2r.core.util.Util;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

final class BlockFluidCapImpl implements ICapabilityProvider
{
	private final Ic2rFluidBlock parent;
	private final BlockEntity be;
	private final LazyOptional<IFluidHandler>[] sides;

	public BlockFluidCapImpl(Ic2rFluidBlock fluidBlock, BlockEntity be)
	{
		this.parent = fluidBlock;
		this.be = be;
		this.sides = new LazyOptional[Util.ALL_DIRS.length];

		for (Direction dir : Util.ALL_DIRS)
		{
			this.sides[dir.ordinal()] = LazyOptional.of(new BlockFluidCapImpl.SideHandler(dir));
		}
	}

	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, Direction facing)
	{
		if (capability != ForgeCapabilities.FLUID_HANDLER)
		{
			return LazyOptional.empty();
		}

		if (facing == null)
		{
			return (LazyOptional<T>) this.sides[0];
		}

		return (LazyOptional<T>) this.sides[facing.ordinal()];
	}

	private class SideHandler implements IFluidHandler, NonNullSupplier<IFluidHandler>
	{
		private final Direction side;

		SideHandler(Direction side)
		{
			this.side = side;
		}

		@Override
		public int getTanks()
		{
			FluidTankInfo[] infos = BlockFluidCapImpl.this.parent.getTankInfos(null, null, null, BlockFluidCapImpl.this.be);
			if (infos == null)
			{
				return 0;
			}

			int sideMaskReq = 1 << this.side.ordinal();
			int ret = 0;

			for (FluidTankInfo info : infos)
			{
				if (((info.drainSideMask() | info.fillSideMask()) & sideMaskReq) != 0)
				{
					ret++;
				}
			}

			return ret;
		}

		private FluidTankInfo getTankInfo(int tank)
		{
			FluidTankInfo[] infos = BlockFluidCapImpl.this.parent.getTankInfos(null, null, null, BlockFluidCapImpl.this.be);
			if (infos == null)
			{
				return null;
			}

			int sideMaskReq = 1 << this.side.ordinal();

			for (FluidTankInfo info : infos)
			{
				if (((info.drainSideMask() | info.fillSideMask()) & sideMaskReq) != 0 && tank-- == 0)
				{
					return info;
				}
			}

			return null;
		}

		@Override
		public int getTankCapacity(int tank)
		{
			FluidTankInfo info = this.getTankInfo(tank);
			return info != null ? info.capacity() : 0;
		}

		@Override
		public @NotNull FluidStack getFluidInTank(int tank)
		{
			FluidTankInfo info = this.getTankInfo(tank);
			return info != null ? EnvFluidHandlerForge.getForgeFs(info.content()) : FluidStack.EMPTY;
		}

		@Override
		public boolean isFluidValid(int tank, @NotNull FluidStack fs)
		{
			FluidTankInfo info = this.getTankInfo(tank);
			return info != null && info.capacity() > 0;
		}

		@Override
		public @NotNull FluidStack drain(int amount, IFluidHandler.FluidAction action)
		{
			return amount <= 0
				? FluidStack.EMPTY
				: EnvFluidHandlerForge.getForgeFs(
				BlockFluidCapImpl.this.parent.drainMb(null, null, null, BlockFluidCapImpl.this.be, this.side, amount, action.simulate())
			);
		}

		@Override
		public @NotNull FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action)
		{
			if (resource != null && !resource.isEmpty())
			{
				int amount = BlockFluidCapImpl.this.parent
					.drainMb(null, null, null, BlockFluidCapImpl.this.be, this.side, new Ic2rFluidStackImpl(resource), action.simulate());
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
			return resource != null && !resource.isEmpty()
				? BlockFluidCapImpl.this.parent.fillMb(null, null, null, BlockFluidCapImpl.this.be, this.side, new Ic2rFluidStackImpl(resource), action.simulate())
				: 0;
		}

		public @NotNull IFluidHandler get()
		{
			return this;
		}
	}
}

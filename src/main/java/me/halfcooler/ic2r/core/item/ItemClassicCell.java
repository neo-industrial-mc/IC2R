package me.halfcooler.ic2r.core.item;

import me.halfcooler.ic2r.core.crop.TileEntityCrop;
import me.halfcooler.ic2r.core.fluid.FluidHandler;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidItem;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.fluid.StandardFluidItem;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;
import me.halfcooler.ic2r.core.util.LiquidUtil;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemClassicCell extends Ic2rBucketItem implements Ic2rFluidItem
{
	private static final int CELL_CAPACITY_MB = 1000;
	private static final Map<Fluid, ItemClassicCell> instances = new IdentityHashMap<>();
	private final Fluid fluid;

	/**
	 * @param fluid still fluid contained by this dedicated cell, or {@link Fluids#EMPTY} for
	 *              {@code facade_cell}. Must not be null — non-fluid “cells” must be plain items
	 *              (null fluid + BucketItem/Forge fluid caps crashes GTCEu tooltips).
	 */
	public ItemClassicCell(Properties settings, Fluid fluid)
	{
		super(Objects.requireNonNull(fluid, "ItemClassicCell fluid must not be null; use Fluids.EMPTY or a plain Item"), settings);
		this.fluid = fluid;
		if (fluid != Fluids.EMPTY)
		{
			instances.put(fluid, this);
		}
	}

	/**
	 * Returns the dedicated cell item for a fluid, or null if none is registered.
	 */
	@Nullable
	public static ItemClassicCell getInstance(Fluid fluid)
	{
		return fluid == null || fluid == Fluids.EMPTY ? null : instances.get(fluid);
	}

	/**
	 * Builds a filled cell stack for the given still fluid.
	 * Uses a dedicated cell item when one exists; otherwise stores the fluid as NBT on {@code facade_cell}
	 * (same idea as AE2 cable facades: one item + many variants).
	 */
	public static ItemStack createFilledStack(Fluid fluid)
	{
		if (fluid == null || fluid == Fluids.EMPTY)
		{
			return ItemStack.EMPTY;
		}

		ItemClassicCell dedicated = instances.get(fluid);
		if (dedicated != null)
		{
			return new ItemStack(dedicated);
		}

		ItemStack stack = new ItemStack(Ic2rItems.FACADE_CELL);
		StandardFluidItem.setFs(stack, Ic2rFluidStack.create(fluid, CELL_CAPACITY_MB));
		return stack;
	}

	@Override
	public Component getName(@NotNull ItemStack stack)
	{
		if (this.fluid == Fluids.EMPTY)
		{
			Ic2rFluidStack stored = StandardFluidItem.getFs(stack);
			if (stored != null && !stored.isEmpty())
			{
				return Component.translatable("ic2r.item.fluid_cell.filled", stored.getFluidDisplayName());
			}
		}

		return super.getName(stack);
	}

	@Override
	public Item getEmptiedBucketItem()
	{
		return Ic2rItems.FACADE_CELL;
	}

	/**
	 * Dedicated cells use their fixed fluid; {@code facade_cell} reads fluid from NBT when filled.
	 */
	@Override
	protected Fluid getContainedFluid(ItemStack stack)
	{
		if (this.fluid != Fluids.EMPTY)
		{
			return this.fluid;
		}

		Ic2rFluidStack stored = StandardFluidItem.getFs(stack);
		if (stored != null && !stored.isEmpty())
		{
			return stored.getFluid();
		}

		return Fluids.EMPTY;
	}

	@Override
	public List<Fluid> getDrainableFluidList()
	{
		return this.fluid == Fluids.EMPTY ? LiquidUtil.getAllFluidsSorted() : List.copyOf(instances.keySet());
	}

	@Override
	public Item getBucketItem(Fluid fluid)
	{
		ItemClassicCell cell = instances.get(fluid);
		if (cell != null)
		{
			return cell;
		}
		// Universal path: return facade_cell; NBT is filled by tryDrainFluid/fillMb for empty facade.
		return Ic2rItems.FACADE_CELL;
	}

	@Override
	public boolean bucketUseOnBlock(UseOnContext context)
	{
		BlockEntity be;
		return (this == Ic2rItems.WATER_CELL || this == Ic2rItems.WEED_EX_CELL)
			&& (be = context.getLevel().getBlockEntity(context.getClickedPos())) instanceof TileEntityCrop
			&& this.useOnCrop(context.getItemInHand(), (TileEntityCrop) be, true);
	}

	@Override
	public ItemStack tryDrainFluid(LevelAccessor world, BlockPos pos, BlockState state)
	{
		if (this.fluid != Fluids.EMPTY)
		{
			return super.tryDrainFluid(world, pos, state);
		}

		if (!(world instanceof Level level))
		{
			return ItemStack.EMPTY;
		}

		Ic2rFluidStack drained = FluidHandler.drainWorldFluid(state, level, pos, true);
		if (drained == null || drained.isEmpty())
		{
			if (state.getBlock() instanceof LiquidBlock && state.getValue(LiquidBlock.LEVEL) == 0)
			{
				drained = Ic2rFluidStack.create(state.getFluidState().getType(), CELL_CAPACITY_MB);
			} else if (state.getBlock() instanceof SimpleWaterloggedBlock && state.getValue(BlockStateProperties.WATERLOGGED))
			{
				drained = Ic2rFluidStack.create(Fluids.WATER, CELL_CAPACITY_MB);
			} else
			{
				return this.tryDrain(world, pos, state);
			}
		}

		if (drained.getAmountMb() < CELL_CAPACITY_MB)
		{
			return ItemStack.EMPTY;
		}

		ItemStack testStack = new ItemStack(this);
		if (this.fillMb(testStack, drained.copyWithAmountMb(CELL_CAPACITY_MB), true, null) < CELL_CAPACITY_MB)
		{
			return ItemStack.EMPTY;
		}

		Ic2rFluidStack actualDrain = FluidHandler.drainWorldFluid(state, level, pos, false);
		if (actualDrain == null || actualDrain.isEmpty())
		{
			if (state.getBlock() instanceof LiquidBlock && state.getValue(LiquidBlock.LEVEL) == 0)
			{
				if (!level.isClientSide())
				{
					world.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
				}

				actualDrain = drained;
			} else if (state.getBlock() instanceof SimpleWaterloggedBlock && state.getValue(BlockStateProperties.WATERLOGGED))
			{
				world.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, false), 3);
				if (!state.canSurvive(world, pos))
				{
					world.destroyBlock(pos, true);
				}

				actualDrain = Ic2rFluidStack.create(Fluids.WATER, CELL_CAPACITY_MB);
			} else
			{
				return ItemStack.EMPTY;
			}
		}

		if (actualDrain.getAmountMb() < CELL_CAPACITY_MB)
		{
			return ItemStack.EMPTY;
		}

		ItemStack result = new ItemStack(this);
		MutableObject<ItemStack> newStack = new MutableObject<>();
		this.fillMb(result, actualDrain.copyWithAmountMb(CELL_CAPACITY_MB), false, newStack);
		ItemStack filled = newStack.getValue() != null ? newStack.getValue() : result;
		return filled.isEmpty() ? ItemStack.EMPTY : filled;
	}

	public boolean useOnCrop(ItemStack stack, TileEntityCrop crop, boolean manual)
	{
		if (this == Ic2rItems.WATER_CELL)
		{
			if (crop.getStorageWater() < 10)
			{
				crop.setStorageWater(10);
				return true;
			}
		} else if (this == Ic2rItems.WEED_EX_CELL)
		{
			return crop.applyWeedEx(50, true, manual, false) > 0;
		}

		return false;
	}

	public void appendHoverText(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag advanced)
	{
		if (this.fluid == Fluids.EMPTY)
		{
			Ic2rFluidStack stored = StandardFluidItem.getFs(stack);
			if (stored != null && !stored.isEmpty())
			{
				Ic2rTooltip.add(
					tooltip,
					Component.translatable("ic2r.item.fluid_container.with_fluid", stored.getFluidDisplayName(), stored.getAmountMb())
				);
			}
		}
	}

	@Override
	public Ic2rFluidStack getFluidStack(ItemStack stack)
	{
		if (this.fluid == Fluids.EMPTY)
		{
			Ic2rFluidStack stored = StandardFluidItem.getFs(stack);
			return stored != null && !stored.isEmpty() ? stored : Ic2rFluidStack.EMPTY;
		}

		return Ic2rFluidStack.create(this.fluid, CELL_CAPACITY_MB);
	}

	@Override
	public int getCapacityMb(ItemStack stack)
	{
		return CELL_CAPACITY_MB;
	}

	@Override
	public Ic2rFluidStack drainMb(ItemStack stack, int amount, boolean simulate, Mutable<ItemStack> newStack)
	{
		if (newStack != null)
		{
			newStack.setValue(stack);
		}

		if (stack.getCount() != 1)
		{
			throw new IllegalArgumentException("invalid stack size");
		}

		if (this.fluid == Fluids.EMPTY)
		{
			Ic2rFluidStack stored = StandardFluidItem.getFs(stack);
			if (stored == null || stored.isEmpty())
			{
				return Ic2rFluidStack.EMPTY;
			}

			if (amount <= 0)
			{
				return Ic2rFluidStack.EMPTY;
			}

			if (simulate)
			{
				return stored.copyWithAmountMb(Math.min(amount, CELL_CAPACITY_MB));
			}

			StandardFluidItem.setFs(stack, null);
			if (newStack != null)
			{
				newStack.setValue(stack);
			}

			return stored;
		}

		if (amount <= 0)
		{
			return Ic2rFluidStack.EMPTY;
		}

		if (simulate)
		{
			return Ic2rFluidStack.create(this.fluid, Math.min(amount, CELL_CAPACITY_MB));
		}

		stack.shrink(1);

		if (newStack != null)
		{
			newStack.setValue(new ItemStack(Ic2rItems.FACADE_CELL));
		}

		return Ic2rFluidStack.create(this.fluid, CELL_CAPACITY_MB);
	}

	@Override
	public int drainMb(ItemStack stack, Ic2rFluidStack drainFs, boolean simulate, Mutable<ItemStack> newStack)
	{
		if (newStack != null)
		{
			newStack.setValue(stack);
		}

		if (stack.getCount() != 1)
		{
			throw new IllegalArgumentException("invalid stack size");
		}

		int amount = drainFs.getAmountMb();
		if (amount <= 0)
		{
			return 0;
		}

		if (this.fluid == Fluids.EMPTY)
		{
			Ic2rFluidStack stored = StandardFluidItem.getFs(stack);
			if (stored == null || stored.isEmpty() || !stored.hasExactFluid(drainFs))
			{
				return 0;
			}

			if (simulate)
			{
				return Math.min(amount, CELL_CAPACITY_MB);
			}

			StandardFluidItem.setFs(stack, null);
			if (newStack != null)
			{
				newStack.setValue(stack);
			}

			return CELL_CAPACITY_MB;
		}

		if (drainFs.hasExactFluid(this.fluid))
		{
			if (simulate)
			{
				return Math.min(amount, CELL_CAPACITY_MB);
			}

			stack.shrink(1);

			if (newStack != null)
			{
				newStack.setValue(new ItemStack(Ic2rItems.FACADE_CELL));
			}

			return CELL_CAPACITY_MB;
		}

		return 0;
	}

	@Override
	public int fillMb(ItemStack stack, Ic2rFluidStack fillFs, boolean simulate, Mutable<ItemStack> newStack)
	{
		if (newStack != null)
		{
			newStack.setValue(stack);
		}

		if (stack.getCount() != 1)
		{
			throw new IllegalArgumentException("invalid stack size");
		}

		if (fillFs.getAmountMb() < CELL_CAPACITY_MB)
		{
			return 0;
		}

		if (this.fluid != Fluids.EMPTY)
		{
			return 0;
		}

		ItemClassicCell newItem = instances.get(fillFs.getFluid());
		if (newItem != null && fillFs.hasExactFluid(newItem.fluid))
		{
			if (!simulate)
			{
				stack.shrink(1);
			}

			if (newStack != null)
			{
				newStack.setValue(new ItemStack(newItem));
			}

			return CELL_CAPACITY_MB;
		}

		Ic2rFluidStack stored = StandardFluidItem.getFs(stack);
		if (stored != null && !stored.isEmpty())
		{
			return 0;
		}

		if (!simulate)
		{
			StandardFluidItem.setFs(stack, fillFs.copyWithAmountMb(CELL_CAPACITY_MB));
		}

		if (newStack != null)
		{
			newStack.setValue(stack);
		}

		return CELL_CAPACITY_MB;
	}
}

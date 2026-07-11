package ic2.core.item;

import ic2.core.crop.TileEntityCrop;
import ic2.core.fluid.FluidHandler;
import ic2.core.fluid.Ic2FluidItem;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.StandardFluidItem;
import ic2.core.ref.Ic2Items;
import ic2.core.util.Ic2Tooltip;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;


import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.core.component.DataComponents;

public class ItemClassicCell extends Ic2BucketItem implements Ic2FluidItem
{
	private static final int CELL_CAPACITY_MB = 1000;
	private static final Map<Fluid, ItemClassicCell> instances = new IdentityHashMap<>();
	private final Fluid fluid;
	private final int charges;

	public ItemClassicCell(Properties settings, Fluid fluid, int charges)
	{
		super(fluid, settings);
		this.fluid = fluid;
		this.charges = charges;
		if (fluid != null && fluid != Fluids.EMPTY)
		{
			instances.put(fluid, this);
		}
	}

	@Override
	public Item getEmptiedBucketItem()
	{
		return Ic2Items.EMPTY_CELL;
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
		return Ic2Items.EMPTY_CELL;
	}

	@Override
	public boolean bucketUseOnBlock(UseOnContext context)
	{
		BlockEntity be;
		return (this == Ic2Items.WATER_CELL || this == Ic2Items.WEED_EX_CELL || this == Ic2Items.HYDRATION_CELL)
			&& (be = context.getLevel().getBlockEntity(context.getClickedPos())) instanceof TileEntityCrop
			&& this.useOnCrop(context.getItemInHand(), (TileEntityCrop) be, true);
	}

	@Override
	public boolean emptyContents(@Nullable Player player, Level world, BlockPos pos, @Nullable BlockHitResult hitResult)
	{
		return super.emptyContents(player, world, pos, hitResult);
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

		Ic2FluidStack drained = FluidHandler.drainWorldFluid(state, level, pos, true);
		if (drained == null || drained.isEmpty())
		{
			if (state.getBlock() instanceof LiquidBlock && state.getValue(LiquidBlock.LEVEL) == 0)
			{
				drained = Ic2FluidStack.create(state.getFluidState().getType(), CELL_CAPACITY_MB);
			} else if (state.getBlock() instanceof SimpleWaterloggedBlock && state.getValue(BlockStateProperties.WATERLOGGED))
			{
				drained = Ic2FluidStack.create(Fluids.WATER, CELL_CAPACITY_MB);
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

		Ic2FluidStack actualDrain = FluidHandler.drainWorldFluid(state, level, pos, false);
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

				actualDrain = Ic2FluidStack.create(Fluids.WATER, CELL_CAPACITY_MB);
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
		if (this == Ic2Items.WATER_CELL)
		{
			if (crop.getStorageWater() < 10)
			{
				crop.setStorageWater(10);
				return true;
			}
		} else if (this == Ic2Items.WEED_EX_CELL)
		{
			return crop.applyWeedEx(50, true, manual, false) > 0;
		} else if (this == Ic2Items.HYDRATION_CELL)
		{
			int consumed = this.getUsage(stack) + 1;
			int amount = Math.max(0, this.charges - consumed);
			if (!manual && amount > 180)
			{
				amount = 180;
			}

			amount = crop.applyHydration(amount, false);
			if (amount > 0)
			{
				consumed += amount;
				if (consumed >= this.charges)
				{
					stack = StackUtil.decSize(stack);
				} else
				{
					this.setUsage(stack, consumed);
				}

				return true;
			}
		}

		return false;
	}

	private int getUsage(ItemStack stack)
	{
		if (this.charges <= 1)
		{
			return 0;
		}

		CompoundTag nbt = StackUtil.getTag(stack);
		return nbt != null ? nbt.getInt("uses") : 0;
	}

	private void setUsage(ItemStack stack, int uses)
	{
		if (uses <= 0)
		{
			StackUtil.setTag(stack, null);
		} else
		{
			StackUtil.getOrCreateNbtData(stack).putInt("uses", uses);
		}
	}

	private double getChargeLevel(ItemStack stack)
	{
		return (double) (this.charges - this.getUsage(stack)) / this.charges;
	}

	public boolean isBarVisible(@NotNull ItemStack stack)
	{
		return this.getUsage(stack) > 0;
	}

	public int getBarWidth(@NotNull ItemStack stack)
	{
		return (int) Math.round(this.getChargeLevel(stack) * 13.0);
	}

	public int getBarColor(@NotNull ItemStack stack)
	{
		return Mth.hsvToRgb((float) (this.getChargeLevel(stack) / 3.0), 1.0F, 1.0F);
	}

	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(@NotNull ItemStack stack, Item.TooltipContext world, @NotNull List<Component> tooltip, @NotNull TooltipFlag advanced)
	{
		if (this.fluid == Fluids.EMPTY)
		{
			Ic2FluidStack stored = StandardFluidItem.getFs(stack);
			if (stored != null && !stored.isEmpty())
			{
				Ic2Tooltip.add(
					tooltip,
					Component.translatable("ic2.item.fluid_container.with_fluid", Component.translatable(stored.getFluidTypeKey()), stored.getAmountMb())
				);
			}
		}

		if (this.charges > 1 && stack.getCount() == 1 && advanced.isAdvanced())
		{
			Ic2Tooltip.add(tooltip, Component.translatable("item.durability", this.charges - this.getUsage(stack), this.charges));
		}
	}

	@Override
	public Ic2FluidStack getFluidStack(ItemStack stack)
	{
		if (this.fluid == Fluids.EMPTY)
		{
			Ic2FluidStack stored = StandardFluidItem.getFs(stack);
			return stored != null && !stored.isEmpty() ? stored : Ic2FluidStack.EMPTY;
		} else
		{
			return this.fluid != null ? Ic2FluidStack.create(this.fluid, CELL_CAPACITY_MB) : null;
		}
	}

	@Override
	public int getCapacityMb(ItemStack stack)
	{
		return this.fluid != null ? CELL_CAPACITY_MB : 0;
	}

	@Override
	public Ic2FluidStack drainMb(ItemStack stack, int amount, boolean simulate, Mutable<ItemStack> newStack)
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
			Ic2FluidStack stored = StandardFluidItem.getFs(stack);
			if (stored == null || stored.isEmpty())
			{
				return Ic2FluidStack.EMPTY;
			}

			if (amount <= 0)
			{
				return Ic2FluidStack.EMPTY;
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

		if (this.fluid == null)
		{
			return null;
		}

		if (amount <= 0)
		{
			return Ic2FluidStack.EMPTY;
		}

		if (simulate)
		{
			return Ic2FluidStack.create(this.fluid, Math.min(amount, CELL_CAPACITY_MB));
		}

		stack.shrink(1);

		if (newStack != null)
		{
			newStack.setValue(new ItemStack(Ic2Items.EMPTY_CELL));
		}

		return Ic2FluidStack.create(this.fluid, CELL_CAPACITY_MB);
	}

	@Override
	public int drainMb(ItemStack stack, Ic2FluidStack drainFs, boolean simulate, Mutable<ItemStack> newStack)
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
			Ic2FluidStack stored = StandardFluidItem.getFs(stack);
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

		if (this.fluid != null && this.fluid != Fluids.EMPTY && drainFs.hasExactFluid(this.fluid))
		{
			if (simulate)
			{
				return Math.min(amount, CELL_CAPACITY_MB);
			}

			stack.shrink(1);

			if (newStack != null)
			{
				newStack.setValue(new ItemStack(Ic2Items.EMPTY_CELL));
			}

			return CELL_CAPACITY_MB;
		} else
		{
			return 0;
		}
	}

	@Override
	public int fillMb(ItemStack stack, Ic2FluidStack fillFs, boolean simulate, Mutable<ItemStack> newStack)
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

		Ic2FluidStack stored = StandardFluidItem.getFs(stack);
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

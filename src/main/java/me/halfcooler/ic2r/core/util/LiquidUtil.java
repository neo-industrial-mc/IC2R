package me.halfcooler.ic2r.core.util;

import me.halfcooler.ic2r.api.util.FluidContainerOutputMode;
import me.halfcooler.ic2r.core.fluid.FluidHandler;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidTank;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableObject;
import net.minecraft.util.RandomSource;

public class LiquidUtil
{
	public static List<Fluid> getAllFluidsSorted()
	{
		List<Fluid> ret = new ArrayList<>(FluidHandler.getAllFluids());
		ret.sort(Comparator.comparing(BuiltInRegistries.FLUID::getKey));
		return ret;
	}

	public static LiquidUtil.LiquidData getLiquid(Level world, BlockPos pos)
	{
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		Fluid liquid = FluidHandler.getWorldFluid(state, world, pos);
		boolean isSource = false;
		if (liquid != null)
		{
			Ic2rFluidStack drained = FluidHandler.drainWorldFluid(state, world, pos, true);
			isSource = drained != null && drained.getAmountMb() >= 1000;
		} else if (block == Blocks.WATER)
		{
			liquid = Fluids.WATER;
			isSource = state.getValue(LiquidBlock.LEVEL) == 0;
		} else if (block == Blocks.LAVA)
		{
			liquid = Fluids.LAVA;
			isSource = state.getValue(LiquidBlock.LEVEL) == 0;
		}

		return liquid != null ? new LiquidUtil.LiquidData(liquid, isSource) : null;
	}

	public static boolean isFluidContainer(ItemStack stack)
	{
		return Ic2rFluidStack.get(stack) != null;
	}

	public static boolean isDrainableFluidContainer(ItemStack stack)
	{
		Ic2rFluidStack fs = Ic2rFluidStack.get(stack);
		return fs != null && !fs.isEmpty();
	}

	public static boolean isFillableFluidContainer(ItemStack stack)
	{
		return isFillableFluidContainer(stack, null);
	}

	public static boolean isFillableFluidContainer(ItemStack stack, Iterable<Fluid> testFluids)
	{
		if (!isFluidContainer(stack))
		{
			return false;
		}

		if (testFluids == null)
		{
			testFluids = FluidHandler.getAllFluids();
		}

		ItemStack singleStack = StackUtil.copyWithSize(stack, 1);
		Ic2rFluidStack[] fss = Ic2rFluidStack.getAll(singleStack);
		if (fss == null)
		{
			return false;
		}

		for (Ic2rFluidStack fs : fss)
		{
			if (fs.getFluid() != null && FluidHandler.fillMb(singleStack, fs.copyWithAmountMb(Integer.MAX_VALUE), true, null) > 0)
			{
				return true;
			}
		}

		for (Fluid fluid : FluidHandler.getAllFluids())
		{
			if (FluidHandler.fillMb(singleStack, Ic2rFluidStack.create(fluid, Integer.MAX_VALUE), true, null) > 0)
			{
				return true;
			}
		}

		return false;
	}

	public static Ic2rFluidStack drainContainer(
		Player player, InteractionHand hand, Fluid fluid, int maxAmount, FluidContainerOutputMode outputMode, boolean simulate
	)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if (StackUtil.isEmpty(stack) || !isFluidContainer(stack) || maxAmount <= 0)
		{
			return null;
		}

		ItemStack remaining = StackUtil.getSize(stack) > 1 ? StackUtil.decSize(stack.copy()) : StackUtil.emptyStack;
		ItemStack single = StackUtil.copyWithSize(stack, 1);
		LiquidUtil.FluidOperationResult result = drainContainer(single, fluid, maxAmount, FluidContainerOutputMode.AnyToOutput);
		if (result == null)
		{
			return null;
		}

		ItemStack output = getFluidOperationOutput(result);
		if (!StackUtil.isEmpty(output) && !StackUtil.storeInventoryItem(output, player, simulate))
		{
			return null;
		}

		if (!simulate)
		{
			StackUtil.set(player, hand, remaining);
		}

		return result.fluidChange;
	}

	public static int fillContainer(Player player, InteractionHand hand, Ic2rFluidStack fs, FluidContainerOutputMode outputMode, boolean simulate)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if (StackUtil.isEmpty(stack) || !isFluidContainer(stack) || fs == null || fs.isEmpty())
		{
			return 0;
		}

		ItemStack remaining = StackUtil.getSize(stack) > 1 ? StackUtil.decSize(stack.copy()) : StackUtil.emptyStack;
		ItemStack single = StackUtil.copyWithSize(stack, 1);
		LiquidUtil.FluidOperationResult result = fillContainer(single, fs, FluidContainerOutputMode.AnyToOutput);
		if (result == null)
		{
			return 0;
		}

		ItemStack output = getFluidOperationOutput(result);
		if (!StackUtil.isEmpty(output) && !StackUtil.storeInventoryItem(output, player, simulate))
		{
			return 0;
		}

		if (!simulate)
		{
			StackUtil.set(player, hand, remaining);
		}

		return result.fluidChange.getAmountMb();
	}

	public static boolean transferFluidFromHandClick(Player player, InteractionHand hand, Ic2rFluidTank tank, boolean shift)
	{
		ItemStack held = StackUtil.get(player, hand);
		if (StackUtil.isEmpty(held) || !isFluidContainer(held))
		{
			return false;
		}

		LiquidUtil.ContainerTankTransferResult result = transferFluidBetweenContainerStackAndTank(held, tank, shift);
		if (!result.changed)
		{
			return false;
		}

		StackUtil.set(player, hand, result.remaining);
		storeRemainingOrDrop(player, result.output);
		return true;
	}

	public static void transferFluidFromGuiClick(Player player, Ic2rFluidTank tank, boolean shift)
	{
		ItemStack carried = player.containerMenu.getCarried();
		if (StackUtil.isEmpty(carried) || !isFluidContainer(carried))
		{
			return;
		}

		LiquidUtil.ContainerTankTransferResult result = transferFluidBetweenContainerStackAndTank(carried, tank, shift);
		if (!result.changed)
		{
			return;
		}

		player.containerMenu.setCarried(result.remaining);
		storeRemainingOrDrop(player, result.output);
		player.containerMenu.broadcastChanges();
	}

	private static LiquidUtil.ContainerTankTransferResult transferFluidBetweenContainerStackAndTank(ItemStack stack, Ic2rFluidTank tank, boolean batch)
	{
		return batch
			? transferFluidBetweenContainerStackAndTankBatch(stack, tank)
			: transferFluidBetweenContainerStackAndTankSingle(stack, tank);
	}

	private static LiquidUtil.ContainerTankTransferResult transferFluidBetweenContainerStackAndTankSingle(ItemStack stack, Ic2rFluidTank tank)
	{
		ItemStack remaining = StackUtil.getSize(stack) > 1 ? StackUtil.decSize(stack.copy()) : StackUtil.emptyStack;
		ItemStack single = StackUtil.copyWithSize(stack, 1);
		Ic2rFluidStack tankFs = tank.getFluidStack();

		if (tankFs != null && !tankFs.isEmpty())
		{
			LiquidUtil.FluidOperationResult result = fillContainerComplete(single.copy(), tankFs.copy(), FluidContainerOutputMode.AnyToOutput);
			if (result != null)
			{
				tank.drainMb(result.fluidChange.getAmountMb(), false);
				return new LiquidUtil.ContainerTankTransferResult(true, remaining, getFluidOperationOutput(result));
			}
		}

		int space = tank.getCapacity() - (tankFs != null ? tankFs.getAmountMb() : 0);
		if (space > 0)
		{
			LiquidUtil.FluidOperationResult result = drainContainerComplete(
				single.copy(),
				tankFs != null && !tankFs.isEmpty() ? tankFs.getFluid() : null,
				space,
				FluidContainerOutputMode.AnyToOutput
			);
			if (result != null)
			{
				tank.fillMb(result.fluidChange, false);
				return new LiquidUtil.ContainerTankTransferResult(true, remaining, getFluidOperationOutput(result));
			}
		}

		return LiquidUtil.ContainerTankTransferResult.UNCHANGED;
	}

	private static LiquidUtil.ContainerTankTransferResult transferFluidBetweenContainerStackAndTankBatch(ItemStack stack, Ic2rFluidTank tank)
	{
		ItemStack remaining = stack.copy();
		ItemStack output = StackUtil.emptyStack;
		boolean changed = false;

		while (!StackUtil.isEmpty(remaining))
		{
			Ic2rFluidStack tankFs = tank.getFluidStack();
			if (tankFs == null || tankFs.isEmpty())
			{
				break;
			}

			ItemStack single = StackUtil.copyWithSize(remaining, 1);
			LiquidUtil.FluidOperationResult result = fillContainerComplete(single, tankFs.copy(), FluidContainerOutputMode.AnyToOutput);
			if (result == null)
			{
				break;
			}

			tank.drainMb(result.fluidChange.getAmountMb(), false);
			remaining = StackUtil.decSize(remaining);
			output = mergeStacks(output, getFluidOperationOutput(result));
			changed = true;
		}

		if (!changed)
		{
			while (!StackUtil.isEmpty(remaining))
			{
				Ic2rFluidStack tankFs = tank.getFluidStack();
				int space = tank.getCapacity() - (tankFs != null ? tankFs.getAmountMb() : 0);
				if (space <= 0)
				{
					break;
				}

				ItemStack single = StackUtil.copyWithSize(remaining, 1);
				LiquidUtil.FluidOperationResult result = drainContainerComplete(
					single,
					tankFs != null && !tankFs.isEmpty() ? tankFs.getFluid() : null,
					space,
					FluidContainerOutputMode.AnyToOutput
				);
				if (result == null)
				{
					break;
				}

				tank.fillMb(result.fluidChange, false);
				remaining = StackUtil.decSize(remaining);
				output = mergeStacks(output, getFluidOperationOutput(result));
				changed = true;
			}
		}

		return changed ? new LiquidUtil.ContainerTankTransferResult(true, remaining, output) : LiquidUtil.ContainerTankTransferResult.UNCHANGED;
	}

	public static LiquidUtil.FluidOperationResult fillContainerComplete(ItemStack stack, Ic2rFluidStack fsIn, FluidContainerOutputMode outputMode)
	{
		ItemStack testSingle = StackUtil.copyWithSize(stack, 1);
		Ic2rFluidStack fillTestFs = fsIn.copy();
		fillTestFs.setAmountMb(Integer.MAX_VALUE);
		MutableObject<ItemStack> newStack = new MutableObject<>();
		int fillAmount = FluidHandler.fillMb(testSingle, fillTestFs, true, newStack);
		if (fillAmount <= 0)
		{
			return null;
		}

		ItemStack afterFill = newStack.getValue() != null ? newStack.getValue() : testSingle;
		fillTestFs.setAmountMb(Integer.MAX_VALUE);
		if (FluidHandler.fillMb(afterFill, fillTestFs, true, null) > 0)
		{
			return null;
		}

		if (fsIn.getAmountMb() < fillAmount)
		{
			return null;
		}

		return fillContainer(stack, fsIn.copyWithAmountMb(fillAmount), outputMode);
	}

	public static LiquidUtil.FluidOperationResult drainContainerComplete(ItemStack stack, Fluid fluid, int maxAmount, FluidContainerOutputMode outputMode)
	{
		ItemStack testSingle = StackUtil.copyWithSize(stack, 1);
		Ic2rFluidStack fullContent = FluidHandler.drainMb(testSingle, Integer.MAX_VALUE, true, null);
		if (fullContent == null || fullContent.isEmpty())
		{
			return null;
		}

		if (fullContent.getAmountMb() > maxAmount)
		{
			return null;
		}

		return drainContainer(stack, fluid, maxAmount, outputMode);
	}

	private static ItemStack getFluidOperationOutput(LiquidUtil.FluidOperationResult result)
	{
		return !StackUtil.isEmpty(result.extraOutput) ? result.extraOutput : result.inPlaceOutput;
	}

	private static ItemStack mergeStacks(ItemStack base, ItemStack addition)
	{
		if (StackUtil.isEmpty(addition))
		{
			return base;
		}

		if (StackUtil.isEmpty(base))
		{
			return addition.copy();
		}

		if (StackUtil.checkItemEqualityStrict(base, addition))
		{
			return StackUtil.copyWithSize(base, StackUtil.getSize(base) + StackUtil.getSize(addition));
		}

		return base;
	}

	private static void storeRemainingOrDrop(Player player, ItemStack stack)
	{
		if (!StackUtil.isEmpty(stack) && !StackUtil.storeInventoryItem(stack, player, false))
		{
			player.drop(stack, false);
		}
	}

	public static LiquidUtil.FluidOperationResult drainContainer(ItemStack stack, Fluid fluid, int maxAmount, FluidContainerOutputMode outputMode)
	{
		if (!StackUtil.isEmpty(stack) && maxAmount > 0)
		{
			Ic2rFluidStack fs = Ic2rFluidStack.get(stack);
			ItemStack inPlace = StackUtil.copy(stack);
			ItemStack extra = null;
			if (fs == null)
			{
				return null;
			}

			ItemStack singleStack = StackUtil.copyWithSize(inPlace, 1);
			MutableObject<ItemStack> newStack = new MutableObject<>();
			if (fluid == null)
			{
				fs = FluidHandler.drainMb(singleStack, maxAmount, false, newStack);
				if (fs == null || fs.isEmpty())
				{
					return null;
				}
			} else
			{
				fs = Ic2rFluidStack.create(fluid, maxAmount);
				int amount = FluidHandler.drainMb(singleStack, fs, false, newStack);
				if (amount <= 0)
				{
					return null;
				}

				fs.setAmountMb(amount);
			}

			if (StackUtil.isEmpty(singleStack))
			{
				inPlace = StackUtil.decSize(inPlace);
				extra = newStack.getValue();
			} else
			{
				Ic2rFluidStack resEmptyTest = FluidHandler.drainMb(singleStack, Integer.MAX_VALUE, true, null);
				boolean isEmpty = resEmptyTest == null || resEmptyTest.isEmpty();
				if ((!isEmpty || !outputMode.isOutputEmptyFull())
					&& outputMode != FluidContainerOutputMode.AnyToOutput
					&& (outputMode != FluidContainerOutputMode.InPlacePreferred || StackUtil.getSize(inPlace) <= 1))
				{
					if (StackUtil.getSize(inPlace) > 1)
					{
						return null;
					}

					inPlace = newStack.getValue();
				} else
				{
					extra = newStack.getValue();
					inPlace = StackUtil.decSize(inPlace);
				}
			}

			assert !fs.isEmpty();
			return new LiquidUtil.FluidOperationResult(fs, inPlace, extra);
		} else
		{
			return null;
		}
	}

	public static LiquidUtil.FluidOperationResult fillContainer(ItemStack stack, Ic2rFluidStack fsIn, FluidContainerOutputMode outputMode)
	{
		if (!StackUtil.isEmpty(stack) && fsIn != null && !fsIn.isEmpty())
		{
			ItemStack inPlace = StackUtil.copy(stack);
			ItemStack singleStack = StackUtil.copyWithSize(inPlace, 1);
			Ic2rFluidStack fsChange = fsIn.copy();
			MutableObject<ItemStack> newStack = new MutableObject<>();
			int amount = FluidHandler.fillMb(singleStack, fsChange, false, newStack);
			if (amount <= 0)
			{
				return null;
			}

			fsChange.setAmountMb(amount);
			Ic2rFluidStack fillTestFs = fsIn.copy();
			fillTestFs.setAmountMb(Integer.MAX_VALUE);
			singleStack = newStack.getValue();
			boolean isFull = FluidHandler.fillMb(singleStack, fillTestFs, true, null) <= 0;
			assert fsChange.getFluid() == fsIn.getFluid();
			assert !fsChange.isEmpty();
			assert StackUtil.getSize(singleStack) == 1;
			ItemStack extra = null;
			if ((!isFull || !outputMode.isOutputEmptyFull())
				&& outputMode != FluidContainerOutputMode.AnyToOutput
				&& (outputMode != FluidContainerOutputMode.InPlacePreferred || StackUtil.getSize(inPlace) <= 1))
			{
				if (StackUtil.getSize(inPlace) > 1)
				{
					return null;
				}

				inPlace = singleStack;
			} else
			{
				extra = singleStack;
				inPlace = StackUtil.decSize(inPlace);
			}

			return new LiquidUtil.FluidOperationResult(fsChange, inPlace, extra);
		} else
		{
			return null;
		}
	}

	public static boolean isFluidTile(BlockEntity te, Direction side)
	{
		return FluidHandler.isFluidBlock(te, side);
	}

	public static boolean isFluidTile(BlockState state, BlockEntity te, Direction side)
	{
		return FluidHandler.isFluidBlock(state, te, side);
	}

	public static Ic2rFluidStack drainTile(BlockState state, BlockEntity te, Direction side, int maxAmount, boolean simulate)
	{
		return FluidHandler.drainMb(state, te, side, maxAmount, simulate);
	}

	public static Ic2rFluidStack drainTile(BlockState state, Level world, BlockPos pos, Direction side, int maxAmount, boolean simulate)
	{
		return FluidHandler.drainMb(state, world, pos, side, maxAmount, simulate);
	}

	public static int drainTile(BlockEntity te, Direction side, Fluid fluid, int maxAmount, boolean simulate)
	{
		return FluidHandler.drainMb(te, side, Ic2rFluidStack.create(fluid, maxAmount), simulate);
	}

	public static int fillTile(BlockEntity te, Direction side, Ic2rFluidStack fs, boolean simulate)
	{
		return FluidHandler.fillMb(te, side, fs, simulate);
	}

	public static List<LiquidUtil.AdjacentFluidHandler> getAdjacentHandlers(BlockEntity source)
	{
		List<LiquidUtil.AdjacentFluidHandler> ret = new ArrayList<>();

		for (Direction dir : Util.ALL_DIRS)
		{
			BlockEntity te = source.getLevel().getBlockEntity(source.getBlockPos().relative(dir));
			if (isFluidTile(te, dir.getOpposite()))
			{
				ret.add(new LiquidUtil.AdjacentFluidHandler(te, dir));
			}
		}

		return ret;
	}

	public static LiquidUtil.AdjacentFluidHandler getAdjacentHandler(BlockEntity source, Direction dir)
	{
		BlockEntity te = source.getLevel().getBlockEntity(source.getBlockPos().relative(dir));
		return !isFluidTile(te, dir.getOpposite()) ? null : new LiquidUtil.AdjacentFluidHandler(te, dir);
	}

	public static int distribute(BlockEntity source, Ic2rFluidStack stack, boolean simulate)
	{
		int transferred = 0;

		for (LiquidUtil.AdjacentFluidHandler handler : getAdjacentHandlers(source))
		{
			int amount = fillTile(handler.handler, handler.dir.getOpposite(), stack, simulate);
			transferred += amount;
			stack.setAmountMb(stack.getAmountMb() - amount);
			if (stack.isEmpty())
			{
				break;
			}
		}

		stack.setAmountMb(stack.getAmountMb() + transferred);
		return transferred;
	}

	public static Ic2rFluidStack transfer(BlockEntity source, Direction dir, BlockEntity target, int amount)
	{
		BlockState state = source.getBlockState();

		while (true)
		{
			Ic2rFluidStack ret = drainTile(state, source, dir, amount, true);
			if (ret != null && !ret.isEmpty())
			{
				if (ret.getAmountMb() > amount)
				{
					throw new IllegalStateException("The fluid handler " + source + " drained more than the requested amount.");
				}

				int cAmount = fillTile(target, getOppositeDir(dir), ret.copy(), true);
				if (cAmount > amount)
				{
					throw new IllegalStateException("The fluid handler " + target + " filled more than the requested amount.");
				}

				amount = cAmount;
				if (amount != ret.getAmountMb() && amount > 0)
				{
					continue;
				}

				if (amount <= 0)
				{
					return null;
				}

				ret = drainTile(state, source, dir, amount, false);
				if (ret == null)
				{
					throw new IllegalStateException(
						"The fluid handler " + source + " drained inconsistently. Expected " + amount + ", couldn't find previous IFluidHandler facing " + dir + "."
					);
				}

				if (ret.getAmountMb() != amount)
				{
					throw new IllegalStateException(
						"The fluid handler " + source + " drained inconsistently. Expected " + amount + ", got " + ret.getAmountMb() + "."
					);
				}

				amount = fillTile(target, getOppositeDir(dir), ret.copy(), false);
				if (amount != ret.getAmountMb())
				{
					throw new IllegalStateException(
						"The fluid handler " + target + " filled inconsistently. Expected " + ret.getAmountMb() + ", got " + amount + "."
					);
				}

				return ret;
			}

			return null;
		}
	}

	public static int distributeAll(BlockEntity source, int amount)
	{
		if (source == null)
		{
			throw new IllegalArgumentException("source has to be a tile entity");
		}

		int transferred = 0;

		for (Direction dir : Util.ALL_DIRS)
		{
			BlockEntity te = source.getLevel().getBlockEntity(source.getBlockPos().relative(dir));
			if (isFluidTile(te, dir.getOpposite()))
			{
				Ic2rFluidStack stack = transfer(source, dir, te, amount);
				if (stack != null)
				{
					amount -= stack.getAmountMb();
					transferred += stack.getAmountMb();
					if (amount <= 0)
					{
						break;
					}
				}
			}
		}

		return transferred;
	}

	private static Direction getOppositeDir(Direction dir)
	{
		return dir == null ? null : dir.getOpposite();
	}

	public static boolean check(Ic2rFluidStack fs)
	{
		return fs.getFluid() != null;
	}

	public static Ic2rFluidStack drainWorldFluidBlock(Level world, BlockPos pos, boolean simulate)
	{
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		Ic2rFluidStack drained = FluidHandler.drainWorldFluid(state, world, pos, simulate);
		if (drained != null)
		{
			return drained;
		}

		if (block instanceof LiquidBlock && state.getValue(LiquidBlock.LEVEL) == 0)
		{
			Ic2rFluidStack fluid = null;
			if (block == Blocks.WATER)
			{
				fluid = Ic2rFluidStack.create(Fluids.WATER, 1000);
			} else if (block == Blocks.LAVA)
			{
				fluid = Ic2rFluidStack.create(Fluids.LAVA, 1000);
			}

			if (fluid != null && !simulate)
			{
				world.removeBlock(pos, false);
			}

			return fluid;
		} else
		{
			return null;
		}
	}

	public static boolean drainWorldFluidBlockToContainer(Level world, BlockPos pos, Player player, InteractionHand hand)
	{
		Ic2rFluidStack fs = drainWorldFluidBlock(world, pos, true);
		if (fs != null && !fs.isEmpty())
		{
			int amount = fillContainer(player, hand, fs, FluidContainerOutputMode.InPlacePreferred, true);
			if (amount != fs.getAmountMb())
			{
				return false;
			}

			fs = drainWorldFluidBlock(world, pos, false);
			fillContainer(player, hand, fs, FluidContainerOutputMode.InPlacePreferred, false);
			return true;
		} else
		{
			return false;
		}
	}

	public static boolean fillWorldFluidBlock(Ic2rFluidStack fs, Level world, BlockPos pos, boolean simulate)
	{
     RandomSource rng = RandomSource.create();
		if (fs != null && fs.getAmountMb() >= 1000)
		{
			Fluid fluid = fs.getFluid();
			Block fluidBlock = fluid.defaultFluidState().createLegacyBlock().getBlock();
			if (fluid != null && fluidBlock != null)
			{
				BlockState state = world.getBlockState(pos);
				Block block = state.getBlock();
				if (!state.isAir() && state.canOcclude())
				{
					return false;
				}

				if (block == fluidBlock && isFullWorldFluidBlock(world, pos, block, state))
				{
					return false;
				}

				if (simulate)
				{
					return true;
				}

				if (world.dimensionType().ultraWarm() && fluidBlock == Blocks.WATER)
				{
					world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (rng.nextFloat() - rng.nextFloat()) * 0.8F);

					for (int i = 0; i < 8; i++)
					{
						world.addParticle(
							ParticleTypes.LARGE_SMOKE, pos.getX() + Math.random(), pos.getY() + Math.random(), pos.getZ() + Math.random(), 0.0, 0.0, 0.0
						);
					}
				} else
				{
					if (!world.isClientSide && !state.canOcclude() && state.getFluidState().isEmpty())
					{
						world.removeBlock(pos, true);
					}

					if (fluid == Fluids.WATER)
					{
						block = Blocks.WATER;
					} else if (fluid == Fluids.LAVA)
					{
						block = Blocks.LAVA;
					} else
					{
						block = fluid.defaultFluidState().createLegacyBlock().getBlock();
					}

					if (!world.setBlockAndUpdate(pos, block.defaultBlockState()))
					{
						return false;
					}
				}

				fs.decreaseMb(1000);
				return true;
			} else
			{
				return false;
			}
		} else
		{
			return false;
		}
	}

	private static boolean isFullWorldFluidBlock(Level world, BlockPos pos, Block block, BlockState state)
	{
		Ic2rFluidStack drained = FluidHandler.drainWorldFluid(state, world, pos, true);
		if (drained != null)
		{
			return drained.getAmountMb() >= 1000;
		} else
		{
			return state.getValues().containsKey(LiquidBlock.LEVEL) ? state.getValue(LiquidBlock.LEVEL) == 0 : false;
		}
	}

	public static boolean fillWorldFluidBlockFromContainer(Level world, BlockPos pos, Player player, InteractionHand hand)
	{
		Ic2rFluidStack fs = drainContainer(player, hand, null, 1000, FluidContainerOutputMode.InPlacePreferred, true);
		if (fs == null || fs.getAmountMb() < 1000)
		{
			return false;
		}

		if (!fillWorldFluidBlock(fs, world, pos, false))
		{
			return false;
		}

		drainContainer(player, hand, null, 1000, FluidContainerOutputMode.InPlacePreferred, false);
		return true;
	}

	public static boolean storeOutputContainer(MutableObject<ItemStack> output, Player player)
	{
		return output.getValue() == null ? true : StackUtil.storeInventoryItem(output.getValue(), player, false);
	}

	public static class AdjacentFluidHandler
	{
		public final BlockEntity handler;
		public final Direction dir;

		AdjacentFluidHandler(BlockEntity handler, Direction dir)
		{
			this.handler = handler;
			this.dir = dir;
		}
	}

	public static class FluidOperationResult
	{
		public final Ic2rFluidStack fluidChange;
		public final ItemStack inPlaceOutput;
		public final ItemStack extraOutput;

		FluidOperationResult(Ic2rFluidStack fluidChange, ItemStack inPlaceOutput, ItemStack extraOutput)
		{
			if (fluidChange == null)
			{
				throw new NullPointerException("null fluid change");
			}

			this.fluidChange = fluidChange;
			this.inPlaceOutput = inPlaceOutput;
			this.extraOutput = extraOutput;
		}
	}

	private static class ContainerTankTransferResult
	{
		static final LiquidUtil.ContainerTankTransferResult UNCHANGED = new LiquidUtil.ContainerTankTransferResult(false, StackUtil.emptyStack, StackUtil.emptyStack);

		final boolean changed;
		final ItemStack remaining;
		final ItemStack output;

		ContainerTankTransferResult(boolean changed, ItemStack remaining, ItemStack output)
		{
			this.changed = changed;
			this.remaining = remaining;
			this.output = output;
		}
	}

	public static class LiquidData
	{
		public final Fluid liquid;
		public final boolean isSource;

		LiquidData(Fluid liquid, boolean isSource)
		{
			this.liquid = liquid;
			this.isSource = isSource;
		}
	}
}

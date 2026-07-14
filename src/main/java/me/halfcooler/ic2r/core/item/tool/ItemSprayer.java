package me.halfcooler.ic2r.core.item.tool;

import me.halfcooler.ic2r.api.item.IBoxable;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.block.misc.FoamBlock;
import me.halfcooler.ic2r.core.block.wiring.CableBlock;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.fluid.StandardFluidItem;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.ref.Ic2rFluids;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;
import me.halfcooler.ic2r.core.util.StackUtil;
import me.halfcooler.ic2r.core.util.Util;

import java.util.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ItemSprayer extends Item implements StandardFluidItem, IBoxable
{
	private static final int CAPACITY = 8000;
	private static final int FLUID_PER_FOAM = 100;

	public ItemSprayer(Properties settings)
	{
		super(settings);
	}

	private static boolean canPlaceFoam(Level world, BlockPos pos, Target target)
	{
		return switch (target)
		{
			case ANY -> world.getBlockState(pos).canBeReplaced();
			case SCAFFOLD -> world.getBlockState(pos).is(Blocks.SCAFFOLDING);
			case CABLE -> world.getBlockState(pos).getBlock() instanceof CableBlock;
		};
	}

	@Override
	public void appendHoverText(@NotNull ItemStack item, @Nullable Level level, @NotNull List<Component> components, @NotNull TooltipFlag flag)
	{
		int all = 0;
		Ic2rFluidStack fluid = Ic2rFluidStack.get(item);
		if (!fluid.isEmpty())
		{
			all = fluid.getAmountMb();
		}

		Ic2rTooltip.add(components, Component.translatable("item.ic2r.foam_sprayer.tooltip.content", String.valueOf(all)));
	}

	@Override
	public int getCapacityMb(ItemStack stack)
	{
		return CAPACITY;
	}

	@Override
	public boolean canFill(ItemStack stack, Ic2rFluidStack fs)
	{
		return fs.getFluid() == Ic2rFluids.CONSTRUCTION_FOAM.still();
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(Level world, @NotNull Player player, @NotNull InteractionHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if (!world.isClientSide && IC2R.keyboard.isModeSwitchKeyDown(player))
		{
			CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
			int mode = nbtData.getInt("mode");
			mode = mode == 0 ? 1 : 0;
			nbtData.putInt("mode", mode);
			String sMode = Component.translatable(mode == 0 ? "ic2r.tooltip.mode.normal" : "ic2r.tooltip.mode.single").getString();
			IC2R.sideProxy.messagePlayer(player, Component.translatable("ic2r.tooltip.mode", sMode).getString());
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
		}

		return new InteractionResultHolder<>(InteractionResult.PASS, stack);
	}

	@Override
	public @NotNull InteractionResult useOn(UseOnContext context)
	{
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();
		InteractionHand hand = context.getHand();

		if (player == null)
		{
			return InteractionResult.PASS;
		}

		if (IC2R.keyboard.isModeSwitchKeyDown(player))
		{
			return InteractionResult.PASS;
		}

		if (world.isClientSide)
		{
			return InteractionResult.SUCCESS;
		}

		int maxFoamBlocks = 0;
		ItemStack stack = StackUtil.get(player, hand);
		Ic2rFluidStack fluid = Ic2rFluidStack.get(stack);
		if (!fluid.isEmpty())
		{
			maxFoamBlocks += fluid.getAmountMb() / FLUID_PER_FOAM;
		}

		ItemStack pack = player.getItemBySlot(EquipmentSlot.CHEST);
		boolean hasPack = false;
		if (!StackUtil.isEmpty(pack) && pack.getItem() == Ic2rItems.CF_PACK)
		{
			fluid = Ic2rFluidStack.get(pack);
			if (!fluid.isEmpty())
			{
				maxFoamBlocks += fluid.getAmountMb() / FLUID_PER_FOAM;
				hasPack = true;
			}
		}

		if (maxFoamBlocks == 0)
		{
			return InteractionResult.FAIL;
		}

		maxFoamBlocks = Math.min(maxFoamBlocks, this.getMaxFoamBlocks(stack));

		Target target;
		BlockState state = world.getBlockState(pos);
		if (state.is(Blocks.SCAFFOLDING))
		{
			target = Target.SCAFFOLD;
		} else if (state.getBlock() instanceof CableBlock)
		{
			target = Target.CABLE;
		} else
		{
			pos = pos.relative(context.getClickedFace());
			target = Target.ANY;
		}

		int amount = this.sprayFoam(world, pos, player.getDirection().getOpposite(), target, maxFoamBlocks);
		amount *= FLUID_PER_FOAM;
		if (amount > 0)
		{
			if (hasPack)
			{
				Ic2rFluidStack toDrain = Ic2rFluidStack.create(Ic2rFluids.CONSTRUCTION_FOAM.still(), amount);
				StandardFluidItem packItem = (StandardFluidItem) pack.getItem();
				int drained = packItem.drainMb(pack, toDrain, false, null);
				amount -= drained;
				player.setItemSlot(EquipmentSlot.CHEST, pack);
			}

			if (amount > 0)
			{
				Ic2rFluidStack toDrain = Ic2rFluidStack.create(Ic2rFluids.CONSTRUCTION_FOAM.still(), amount);
				this.drainMb(stack, toDrain, false, null);
			}

			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	private int sprayFoam(Level world, BlockPos pos, Direction excludedDir, Target target, int maxFoamBlocks)
	{
		if (!canPlaceFoam(world, pos, target))
		{
			return 0;
		}

		Queue<BlockPos> toCheck = new ArrayDeque<>();
		Set<BlockPos> positions = new HashSet<>();
		toCheck.add(pos);

		BlockPos cPos;
		while ((cPos = toCheck.poll()) != null && positions.size() < maxFoamBlocks)
		{
			if (canPlaceFoam(world, cPos, target) && positions.add(cPos))
			{
				for (Direction dir : Util.ALL_DIRS)
				{
					if (dir != excludedDir)
					{
						toCheck.add(cPos.relative(dir));
					}
				}
			}
		}

		toCheck.clear();
		int failedPlacements = 0;

		for (BlockPos targetPos : positions)
		{
			BlockState state = world.getBlockState(targetPos);
			if (state.is(Blocks.SCAFFOLDING))
			{
				world.destroyBlock(targetPos, true);
				world.setBlockAndUpdate(targetPos, Ic2rBlocks.FOAM.get().defaultBlockState().setValue(FoamBlock.typeProperty, FoamBlock.FoamType.normal));
			} else if (state.getBlock() instanceof CableBlock cable)
			{
				BlockState foamState = cable.toFoamState(state, cable.getFoamCableBlock());
				world.setBlockAndUpdate(targetPos, foamState);
			} else if (!world.setBlockAndUpdate(targetPos, Ic2rBlocks.FOAM.get().defaultBlockState().setValue(FoamBlock.typeProperty, FoamBlock.FoamType.normal)))
			{
				failedPlacements++;
			}
		}

		return positions.size() - failedPlacements;
	}

	private int getMaxFoamBlocks(ItemStack stack)
	{
		CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
		return nbtData.getInt("mode") == 0 ? 10 : 1;
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack stack)
	{
		return true;
	}

	private enum Target
	{
		ANY,
		SCAFFOLD,
		CABLE
	}
}

package me.halfcooler.ic2r.core.item.tool;

import me.halfcooler.ic2r.api.item.IEnhancedOverlayProvider;
import me.halfcooler.ic2r.core.block.wiring.CableBlock;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.ref.Ic2rSoundEvents;
import me.halfcooler.ic2r.core.util.StackUtil;

import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ItemToolCutter extends ItemToolCrafting implements IEnhancedOverlayProvider
{
	public ItemToolCutter(Properties settings)
	{
		super(settings);
	}

	public @NotNull InteractionResult useOn(UseOnContext context)
	{
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof CableBlock cable)
		{
			Player player = context.getPlayer();
			Predicate<ItemStack> request = StackUtil.sameItem(Ic2rItems.RUBBER);
			if (player == null)
			{
				return InteractionResult.PASS;
			}

			if (StackUtil.consumeFromPlayerInventory(player, request, 1, true) && cable.tryAddInsulation(state, world, pos))
			{
				StackUtil.consumeFromPlayerInventory(player, request, 1, false);
				StackUtil.damageOrError(player, context.getHand(), 1);
				return InteractionResult.SUCCESS;
			}
		}

		return InteractionResult.PASS;
	}

	public void removeInsulation(Player player, InteractionHand hand, BlockState state, Level world, BlockPos pos)
	{
		CableBlock cable = (CableBlock) state.getBlock();
		if (cable.tryRemoveInsulation(state, world, pos, true) && StackUtil.damage(player, hand, StackUtil.sameItem(this), 3))
		{
			cable.tryRemoveInsulation(state, world, pos, false);
			if (world.isClientSide)
			{
				player.playSound(Ic2rSoundEvents.ITEM_CUTTER_USE, 1.0F, 1.0F);
			} else
			{
				StackUtil.dropAsEntity(world, pos, new ItemStack(Ic2rItems.RUBBER));
			}

		}
	}

	@Override
	public boolean providesEnhancedOverlay(Level world, BlockPos pos, Direction side, Player player, ItemStack stack)
	{
		return false;
	}
}

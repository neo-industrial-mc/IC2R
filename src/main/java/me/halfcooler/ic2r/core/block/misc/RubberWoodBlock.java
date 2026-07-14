package me.halfcooler.ic2r.core.block.misc;

import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class RubberWoodBlock extends Block
{
	public RubberWoodBlock(Properties settings)
	{
		super(settings);
	}

	public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit)
	{
		ItemStack mainHandItem = player.getMainHandItem();
		if (mainHandItem.getItem() instanceof AxeItem)
		{
			WorldUtil.strip(state, world, pos, player, mainHandItem, Ic2rBlocks.STRIPPED_RUBBER_WOOD.get().defaultBlockState());
			return InteractionResult.sidedSuccess(world.isClientSide);
		} else
		{
			return super.use(state, world, pos, player, hand, hit);
		}
	}
}

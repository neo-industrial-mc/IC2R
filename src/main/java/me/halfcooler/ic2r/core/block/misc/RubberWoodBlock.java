package me.halfcooler.ic2r.core.block.misc;

import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
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
    public static final com.mojang.serialization.MapCodec<RubberWoodBlock> CODEC = simpleCodec(RubberWoodBlock::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.Block> codec() {
        return CODEC;
    }

	public RubberWoodBlock(Properties settings)
	{
		super(settings);
	}

	@Override
	protected @NotNull net.minecraft.world.ItemInteractionResult useItemOn(@NotNull ItemStack mainHandItem, @NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit)
	{
		if (mainHandItem.getItem() instanceof AxeItem)
		{
			WorldUtil.strip(state, world, pos, player, mainHandItem, Ic2rBlocks.STRIPPED_RUBBER_WOOD.get().defaultBlockState());
			return net.minecraft.world.ItemInteractionResult.sidedSuccess(world.isClientSide);
		}
		return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}
}

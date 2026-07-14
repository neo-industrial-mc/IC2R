package me.halfcooler.ic2r.core.item.block;

import me.halfcooler.ic2r.api.item.IBoxable;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.block.BlockDynamite;
import me.halfcooler.ic2r.core.entity.DynamiteEntity;
import me.halfcooler.ic2r.core.entity.StickyDynamiteEntity;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.util.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ItemDynamite extends Item implements IBoxable
{
	public final boolean sticky;

	public ItemDynamite(Properties properties, boolean sticky)
	{
		super(properties.stacksTo(16));
		this.sticky = sticky;
	}

	@NotNull
	public InteractionResult useOn(UseOnContext context)
	{
		if (this.sticky)
		{
			return InteractionResult.PASS;
		}

		Level level = context.getLevel();
		Player player = context.getPlayer();
		BlockPlaceContext placeContext = new BlockPlaceContext(context);
		BlockPos pos = placeContext.getClickedPos();
		BlockDynamite dynamite = (BlockDynamite) Ic2rBlocks.DYNAMITE.get();

		if (!level.getWorldBorder().isWithinBounds(pos) || player != null && !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand()))
		{
			return InteractionResult.FAIL;
		}

		// Classic IC2R: only place into empty space. Never overwrite an existing dynamite
		// (same cell, different face) — that would delete the old stick and still consume one.
		BlockState existing = level.getBlockState(pos);
		if (!existing.canBeReplaced(placeContext))
		{
			return InteractionResult.FAIL;
		}

		BlockState placeState = dynamite.getStateForPlacement(placeContext);
		if (placeState == null || !dynamite.canSurvive(placeState, level, pos))
		{
			return InteractionResult.FAIL;
		}

		if (!level.setBlock(pos, placeState, 3))
		{
			return InteractionResult.FAIL;
		}

		if (player != null && !player.getAbilities().instabuild)
		{
			StackUtil.consumeOrError(player, context.getHand(), 1);
		}

		level.playSound(null, pos, SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);

		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@NotNull
	public InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if (!player.getAbilities().instabuild)
		{
			stack = StackUtil.decSize(stack);
		}

		level.playSound(
			null,
			player.getX(),
			player.getY(),
			player.getZ(),
			SoundEvents.ARROW_SHOOT,
			SoundSource.PLAYERS,
			0.5F,
			0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
		);

		if (IC2R.sideProxy.isSimulating())
		{
			if (this.sticky)
			{
				level.addFreshEntity(new StickyDynamiteEntity(level, player));
			}
			else
			{
				level.addFreshEntity(new DynamiteEntity(level, player));
			}
		}

		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack itemstack)
	{
		return true;
	}
}

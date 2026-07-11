package ic2.core.item.block;

import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.block.BlockDynamite;
import ic2.core.entity.DynamiteEntity;
import ic2.core.entity.StickyDynamiteEntity;
import ic2.core.ref.Ic2Blocks;
import ic2.core.util.StackUtil;
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
		BlockDynamite dynamite = (BlockDynamite) Ic2Blocks.DYNAMITE;

		if (!level.getWorldBorder().isWithinBounds(pos) || player != null && !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand()))
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

		if (IC2.sideProxy.isSimulating())
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

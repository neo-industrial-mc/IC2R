package me.halfcooler.ic2r.core.item.tool;

import me.halfcooler.ic2r.api.item.BlockBreakableItem;
import me.halfcooler.ic2r.api.item.IBoxable;
import me.halfcooler.ic2r.api.item.IEnhancedOverlayProvider;
import me.halfcooler.ic2r.api.tile.IWrenchAble;
import me.halfcooler.ic2r.core.IHitSoundOverride;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.item.PriorityUsableItem;
import me.halfcooler.ic2r.core.ref.Ic2rBlockTags;
import me.halfcooler.ic2r.core.ref.Ic2rItemTags;
import me.halfcooler.ic2r.core.ref.Ic2rSoundEvents;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;
import me.halfcooler.ic2r.core.util.LogCategory;
import me.halfcooler.ic2r.core.util.RotationUtil;
import me.halfcooler.ic2r.core.util.StackUtil;
import me.halfcooler.ic2r.core.util.Util;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manual wrench, ported from 1.12 {@code ItemToolWrenchNew}:
 * <ul>
 *   <li>Left-click: safely mine {@link IWrenchAble} machines (wrench drops), 1 durability</li>
 *   <li>Right-click: set facing from the face hit grid (no durability cost)</li>
 * </ul>
 * Shared helpers below are also used by {@link ItemToolWrenchElectric}.
 */
public class ItemToolWrench extends Item implements PriorityUsableItem, IBoxable, BlockBreakableItem, IEnhancedOverlayProvider, IHitSoundOverride
{
	/** Durability cost when mining/removing a machine (matches 1.12 ItemTool mining). */
	private static final int MINE_DAMAGE = 1;

	/** Mining efficiency for wrenchable machines (HarvestLevel.Iron in 1.12 ItemToolWrenchNew). */
	public static final float WRENCH_DESTROY_SPEED = 6.0F;

	public ItemToolWrench(Properties settings)
	{
		super(settings);
	}

	// === Shared helpers (manual + electric wrench) ===

	/** Whether this block is a machine the wrench is meant to harvest / overlay. */
	public static boolean isWrenchTarget(BlockState state)
	{
		return state.is(Ic2rBlockTags.MINEABLE_WITH_WRENCH) || state.getBlock() instanceof IWrenchAble;
	}

	/**
	 * Resolve facing from the clicked face's 3×3 hit grid (matches the enhanced overlay).
	 */
	public static Direction facingFromHit(Direction side, BlockPos pos, Vec3 hitLocation)
	{
		float hitX = (float) (hitLocation.x - pos.getX());
		float hitY = (float) (hitLocation.y - pos.getY());
		float hitZ = (float) (hitLocation.z - pos.getZ());
		return RotationUtil.rotateByHit(side, hitX, hitY, hitZ);
	}

	/**
	 * Right-click: always attempt {@link IWrenchAble#setFacing} from the hit region.
	 *
	 * @return {@link InteractionResult#FAIL} if not an IWrenchAble / air;
	 *         {@link InteractionResult#PASS} on client after sound;
	 *         {@link InteractionResult#SUCCESS} on server
	 */
	public static InteractionResult trySetFacingFromHit(UseOnContext context, Player player)
	{
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = world.getBlockState(pos);
		if (state.isAir())
		{
			return InteractionResult.FAIL;
		}

		if (!(state.getBlock() instanceof IWrenchAble wrenchAble))
		{
			return InteractionResult.FAIL;
		}

		Direction newFacing = facingFromHit(context.getClickedFace(), pos, context.getClickLocation());
		// Match 1.12 ItemToolWrenchNew: always attempt setFacing from the hit region.
		wrenchAble.setFacing(world, pos, newFacing, player);

		if (world.isClientSide)
		{
			player.playSound(Ic2rSoundEvents.ITEM_WRENCH_USE, 1.0F, 1.0F);
			return InteractionResult.PASS;
		}

		return InteractionResult.SUCCESS;
	}

	/**
	 * Left-click removal: cancel vanilla break and drop the machine itself (not the casing).
	 * Caller is responsible for durability / energy cost.
	 *
	 * @return true if the block was handled as a wrench remove (vanilla break should be canceled)
	 */
	public static boolean tryRemoveWithWrench(Level world, Player player, BlockPos pos, BlockState state)
	{
		if (!(state.getBlock() instanceof IWrenchAble wrenchAble) || !wrenchAble.wrenchCanRemove(world, pos, player))
		{
			return false;
		}

		removeBlockWithWrench(world, pos, state, player, wrenchAble);
		return true;
	}

	static void removeBlockWithWrench(Level world, BlockPos pos, BlockState state, Player player, IWrenchAble wrenchAble)
	{
		if (world.isClientSide)
		{
			return;
		}

		if (player.blockActionRestricted(world, pos, ((ServerPlayer) player).gameMode.getGameModeForPlayer()))
		{
			return;
		}

		Block block = state.getBlock();
		BlockEntity te = world.getBlockEntity(pos);

		if (IC2RConfig.protection.wrenchLogging.get())
		{
			String playerName = player.getGameProfile().getName() + "/" + player.getGameProfile().getId();
			IC2R.log.info(LogCategory.PlayerActivity,
				"Player %s used a wrench to remove the block %s (te %s) at %s.",
				playerName, state, getTeName(te), Util.formatPosition(world, pos));
		}

		block.playerWillDestroy(world, pos, state, player);
		if (world.removeBlock(pos, false))
		{
			block.destroy(world, pos, state);
		}

		List<ItemStack> drops = wrenchAble.getWrenchDrops(world, pos, state, te, player, 0);
		if (drops != null && !drops.isEmpty())
		{
			for (ItemStack drop : drops)
			{
				StackUtil.dropAsEntity(world, pos, drop);
			}
		} else if (IC2RConfig.debug.logEmptyWrenchDrops.get())
		{
			IC2R.log.warn(LogCategory.General,
				"The block %s (te %s) at %s didn't yield any wrench drops.",
				state, getTeName(te), Util.formatPosition(world, pos));
		}

		if (!player.getAbilities().instabuild)
		{
			state.spawnAfterBreak((ServerLevel) world, pos, player.getUseItem(), false);
		}
	}

	private static String getTeName(BlockEntity te)
	{
		return te != null ? ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(te.getType()).toString() : "none";
	}

	// === Left-click (mining) behavior — 1.12 ItemToolWrenchNew tool harvest ===

	@Override
	public InteractionResult onBlockStartBreak(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction)
	{
		return InteractionResult.PASS;
	}

	/**
	 * Cancel vanilla break for {@link IWrenchAble} and drop the machine itself (not the casing).
	 * Costs {@link #MINE_DAMAGE} durability (1), matching 1.12 {@code ItemTool} mining damage.
	 */
	@Override
	public boolean beforeBlockBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity)
	{
		if (tryRemoveWithWrench(world, player, pos, state))
		{
			player.getMainHandItem().hurtAndBreak(MINE_DAMAGE, player, p -> p.broadcastBreakEvent(p.getUsedItemHand()));
			return false;
		}

		return true;
	}

	@Override
	public void afterBlockBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity)
	{
	}

	/**
	 * Wrench is the correct tool for {@link Ic2rBlockTags#MINEABLE_WITH_WRENCH} / {@link IWrenchAble} machines.
	 * Pickaxes are no longer listed for those blocks, so they mine slowly and yield no drops when
	 * {@code requiresCorrectToolForDrops} is set.
	 */
	@Override
	public boolean isCorrectToolForDrops(BlockState state)
	{
		return isWrenchTarget(state);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state)
	{
		if (this.isCorrectToolForDrops(state))
		{
			return WRENCH_DESTROY_SPEED;
		}

		return super.getDestroySpeed(stack, state);
	}

	// === Right-click: set facing only, no durability (ItemToolWrenchNew) ===

	public boolean canTakeDamage()
	{
		return true;
	}

	public boolean canTakeDamage(ItemStack stack, int amount)
	{
		return true;
	}

	/**
	 * Hit-grid facing only. Does not damage the tool — durability is spent on mining.
	 */
	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
	{
		if (!this.canTakeDamage(stack, 1))
		{
			return InteractionResult.FAIL;
		}

		Player player = context.getPlayer();
		if (player == null)
		{
			return InteractionResult.PASS;
		}

		return trySetFacingFromHit(context, player);
	}

	public void damage(ItemStack is, int damage, Player player, InteractionHand hand)
	{
		is.hurtAndBreak(damage, player, p -> p.broadcastBreakEvent(hand));
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack itemstack)
	{
		return true;
	}

	public boolean isValidRepairItem(@NotNull ItemStack toRepair, ItemStack repair)
	{
		return repair.is(Ic2rItemTags.BRONZE_INGOTS);
	}

	public boolean isEnchantable(@NotNull ItemStack stack)
	{
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(@NotNull ItemStack stack, Level world, List<Component> info, @NotNull TooltipFlag flag)
	{
		Component attackKey = Minecraft.getInstance().options.keyAttack.getTranslatedKeyMessage();
		Component useKey = Minecraft.getInstance().options.keyUse.getTranslatedKeyMessage();
		Ic2rTooltip.add(info, Component.translatable("item.ic2r.wrench.tooltip.mine", attackKey));
		Ic2rTooltip.add(info, Component.translatable("item.ic2r.wrench.tooltip.rotate", useKey));
	}

	@Override
	public boolean providesEnhancedOverlay(Level world, BlockPos pos, Direction side, Player player, ItemStack stack)
	{
		return world.getBlockState(pos).getBlock() instanceof IWrenchAble;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public SoundEvent getHitSoundForBlock(LocalPlayer player, Level world, BlockPos pos, ItemStack stack)
	{
		// 1.12 returned "" to silence the default hit; 1.20 has no silence path — leave default.
		return null;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public SoundEvent getBreakSoundForBlock(LocalPlayer player, Level world, BlockPos pos, ItemStack stack)
	{
		if (player.getAbilities().instabuild)
		{
			return null;
		}

		return world.getBlockState(pos).getBlock() instanceof IWrenchAble
			? Ic2rSoundEvents.ITEM_WRENCH_USE
			: null;
	}
}

package ic2.core.item.tool;

import ic2.api.item.BlockBreakableItem;
import ic2.api.item.IBoxable;
import ic2.api.item.IEnhancedOverlayProvider;
import ic2.api.tile.IWrenchAble;
import ic2.core.IHitSoundOverride;
import ic2.core.IC2;
import ic2.core.init.IC2Config;
import ic2.core.item.PriorityUsableItem;
import ic2.core.ref.Ic2BlockTags;
import ic2.core.ref.Ic2ItemTags;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.Ic2Tooltip;
import ic2.core.util.LogCategory;
import ic2.core.util.RotationUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
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
 * Shared {@link #onWrenchUse} is kept for the electric wrench's rotation + energy path.
 */
public class ItemToolWrench extends Item implements PriorityUsableItem, IBoxable, BlockBreakableItem, IEnhancedOverlayProvider, IHitSoundOverride
{
	/** Durability cost when mining/removing a machine (matches 1.12 ItemTool mining). */
	private static final int MINE_DAMAGE = 1;

	public ItemToolWrench(Properties settings)
	{
		super(settings);
	}

	/**
	 * Shared right-click use used by the electric wrench.
	 * Rotate {@link IWrenchAble} blocks only; removal is left-click mining.
	 *
	 * @return damage/energy amount on success (server), -2 client success (sound played), -1 no-op
	 */
	public static int onWrenchUse(Player player, UseOnContext context)
	{
		WrenchResult result = wrenchBlock(context, player);
		if (result != WrenchResult.Nothing)
		{
			if (!context.getLevel().isClientSide)
			{
				return 1;
			}

			player.playSound(Ic2SoundEvents.ITEM_WRENCH_USE, 1.0F, 1.0F);
			return -2;
		}

		return -1;
	}

	public static WrenchResult wrenchBlock(UseOnContext context, Player player)
	{
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Direction side = context.getClickedFace();
		BlockState state = world.getBlockState(pos);
		if (state.isAir())
		{
			return WrenchResult.Nothing;
		}

		Block block = state.getBlock();
		if (block instanceof IWrenchAble wrenchAble)
		{
			return wrenchAbleBlock(world, pos, side, player, wrenchAble, context.getClickLocation());
		}

		return WrenchResult.Nothing;
	}

	/**
	 * Resolve facing from the clicked face's 3×3 hit grid, or Alt-rotate around the face axis
	 * (Alt kept for the electric wrench path; the manual wrench uses pure hit-based facing).
	 */
	private static Direction resolveNewFacing(Direction side, Player player, Vec3 hitLocation, BlockPos pos, Direction currentFacing)
	{
		if (IC2.keyboard.isAltKeyDown(player))
		{
			Axis axis = side.getAxis();
			return isAltRotationClockwise(side, player)
				? currentFacing.getClockWise(axis)
				: currentFacing.getCounterClockWise(axis);
		}

		float hitX = (float) (hitLocation.x - pos.getX());
		float hitY = (float) (hitLocation.y - pos.getY());
		float hitZ = (float) (hitLocation.z - pos.getZ());
		return RotationUtil.rotateByHit(side, hitX, hitY, hitZ);
	}

	private static Direction facingFromHit(Direction side, BlockPos pos, Vec3 hitLocation)
	{
		float hitX = (float) (hitLocation.x - pos.getX());
		float hitY = (float) (hitLocation.y - pos.getY());
		float hitZ = (float) (hitLocation.z - pos.getZ());
		return RotationUtil.rotateByHit(side, hitX, hitY, hitZ);
	}

	private static WrenchResult wrenchAbleBlock(
		Level world, BlockPos pos, Direction side, Player player,
		IWrenchAble wrenchAble, Vec3 hitLocation
	)
	{
		Direction currentFacing = wrenchAble.getFacing(world, pos);
		Direction newFacing = resolveNewFacing(side, player, hitLocation, pos, currentFacing);

		if (newFacing != currentFacing && wrenchAble.setFacing(world, pos, newFacing, player))
		{
			return WrenchResult.Rotated;
		}

		return WrenchResult.Nothing;
	}

	private static boolean isAltRotationClockwise(Direction sideHit, Player player)
	{
		return sideHit.getAxisDirection() == AxisDirection.POSITIVE != player.isShiftKeyDown();
	}

	private static String getTeName(BlockEntity te)
	{
		return te != null ? ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(te.getType()).toString() : "none";
	}

	private static void removeBlockWithWrench(Level world, BlockPos pos, BlockState state, Player player, IWrenchAble wrenchAble)
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

		if (IC2Config.protection.wrenchLogging.get())
		{
			String playerName = player.getGameProfile().getName() + "/" + player.getGameProfile().getId();
			IC2.log.info(LogCategory.PlayerActivity,
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
		} else if (IC2Config.debug.logEmptyWrenchDrops.get())
		{
			IC2.log.warn(LogCategory.General,
				"The block %s (te %s) at %s didn't yield any wrench drops.",
				state, getTeName(te), Util.formatPosition(world, pos));
		}

		if (!player.getAbilities().instabuild)
		{
			state.spawnAfterBreak((ServerLevel) world, pos, player.getUseItem(), false);
		}
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
		if (state.getBlock() instanceof IWrenchAble wrenchAble && wrenchAble.wrenchCanRemove(world, pos, player))
		{
			removeBlockWithWrench(world, pos, state, player, wrenchAble);
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
	 * Wrench is the correct tool for {@link Ic2BlockTags#MINEABLE_WITH_WRENCH} / {@link IWrenchAble} machines.
	 * Pickaxes are no longer listed for those blocks, so they mine slowly and yield no drops when
	 * {@code requiresCorrectToolForDrops} is set.
	 */
	@Override
	public boolean isCorrectToolForDrops(BlockState state)
	{
		return state.is(Ic2BlockTags.MINEABLE_WITH_WRENCH) || state.getBlock() instanceof IWrenchAble;
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state)
	{
		// Iron tool efficiency (HarvestLevel.Iron in 1.12 ItemToolWrenchNew)
		if (this.isCorrectToolForDrops(state))
		{
			return 6.0F;
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
	 * Hit-grid facing only (no Alt path). Does not damage the tool — durability is spent on mining.
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
			player.playSound(Ic2SoundEvents.ITEM_WRENCH_USE, 1.0F, 1.0F);
			return InteractionResult.PASS;
		}

		return InteractionResult.SUCCESS;
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
		return repair.is(Ic2ItemTags.BRONZE_INGOTS);
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
		Ic2Tooltip.add(info, Component.translatable("item.ic2.wrench.tooltip.mine", attackKey));
		Ic2Tooltip.add(info, Component.translatable("item.ic2.wrench.tooltip.rotate", useKey));
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
			? Ic2SoundEvents.ITEM_WRENCH_USE
			: null;
	}

	public enum WrenchResult
	{
		Rotated,
		Nothing
	}
}

package me.halfcooler.ic2r.core.item.tool;

import me.halfcooler.ic2r.api.item.BlockBreakableItem;
import me.halfcooler.ic2r.api.item.ElectricItem;
import me.halfcooler.ic2r.api.item.IBoxable;
import me.halfcooler.ic2r.api.item.IEnhancedOverlayProvider;
import me.halfcooler.ic2r.api.tile.IWrenchAble;
import me.halfcooler.ic2r.core.IHitSoundOverride;
import me.halfcooler.ic2r.core.item.PriorityUsableItem;
import me.halfcooler.ic2r.core.ref.Ic2rSoundEvents;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Electric wrench — same left/right-click logic as {@link ItemToolWrench}:
 * <ul>
 *   <li>Left-click: safely mine {@link IWrenchAble} machines (wrench drops), costs energy</li>
 *   <li>Right-click: set facing from the face hit grid (no energy cost)</li>
 * </ul>
 */
public class ItemToolWrenchElectric extends ItemElectricTool implements PriorityUsableItem, IBoxable, BlockBreakableItem, IEnhancedOverlayProvider, IHitSoundOverride
{
	/** Energy units charged against {@link #consumeEnergy} (×100 EU via override). */
	private static final double MINE_ENERGY_UNITS = 1.0;

	public ItemToolWrenchElectric(Properties settings)
	{
		super(settings, 100);
		this.tier = 1;
		this.maxCharge = 12000;
		this.transferLimit = 250;
	}

	// === Right-click: set facing only, no energy (same as manual wrench) ===

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
	{
		Player player = context.getPlayer();
		if (player == null)
		{
			return InteractionResult.PASS;
		}

		// Rotation does not require charge; mining does.
		return ItemToolWrench.trySetFacingFromHit(context, player);
	}

	// === Left-click (mining) — same path as ItemToolWrench, energy instead of durability ===

	@Override
	public InteractionResult onBlockStartBreak(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction)
	{
		return InteractionResult.PASS;
	}

	@Override
	public boolean beforeBlockBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity)
	{
		ItemStack stack = player.getMainHandItem();
		if (!this.canTakeDamage(stack, MINE_ENERGY_UNITS))
		{
			// No charge: fall through to vanilla (slow, no correct-tool wrench drops).
			return true;
		}

		if (ItemToolWrench.tryRemoveWithWrench(world, player, pos, state))
		{
			this.consumeEnergy(stack, MINE_ENERGY_UNITS, player);
			return false;
		}

		return true;
	}

	@Override
	public void afterBlockBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity)
	{
	}

	@Override
	public boolean isCorrectToolForDrops(ItemStack stack, BlockState state)
	{
		return ItemToolWrench.isWrenchTarget(state);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state)
	{
		if (this.isCorrectToolForDrops(stack, state) && this.canTakeDamage(stack, MINE_ENERGY_UNITS))
		{
			return ItemToolWrench.WRENCH_DESTROY_SPEED;
		}

		return 1.0F;
	}

	public boolean canTakeDamage(ItemStack stack, double amount)
	{
		amount *= 100.0;
		return ElectricItem.manager.getCharge(stack) >= amount;
	}

	@Override
	public boolean consumeEnergy(ItemStack stack, double amount, LivingEntity entity)
	{
		double operationEnergyCost = 100.0 * amount;
		return super.consumeEnergy(stack, operationEnergyCost, entity);
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack stack)
	{
		return true;
	}

	@Override
	public boolean providesEnhancedOverlay(Level world, BlockPos pos, Direction side, Player player, ItemStack stack)
	{
		// Still show the grid with empty charge so the player can plan the click.
		return world.getBlockState(pos).getBlock() instanceof IWrenchAble;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext world, List<Component> tooltip, TooltipFlag flag)
	{
		super.appendHoverText(stack, world, tooltip, flag);
		Component attackKey = Minecraft.getInstance().options.keyAttack.getTranslatedKeyMessage();
		Component useKey = Minecraft.getInstance().options.keyUse.getTranslatedKeyMessage();
		Ic2rTooltip.add(tooltip, Component.translatable("item.ic2r.wrench.tooltip.mine", attackKey));
		Ic2rTooltip.add(tooltip, Component.translatable("item.ic2r.wrench.tooltip.rotate", useKey));
	}

	@Override
	public SoundEvent getHitSoundForBlock(LocalPlayer player, Level world, BlockPos pos, ItemStack stack)
	{
		return null;
	}

	@Override
	public SoundEvent getBreakSoundForBlock(LocalPlayer player, Level world, BlockPos pos, ItemStack stack)
	{
		if (player.getAbilities().instabuild)
		{
			return null;
		}

		return world.getBlockState(pos).getBlock() instanceof IWrenchAble
			? Ic2rSoundEvents.ITEM_WRENCH_USE.get()
			: null;
	}
}

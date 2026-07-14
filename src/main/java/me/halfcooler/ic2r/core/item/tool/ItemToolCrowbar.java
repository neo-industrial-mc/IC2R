package me.halfcooler.ic2r.core.item.tool;

import me.halfcooler.ic2r.api.item.IEnhancedOverlayProvider;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.block.transport.cover.ICoverHolder;
import me.halfcooler.ic2r.core.item.PriorityUsableItem;
import me.halfcooler.ic2r.core.ref.Ic2rItemTags;
import me.halfcooler.ic2r.core.ref.Ic2rSoundEvents;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;
import me.halfcooler.ic2r.core.util.RotationUtil;
import me.halfcooler.ic2r.core.util.Util;

import java.util.List;


import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ItemToolCrowbar extends TieredItem implements IEnhancedOverlayProvider, PriorityUsableItem
{
	public ItemToolCrowbar(Tier material, Properties settings)
	{
		super(material, settings);
	}

	public boolean canTakeDamage()
	{
		return true;
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
	{
		Level world = context.getLevel();
		Player player = context.getPlayer();
		InteractionHand hand = context.getHand();
		BlockPos pos = context.getClickedPos();
		Direction side = context.getClickedFace();
		Vec3 hitPos = context.getClickLocation();
		if (!this.canTakeDamage())
		{
			return InteractionResult.FAIL;
		}

		BlockState state = world.getBlockState(pos);
		if (state.isAir())
		{
			return InteractionResult.FAIL;
		}

		if (world.getBlockEntity(pos) instanceof ICoverHolder target)
		{
			// hitPos is absolute world coords; rotateByHit expects face-local 0..1
			Direction selectedFacing = RotationUtil.rotateByHit(
				side,
				(float) (hitPos.x - pos.getX()),
				(float) (hitPos.y - pos.getY()),
				(float) (hitPos.z - pos.getZ())
			);
			if (target.canRemoveCover(world, pos, selectedFacing))
			{
				if (!world.isClientSide)
				{
					target.removeCover(world, pos, selectedFacing);
					if (player != null)
					{
						stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
					}
				} else
				{
					IC2R.soundManager.playOnce(Ic2rSoundEvents.ITEM_CROWBAR_USE.get(), SoundSource.BLOCKS, 1.0F, 1.0F, player);
				}
			}

			return world.isClientSide ? InteractionResult.PASS : InteractionResult.SUCCESS;
		} else
		{
			return InteractionResult.FAIL;
		}
	}

	public boolean isValidRepairItem(@NotNull ItemStack toRepair, @NotNull ItemStack repair)
	{
		return Util.matchesOD(repair, Ic2rItemTags.BRONZE_INGOTS);
	}

	public boolean isEnchantable(@NotNull ItemStack stack)
	{
		return false;
	}

	public void appendHoverText(@NotNull ItemStack stack, Level worldIn, List<Component> info, @NotNull TooltipFlag flagIn)
	{
		Ic2rTooltip.add(info, Component.translatable("item.ic2r.crowbar.tooltip.remove", Minecraft.getInstance().options.keyRight.getKey().getDisplayName()));
	}

	@Override
	public boolean providesEnhancedOverlay(Level world, BlockPos pos, Direction side, Player player, ItemStack stack)
	{
		BlockEntity tileEntity = world.getBlockEntity(pos);
		return tileEntity instanceof ICoverHolder;
	}
}

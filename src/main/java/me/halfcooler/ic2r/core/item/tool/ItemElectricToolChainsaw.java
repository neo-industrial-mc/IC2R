package me.halfcooler.ic2r.core.item.tool;

import me.halfcooler.ic2r.api.item.BlockBreakableItem;
import me.halfcooler.ic2r.api.item.IEntityAttackableItem;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.IHitSoundOverride;
import me.halfcooler.ic2r.core.ref.Ic2rSoundEvents;
import me.halfcooler.ic2r.core.ref.Ic2rToolMaterials;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;
import me.halfcooler.ic2r.core.util.KeyboardClient;
import me.halfcooler.ic2r.core.util.StackUtil;
import me.halfcooler.ic2r.core.util.Util;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemElectricToolChainsaw extends ItemElectricTool implements IHitSoundOverride, BlockBreakableItem, IEntityAttackableItem
{
	private static ShearableAccess shearableAccess;

	public interface ShearableAccess
	{
		boolean isShearable(net.minecraft.world.entity.Entity entity, net.minecraft.world.item.ItemStack stack, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos);
		java.util.List<net.minecraft.world.item.ItemStack> onSheared(net.minecraft.world.entity.Entity entity, net.minecraft.world.entity.player.Player player, net.minecraft.world.item.ItemStack stack, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos);
	}

	public static void setShearableAccess(ShearableAccess access)
	{
		shearableAccess = access;
	}

	public ItemElectricToolChainsaw(Properties settings)
	{
		super(settings, 100, Ic2rToolMaterials.CHAINSAW, Collections.singletonList(BlockTags.MINEABLE_WITH_AXE));
		this.maxCharge = 30000;
		this.transferLimit = 100;
		this.tier = 1;
	}

	private boolean isShearMode(ItemStack stack)
	{
		return !StackUtil.getOrCreateNbtData(stack).getBoolean("disableShear");
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext world, @NotNull List<Component> list, @NotNull TooltipFlag par4)
	{
		super.appendHoverText(stack, world, list, par4);
		Ic2rTooltip.add(list, Component.translatable("item.ic2r.tooltip.mode.switch", KeyboardClient.modeSwitchKey.getKey().getDisplayName(), Minecraft.getInstance().options.keyUse.getKey().getDisplayName()));
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand)
	{
		if (world.isClientSide)
		{
			return super.use(world, player, hand);
		}

		if (IC2R.keyboard.isModeSwitchKeyDown(player))
		{
			ItemStack held = StackUtil.get(player, hand);
			boolean disableShear = StackUtil.getOrCreateNbtData(held).getBoolean("disableShear");
			StackUtil.editTag(held, nbt -> nbt.putBoolean("disableShear", !disableShear));
			if (disableShear)
			{
				IC2R.sideProxy.messagePlayer(player, Component.translatable("item.ic2r.mining_laser.tooltip.mode", Component.translatable("item.ic2r.mining_laser.tooltip.mode.normal")));
			}
			else
			{
				IC2R.sideProxy.messagePlayer(player, Component.translatable("item.ic2r.mining_laser.tooltip.mode", Component.translatable("item.ic2r.mining_laser.tooltip.mode.no_shear")));
			}
		}

		return super.use(world, player, hand);
	}

	@Override
	public boolean isCorrectToolForDrops(@NotNull ItemStack stack, @NotNull BlockState state)
	{
		return super.isCorrectToolForDrops(stack, state) || state.is(Blocks.COBWEB) || Util.canShear(state);
	}

	@Override
	public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state)
	{
		return !this.canUse(stack) || !state.is(BlockTags.MINEABLE_WITH_AXE) && !state.is(Blocks.COBWEB) && !Util.canShear(state) ? 1.0F : this.getToolSpeed();
	}

	@Override
	public boolean onAttackEntity(Player player, Entity target)
	{
		ItemStack itemstack = player.getMainHandItem();
		if (this.consumeEnergy(itemstack, this.operationEnergyCost, player))
		{
			this.playUsingSound(player);
		}

		return true;
	}

	private void handleVanillaBlockBreakLogic(Player player, Level world, BlockPos pos, BlockState state)
	{
		world.levelEvent(player, 2001, pos, Block.getId(state));
		if (state.is(BlockTags.GUARDED_BY_PIGLINS))
		{
			PiglinAi.angerNearbyPiglins(player, false);
		}

		world.gameEvent(GameEvent.BLOCK_DESTROY, pos, Context.of(player, state));
	}

	@Override
	public InteractionResult onBlockStartBreak(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction)
	{
		BlockState state = world.getBlockState(pos);
		ItemStack stack = player.getMainHandItem();
		if (!this.isShearMode(stack) || !Util.canShear(state))
		{
			return InteractionResult.PASS;
		} else if (this.consumeEnergy(stack, this.operationEnergyCost, player))
		{
			this.handleVanillaBlockBreakLogic(player, world, pos, state);
			StackUtil.dropAsEntity(world, pos, new ItemStack(state.getBlock().asItem()));
			world.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
			this.playUsingSound(player);
			return InteractionResult.SUCCESS;
		} else
		{
			return InteractionResult.PASS;
		}
	}

	public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player user, @NotNull LivingEntity entity, @NotNull InteractionHand hand)
	{
		if (!StackUtil.getOrCreateNbtData(stack).getBoolean("disableShear"))
		{
			if (shearableAccess != null)
			{
				Level level = entity.level();
				BlockPos pos = entity.blockPosition();

				if (shearableAccess.isShearable(entity, stack, level, pos))
				{
					if (this.consumeEnergy(stack, this.operationEnergyCost, user))
					{
						List<ItemStack> drops = shearableAccess.onSheared(entity, user, stack, level, pos);

						for (ItemStack drop : drops)
						{
							if (!drop.isEmpty())
							{
								entity.spawnAtLocation(drop);
							}
						}
						this.playUsingSound(user);
						return InteractionResult.SUCCESS;
					}
				}
			}
		}
		return InteractionResult.PASS;
	}

	public void playUsingSound(LivingEntity user)
	{
		if (user.level().isClientSide)
		{
			user.playSound(this.getToolUsingSound(), 1.0F, 1.0F);
		}
	}

	public SoundEvent getToolUsingSound()
	{
		return IC2R.random.nextBoolean() ? Ic2rSoundEvents.ITEM_CHAINSAW_USE1.get() : Ic2rSoundEvents.ITEM_CHAINSAW_USE2.get();
	}

	@Override
	public SoundEvent getHitSoundForBlock(LocalPlayer player, Level world, BlockPos pos, ItemStack stack)
	{
		return this.getToolUsingSound();
	}

	@Override
	public SoundEvent getBreakSoundForBlock(LocalPlayer player, Level world, BlockPos pos, ItemStack stack)
	{
		return null;
	}

	@Override
	protected SoundEvent getIdleSound(LivingEntity player, ItemStack stack)
	{
		return Ic2rSoundEvents.ITEM_CHAINSAW_IDLE.get();
	}

	@Override
	protected SoundEvent getStopSound(LivingEntity player, ItemStack stack)
	{
		return Ic2rSoundEvents.ITEM_CHAINSAW_STOP.get();
	}

	@Override
	public boolean beforeBlockBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity)
	{
		return true;
	}

	@Override
	public void afterBlockBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity)
	{
	}
}

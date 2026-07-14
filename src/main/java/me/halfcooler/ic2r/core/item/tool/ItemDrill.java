package me.halfcooler.ic2r.core.item.tool;

import me.halfcooler.ic2r.api.item.IMiningDrill;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.IHitSoundOverride;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.ref.Ic2rSoundEvents;
import me.halfcooler.ic2r.core.util.StackUtil;
import me.halfcooler.ic2r.platform.services.PlatformServices;

import java.util.Arrays;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ItemDrill extends ItemElectricTool implements IMiningDrill, IHitSoundOverride
{
	private final float extraSpeedMultiplier;

	public ItemDrill(Properties settings, int operationEnergyCost, Tier material, int maxCharge, int transferLimit, int tier, float miningSpeed)
	{
		super(settings, operationEnergyCost, material, Arrays.asList(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.MINEABLE_WITH_SHOVEL));
		this.maxCharge = maxCharge;
		this.transferLimit = transferLimit;
		this.tier = tier;
		this.extraSpeedMultiplier = miningSpeed / material.getSpeed();
	}

	private static Player getPlayerHoldingItem(ItemStack stack)
	{
		if (IC2R.sideProxy.isRendering())
		{
			Player player = IC2R.sideProxy.getPlayerInstance();
			if (player != null && player.getInventory().getSelected() == stack)
			{
				return player;
			}
		} else
		{
			MinecraftServer server = PlatformServices.lifecycle().getServer();
			if (server == null)
			{
				return null;
			}

			for (Player player : server.getPlayerList().getPlayers())
			{
				if (player.getInventory().getSelected() == stack)
				{
					return player;
				}
			}
		}

		return null;
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state)
	{
		float speed = super.getDestroySpeed(stack, state);
		if (speed == 1.0F)
		{
			return speed;
		}

		Player player = getPlayerHoldingItem(stack);
		if (player != null)
		{
			if (player.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player))
			{
				speed *= 3.0F;
			}

			if (!player.onGround())
			{
				speed *= 3.0F;
			}
		}

		return speed * this.extraSpeedMultiplier;
	}

	@Override
	public int energyUse(ItemStack stack, Level world, BlockPos pos, BlockState state)
	{
		Item item = stack.getItem();
		if (item == Ic2rItems.DRILL)
		{
			return 6;
		} else if (item == Ic2rItems.DIAMOND_DRILL)
		{
			return 20;
		} else if (item == Ic2rItems.IRIDIUM_DRILL)
		{
			return 200;
		} else
		{
			throw new IllegalArgumentException("Invalid drill: " + StackUtil.toStringSafe(stack));
		}
	}

	@Override
	public int breakTime(ItemStack stack, Level world, BlockPos pos, BlockState state)
	{
		Item item = stack.getItem();
		if (item == Ic2rItems.DRILL)
		{
			return 200;
		} else if (item == Ic2rItems.DIAMOND_DRILL)
		{
			return 50;
		} else if (item == Ic2rItems.IRIDIUM_DRILL)
		{
			return 20;
		} else
		{
			throw new IllegalArgumentException("Invalid drill: " + StackUtil.toStringSafe(stack));
		}
	}

	@Override
	public boolean breakBlock(ItemStack stack, Level world, BlockPos pos, BlockState state)
	{
		Item item = stack.getItem();
		if (item == Ic2rItems.DRILL)
		{
			return this.tryUsePower(stack, 50.0);
		} else if (item == Ic2rItems.DIAMOND_DRILL)
		{
			return this.tryUsePower(stack, 80.0);
		} else if (item == Ic2rItems.IRIDIUM_DRILL)
		{
			return this.tryUsePower(stack, 800.0);
		} else
		{
			throw new IllegalArgumentException("Invalid drill: " + StackUtil.toStringSafe(stack));
		}
	}

	@Override
	protected SoundEvent getIdleSound(LivingEntity player, ItemStack stack)
	{
		return stack.getItem() == Ic2rItems.DRILL ? Ic2rSoundEvents.ITEM_DRILL_IDLE.get() : null;
	}

	@Override
	public SoundEvent getHitSoundForBlock(LocalPlayer player, Level world, BlockPos pos, ItemStack stack)
	{
		return null;
	}

	@Override
	public SoundEvent getBreakSoundForBlock(LocalPlayer player, Level world, BlockPos pos, ItemStack stack)
	{
		Block block = world.getBlockState(pos).getBlock();
		return block.defaultDestroyTime() >= 3.0F ? Ic2rSoundEvents.ITEM_DRILL_HARD.get() : Ic2rSoundEvents.ITEM_DRILL_SOFT.get();
	}
}

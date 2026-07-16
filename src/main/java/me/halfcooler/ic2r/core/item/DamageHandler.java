package me.halfcooler.ic2r.core.item;

import me.halfcooler.ic2r.core.IC2R;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DamageHandler
{
	public static int getDamage(ItemStack stack)
	{
		Item item = stack.getItem();
		return item == null ? 0 : stack.getDamageValue();
	}

	public static void setDamage(ItemStack stack, int damage, boolean displayOnly)
	{
		Item item = stack.getItem();
		if (item != null)
		{
			if (item instanceof IPseudoDamageItem)
			{
				if (!displayOnly)
				{
					throw new IllegalStateException("can't damage " + stack + " physically");
				}

				((IPseudoDamageItem) item).setStackDamage(stack, damage);
			} else if (stack.isDamageableItem())
			{
				stack.setDamageValue(damage);
			}
		}
	}

	public static int getMaxDamage(ItemStack stack)
	{
		Item item = stack.getItem();
		return item == null ? 0 : stack.getMaxDamage();
	}

	public static boolean damage(ItemStack stack, int damage, LivingEntity src, InteractionHand hand)
	{
		Item item = stack.getItem();
		if (item == null)
		{
			return false;
		} else if (src != null)
		{
			EquipmentSlot slot = hand != null
				? LivingEntity.getSlotForHand(hand)
				: src.getEquipmentSlotForItem(stack);
			stack.hurtAndBreak(damage, src, slot);
			return true;
		} else
		{
			// 1.21: ItemStack.hurt(int, RandomSource, ServerPlayer) removed; simulate damage without entity.
			if (!stack.isDamageableItem())
			{
				return false;
			}
			int newDamage = stack.getDamageValue() + damage;
			if (newDamage >= stack.getMaxDamage())
			{
				stack.shrink(1);
				return true;
			}
			stack.setDamageValue(newDamage);
			return true;
		}
	}
}

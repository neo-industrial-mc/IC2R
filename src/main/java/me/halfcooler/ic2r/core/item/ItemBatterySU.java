package me.halfcooler.ic2r.core.item;

import me.halfcooler.ic2r.api.item.ElectricItem;
import me.halfcooler.ic2r.api.item.IBoxable;
import me.halfcooler.ic2r.api.item.IItemHudInfo;
import me.halfcooler.ic2r.core.util.StackUtil;
import me.halfcooler.ic2r.core.util.Util;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemBatterySU extends Item implements IBoxable, IItemHudInfo
{
	public int capacity;
	public int tier;

	public ItemBatterySU(Properties settings, int capacity, int tier)
	{
		super(settings);
		this.capacity = capacity;
		this.tier = tier;
	}

	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		double energy = this.capacity;

		for (int i = 0; i < 9 && energy > 0.0; i++)
		{
			ItemStack target = player.getInventory().items.get(i);
			if (target != null && target != stack)
			{
				energy -= ElectricItem.manager.charge(target, energy, this.tier, true, false);
			}
		}

		if (!Util.isSimilar(energy, this.capacity))
		{
			stack = StackUtil.decSize(stack);
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
		} else
		{
			return new InteractionResultHolder<>(InteractionResult.PASS, stack);
		}
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack itemstack)
	{
		return true;
	}

	@Override
	public List<String> getHudInfo(ItemStack stack, boolean advanced)
	{
		List<String> info = new LinkedList<>();
		info.add(this.capacity + " EU");
		return info;
	}
}

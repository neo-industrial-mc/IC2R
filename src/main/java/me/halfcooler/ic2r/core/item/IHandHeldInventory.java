package me.halfcooler.ic2r.core.item;

import me.halfcooler.ic2r.core.IHasGui;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IHandHeldInventory
{
	IHasGui getInventory(Player var1, InteractionHand var2, ItemStack var3);
}

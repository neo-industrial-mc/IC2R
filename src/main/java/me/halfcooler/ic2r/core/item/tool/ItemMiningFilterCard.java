package me.halfcooler.ic2r.core.item.tool;

import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.item.IHandHeldInventory;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;
import me.halfcooler.ic2r.core.util.StackUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemMiningFilterCard extends Item implements IHandHeldInventory
{
	public ItemMiningFilterCard(Properties settings)
	{
		super(settings);
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if (!world.isClientSide)
		{
			this.getInventory(player, hand, stack).openManagedItem(player, hand, null);
		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	@Override
	public IHasGui getInventory(Player player, InteractionHand hand, ItemStack stack)
	{
		return new HandHeldMiningFilter(player, hand, stack);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, Item.TooltipContext world, @NotNull List<Component> tooltip, @NotNull TooltipFlag advanced)
	{
		CompoundTag nbt = stack.getTag();
		if (nbt != null)
		{
			boolean isBlacklist = !nbt.contains("blacklist") || nbt.getBoolean("blacklist");
			ListTag items = nbt.getList("Items", 10);
			int count = items.size();
			Ic2rTooltip.add(tooltip, Component.translatable(isBlacklist ? "ic2r.MiningFilter.gui.mode.blacklist" : "ic2r.MiningFilter.gui.mode.whitelist"));
			Ic2rTooltip.add(tooltip, Component.translatable("ic2r.MiningFilter.tooltip.entries", count));
		}
	}
}

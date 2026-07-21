package me.halfcooler.ic2r.core.item;

import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.util.LegacyItemStackNbt;
import me.halfcooler.ic2r.core.util.StackUtil;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;
import me.halfcooler.ic2r.core.util.Util;
import me.halfcooler.ic2r.core.uu.UuIndex;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

@NotClassic
public class ItemCrystalMemory extends Item
{
	public ItemCrystalMemory(Properties settings)
	{
		super(settings);
	}

	public void appendHoverText(@NotNull ItemStack stack, Item.TooltipContext world, @NotNull List<Component> tooltip, @NotNull TooltipFlag advanced)
	{
		ItemStack recorded = this.readItemStack(stack);
		if (!StackUtil.isEmpty(recorded))
		{
			Ic2rTooltip.add(tooltip, Component.translatable("item.ic2r.crystal_memory.tooltip.item", Component.translatable(recorded.getDescriptionId())));
			Ic2rTooltip.add(tooltip, Component.translatable("item.ic2r.crystal_memory.tooltip.uu_matter", Util.toSiString(UuIndex.instance.getInBuckets(recorded), 4)));
		} else
		{
			Ic2rTooltip.add(tooltip, Component.translatable("item.ic2r.crystal_memory.tooltip.empty"));
		}
	}

	public ItemStack readItemStack(ItemStack stack)
	{
		CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
		CompoundTag contentTag = nbt.getCompound("Pattern");
		return LegacyItemStackNbt.parseOptional(null, contentTag);
	}

	public void writeContentsTag(ItemStack stack, ItemStack recorded)
	{
		CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
		CompoundTag contentTag = new CompoundTag();
		LegacyItemStackNbt.saveInto(null, recorded, contentTag);
		nbt.put("Pattern", contentTag);
	}
}

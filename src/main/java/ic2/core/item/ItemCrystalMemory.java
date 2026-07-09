package ic2.core.item;

import ic2.core.profile.NotClassic;
import ic2.core.util.StackUtil;
import ic2.core.util.Ic2Tooltip;
import ic2.core.util.Util;
import ic2.core.uu.UuIndex;

import java.util.List;

import net.minecraft.ChatFormatting;
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
			Ic2Tooltip.add(tooltip, Component.translatable("item.ic2.crystal_memory.tooltip.item", Component.translatable(recorded.getDescriptionId())));
			Ic2Tooltip.add(tooltip, Component.literal(Component.translatable("item.ic2.crystal_memory.tooltip.uu_matter") + " " + Util.toSiString(UuIndex.instance.getInBuckets(recorded), 4) + "B"));
		} else
		{
			Ic2Tooltip.add(tooltip, Component.translatable("item.ic2.crystal_memory.tooltip.empty"));
		}
	}

	public ItemStack readItemStack(ItemStack stack)
	{
		CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
		CompoundTag contentTag = nbt.getCompound("Pattern");
		return ItemStack.parseOptional(net.minecraft.core.RegistryAccess.EMPTY, contentTag);
	}

	public void writeContentsTag(ItemStack stack, ItemStack recorded)
	{
		CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
		CompoundTag contentTag = new CompoundTag();
		recorded.save(net.minecraft.core.RegistryAccess.EMPTY, contentTag);
		nbt.put("Pattern", contentTag);
	}
}

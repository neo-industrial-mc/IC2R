package me.halfcooler.ic2r.core.item.crafting;

import me.halfcooler.ic2r.api.item.IBlockCuttingBlade;
import me.halfcooler.ic2r.core.item.type.BlockCuttingBladeType;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class BlockCuttingBlade extends Item implements IBlockCuttingBlade
{
	private final BlockCuttingBladeType type;

	public BlockCuttingBlade(Properties settings, BlockCuttingBladeType type)
	{
		super(settings);
		this.type = type;
	}

	@Override
	public int getHardness(ItemStack stack)
	{
		return switch (this.type)
		{
			case iron -> 3;
			case steel -> 6;
			case diamond -> 9;
		};
	}

	public void appendHoverText(@NotNull ItemStack stack, Item.TooltipContext world, @NotNull List<Component> tooltip, @NotNull TooltipFlag advanced)
	{
		Ic2rTooltip.add(tooltip, Component.translatable(switch (type)
		{
			case iron -> "ic2r.iron_cutting_blade.info";
			case steel -> "ic2r.steel_cutting_blade.info";
			case diamond -> "ic2r.diamond_cutting_blade.info";
		}));
		Ic2rTooltip.add(tooltip, Component.translatable("ic2r.cutting_blade.hardness", this.getHardness(stack)));
	}
}

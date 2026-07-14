package me.halfcooler.ic2r.core.item;

import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntity;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntityBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockIc2r extends BlockItem
{
	public ItemBlockIc2r(Block block, Properties properties)
	{
		super(block, properties);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag)
	{
		if (this.getBlock() instanceof Ic2rTileEntityBlock block)
		{
			Ic2rTileEntity dummyTe = block.getDummyTe();
			dummyTe.appendItemTooltip(stack, tooltip, flag);
		}
	}
}
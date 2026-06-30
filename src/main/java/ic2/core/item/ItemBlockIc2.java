package ic2.core.item;

import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockIc2 extends BlockItem
{
	public ItemBlockIc2(Block block, Properties properties)
	{
		super(block, properties);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag)
	{
		if (this.getBlock() instanceof Ic2TileEntityBlock block)
		{
			Ic2TileEntity dummyTe = block.getDummyTe();
			dummyTe.appendItemTooltip(stack, tooltip, flag);
		}
	}
}
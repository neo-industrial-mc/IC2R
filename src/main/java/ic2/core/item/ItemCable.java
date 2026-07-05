package ic2.core.item;

import ic2.core.block.wiring.AbstractCableBlock;
import ic2.core.energy.profile.ElectricalDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCable extends BlockItem
{
	private final AbstractCableBlock cableBlock;
	public ItemCable(Block block, Properties p)
	{
		super(block, p);
		this.cableBlock = (AbstractCableBlock) block;
	}

	@Override
	public void appendHoverText(@NotNull ItemStack item, @Nullable Level level, @NotNull List<Component> component, @NotNull TooltipFlag flag)
	{
		ElectricalDisplay.appendCableTooltip(cableBlock, component);
	}
}

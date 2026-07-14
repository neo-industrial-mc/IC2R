package me.halfcooler.ic2r.core.crop;

import me.halfcooler.ic2r.api.crops.CropSoilType;
import me.halfcooler.ic2r.api.item.IBoxable;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ItemCrop extends BlockItem implements IBoxable
{
	public ItemCrop(Properties settings)
	{
		super(Ic2rBlocks.CROP_STICK.get(), settings);
	}

	protected boolean canPlace(BlockPlaceContext context, @NotNull BlockState state)
	{
		return CropSoilType.contains(context.getLevel().getBlockState(context.getClickedPos().below()).getBlock()) && super.canPlace(context, state);
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack itemStack)
	{
		return true;
	}
}

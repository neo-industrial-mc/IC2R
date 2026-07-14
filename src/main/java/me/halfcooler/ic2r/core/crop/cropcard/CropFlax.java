package me.halfcooler.ic2r.core.crop.cropcard;

import me.halfcooler.ic2r.api.crops.CropProperties;
import me.halfcooler.ic2r.api.crops.ICropTile;
import me.halfcooler.ic2r.api.crops.ICropType;
import me.halfcooler.ic2r.core.crop.Ic2rCropCard;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class CropFlax extends Ic2rCropCard
{
	public CropFlax(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2rBlocks.FLAX_CROP;
	}

	@Override
	public String getDiscoveredBy()
	{
		return "Eloraam";
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(2, 1, 1, 2, 0, 1);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Silk", "Vine", "Addictive" };
	}

	@Override
	public boolean canGrow(ICropTile crop)
	{
		return crop.getCurrentAge() < this.getMaxAge() && crop.getLightLevel() >= 9;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		return new ItemStack(Items.STRING);
	}
}

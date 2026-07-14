package me.halfcooler.ic2r.core.crop.cropcard;

import me.halfcooler.ic2r.api.crops.CropProperties;
import me.halfcooler.ic2r.api.crops.ICropTile;
import me.halfcooler.ic2r.api.crops.ICropType;
import me.halfcooler.ic2r.core.crop.Ic2rCropCard;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class CropHops extends Ic2rCropCard
{
	public CropHops(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2rBlocks.HOPS_CROP;
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(5, 2, 2, 0, 1, 1);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Green", "Ingredient", "Wheat" };
	}

	@Override
	public int getGrowthDuration(ICropTile crop)
	{
		return 600;
	}

	@Override
	public boolean canGrow(ICropTile crop)
	{
		return crop.getCurrentAge() < this.getMaxAge() && crop.getLightLevel() >= 9;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		return new ItemStack(Ic2rItems.HOPS);
	}

	@Override
	public int getAgeAfterHarvest(ICropTile crop)
	{
		return 2;
	}
}

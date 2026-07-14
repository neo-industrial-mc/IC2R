package me.halfcooler.ic2r.core.crop.cropcard;

import me.halfcooler.ic2r.api.crops.CropProperties;
import me.halfcooler.ic2r.api.crops.ICropTile;
import me.halfcooler.ic2r.api.crops.ICropType;
import me.halfcooler.ic2r.core.crop.Ic2rCropCard;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class CropCoffee extends Ic2rCropCard
{
	public CropCoffee(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2rBlocks.COFFEE_CROP.get();
	}

	@Override
	public String getDiscoveredBy()
	{
		return "Snoochy";
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(7, 1, 4, 1, 2, 0);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Leaves", "Ingredient", "Beans" };
	}

	@Override
	public boolean canGrow(ICropTile crop)
	{
		return crop.getCurrentAge() < this.getMaxAge() && crop.getLightLevel() >= 9;
	}

	@Override
	public int getWeightInfluences(ICropTile crop, int humidity, int nutrients, int air)
	{
		return (int) (0.4 * humidity + 1.4 * nutrients + 1.2 * air);
	}

	@Override
	public int getGrowthDuration(ICropTile crop)
	{
		if (crop.getCurrentAge() == this.getMaxAge() - 2)
		{
			return (int) (super.getGrowthDuration(crop) * 0.5);
		} else
		{
			return crop.getCurrentAge() == this.getMaxAge() - 3 ? (int) (super.getGrowthDuration(crop) * 1.5) : super.getGrowthDuration(crop);
		}
	}

	@Override
	public boolean canBeHarvested(ICropTile crop)
	{
		return crop.getCurrentAge() >= this.getMaxAge() - 1;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		return crop.getCurrentAge() == this.getMaxAge() - 1 ? null : new ItemStack(Ic2rItems.COFFEE_BEANS);
	}

	@Override
	public int getAgeAfterHarvest(ICropTile crop)
	{
		return this.getMaxAge() - 2;
	}
}

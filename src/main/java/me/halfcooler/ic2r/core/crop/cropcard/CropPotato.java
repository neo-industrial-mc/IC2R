package me.halfcooler.ic2r.core.crop.cropcard;

import me.halfcooler.ic2r.api.crops.CropProperties;
import me.halfcooler.ic2r.api.crops.ICropTile;
import me.halfcooler.ic2r.api.crops.ICropType;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.crop.Ic2rCropCard;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class CropPotato extends Ic2rCropCard
{
	public CropPotato(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2rBlocks.POTATO_CROP;
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(2, 0, 4, 0, 0, 2);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Yellow", "Food", "Potato" };
	}

	@Override
	public boolean canGrow(ICropTile crop)
	{
		return crop.getCurrentAge() < 4 && crop.getLightLevel() >= 9;
	}

	@Override
	public int getOptimalHarvestAge(ICropTile crop)
	{
		return this.getMaxAge() - 1;
	}

	@Override
	public boolean canBeHarvested(ICropTile crop)
	{
		return crop.getCurrentAge() >= this.getMaxAge() - 1;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		if (crop.getCurrentAge() >= this.getMaxAge() && IC2R.random.nextInt(20) <= 0)
		{
			return new ItemStack(Items.POISONOUS_POTATO);
		} else
		{
			return crop.getCurrentAge() >= this.getMaxAge() - 1 ? new ItemStack(Items.POTATO) : null;
		}
	}
}

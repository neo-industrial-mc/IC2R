package me.halfcooler.ic2r.core.crop.cropcard;

import me.halfcooler.ic2r.api.crops.CropProperties;
import me.halfcooler.ic2r.api.crops.ICropTile;
import me.halfcooler.ic2r.api.crops.ICropType;
import me.halfcooler.ic2r.core.crop.Ic2rCropCard;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class CropReed extends Ic2rCropCard
{
	public CropReed(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2rBlocks.REED_CROP;
	}

	@Override
	public String getDiscoveredBy()
	{
		return "Notch";
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(2, 0, 0, 1, 0, 2);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Reed" };
	}

	@Override
	public int getWeightInfluences(ICropTile crop, int humidity, int nutrients, int air)
	{
		return (int) (humidity * 1.2 + nutrients + air * 0.8);
	}

	@Override
	public boolean canBeHarvested(ICropTile crop)
	{
		return crop.getCurrentAge() > 0;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		return new ItemStack(Items.SUGAR_CANE, crop.getCurrentAge());
	}

	@Override
	public boolean onEntityCollision(ICropTile crop, Entity entity)
	{
		return false;
	}

	@Override
	public int getGrowthDuration(ICropTile crop)
	{
		return 200;
	}
}

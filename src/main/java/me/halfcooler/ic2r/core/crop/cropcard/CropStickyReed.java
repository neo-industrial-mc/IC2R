package me.halfcooler.ic2r.core.crop.cropcard;

import me.halfcooler.ic2r.api.crops.CropProperties;
import me.halfcooler.ic2r.api.crops.ICropTile;
import me.halfcooler.ic2r.api.crops.ICropType;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.crop.Ic2rCropCard;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class CropStickyReed extends Ic2rCropCard
{
	public CropStickyReed(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2rBlocks.STICKY_REED_CROP;
	}

	@Override
	public String getDiscoveredBy()
	{
		return "raa1337";
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(4, 2, 0, 1, 0, 1);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Reed", "Resin" };
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
		return crop.getCurrentAge() <= this.getMaxAge() - 1 ? new ItemStack(Items.SUGAR_CANE, crop.getCurrentAge()) : new ItemStack(Ic2rItems.RESIN);
	}

	@Override
	public int getAgeAfterHarvest(ICropTile crop)
	{
		return crop.getCurrentAge() == this.getMaxAge() ? (byte) (2 - IC2R.random.nextInt(2)) : 0;
	}

	@Override
	public boolean onEntityCollision(ICropTile crop, Entity entity)
	{
		return false;
	}

	@Override
	public int getGrowthDuration(ICropTile crop)
	{
		return crop.getCurrentAge() == this.getMaxAge() ? 400 : 100;
	}
}

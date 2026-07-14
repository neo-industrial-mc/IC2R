package me.halfcooler.ic2r.core.crop.cropcard;

import me.halfcooler.ic2r.api.crops.CropProperties;
import me.halfcooler.ic2r.api.crops.ICropTile;
import me.halfcooler.ic2r.api.crops.ICropType;
import me.halfcooler.ic2r.core.crop.Ic2rCropCard;
import me.halfcooler.ic2r.core.crop.Ic2rCrops;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class CropNetherWart extends Ic2rCropCard
{
	public CropNetherWart(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2rBlocks.NETHER_WART_CROP;
	}

	@Override
	public String getDiscoveredBy()
	{
		return "Notch";
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(5, 4, 2, 0, 2, 1);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Red", "Nether", "Ingredient", "Soulsand" };
	}

	@Override
	public double dropGainChance()
	{
		return 2.0;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		return new ItemStack(Items.NETHER_WART, 1);
	}

	@Override
	public void tick(ICropTile crop)
	{
		if (crop.isBlockBelow(Blocks.SOUL_SAND))
		{
			if (this.canGrow(crop))
			{
				crop.setGrowthPoints(crop.getGrowthPoints() + 100);
			}
		} else if (crop.isBlockBelow(Blocks.SNOW) && crop.getWorldObj().random.nextInt(300) == 0)
		{
			crop.setCrop(Ic2rCrops.cropTerraWart);
		}
	}

	@Override
	public int getRootsLength(ICropTile crop)
	{
		return 5;
	}
}

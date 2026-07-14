package me.halfcooler.ic2r.core.crop.cropcard;

import me.halfcooler.ic2r.api.crops.CropProperties;
import me.halfcooler.ic2r.api.crops.ICropTile;
import me.halfcooler.ic2r.api.crops.ICropType;
import me.halfcooler.ic2r.core.crop.Ic2rCropCard;
import me.halfcooler.ic2r.core.crop.Ic2rCrops;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class CropTerraWart extends Ic2rCropCard
{
	public CropTerraWart(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2rBlocks.TERRA_WART_CROP.get();
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(5, 2, 4, 0, 3, 0);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Blue", "Aether", "Consumable", "Snow" };
	}

	@Override
	public double dropGainChance()
	{
		return 0.8;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		return new ItemStack(Ic2rItems.TERRA_WART);
	}

	@Override
	public void tick(ICropTile crop)
	{
		if (crop.isBlockBelow(Blocks.SNOW))
		{
			if (this.canGrow(crop))
			{
				crop.setGrowthPoints(crop.getGrowthPoints() + 100);
			}
		} else if (crop.isBlockBelow(Blocks.SOUL_SAND) && crop.getWorldObj().random.nextInt(300) == 0)
		{
			crop.setCrop(Ic2rCrops.cropNetherWart);
		}
	}

	@Override
	public int getRootsLength(ICropTile crop)
	{
		return 5;
	}
}

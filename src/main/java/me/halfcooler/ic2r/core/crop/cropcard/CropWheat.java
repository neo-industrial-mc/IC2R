package me.halfcooler.ic2r.core.crop.cropcard;

import me.halfcooler.ic2r.api.crops.CropProperties;
import me.halfcooler.ic2r.api.crops.ICropTile;
import me.halfcooler.ic2r.api.crops.ICropType;
import me.halfcooler.ic2r.core.crop.CropVanilla;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class CropWheat extends CropVanilla
{
	public CropWheat(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2rBlocks.WHEAT_CROP.get();
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(1, 0, 4, 0, 0, 2);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Yellow", "Food", "Wheat" };
	}

	@Override
	public ItemStack getProduct()
	{
		return new ItemStack(Items.WHEAT, 1);
	}

	@Override
	public ItemStack getSeeds()
	{
		return new ItemStack(Items.WHEAT_SEEDS);
	}

	@Override
	public int getAgeAfterHarvest(ICropTile crop)
	{
		return 2;
	}
}

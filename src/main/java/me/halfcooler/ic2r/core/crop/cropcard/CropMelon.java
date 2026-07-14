package me.halfcooler.ic2r.core.crop.cropcard;

import me.halfcooler.ic2r.api.crops.CropProperties;
import me.halfcooler.ic2r.api.crops.ICropTile;
import me.halfcooler.ic2r.api.crops.ICropType;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.crop.CropVanillaStem;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class CropMelon extends CropVanillaStem
{
	public CropMelon(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2rBlocks.MELON_CROP;
	}

	@Override
	public String getDiscoveredBy()
	{
		return "Chao";
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(2, 0, 4, 0, 2, 0);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Green", "Food", "Stem" };
	}

	@Override
	protected ItemStack getProduct()
	{
		return IC2R.random.nextInt(3) == 0 ? new ItemStack(Blocks.MELON) : new ItemStack(Items.MELON, IC2R.random.nextInt(4) + 2);
	}

	@Override
	protected ItemStack getSeeds()
	{
		return new ItemStack(Items.MELON_SEEDS, IC2R.random.nextInt(2) + 1);
	}

	@Override
	public int getGrowthDuration(ICropTile crop)
	{
		return crop.getCurrentAge() == this.getMaxAge() - 1 ? 700 : 250;
	}
}

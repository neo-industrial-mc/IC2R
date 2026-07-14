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

public class CropPumpkin extends CropVanillaStem
{
	public CropPumpkin(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2rBlocks.PUMPKIN_CROP.get();
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(1, 0, 1, 0, 3, 1);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Orange", "Decoration", "Stem" };
	}

	@Override
	protected ItemStack getProduct()
	{
		return new ItemStack(Blocks.PUMPKIN);
	}

	@Override
	protected ItemStack getSeeds()
	{
		return new ItemStack(Items.PUMPKIN_SEEDS, IC2R.random.nextInt(3) + 1);
	}

	@Override
	public int getGrowthDuration(ICropTile crop)
	{
		return crop.getCurrentAge() == this.getMaxAge() - 1 ? 600 : 200;
	}
}

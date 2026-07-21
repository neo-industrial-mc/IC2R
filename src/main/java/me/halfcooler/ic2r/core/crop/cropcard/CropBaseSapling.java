package me.halfcooler.ic2r.core.crop.cropcard;

import me.halfcooler.ic2r.api.crops.CropProperties;
import me.halfcooler.ic2r.api.crops.ICropTile;
import me.halfcooler.ic2r.api.crops.ICropType;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.crop.CropBase;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class CropBaseSapling extends CropBase
{
	protected final Block cropBlock;
	protected final String saplingName;
	protected final ItemStack cropSapling;

	public CropBaseSapling(ICropType cropType, Block cropBlock, String saplingName, ItemStack cropDrop, ItemStack cropSapling)
	{
		super(cropType, cropDrop);
		this.cropBlock = cropBlock;
		this.saplingName = "ic2r.crop." + saplingName;
		this.cropSapling = cropSapling;
	}

	@Override
	public Block getCropBlock()
	{
		return this.cropBlock;
	}

	@Override
	public String getSeedType()
	{
		return this.saplingName;
	}

	@Override
	public String getDiscoveredBy()
	{
		return "Speiger";
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(3, 1, 0, 4, 4, 0);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Leaves", "Sapling", "Green" };
	}

	@Override
	public boolean canGrow(ICropTile crop)
	{
		return crop.getCurrentAge() < this.getMaxAge() && crop.getLightLevel() >= 9;
	}

	@Override
	public ItemStack[] getGains(ICropTile crop)
	{
		List<ItemStack> drops = new ArrayList<>();
		drops.add(this.cropDrop.copy());
		if (IC2R.random.nextInt(100) >= 75)
		{
			drops.add(this.cropSapling.copy());
			// Cherry: if a sapling dropped, 25% chance for 4 pink petals.
			if (this.getId().equalsIgnoreCase("cherry_sapling") && IC2R.random.nextInt(100) >= 75)
			{
				drops.add(new ItemStack(Items.PINK_PETALS, 4));
			}
		}

		if (this.getId().equalsIgnoreCase("oak_sapling") && IC2R.random.nextInt(100) >= 75)
		{
			drops.add(new ItemStack(Items.APPLE));
		}

		return drops.toArray(new ItemStack[0]);
	}

	@Override
	public int getGrowthDuration(ICropTile crop)
	{
		return crop.getCurrentAge() >= this.getMaxAge() - 1 ? 150 : 600;
	}

	@Override
	public int getAgeAfterHarvest(ICropTile crop)
	{
		return this.getMaxAge() - 1;
	}
}

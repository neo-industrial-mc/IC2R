package me.halfcooler.ic2r.core.crop.cropcard;

import me.halfcooler.ic2r.api.crops.CropProperties;
import me.halfcooler.ic2r.api.crops.ICropTile;
import me.halfcooler.ic2r.api.crops.ICropType;

import java.util.Collection;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class CropBaseMetalUncommon extends CropBaseMetalCommon
{
	public CropBaseMetalUncommon(
		ICropType cropType, Block cropBlock, String[] cropAttributes, Collection<TagKey<Block>> cropRootsRequirement, ItemStack cropDrop
	)
	{
		super(cropType, cropBlock, cropAttributes, cropRootsRequirement, cropDrop);
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(6, 2, 0, 0, 2, 0);
	}

	@Override
	public double dropGainChance()
	{
		return Math.pow(0.95, this.getProperties().tier());
	}

	@Override
	public int getGrowthDuration(ICropTile crop)
	{
		return crop.getCurrentAge() == this.getMaxAge() - 1 ? 2200 : 750;
	}
}

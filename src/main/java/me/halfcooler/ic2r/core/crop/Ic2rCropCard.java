package me.halfcooler.ic2r.core.crop;

import me.halfcooler.ic2r.api.crops.CropCard;
import me.halfcooler.ic2r.api.crops.ICropType;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;

public abstract class Ic2rCropCard extends CropCard
{
	public Ic2rCropCard(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public String getUnlocalizedName()
	{
		return "ic2r.crop." + this.getId();
	}

	@Override
	public String getDiscoveredBy()
	{
		return "IC2R Team";
	}

	@Override
	public List<ResourceLocation> getTexturesLocation()
	{
		List<ResourceLocation> ret = new ArrayList<>(this.getMaxAge());

		for (int size = 1; size <= this.getMaxAge(); size++)
		{
			ret.add(ResourceLocation.fromNamespaceAndPath("ic2r", "blocks/crop/" + this.getId() + "_" + size));
		}

		return ret;
	}
}

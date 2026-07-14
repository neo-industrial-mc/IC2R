package me.halfcooler.ic2r.core.crop;

import me.halfcooler.ic2r.api.crops.ICropType;
import net.minecraft.world.item.ItemStack;

public abstract class CropBase extends Ic2rCropCard
{
	protected final ItemStack cropDrop;

	public CropBase(ICropType cropType, ItemStack cropDrop)
	{
		super(cropType);
		this.cropDrop = cropDrop;
	}
}

package me.halfcooler.ic2r.core.crop.cropcard;

import me.halfcooler.ic2r.api.crops.CropProperties;
import me.halfcooler.ic2r.api.crops.ICropTile;
import me.halfcooler.ic2r.api.crops.ICropType;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.crop.Ic2rCropCard;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class CropVenomilia extends Ic2rCropCard
{
	public CropVenomilia(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2rBlocks.VENOMILIA_CROP.get();
	}

	@Override
	public String getDiscoveredBy()
	{
		return "raGan";
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(3, 3, 1, 3, 3, 3);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Purple", "Flower", "Tulip", "Poison" };
	}

	@Override
	public boolean canGrow(ICropTile crop)
	{
		return crop.getCurrentAge() <= 3 && crop.getLightLevel() >= 12 || crop.getCurrentAge() == 4;
	}

	@Override
	public boolean canBeHarvested(ICropTile crop)
	{
		return crop.getCurrentAge() >= 3;
	}

	@Override
	public int getOptimalHarvestAge(ICropTile crop)
	{
		return 3;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		if (crop.getCurrentAge() == 4)
		{
			return new ItemStack(Ic2rItems.GRIN_POWDER);
		} else
		{
			return crop.getCurrentAge() >= 3 ? new ItemStack(Items.PURPLE_DYE) : null;
		}
	}

	@Override
	public int getAgeAfterHarvest(ICropTile crop)
	{
		return 2;
	}

	@Override
	public int getGrowthDuration(ICropTile crop)
	{
		return crop.getCurrentAge() >= 2 ? 600 : 400;
	}

	@Override
	public boolean onRightClick(ICropTile crop, Player player)
	{
		if (!player.isShiftKeyDown())
		{
			this.onEntityCollision(crop, player);
		}

		return crop.performManualHarvest();
	}

	@Override
	public boolean onLeftClick(ICropTile crop, Player player)
	{
		if (!player.isShiftKeyDown())
		{
			this.onEntityCollision(crop, player);
		}

		return crop.pick();
	}

	@Override
	public boolean onEntityCollision(ICropTile crop, Entity entity)
	{
		if (crop.getCurrentAge() == 4 && entity instanceof LivingEntity)
		{
			if (entity instanceof Player && entity.isShiftKeyDown() && IC2R.random.nextInt(50) != 0)
			{
				return super.onEntityCollision(crop, entity);
			}

			((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.POISON, (IC2R.random.nextInt(10) + 5) * 20, 0));
			crop.setCurrentAge(3);
			crop.updateState();
		}

		return super.onEntityCollision(crop, entity);
	}

	@Override
	public boolean isWeed(ICropTile crop)
	{
		return crop.getCurrentAge() == 4 && crop.getStatGrowth() >= 8;
	}
}

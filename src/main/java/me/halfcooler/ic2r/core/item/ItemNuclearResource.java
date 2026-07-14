package me.halfcooler.ic2r.core.item;

import me.halfcooler.ic2r.api.reactor.IBaseReactorComponent;
import me.halfcooler.ic2r.api.reactor.IReactor;
import me.halfcooler.ic2r.core.Ic2rPotion;
import me.halfcooler.ic2r.core.item.armor.ItemArmorHazmat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemNuclearResource extends Item implements IBaseReactorComponent
{
	private final int radiationDuration;
	private final int radiationAmplifier;

	public ItemNuclearResource(Properties settings, int radiationDuration, int radiationAmplifier)
	{
		super(settings);
		this.radiationDuration = radiationDuration;
		this.radiationAmplifier = radiationAmplifier;
	}

	@Override
	public boolean canBePlacedIn(ItemStack stack, IReactor reactor)
	{
		return false;
	}

	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level world, @NotNull Entity rawEntity, int slot, boolean selected)
	{
		if (rawEntity instanceof LivingEntity entity)
		{
			if (!ItemArmorHazmat.hasCompleteHazmat(entity))
			{
				Ic2rPotion.radiation.applyTo(entity, this.radiationDuration * 20, this.radiationAmplifier);
			}
		}
	}
}

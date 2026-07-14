package me.halfcooler.ic2r.core.item;

import me.halfcooler.ic2r.core.Ic2rPotion;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemTerraWart extends Item
{
	public ItemTerraWart(Properties settings)
	{
		super(settings);
	}

	public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level world, LivingEntity player)
	{
		player.removeEffect(MobEffects.CONFUSION);
		player.removeEffect(MobEffects.DIG_SLOWDOWN);
		player.removeEffect(MobEffects.HUNGER);
		player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
		player.removeEffect(MobEffects.WEAKNESS);
		player.removeEffect(MobEffects.BLINDNESS);
		player.removeEffect(MobEffects.POISON);
		player.removeEffect(MobEffects.WITHER);
		MobEffectInstance effect = player.getEffect(Ic2rPotion.radiation);
		if (effect != null)
		{
			if (effect.getDuration() <= 600)
			{
				player.removeEffect(Ic2rPotion.radiation);
			} else
			{
				player.removeEffect(Ic2rPotion.radiation);
				Ic2rPotion.radiation.applyTo(player, effect.getDuration() - 600, effect.getAmplifier());
			}
		}

		return super.finishUsingItem(stack, world, player);
	}
}

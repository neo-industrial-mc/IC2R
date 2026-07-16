package me.halfcooler.ic2r.core;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class Ic2rPotion extends MobEffect
{
	public static Ic2rPotion radiation;

	public Ic2rPotion(MobEffectCategory type, int liquidColor)
	{
		super(type, liquidColor);
	}

	/**
	 * Registry-backed holder for 1.21 APIs that require {@code Holder<MobEffect>}.
	 */
	public static Holder<MobEffect> radiationHolder()
	{
		return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(radiation);
	}

	public Holder<MobEffect> asHolder()
	{
		return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(this);
	}

	@Override
	public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier)
	{
		if (this == radiation)
		{
			if (Ic2rDamageSource.radiation == null)
			{
				Ic2rDamageSource.init(entity.level().registryAccess());
			}
			entity.hurt(Ic2rDamageSource.radiation, (float) amplifier / 100 + 0.5F);
		}
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier)
	{
		if (this == radiation)
		{
			int rate = 25 >> amplifier;
			return rate == 0 || duration % rate == 0;
		} else
		{
			return false;
		}
	}

	public void applyTo(LivingEntity entity, int duration, int amplifier)
	{
		MobEffectInstance effect = new MobEffectInstance(this.asHolder(), duration, amplifier);
		entity.addEffect(effect);
	}
}

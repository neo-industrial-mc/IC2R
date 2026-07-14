package me.halfcooler.ic2r.core.init;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.Ic2rPotion;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.ref.Ic2rFluids;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import net.minecraft.world.effect.MobEffectCategory;

public class BlocksItems
{
	public static void init()
	{
		initPotions();
		Ic2rFluids.init();
		Ic2rBlocks.init();
		Ic2rBlockEntities.init();
		Ic2rItems.init();
		Ic2rScreenHandlers.init();
		initMigration();
	}

	private static void initPotions()
	{
		Ic2rPotion.radiation = new Ic2rPotion(MobEffectCategory.HARMFUL, 5149489);
		IC2R.envProxy.registerStatusEffect(IC2R.getIdentifier("radiation"), Ic2rPotion.radiation);
	}

	private static void initMigration()
	{
	}
}

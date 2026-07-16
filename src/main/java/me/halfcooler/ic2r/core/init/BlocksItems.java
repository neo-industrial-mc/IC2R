package me.halfcooler.ic2r.core.init;

import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.ref.Ic2rFluids;
import me.halfcooler.ic2r.core.item.armor.jetpack.JetpackHandler;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;

public class BlocksItems
{
	public static void init()
	{
		// Status effects: queued in FmlMod ctor via EnvProxyForge.queueCoreStatusEffects()
		// (MOB_EFFECT freezes before BLOCK; cannot DeferredRegister here in onInitEarly).
		Ic2rFluids.init();
		Ic2rBlocks.init();
		Ic2rBlockEntities.init();
		Ic2rItems.init();
		JetpackHandler.init();
		Ic2rScreenHandlers.init();
		initMigration();
	}

	private static void initMigration()
	{
	}
}

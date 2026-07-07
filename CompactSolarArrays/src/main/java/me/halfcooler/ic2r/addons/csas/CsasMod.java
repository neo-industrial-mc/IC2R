package me.halfcooler.ic2r.addons.csas;

import me.halfcooler.ic2r.addons.csas.init.CsasBlockEntities;
import me.halfcooler.ic2r.addons.csas.init.CsasBlocks;
import me.halfcooler.ic2r.addons.csas.init.CsasConfig;
import me.halfcooler.ic2r.addons.csas.init.CsasItems;
import me.halfcooler.ic2r.addons.csas.init.CsasScreenHandlers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CsasMod.MOD_ID)
public final class CsasMod
{
	public static final String MOD_ID = "ic2r_csas";

	public CsasMod(FMLJavaModLoadingContext context)
	{
		IEventBus modBus = context.getModEventBus();
		context.registerConfig(ModConfig.Type.COMMON, CsasConfig.SPEC);
		CsasBlocks.BLOCKS.register(modBus);
		CsasItems.ITEMS.register(modBus);
		CsasBlockEntities.BLOCK_ENTITIES.register(modBus);
		CsasScreenHandlers.MENUS.register(modBus);
	}
}
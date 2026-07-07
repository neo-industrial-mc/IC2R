package me.halfcooler.ic2r.addons.csas.client;

import me.halfcooler.ic2r.addons.csas.CsasMod;
import me.halfcooler.ic2r.addons.csas.generator.gui.GuiCompactSolar;
import me.halfcooler.ic2r.addons.csas.init.CsasScreenHandlers;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CsasMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CsasClient
{
	private CsasClient()
	{
	}

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event)
	{
		event.enqueueWork(() -> MenuScreens.register(CsasScreenHandlers.COMPACT_SOLAR.get(), GuiCompactSolar::new));
	}
}
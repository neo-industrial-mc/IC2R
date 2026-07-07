package me.halfcooler.ic2r.addons.csas.init;

import ic2.core.IC2;
import me.halfcooler.ic2r.addons.csas.CsasMod;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CsasMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CsasCreativeTabs
{
	private CsasCreativeTabs()
	{
	}

	@SubscribeEvent
	public static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event)
	{
		if (event.getTab() != IC2.tabIc2GeneratorsAndWiring)
		{
			return;
		}

		event.accept(CsasItems.LOW_VOLTAGE_SOLAR_ARRAY);
		event.accept(CsasItems.MEDIUM_VOLTAGE_SOLAR_ARRAY);
		event.accept(CsasItems.HIGH_VOLTAGE_SOLAR_ARRAY);
		event.accept(CsasItems.SOLAR_HAT_LOW_VOLTAGE);
		event.accept(CsasItems.SOLAR_HAT_MEDIUM_VOLTAGE);
		event.accept(CsasItems.SOLAR_HAT_HIGH_VOLTAGE);
	}
}
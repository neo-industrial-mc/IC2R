package me.halfcooler.ic2r.integration.jade;

import me.halfcooler.ic2r.core.IC2R;

import java.util.function.Predicate;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;

/**
 * Extra Jade plugin options shown in Jade's in-game config UI
 * (Plugins → IC2R), stored in Jade's plugin config files.
 */
public final class Ic2rJadePluginConfigs
{
	// Machine text lines
	public static final ResourceLocation MACHINE_VOLTAGE = id("machine.voltage");
	public static final ResourceLocation MACHINE_POWER = id("machine.power");
	public static final ResourceLocation MACHINE_REDSTONE = id("machine.redstone_mode");
	public static final ResourceLocation MACHINE_ACTIVE = id("machine.active_state");

	// Energy bar (nested under provider uid "energy_storage" in Jade's plugin UI)
	public static final ResourceLocation ENERGY_DISPLAY = id("energy_storage.display");
	public static final ResourceLocation ENERGY_TEXT_MODE = id("energy_storage.text_mode");
	public static final ResourceLocation ENERGY_UNIT = id("energy_storage.unit");
	public static final ResourceLocation ENERGY_COLOR = id("energy_storage.color");
	public static final ResourceLocation ENERGY_COLOR2 = id("energy_storage.color2");

	// Progress bar (nested under provider uid "progress")
	public static final ResourceLocation PROGRESS_DISPLAY = id("progress.display");
	public static final ResourceLocation PROGRESS_TEXT_MODE = id("progress.text_mode");
	public static final ResourceLocation PROGRESS_COLOR = id("progress.color");
	public static final ResourceLocation PROGRESS_COLOR2 = id("progress.color2");

	private static final Predicate<String> NON_BLANK = s -> s != null && !s.isBlank() && s.length() <= 16;
	private static final Predicate<String> COLOR = Ic2rJadePluginConfigs::isValidColorString;

	private Ic2rJadePluginConfigs()
	{
	}

	public static void register(IWailaClientRegistration registration)
	{
		// Machine
		registration.addConfig(MACHINE_VOLTAGE, JadeDisplayMode.ALWAYS);
		registration.addConfig(MACHINE_POWER, JadeDisplayMode.ALWAYS);
		registration.addConfig(MACHINE_REDSTONE, JadeDisplayMode.ALWAYS);
		registration.addConfig(MACHINE_ACTIVE, JadeDisplayMode.SHIFT);

		// Energy
		registration.addConfig(ENERGY_DISPLAY, JadeDisplayMode.ALWAYS);
		registration.addConfig(ENERGY_TEXT_MODE, JadeEnergyTextMode.AMOUNT);
		registration.addConfig(ENERGY_UNIT, "EU", NON_BLANK);
		registration.addConfig(ENERGY_COLOR, "#FFAA0000", COLOR);
		registration.addConfig(ENERGY_COLOR2, "#FF660000", COLOR);

		// Progress
		registration.addConfig(PROGRESS_DISPLAY, JadeDisplayMode.ALWAYS);
		registration.addConfig(PROGRESS_TEXT_MODE, JadeProgressTextMode.BOTH);
		registration.addConfig(PROGRESS_COLOR, "#FF55FF55", COLOR);
		registration.addConfig(PROGRESS_COLOR2, "#FF55FF55", COLOR);

		// Pure client-side display options (no server sync needed).
		for (ResourceLocation key : new ResourceLocation[] {
			MACHINE_VOLTAGE, MACHINE_POWER, MACHINE_REDSTONE, MACHINE_ACTIVE,
			ENERGY_DISPLAY, ENERGY_TEXT_MODE, ENERGY_UNIT, ENERGY_COLOR, ENERGY_COLOR2,
			PROGRESS_DISPLAY, PROGRESS_TEXT_MODE, PROGRESS_COLOR, PROGRESS_COLOR2
		})
		{
			registration.markAsClientFeature(key);
		}
	}

	static boolean isValidColorString(String value)
	{
		if (value == null || value.isBlank())
		{
			return false;
		}

		String s = value.trim();
		if (s.startsWith("#"))
		{
			s = s.substring(1);
		} else if (s.regionMatches(true, 0, "0x", 0, 2))
		{
			s = s.substring(2);
		}

		if (s.length() != 6 && s.length() != 8)
		{
			return false;
		}

		try
		{
			Long.parseLong(s, 16);
			return true;
		} catch (NumberFormatException ignored)
		{
			return false;
		}
	}

	private static ResourceLocation id(String path)
	{
		return IC2R.getIdentifier(path);
	}
}

package me.halfcooler.ic2r.integration.jade;

import java.util.Locale;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.IProgressStyle;

/**
 * Client-side helpers for reading IC2R options from Jade's plugin config UI.
 * Only call from client code paths (tooltip rendering / client view groups).
 */
public final class JadeConfigHelper
{
	private JadeConfigHelper()
	{
	}

	public static IPluginConfig plugin()
	{
		return IWailaConfig.get().getPlugin();
	}

	public static JadeDisplayMode voltageMode()
	{
		return getEnum(Ic2rJadePluginConfigs.MACHINE_VOLTAGE, JadeDisplayMode.ALWAYS);
	}

	public static JadeDisplayMode powerMode()
	{
		return getEnum(Ic2rJadePluginConfigs.MACHINE_POWER, JadeDisplayMode.ALWAYS);
	}

	public static JadeDisplayMode redstoneMode()
	{
		return getEnum(Ic2rJadePluginConfigs.MACHINE_REDSTONE, JadeDisplayMode.ALWAYS);
	}

	public static JadeDisplayMode activeMode()
	{
		return getEnum(Ic2rJadePluginConfigs.MACHINE_ACTIVE, JadeDisplayMode.SHIFT);
	}

	public static JadeDisplayMode energyMode()
	{
		return getEnum(Ic2rJadePluginConfigs.ENERGY_DISPLAY, JadeDisplayMode.ALWAYS);
	}

	public static JadeDisplayMode progressMode()
	{
		return getEnum(Ic2rJadePluginConfigs.PROGRESS_DISPLAY, JadeDisplayMode.ALWAYS);
	}

	public static JadeEnergyTextMode energyTextMode()
	{
		return getEnum(Ic2rJadePluginConfigs.ENERGY_TEXT_MODE, JadeEnergyTextMode.AMOUNT);
	}

	public static JadeProgressTextMode progressTextMode()
	{
		// Default: elapsed / total recipe time + percent in parentheses.
		return getEnum(Ic2rJadePluginConfigs.PROGRESS_TEXT_MODE, JadeProgressTextMode.BOTH);
	}

	public static String energyUnit()
	{
		String unit = plugin().getString(Ic2rJadePluginConfigs.ENERGY_UNIT);
		return unit == null || unit.isBlank() ? "EU" : unit.trim();
	}

	/**
	 * @param defaultArgb ARGB used when the config string is empty or invalid
	 */
	public static int parseColor(String value, int defaultArgb)
	{
		if (value == null || value.isBlank())
		{
			return defaultArgb;
		}

		String s = value.trim();
		if (s.startsWith("#"))
		{
			s = s.substring(1);
		} else if (s.regionMatches(true, 0, "0x", 0, 2))
		{
			s = s.substring(2);
		}

		try
		{
			long parsed = Long.parseLong(s, 16);
			if (s.length() <= 6)
			{
				// RRGGBB → force full alpha
				parsed |= 0xFF000000L;
			}
			return (int) parsed;
		} catch (NumberFormatException ignored)
		{
			return defaultArgb;
		}
	}

	public static int progressColor()
	{
		return parseColor(plugin().getString(Ic2rJadePluginConfigs.PROGRESS_COLOR), 0xFF55FF55);
	}

	public static int progressColor2()
	{
		return parseColor(plugin().getString(Ic2rJadePluginConfigs.PROGRESS_COLOR2), progressColor());
	}

	public static int energyColor()
	{
		// Jade default energy bar colors: 0xFFAA0000 / 0xFF660000
		return parseColor(plugin().getString(Ic2rJadePluginConfigs.ENERGY_COLOR), 0xFFAA0000);
	}

	public static int energyColor2()
	{
		return parseColor(plugin().getString(Ic2rJadePluginConfigs.ENERGY_COLOR2), 0xFF660000);
	}

	public static IProgressStyle progressStyle()
	{
		return IElementHelper.get().progressStyle().color(progressColor(), progressColor2());
	}

	public static IProgressStyle energyStyle()
	{
		return IElementHelper.get().progressStyle().color(energyColor(), energyColor2());
	}

	public static Component formatEnergyText(long stored, long capacity, float ratio)
	{
		JadeEnergyTextMode mode = energyTextMode();
		if (mode == JadeEnergyTextMode.NONE)
		{
			return null;
		}

		String unit = energyUnit();
		int percent = Math.round(Math.min(1.0F, Math.max(0.0F, ratio)) * 100.0F);
		IDisplayHelper display = IDisplayHelper.get();
		String current = display.humanReadableNumber(stored, unit, false);
		String max = display.humanReadableNumber(capacity, unit, false);

		return switch (mode)
		{
			case AMOUNT -> Component.translatable("ic2r.jade.energy.amount", current, max);
			case PERCENT -> Component.translatable("ic2r.jade.progress", percent);
			case BOTH -> Component.translatable("ic2r.jade.energy.both", current, max, percent);
			case NONE -> null;
		};
	}

	/**
	 * @param current   ticks (if {@code timeBased}) or raw amount units
	 * @param max       total ticks / amount
	 * @param timeBased when true, format absolute values as seconds (recipe duration)
	 */
	public static Component formatProgressText(float ratio, long current, long max, boolean timeBased)
	{
		JadeProgressTextMode mode = progressTextMode();
		if (mode == JadeProgressTextMode.NONE)
		{
			return null;
		}

		int percent = Math.round(Math.min(1.0F, Math.max(0.0F, ratio)) * 100.0F);
		boolean hasAbsolute = max > 0L;

		if (!hasAbsolute || mode == JadeProgressTextMode.PERCENT)
		{
			return Component.translatable("ic2r.jade.progress", percent);
		}

		if (timeBased)
		{
			// Recipe machines: elapsed time / operation length, optional percent.
			String elapsed = formatSeconds(current);
			String total = formatSeconds(max);
			return switch (mode)
			{
				case FRACTION -> Component.translatable("ic2r.jade.progress.time", elapsed, total);
				case BOTH -> Component.translatable("ic2r.jade.progress.time_both", elapsed, total, percent);
				default -> Component.translatable("ic2r.jade.progress", percent);
			};
		}

		// Non-time progress (e.g. replicator UU amount).
		return switch (mode)
		{
			case FRACTION -> Component.translatable("ic2r.jade.progress.fraction", current, max);
			case BOTH -> Component.translatable("ic2r.jade.progress.both", current, max, percent);
			default -> Component.translatable("ic2r.jade.progress", percent);
		};
	}

	/** Formats game ticks as seconds for progress labels. */
	static String formatSeconds(long ticks)
	{
		double seconds = Math.max(0L, ticks) / 20.0;
		if (seconds >= 100.0)
		{
			return String.format(Locale.ROOT, "%.0f", seconds);
		}
		if (seconds >= 10.0 || seconds == Math.rint(seconds))
		{
			return String.format(Locale.ROOT, "%.1f", seconds);
		}
		return String.format(Locale.ROOT, "%.2f", seconds);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Enum<T>> T getEnum(ResourceLocation key, T fallback)
	{
		try
		{
			T value = (T) plugin().getEnum(key);
			return value != null ? value : fallback;
		} catch (RuntimeException ignored)
		{
			// Config not registered yet or wrong type during early load.
			return fallback;
		}
	}
}

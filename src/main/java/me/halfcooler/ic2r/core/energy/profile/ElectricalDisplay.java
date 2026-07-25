package me.halfcooler.ic2r.core.energy.profile;

import me.halfcooler.ic2r.api.energy.profile.IElectricalNode;
import me.halfcooler.ic2r.api.energy.profile.VoltageTier;
import me.halfcooler.ic2r.core.block.comp.Energy;
import me.halfcooler.ic2r.core.block.wiring.AbstractCableBlock;
import me.halfcooler.ic2r.core.energy.EnergyNetMode;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;

import java.math.BigDecimal;
import java.util.List;

import net.minecraft.network.chat.Component;

/**
 * Player-facing electrical text. Classic IC2R shows power tier / EU/t only;
 * GT mode (optional addon) shows voltage tiers and amperage.
 */
public final class ElectricalDisplay
{
	private ElectricalDisplay()
	{
	}

	public static boolean isGtDisplay()
	{
		return EnergyNetMode.isGt();
	}

	public static Component formatVoltage(VoltageTier tier)
	{
		if (isGtDisplay())
		{
			return Component.translatable("ic2r.electric.tooltip.voltage", formatTierWithValue(tier));
		}

		return Component.translatable("ic2r.item.tooltip.power_tier", tier.getIcTier());
	}

	public static Component formatPower(IElectricalNode node)
	{
		int power = resolvePowerEuPerTick(node);
		VoltageTier tier = node.getWorkingVoltage();
		double avgAmps = node.getAverageCurrent();
		if (avgAmps <= 0.0 && power > 0)
		{
			avgAmps = (double) power / Math.max(1, tier.getVoltage());
		}

		return formatPower(power, tier, avgAmps);
	}

	public static Component formatPower(int euPerTick, VoltageTier tier, double avgAmps)
	{
		if (!isGtDisplay())
		{
			return Component.translatable("ic2r.item.tooltip.Output", euPerTick);
		}

		if (avgAmps > 0.0 && avgAmps == Math.rint(avgAmps) && (int) avgAmps == 1)
		{
			return formatPower(euPerTick, tier, 1);
		}

		return Component.translatable("ic2r.electric.tooltip.power", euPerTick, formatAmperage(avgAmps));
	}

	public static Component formatPower(int euPerTick, VoltageTier tier, int amps)
	{
		if (!isGtDisplay())
		{
			return Component.translatable("ic2r.item.tooltip.Output", euPerTick);
		}

		return Component.translatable("ic2r.electric.tooltip.power.with_tier", euPerTick, amps, formatTierName(tier));
	}

	public static Component formatPowerCompact(int euPerTick, VoltageTier tier, int amps)
	{
		if (!isGtDisplay())
		{
			return Component.literal(euPerTick + " " + Component.translatable("ic2r.generic.text.EUt").getString());
		}

		return Component.translatable("ic2r.electric.tooltip.power.compact", euPerTick, amps, formatTierName(tier));
	}

	public static Component formatStorageOutput(Energy energy)
	{
		VoltageTier tier = energy.getWorkingVoltage();
		int euPerTick = tier.getVoltage() * energy.getMaxSourceAmperage();
		if (!isGtDisplay())
		{
			return Component.translatable("ic2r.item.tooltip.Output", euPerTick);
		}

		return Component.translatable("ic2r.electric.tooltip.output", formatPowerCompact(euPerTick, tier, energy.getMaxSourceAmperage()));
	}

	public static void appendCableTooltip(AbstractCableBlock block, List<Component> tooltip)
	{
		if (isGtDisplay())
		{
			CableSpec spec = CableSpec.forType(block.type);
			int loss = spec.getLossPerMeterPerAmp();
			if (block.getCableInsulation() == 0 && loss > 0)
			{
				loss *= 2;
			}

			Ic2rTooltip.add(tooltip, Component.translatable("ic2r.electric.tooltip.cable.max_voltage", formatTierWithValue(spec.getMaxVoltage())));
			Ic2rTooltip.add(tooltip, Component.translatable("ic2r.electric.tooltip.cable.max_amperage", spec.getMaxAmperage()));
			Ic2rTooltip.add(tooltip, Component.translatable("ic2r.electric.tooltip.cable.loss", loss));
		}
		else
		{
			Ic2rTooltip.add(tooltip, Component.translatable("item.ic2r.cable.tooltip0", block.type.capacity));
			Ic2rTooltip.add(tooltip, Component.translatable("item.ic2r.cable.tooltip1", block.getLoss()));
		}
	}

	public static String formatTierName(VoltageTier tier)
	{
		return Component.translatable(tier.getTranslationKey()).getString();
	}

	public static String formatTierWithValue(VoltageTier tier)
	{
		return formatTierName(tier) + " (" + tier.getVoltage() + " V)";
	}

	public static String formatAmperage(double amps)
	{
		if (amps == Math.rint(amps))
		{
			return String.valueOf((long) amps);
		}

		return BigDecimal.valueOf(amps).stripTrailingZeros().toPlainString();
	}

	private static int resolvePowerEuPerTick(IElectricalNode node)
	{
		if (node instanceof Energy energy)
		{
			int recipePower = energy.getElectricalProfile().getRecipePower();
			if (recipePower > 0)
			{
				return recipePower;
			}
		}

		if (node.getMaxSourceAmperage() > 0)
		{
			return node.getWorkingVoltage().getVoltage() * node.getMaxSourceAmperage();
		}

		return (int) Math.round(node.getWorkingVoltage().getVoltage() * node.getAverageCurrent());
	}
}

package ic2.core.energy.profile;

import ic2.api.energy.profile.IElectricalNode;
import ic2.api.energy.profile.VoltageTier;
import ic2.core.block.comp.Energy;
import ic2.core.block.wiring.AbstractCableBlock;
import ic2.core.energy.EnergyNetMode;
import ic2.core.init.IC2Config;
import ic2.core.util.Ic2Tooltip;

import java.math.BigDecimal;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

public final class ElectricalDisplay
{
	private ElectricalDisplay()
	{
	}

	public static Component formatVoltage(VoltageTier tier)
	{
		return Component.translatable("ic2.electric.tooltip.voltage", formatTierWithValue(tier));
	}

	public static Component formatVoltageWithValue(VoltageTier tier)
	{
		return Component.translatable("ic2.electric.tooltip.voltage", formatTierWithValue(tier));
	}

	public static Component formatPower(IElectricalNode node)
	{
		int power = resolvePowerEuPerTick(node);
		VoltageTier tier = node.getWorkingVoltage();
		double avgAmps = node.getAverageCurrent();
		if (avgAmps <= 0.0 && power > 0)
		{
			avgAmps = (double) power / tier.getVoltage();
		}

		return formatPower(power, tier, avgAmps);
	}

	public static Component formatPower(int euPerTick, VoltageTier tier, double avgAmps)
	{
		if (avgAmps > 0.0 && avgAmps == Math.rint(avgAmps) && (int) avgAmps == 1)
		{
			return formatPower(euPerTick, tier, 1);
		}

		return Component.translatable("ic2.electric.tooltip.power", euPerTick, formatAmperage(avgAmps));
	}

	public static Component formatPower(int euPerTick, VoltageTier tier, int amps)
	{
		return Component.translatable("ic2.electric.tooltip.power.with_tier", euPerTick, amps, formatTierName(tier));
	}

	public static Component formatPowerCompact(int euPerTick, VoltageTier tier, int amps)
	{
		return Component.translatable("ic2.electric.tooltip.power.compact", euPerTick, amps, formatTierName(tier));
	}

	public static Component formatStorageOutput(Energy energy)
	{
		VoltageTier tier = energy.getWorkingVoltage();
		int euPerTick = tier.getVoltage() * energy.getMaxSourceAmperage();
		return Component.translatable("ic2.electric.tooltip.output", formatPower(euPerTick, tier, energy.getMaxSourceAmperage()));
	}

	public static void appendCableTooltip(AbstractCableBlock block, List<Component> tooltip, TooltipFlag flag)
	{
		if (EnergyNetMode.fromConfig(IC2Config.misc.energyNetMode.get()) == EnergyNetMode.GT)
		{
			CableSpec spec = CableSpec.forType(block.type);
			int loss = spec.getLossPerMeterPerAmp();
			if (block.getCableInsulation() == 0 && loss > 0)
			{
				loss *= 2;
			}

			Ic2Tooltip.add(tooltip, Component.translatable("ic2.electric.tooltip.cable.max_voltage", formatTierWithValue(spec.getMaxVoltage())));
			Ic2Tooltip.add(tooltip, Component.translatable("ic2.electric.tooltip.cable.max_amperage", spec.getMaxAmperage()));
			Ic2Tooltip.add(tooltip, Component.translatable("ic2.electric.tooltip.cable.loss", loss));
		}
		else
		{
			Ic2Tooltip.add(tooltip, Component.translatable("item.ic2.cable.tooltip0", block.type.capacity));
			Ic2Tooltip.add(tooltip, Component.translatable("item.ic2.cable.tooltip1", block.getLoss()));
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
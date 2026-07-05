package ic2.core.energy.grid;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.profile.IElectricalNode;
import ic2.api.energy.profile.VoltageTier;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.tile.IMultiEnergySource;
import ic2.core.energy.profile.ElectricalProfile;

public final class ElectricalNodes
{
	private ElectricalNodes()
	{
	}

	public static IElectricalNode resolve(IEnergyTile tile)
	{
		if (tile instanceof IElectricalNode node)
		{
			return node;
		}

		return null;
	}

	public static double getBufferFill(IElectricalNode node)
	{
		return node.getEnergyBufferCapacity() - node.getEnergyBufferFree();
	}

	public static int getGtOfferAmps(IEnergySource source)
	{
		double offered = source.getOfferedEnergy();
		if (offered <= 0.0)
		{
			return 0;
		}

		IElectricalNode node = resolve(source);
		if (node != null)
		{
			int voltage = node.getWorkingVoltage().getVoltage();
			if (voltage <= 0 || offered < voltage)
			{
				return 0;
			}

			return Math.min(node.getMaxSourceAmperage(), (int) Math.floor(offered / voltage));
		}

		int tier = source.getSourceTier();
		if (tier < 0)
		{
			return 0;
		}

		int voltage = (int) EnergyNet.instance.getPowerFromTier(tier);
		if (voltage <= 0 || offered < voltage)
		{
			return 0;
		}

		return (int) Math.floor(offered / voltage);
	}

	public static int getGtDemandAmps(IEnergySink sink)
	{
		double demanded = sink.getDemandedEnergy();
		if (demanded <= 0.0)
		{
			return 0;
		}

		IElectricalNode node = resolve(sink);
		if (node != null)
		{
			int voltage = node.getWorkingVoltage().getVoltage();
			if (voltage <= 0)
			{
				return 0;
			}

			int fromBuffer = (int) Math.floor(node.getEnergyBufferFree() / voltage);
			if (fromBuffer <= 0)
			{
				return 0;
			}

			return Math.min(node.getMaxSinkAmperage(), fromBuffer);
		}

		int tier = sink.getSinkTier();
		if (tier < 0)
		{
			return 0;
		}

		int voltage = (int) EnergyNet.instance.getPowerFromTier(tier);
		return voltage > 0 ? (int) Math.floor(demanded / voltage) : 0;
	}

	/**
	 * Per-packet EU for sources, or working voltage (V) for sinks used in inject tier lookup.
	 * Falls back to {@link EnergyNet#getPowerFromTier(int)} when no electrical profile is available.
	 */
	public static double getPacketPower(IEnergyTile tile, int packetIndex)
	{
		if (tile instanceof IEnergySource source)
		{
			return getSourcePacketPower(source, packetIndex);
		}

		if (tile instanceof IEnergySink sink)
		{
			return getSinkInjectVoltage(sink);
		}

		return 0.0;
	}

	public static double getMaxOfferPower(IEnergySource source, int packetCount)
	{
		if (packetCount <= 0)
		{
			return 0.0;
		}

		IElectricalNode node = resolve(source);
		if (node == null)
		{
			int tier = source.getSourceTier();
			return tier < 0 ? 0.0 : EnergyNet.instance.getPowerFromTier(tier) * packetCount;
		}

		int ampsPerPacket = getSourceAmpsPerPacket(node, source, packetCount);
		return ampsPerPacket * node.getWorkingVoltage().getVoltage() * packetCount;
	}

	static double getInjectTierParameter(IEnergySink sink, double amount)
	{
		IElectricalNode node = resolve(sink);
		double powerForTier = node != null ? node.getWorkingVoltage().getVoltage() : amount;
		return EnergyNet.instance.getTierFromPower(powerForTier);
	}

	private static double getSourcePacketPower(IEnergySource source, int packetIndex)
	{
		IElectricalNode node = resolve(source);
		if (node == null)
		{
			int tier = source.getSourceTier();
			return tier < 0 ? 0.0 : EnergyNet.instance.getPowerFromTier(tier);
		}

		int packetCount = getSourcePacketCount(source);
		int ampsPerPacket = getSourceAmpsPerPacket(node, source, packetCount);
		return ampsPerPacket * node.getWorkingVoltage().getVoltage();
	}

	private static double getSinkInjectVoltage(IEnergySink sink)
	{
		IElectricalNode node = resolve(sink);
		if (node != null)
		{
			return node.getWorkingVoltage().getVoltage();
		}

		int tier = sink.getSinkTier();
		return tier < 0 ? 0.0 : EnergyNet.instance.getPowerFromTier(tier);
	}

	private static int getSourcePacketCount(IEnergySource source)
	{
		if (source instanceof IMultiEnergySource multi && multi.sendMultipleEnergyPackets())
		{
			return Math.max(1, multi.getMultipleEnergyPacketAmount());
		}

		return 1;
	}

	private static int getSourceAmpsPerPacket(IElectricalNode node, IEnergySource source, int packetCount)
	{
		int workingCurrent = node.getWorkingCurrent();
		if (workingCurrent > 0)
		{
			return workingCurrent;
		}

		int maxAmps = Math.max(1, node.getMaxSourceAmperage());
		if (source instanceof IMultiEnergySource multi && multi.sendMultipleEnergyPackets() && packetCount > 1)
		{
			return Math.max(1, maxAmps / packetCount);
		}

		return maxAmps;
	}

	/**
	 * Dev validation for PR-3 IC Mode packet caps (no test harness).
	 * Macerator-like LV profile: 2 EU/t → 1 A × 32 V = 32 EU per packet.
	 */
	static boolean validateIcModePacketCaps()
	{
		ElectricalProfile maceratorLike = new ElectricalProfile(VoltageTier.LV);
		maceratorLike.setRecipePower(2);
		int packetPower = maceratorLike.getWorkingCurrent() * maceratorLike.getWorkingVoltage().getVoltage();
		if (packetPower != 32)
		{
			return false;
		}

		ElectricalProfile lvGenerator = new ElectricalProfile(VoltageTier.LV);
		lvGenerator.setRecipePower(10);
		return lvGenerator.getWorkingCurrent() * lvGenerator.getWorkingVoltage().getVoltage() == 32;
	}
}
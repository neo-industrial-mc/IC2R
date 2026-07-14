package me.halfcooler.ic2r.core.energy.grid;

/**
 * Pure transfer formulas shared by EnergyNet calculators.
 * <p>
 * Extracted so Golden Suite cases (EN-IC / EN-GT) can be unit-tested without a Level,
 * BlockEntity, or live grid topology.
 */
public final class EnergyTransferMath
{
	private EnergyTransferMath()
	{
	}

	/**
	 * IC Mode: EU injectable at the sink after path loss.
	 * Mirrors {@code EnergyCalculatorUnified.emit}: {@code offer - path.loss}, floored at 0
	 * (no negative EU may enter a sink).
	 *
	 * @param offer    EU offered into the path this packet
	 * @param pathLoss cumulative conductor loss along the chosen path
	 * @return deliverable EU at sink, always ≥ 0
	 */
	public static double icInjectAmount(double offer, double pathLoss)
	{
		double injectAmount = offer - pathLoss;
		return injectAmount <= 0.0 ? 0.0 : injectAmount;
	}

	/**
	 * GT Mode: reduce 1A packet EU by one conductor segment's loss (V/m/A).
	 * Packet dies when remaining EU would drop to ≤ 0.
	 *
	 * @param packetEu            remaining EU in the 1A packet
	 * @param lossPerMeterPerAmp  cable loss for this block
	 * @return remaining packet EU, or 0 if the amp dies
	 */
	public static int gtReducePacketByConductor(int packetEu, int lossPerMeterPerAmp)
	{
		if (packetEu <= 0)
		{
			return 0;
		}

		int next = packetEu - lossPerMeterPerAmp;
		return next <= 0 ? 0 : next;
	}

	/**
	 * GT Mode: remaining EU in a 1A packet after traversing a sequence of conductor losses.
	 * Loss only shrinks packet EU; amperage is never fractional (see {@link #gtDeliverableAmps}).
	 */
	public static int gtPacketEuAfterPathLoss(int packetVoltage, int... lossesPerMeterPerAmp)
	{
		int packetEu = packetVoltage;
		if (lossesPerMeterPerAmp == null)
		{
			return Math.max(0, packetEu);
		}

		for (int loss : lossesPerMeterPerAmp)
		{
			packetEu = gtReducePacketByConductor(packetEu, loss);
			if (packetEu == 0)
			{
				return 0;
			}
		}

		return packetEu;
	}

	/**
	 * GT Mode: amps that actually arrive. 1A packets are indivisible — either the whole amp
	 * is delivered (if packet EU after loss &gt; 0) or none.
	 */
	public static int gtDeliverableAmps(int requestedAmps, int packetEuAfterLoss)
	{
		if (requestedAmps <= 0 || packetEuAfterLoss <= 0)
		{
			return 0;
		}

		return requestedAmps;
	}

	/**
	 * GT Mode: cable melts when packet voltage exceeds cable max voltage.
	 */
	public static boolean gtCableOverVoltage(int packetVoltage, int cableMaxVoltage)
	{
		return packetVoltage > cableMaxVoltage;
	}

	/**
	 * GT Mode: cable melts when cumulative amps on the conductor would exceed its rating.
	 */
	public static boolean gtCableOverCurrent(int currentLoadAmps, int ampsToSend, int cableMaxAmperage)
	{
		return currentLoadAmps + ampsToSend > cableMaxAmperage;
	}
}

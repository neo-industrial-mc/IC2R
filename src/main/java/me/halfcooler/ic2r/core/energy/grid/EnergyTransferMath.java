package me.halfcooler.ic2r.core.energy.grid;

/**
 * Pure transfer formulas shared by EnergyNet calculators.
 * <p>
 * Extracted so Golden Suite cases (EN-IC / EN-GT) can be unit-tested without a Level,
 * BlockEntity, or live grid topology.
 */
public final class EnergyTransferMath
{
	/** IC transformer insulation-strip threshold used by standard cables ({@code 9001}). */
	public static final double IC_INSULATION_BREAKDOWN_ENERGY = 9001.0;

	private EnergyTransferMath()
	{
	}

	// --- IC Mode: inject / multi-sink / protection / transformer ---

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
	 * IC Mode: EU charged to the source for a successful inject along a path
	 * ({@code delivered + path.loss}; rejected assumed 0).
	 */
	public static double icSourceConsumed(double deliveredToSink, double pathLoss)
	{
		if (deliveredToSink <= 0.0)
		{
			return 0.0;
		}

		return deliveredToSink + pathLoss;
	}

	/**
	 * IC Mode: one path step of sequential multi-sink distribution
	 * (mirrors {@code EnergyCalculatorUnified.emit} with rejected = 0).
	 *
	 * @return EU delivered to the sink this step (0 if path dead or no demand)
	 */
	public static double icDeliverToSink(double offer, double pathLoss, double sinkDemand)
	{
		if (offer <= 0.0 || sinkDemand <= 0.0)
		{
			return 0.0;
		}

		double injectAmount = icInjectAmount(offer, pathLoss);
		if (injectAmount <= 0.0)
		{
			return 0.0;
		}

		return Math.min(injectAmount, sinkDemand);
	}

	/**
	 * IC Mode: sequential multi-sink fill in fixed path order
	 * (mirrors {@code EnergyCalculatorUnified.distributeSingle} without shuffle / reject).
	 * <p>
	 * Invariant EN-IC-004: sum of delivered ≤ {@code offer}.
	 * Strategy EN-IC-005: earlier paths take min(demand, inject) before later paths see remainder.
	 *
	 * @param offer      total EU the source offers this packet
	 * @param pathLosses per-path cumulative loss (same length as {@code demands})
	 * @param demands    per-path sink demand remaining
	 * @return delivered EU per path (same length); empty array if inputs invalid
	 */
	public static double[] icDistributeSequential(double offer, double[] pathLosses, double[] demands)
	{
		if (pathLosses == null || demands == null || pathLosses.length != demands.length)
		{
			return new double[0];
		}

		double remaining = offer;
		double[] delivered = new double[pathLosses.length];

		for (int i = 0; i < pathLosses.length; i++)
		{
			if (remaining <= 0.0)
			{
				break;
			}

			double toSink = icDeliverToSink(remaining, pathLosses[i], demands[i]);
			delivered[i] = toSink;
			remaining -= icSourceConsumed(toSink, pathLosses[i]);
		}

		return delivered;
	}

	/**
	 * IC Mode path cache: prefer a newly discovered path only when its loss is
	 * <em>strictly</em> lower than the previously kept path
	 * ({@code prev == null || !(prev.loss <= loss)}).
	 */
	public static boolean icPreferNewPath(double previousLoss, double candidateLoss)
	{
		return candidateLoss < previousLoss;
	}

	/**
	 * IC Mode: conductor melts / is removed when packet energy exceeds breakdown energy
	 * (mirrors {@code applyCableEffects}: {@code amount > getConductorBreakdownEnergy()}).
	 */
	public static boolean icConductorBreakdown(double packetAmount, double conductorBreakdownEnergy)
	{
		return packetAmount > conductorBreakdownEnergy;
	}

	/**
	 * IC Mode: insulation strips when packet exceeds insulation breakdown but not conductor
	 * breakdown (else-if arm in {@code applyCableEffects}).
	 */
	public static boolean icInsulationBreakdown(double packetAmount, double insulationBreakdownEnergy, double conductorBreakdownEnergy)
	{
		return packetAmount > insulationBreakdownEnergy && packetAmount <= conductorBreakdownEnergy;
	}

	/**
	 * IC Mode: sink is over-volted when packet voltage exceeds the sink's rated max
	 * (mirrors {@code EnergyNetExplosions.isOverVoltage} comparison).
	 */
	public static boolean icSinkOverVoltage(double packetVoltage, double sinkMaxVoltage)
	{
		return packetVoltage > sinkMaxVoltage;
	}

	/**
	 * IC tier → max packet EU / voltage ladder used by EnergyNet
	 * ({@code EnergyNetGlobal.getPowerFromTier}).
	 */
	public static double icPowerFromTier(int tier)
	{
		if (tier < 14)
		{
			return 8 << tier * 2;
		}

		return tier < 30 ? 8.0 * Math.pow(4.0, tier) : 9.223372E18F;
	}

	/**
	 * Inverse of {@link #icPowerFromTier(int)} ({@code EnergyNetGlobal.getTierFromPower}).
	 */
	public static int icTierFromPower(double power)
	{
		return power <= 0.0 ? 0 : (int) Math.ceil(Math.log(power / 8.0) / Math.log(4.0));
	}

	/**
	 * Standard cable conductor breakdown energy: {@code capacity + 1}
	 * (mirrors {@code AbstractCableBlock.CableTe.getConductorBreakdownEnergy}).
	 */
	public static double icConductorBreakdownEnergy(int cableCapacity)
	{
		return cableCapacity + 1.0;
	}

	/**
	 * IC transformer output packet count
	 * ({@code TileEntityTransformer}: step-up → 1 high packet; step-down → 4 low packets).
	 */
	public static int icTransformerOutputPackets(boolean stepUp)
	{
		return stepUp ? 1 : 4;
	}

	/**
	 * IC transformer input-side amp demand for mode configuration
	 * (step-up sinks 4× low; step-down sinks 1× high).
	 */
	public static int icTransformerInputAmps(boolean stepUp)
	{
		return stepUp ? 4 : 1;
	}

	/**
	 * IC transformer energy conservation across one mode switch configuration
	 * (adjacent tiers: high = 4 × low). Ignores intentional path loss.
	 */
	public static boolean icTransformerConservesEnergy(int lowVoltage, int highVoltage, boolean stepUp)
	{
		int inputAmps = icTransformerInputAmps(stepUp);
		int outputPackets = icTransformerOutputPackets(stepUp);
		if (stepUp)
		{
			return (long) lowVoltage * inputAmps == (long) highVoltage * outputPackets;
		}

		return (long) highVoltage * inputAmps == (long) lowVoltage * outputPackets;
	}

	// --- GT Mode ---

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
	 * GT Mode: whole-amp offer from buffer EU at a voltage
	 * (mirrors {@code ElectricalNodes.getGtOfferAmps} floor; EN-GT-009 full 1A before output).
	 *
	 * @return amps that can leave the source (0 if buffer has less than one full amp)
	 */
	public static int gtOfferAmps(double offeredEu, int voltage)
	{
		if (offeredEu <= 0.0 || voltage <= 0 || offeredEu < voltage)
		{
			return 0;
		}

		return (int) Math.floor(offeredEu / voltage);
	}

	/**
	 * GT Mode: offer amps capped by source max amperage.
	 */
	public static int gtOfferAmpsCapped(double offeredEu, int voltage, int maxSourceAmperage)
	{
		if (maxSourceAmperage <= 0)
		{
			return 0;
		}

		return Math.min(maxSourceAmperage, gtOfferAmps(offeredEu, voltage));
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

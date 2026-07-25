package me.halfcooler.ic2r.core.energy.grid;

import me.halfcooler.ic2r.api.energy.EnergyNet;
import me.halfcooler.ic2r.api.energy.tile.IEnergyConductor;
import me.halfcooler.ic2r.api.energy.tile.IEnergyTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.List;

/**
 * Cached source→sink path used by energy calculators. Public for optional external solvers
 * (GT addon) that live outside this package.
 */
public class EnergyPath
{
	public final Node source;
	public final Node target;
	public final List<Node> conductors;
	public final double loss;
	public final Direction targetDirection;
	public final double minEffectEnergy;
	public final double minInsulationEnergyAbsorption;
	public final double minInsulationBreakdownEnergy;
	public final double minConductorBreakdownEnergy;
	public final int minX;
	public final int minY;
	public final int minZ;
	public final int maxX;
	public final int maxY;
	public final int maxZ;
	public int lastCalcId = -1;
	public double energySupplied;
	public double maxPacketConducted;

	EnergyPath(Node source, Node target, List<Node> conductors, double loss)
	{
		this(source, target, conductors, loss, null);
	}

	EnergyPath(Node source, Node target, List<Node> conductors, double loss, Direction knownTargetDirection)
	{
		this.source = source;
		this.target = target;
		this.conductors = conductors;
		this.loss = loss;
		if (knownTargetDirection != null)
		{
			this.targetDirection = knownTargetDirection;
		} else
		{
			NodeLink lastLink = conductors.isEmpty() ? source.getLinkTo(target) : target.getLinkTo(conductors.getLast());
			this.targetDirection = lastLink != null ? lastLink.getDirFrom(target) : null;
		}
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		double minInsulationEnergyAbsorption = Double.POSITIVE_INFINITY;
		double minInsulationBreakdownEnergy = Double.POSITIVE_INFINITY;
		double minConductorBreakdownEnergy = Double.POSITIVE_INFINITY;

		for (Node node : conductors)
		{
			IEnergyConductor conductor = (IEnergyConductor) node.getTile().getMainTile();
			minInsulationEnergyAbsorption = Math.min(minInsulationEnergyAbsorption, conductor.getInsulationEnergyAbsorption());
			minInsulationBreakdownEnergy = Math.min(minInsulationBreakdownEnergy, conductor.getInsulationBreakdownEnergy());
			minConductorBreakdownEnergy = Math.min(minConductorBreakdownEnergy, conductor.getConductorBreakdownEnergy());

			for (IEnergyTile tile : node.getTile().getSubTiles())
			{
				BlockPos pos = EnergyNet.instance.getPos(tile);
				minX = Math.min(minX, pos.getX());
				minY = Math.min(minY, pos.getY());
				minZ = Math.min(minZ, pos.getZ());
				maxX = Math.max(maxX, pos.getX());
				maxY = Math.max(maxY, pos.getY());
				maxZ = Math.max(maxZ, pos.getZ());
			}
		}

		this.minEffectEnergy = Math.min(Math.min(minInsulationEnergyAbsorption, minInsulationBreakdownEnergy), minConductorBreakdownEnergy);
		this.minInsulationEnergyAbsorption = minInsulationEnergyAbsorption;
		this.minInsulationBreakdownEnergy = minInsulationBreakdownEnergy;
		this.minConductorBreakdownEnergy = minConductorBreakdownEnergy;
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}
}

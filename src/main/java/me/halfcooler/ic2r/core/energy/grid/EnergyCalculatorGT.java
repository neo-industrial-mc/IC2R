package me.halfcooler.ic2r.core.energy.grid;

import me.halfcooler.ic2r.api.energy.EnergyNet;
import me.halfcooler.ic2r.api.energy.NodeStats;
import me.halfcooler.ic2r.api.energy.profile.IElectricalNode;
import me.halfcooler.ic2r.api.energy.tile.IEnergyConductor;
import me.halfcooler.ic2r.api.energy.tile.IEnergySink;
import me.halfcooler.ic2r.api.energy.tile.IEnergySource;
import me.halfcooler.ic2r.api.energy.tile.IEnergyTile;
import me.halfcooler.ic2r.api.energy.tile.IMultiEnergySource;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.energy.profile.CableSpec;
import me.halfcooler.ic2r.core.util.LogCategory;
import me.halfcooler.ic2r.core.util.Util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableInt;

public class EnergyCalculatorGT implements IEnergyCalculator
{
	private static final Direction[] DIRECTION_PRIORITY = {
		Direction.DOWN,
		Direction.UP,
		Direction.NORTH,
		Direction.SOUTH,
		Direction.WEST,
		Direction.EAST
	};

	private final EnergyCalculatorUnified pathCacheDelegate = new EnergyCalculatorUnified();

	@Override
	public void handleGridChange(Grid grid)
	{
		this.pathCacheDelegate.handleGridChange(grid);
	}

	@Override
	public boolean runSyncStep(EnergyNetLocal enet)
	{
		boolean foundAny = false;

		for (Tile tile : enet.getSources())
		{
			if (tile.isDisabled())
			{
				tile.setSourceData(0.0, 0);
				continue;
			}

			IEnergySource source = (IEnergySource) tile.getMainTile();
			IElectricalNode node = ElectricalNodes.resolve(source);
			int offerAmps = ElectricalNodes.getGtOfferAmps(source);
			if (offerAmps <= 0)
			{
				tile.setSourceData(0.0, 0);
				continue;
			}

			int voltage;
			if (node != null)
			{
				voltage = node.getWorkingVoltage().getVoltage();
			} else
			{
				int tier = source.getSourceTier();
				if (tier < 0)
				{
					if (EnergyNetSettings.logGridCalculationIssues)
					{
						IC2R.log.warn(LogCategory.EnergyNet, "Tile %s reported an invalid tier (%d).", Util.toString(source, enet.getWorld(), EnergyNet.instance.getPos(source)), tier);
					}

					tile.setSourceData(0.0, 0);
					continue;
				}

				voltage = (int) EnergyNet.instance.getPowerFromTier(tier);
			}

			if (source instanceof IMultiEnergySource multi && multi.sendMultipleEnergyPackets())
			{
				int packetAmount = multi.getMultipleEnergyPacketAmount();
				if (packetAmount <= 0)
				{
					tile.setSourceData(0.0, 0);
					continue;
				}
			}

			foundAny = true;
			tile.setSourceData((double) offerAmps * voltage, offerAmps);
		}

		if (!foundAny)
		{
			// Same sticky-stat issue as Unified: without a calc pass, path energy never ages out.
			GridData.advanceCalcIds(enet);
		}

		return foundAny;
	}

	@Override
	public boolean runSyncStep(Grid grid)
	{
		runCalculation(grid, GridData.get(grid));
		// GT transfer runs entirely on the server thread; async would call inject/draw and
		// CableSpec.fromConductor (world access) off-thread and deadlock when work remains.
		return false;
	}

	@Override
	public void runAsyncStep(Grid grid)
	{
	}

	@Override
	public NodeStats getNodeStats(Tile tile)
	{
		return this.pathCacheDelegate.getNodeStats(tile);
	}

	@Override
	public void dumpNodeInfo(Node node, String prefix, PrintStream console, PrintStream chat)
	{
		this.pathCacheDelegate.dumpNodeInfo(node, prefix, console, chat);
	}

	private static boolean runCalculation(Grid grid, GridData data)
	{
		if (!data.active)
		{
			return false;
		}

		List<Node> activeSources = data.activeSources;
		Map<Node, MutableInt> activeSinks = new IdentityHashMap<>();
		activeSources.clear();
		data.activeSinks.clear();
		int calcId = ++data.currentCalcId;

		for (Node node : grid.getNodes())
		{
			Tile tile = node.getTile();
			if (tile.isDisabled())
			{
				continue;
			}

			if (node.getType() == NodeType.Source && data.energySourceToEnergyPathMap.containsKey(node) && tile.getPacketCount() > 0 && tile.getAmount() > 0.0)
			{
				activeSources.add(node);
			} else if (node.getType() == NodeType.Sink)
			{
				IEnergySink sink = (IEnergySink) tile.getMainTile();
				int demandAmps = ElectricalNodes.getGtDemandAmps(sink);
				if (demandAmps > 0)
				{
					activeSinks.put(node, new MutableInt(demandAmps));
				}
			}
		}

		if (activeSources.isEmpty() || activeSinks.isEmpty())
		{
			return false;
		}

		Level world = grid.getEnergyNet().getWorld();
		RandomSource rand = RandomSource.create();
		boolean shufflePaths = (world.getGameTime() & 3L) != 0L;
		int sourcesOffset = activeSources.size() > 1 ? rand.nextInt(activeSources.size()) : 0;
		Map<Node, Integer> conductorAmpLoads = new IdentityHashMap<>();
		Set<Tile> cablesToRemove = Collections.newSetFromMap(new IdentityHashMap<>());
		Map<Tile, Double> sinksToExplode = new IdentityHashMap<>();

		for (int i = sourcesOffset; i < activeSources.size() && !activeSinks.isEmpty(); i++)
		{
			distribute(activeSources.get(i), data, activeSinks, shufflePaths, calcId, rand, conductorAmpLoads, cablesToRemove, sinksToExplode);
		}

		for (int i = 0; i < sourcesOffset && !activeSinks.isEmpty(); i++)
		{
			distribute(activeSources.get(i), data, activeSinks, shufflePaths, calcId, rand, conductorAmpLoads, cablesToRemove, sinksToExplode);
		}

		queueDeferredEffects(data, cablesToRemove, sinksToExplode);
		return true;
	}

	private static void distribute(
		Node srcNode,
		GridData data,
		Map<Node, MutableInt> activeSinks,
		boolean shufflePaths,
		int calcId,
		RandomSource rand,
		Map<Node, Integer> conductorAmpLoads,
		Set<Tile> cablesToRemove,
		Map<Tile, Double> sinksToExplode
	)
	{
		Tile tile = srcNode.getTile();
		int remainingAmps = tile.getPacketCount();
		if (remainingAmps <= 0)
		{
			return;
		}

		int voltage = getSourceVoltage(tile);
		if (voltage <= 0)
		{
			return;
		}

		List<EnergyPath> paths = data.energySourceToEnergyPathMap.get(srcNode);
		if (paths == null || paths.isEmpty())
		{
			return;
		}

		List<EnergyPath> orderedPaths = sortPathsByDirection(paths);
		int pathOffset = orderedPaths.size() > 1 && shufflePaths ? rand.nextInt(orderedPaths.size()) : 0;
		int ampsUsed = 0;

		while (remainingAmps > 0 && !activeSinks.isEmpty())
		{
			boolean progress = false;

			for (int pass = 0; pass < orderedPaths.size() && remainingAmps > 0; pass++)
			{
				EnergyPath path = orderedPaths.get((pass + pathOffset) % orderedPaths.size());
				int sent = emitAmps(path, 1, voltage, data, calcId, activeSinks, conductorAmpLoads, cablesToRemove, sinksToExplode);
				if (sent > 0)
				{
					remainingAmps -= sent;
					ampsUsed += sent;
					progress = true;
				}
			}

			if (!progress)
			{
				break;
			}
		}

		if (ampsUsed > 0)
		{
			IEnergySource source = (IEnergySource) tile.getMainTile();
			double draw = Math.min((double) ampsUsed * voltage, source.getOfferedEnergy());
			if (draw > 0.0)
			{
				source.drawEnergy(draw);
			}
		}

		if (remainingAmps > 0)
		{
			tile.setSourceData((double) remainingAmps * voltage, remainingAmps);
		} else
		{
			tile.setSourceData(0.0, 0);
		}
	}

	private static int emitAmps(
		EnergyPath path,
		int ampsToSend,
		int packetVoltage,
		GridData data,
		int calcId,
		Map<Node, MutableInt> activeSinks,
		Map<Node, Integer> conductorAmpLoads,
		Set<Tile> cablesToRemove,
		Map<Tile, Double> sinksToExplode
	)
	{
		Tile targetTile = path.target.getTile();
		if (targetTile.isDisabled())
		{
			return 0;
		}

		MutableInt sinkDemand = activeSinks.get(path.target);
		if (sinkDemand == null)
		{
			return 0;
		}

		ampsToSend = Math.min(ampsToSend, sinkDemand.intValue());
		if (ampsToSend <= 0)
		{
			return 0;
		}

		int packetEU = packetVoltage;
		int traversedConductors = 0;

		for (Node conductorNode : path.conductors)
		{
			Tile cableTile = conductorNode.getTile();
			IEnergyConductor conductor = (IEnergyConductor) cableTile.getMainTile();
			CableSpec cable = CableSpec.fromConductor(conductor);
			if (packetVoltage > cable.getMaxVoltage().getVoltage())
			{
				cablesToRemove.add(cableTile);
				addConductorAmpLoad(path.conductors, traversedConductors + 1, ampsToSend, conductorAmpLoads);
				return 0;
			}

			int currentLoad = conductorAmpLoads.getOrDefault(conductorNode, 0);
			if (currentLoad + ampsToSend > cable.getMaxAmperage())
			{
				cablesToRemove.add(cableTile);
				addConductorAmpLoad(path.conductors, traversedConductors + 1, ampsToSend, conductorAmpLoads);
				return 0;
			}

			packetEU -= cable.getLossPerMeterPerAmp();
			traversedConductors++;
			if (packetEU <= 0)
			{
				packetEU = 0;
				break;
			}
		}

		addConductorAmpLoad(path.conductors, traversedConductors, ampsToSend, conductorAmpLoads);
		if (packetEU <= 0)
		{
			return 0;
		}

		IEnergySink sink = (IEnergySink) targetTile.getMainTile();
		double totalEU = (double) ampsToSend * packetEU;
		double injectTier = ElectricalNodes.getInjectTierParameter(sink, totalEU);
		double rejected = sink.injectEnergy(path.targetDirection, totalEU, injectTier);
		if (rejected >= totalEU)
		{
			return 0;
		}

		double deliveredEU = totalEU - rejected;
		if (path.lastCalcId != calcId)
		{
			path.lastCalcId = calcId;
			path.energySupplied = 0.0;
			path.maxPacketConducted = 0.0;
		}

		path.energySupplied += deliveredEU;
		path.maxPacketConducted = Math.max(path.maxPacketConducted, packetVoltage);
		queueSinkExplosion(targetTile, sink, packetVoltage, sinksToExplode);
		sinkDemand.subtract(ampsToSend);
		if (sinkDemand.intValue() <= 0 || rejected > 0.0)
		{
			activeSinks.remove(path.target);
		}

		return ampsToSend;
	}

	private static void addConductorAmpLoad(List<Node> conductors, int count, int amps, Map<Node, Integer> conductorAmpLoads)
	{
		for (int i = 0; i < count && i < conductors.size(); i++)
		{
			conductorAmpLoads.merge(conductors.get(i), amps, Integer::sum);
		}
	}

	private static void queueSinkExplosion(Tile sinkTile, IEnergySink sink, int packetVoltage, Map<Tile, Double> sinksToExplode)
	{
		if (EnergyNetExplosions.isOverVoltage(sink, packetVoltage))
		{
			Double prev = sinksToExplode.get(sinkTile);
			double power = packetVoltage;
			if (prev == null || prev < power)
			{
				sinksToExplode.put(sinkTile, power);
			}
		}
	}

	private static int getSourceVoltage(Tile tile)
	{
		IEnergySource source = (IEnergySource) tile.getMainTile();
		IElectricalNode node = ElectricalNodes.resolve(source);
		if (node != null)
		{
			return node.getWorkingVoltage().getVoltage();
		}

		int tier = source.getSourceTier();
		return tier < 0 ? 0 : (int) EnergyNet.instance.getPowerFromTier(tier);
	}

	private static List<EnergyPath> sortPathsByDirection(List<EnergyPath> paths)
	{
		List<EnergyPath> sorted = new ArrayList<>(paths);
		sorted.sort(Comparator.comparingInt(EnergyCalculatorGT::getFirstHopPriority));
		return sorted;
	}

	private static int getFirstHopPriority(EnergyPath path)
	{
		Direction direction = getFirstHopDirection(path);
		if (direction == null)
		{
			return DIRECTION_PRIORITY.length;
		}

		for (int i = 0; i < DIRECTION_PRIORITY.length; i++)
		{
			if (DIRECTION_PRIORITY[i] == direction)
			{
				return i;
			}
		}

		return DIRECTION_PRIORITY.length;
	}

	private static Direction getFirstHopDirection(EnergyPath path)
	{
		Node first = path.conductors.isEmpty() ? path.target : path.conductors.get(0);
		NodeLink link = path.source.getLinkTo(first);
		return link != null ? link.getDirFrom(path.source) : null;
	}

	@Override
	public void applyDeferredEffects(EnergyNetLocal enet)
	{
		Level world = enet.getWorld();

		for (Grid grid : enet.getGrids())
		{
			GridData data = grid.getData();
			if (data == null || data.deferredCablesToRemove.isEmpty() && data.deferredSinksToExplode.isEmpty())
			{
				continue;
			}

			applyGtCableEffects(data.deferredCablesToRemove);
			applyExplosions(world, data.deferredSinksToExplode);
			data.deferredCablesToRemove.clear();
			data.deferredSinksToExplode.clear();
		}
	}

	private static void queueDeferredEffects(GridData data, Set<Tile> cablesToRemove, Map<Tile, Double> sinksToExplode)
	{
		data.deferredCablesToRemove.addAll(cablesToRemove);

		for (Entry<Tile, Double> entry : sinksToExplode.entrySet())
		{
			Double prev = data.deferredSinksToExplode.get(entry.getKey());
			double power = entry.getValue();
			if (prev == null || prev < power)
			{
				data.deferredSinksToExplode.put(entry.getKey(), power);
			}
		}
	}

	private static void applyGtCableEffects(Set<Tile> cablesToRemove)
	{
		if (!IC2RConfig.misc.enableEnetCableMeltdown.get())
		{
			return;
		}

		for (Tile tile : cablesToRemove)
		{
			((IEnergyConductor) tile.getMainTile()).removeConductor();
		}
	}

	private static void applyExplosions(Level world, Map<Tile, Double> sinksToExplode)
	{
		for (Entry<Tile, Double> entry : sinksToExplode.entrySet())
		{
			EnergyNetExplosions.explodeTile(world, entry.getKey(), entry.getValue());
		}
	}
}
package me.halfcooler.ic2r.core.energy.grid;

import me.halfcooler.ic2r.api.energy.EnergyNet;
import me.halfcooler.ic2r.api.energy.NodeStats;
import me.halfcooler.ic2r.api.energy.tile.IEnergyConductor;
import me.halfcooler.ic2r.api.energy.tile.IEnergySink;
import me.halfcooler.ic2r.api.energy.tile.IEnergySource;
import me.halfcooler.ic2r.api.energy.tile.IMultiEnergySource;
import me.halfcooler.ic2r.api.energy.tile.IEnergyTile;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.Ic2rDamageSource;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.util.LogCategory;
import me.halfcooler.ic2r.core.util.Util;

import java.io.PrintStream;
import java.util.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableDouble;

/**
 * Modern IC energy-net solver (A40.3).
 * <p>
 * Uses {@link EnergyTransferMath} as the single source of truth for all energy arithmetic.
 * Path-finding reuses the proven BFS + OptimizedGraph infrastructure; distribution and cable
 * effects are rewritten with clean, testable delegation to {@link EnergyTransferMath}.
 * <p>
 * Replaces {@link EnergyCalculatorUnified} as the default IC-mode calculator.
 */
public class IcEnergySolver implements IEnergyCalculator
{
	// ---- Path infrastructure (shared with legacy EnergyCalculatorUnified / GT) ----

	static Collection<EnergyPath> getPaths(Node node, GridData data)
	{
		if (node.getType() == NodeType.Source)
		{
			List<EnergyPath> ret = data.energySourceToEnergyPathMap.get(node);
			return ret == null ? Collections.emptyList() : ret;
		}

		List<EnergyPath> cached = data.pathCache.get(node);
		if (cached != null)
		{
			return cached;
		}

		List<EnergyPath> ret = new ArrayList<>();
		for (List<EnergyPath> paths : data.energySourceToEnergyPathMap.values())
		{
			for (EnergyPath path : paths)
			{
				if (node.getType() == NodeType.Sink)
				{
					if (path.target == node) ret.add(path);
				}
				else if (path.conductors.contains(node))
				{
					ret.add(path);
				}
			}
		}

		data.pathCache.put(node, ret);
		return ret;
	}

	static void updateCache(Grid grid, GridData data)
	{
		data.active = false;
		data.energySourceToEnergyPathMap.clear();
		data.activeSources.clear();
		data.activeSinks.clear();
		data.pathCache.clear();
		data.eventPaths.clear();
		data.deferredEventPaths.clear();
		data.currentCalcId = -1;

		Collection<Node> nodes = grid.getNodes();
		if (nodes.size() < 2) return;

		List<Node> sources = new ArrayList<>();
		int sinkCount = 0;
		for (Node node : nodes)
		{
			if (node.getType() == NodeType.Source) sources.add(node);
			else if (node.getType() == NodeType.Sink) sinkCount++;
		}

		if (sources.isEmpty() || sinkCount == 0) return;

		OptimizedGraph optGraph = buildOptimizedGraph(nodes);
		Map<Node, Node> parentMap = new IdentityHashMap<>();
		Map<Node, OptLink> incomingLinkMap = new IdentityHashMap<>();
		Map<Node, Double> lossMap = new IdentityHashMap<>();
		Queue<Node> queue = sources.size() <= EnergyNetSettings.bfsThreshold
			? new PriorityQueue<>(nodes.size(), Comparator.comparing(lossMap::get))
			: new ArrayDeque<>(nodes.size());

		Map<IEnergyTile, EnergyPath> paths = new LinkedHashMap<>();

		for (Node srcNode : sources)
		{
			lossMap.put(srcNode, 0.0);
			queue.add(srcNode);

			Node node;
			while ((node = queue.poll()) != null)
			{
				if (node.getType() == NodeType.Sink)
				{
					double loss = lossMap.get(node);
					IEnergyTile tile = node.getTile().getMainTile();
					EnergyPath prev = paths.get(tile);
					if (prev == null || EnergyTransferMath.icPreferNewPath(prev.loss, loss))
					{
						if (EnergyNetSettings.roundLossDown) loss = Math.floor(loss);
						paths.put(tile, new EnergyPath(srcNode, node,
							reconstructPathWithLinks(srcNode, node, parentMap, incomingLinkMap), loss));
						if (paths.size() == sinkCount) break;
					}
				}
				else if (node.getType() == NodeType.Conductor || node == srcNode)
				{
					double loss = lossMap.get(node);
					List<OptLink> optLinks = optGraph.nodeToLinks.get(node);
					if (optLinks != null)
					{
						for (OptLink optLink : optLinks)
						{
							Node neighbor = optLink.getNeighbor(node);
							if (neighbor.getType() == NodeType.Source)
							{
								List<EnergyPath> srcPaths = data.energySourceToEnergyPathMap.get(neighbor);
								if (srcPaths != null && !srcPaths.isEmpty())
								{
									double baseLoss = loss - optLink.loss;
									List<Node> pathToHere = null;
									for (EnergyPath cPath : srcPaths)
									{
										double cLoss = baseLoss + cPath.loss;
										IEnergyTile tile = cPath.target.getTile().getMainTile();
										EnergyPath prev = paths.get(tile);
										if (prev == null || EnergyTransferMath.icPreferNewPath(prev.loss, cLoss))
										{
											if (EnergyNetSettings.roundLossDown) cLoss = Math.floor(cLoss);
											if (pathToHere == null)
												pathToHere = reconstructPathWithLinks(srcNode, node, parentMap, incomingLinkMap);
											List<Node> conductors = new ArrayList<>(pathToHere.size() + optLink.skippedNodes.size() + cPath.conductors.size());
											conductors.addAll(pathToHere);
											conductors.addAll(optLink.skippedNodes);
											conductors.addAll(cPath.conductors);
											Direction targetDir = cPath.conductors.isEmpty() ? cPath.targetDirection : null;
											paths.put(tile, new EnergyPath(srcNode, cPath.target, conductors, cLoss, targetDir));
										}
									}
								}
							}
							else
							{
								double newLoss = loss + optLink.loss;
								Double prevLoss = lossMap.get(neighbor);
								if (prevLoss == null || prevLoss > newLoss)
								{
									if (prevLoss != null) queue.remove(neighbor);
									lossMap.put(neighbor, newLoss);
									parentMap.put(neighbor, node);
									incomingLinkMap.put(neighbor, optLink);
									queue.add(neighbor);
								}
							}
						}
					}
				}
			}

			if (!paths.isEmpty())
				data.energySourceToEnergyPathMap.put(srcNode, new ArrayList<>(paths.values()));

			lossMap.clear();
			parentMap.clear();
			incomingLinkMap.clear();
			paths.clear();
			queue.clear();
		}

		if (!data.energySourceToEnergyPathMap.isEmpty())
			data.active = true;
	}

	// ---- Graph optimization (shared) ----

	private static OptimizedGraph buildOptimizedGraph(Collection<Node> nodes)
	{
		Map<Node, List<OptLink>> nodeToLinks = new IdentityHashMap<>();
		for (Node node : nodes) nodeToLinks.put(node, new ArrayList<>());

		for (Node node : nodes)
		{
			for (NodeLink link : node.getLinks())
			{
				if (link.nodeA == node)
				{
					OptLink optLink = new OptLink(link.nodeA, link.nodeB, link.getLoss(), new ArrayList<>());
					nodeToLinks.get(link.nodeA).add(optLink);
					nodeToLinks.get(link.nodeB).add(optLink);
				}
			}
		}

		boolean changed;
		do
		{
			changed = false;
			Iterator<Map.Entry<Node, List<OptLink>>> it = nodeToLinks.entrySet().iterator();
			while (it.hasNext())
			{
				Map.Entry<Node, List<OptLink>> entry = it.next();
				Node node = entry.getKey();
				if (node.getType() != NodeType.Conductor) continue;

				List<OptLink> links = entry.getValue();
				if (links.isEmpty()) { it.remove(); changed = true; }
				else if (links.size() == 1)
				{
					OptLink link = links.get(0);
					Node neighbor = link.getNeighbor(node);
					List<OptLink> neighborLinks = nodeToLinks.get(neighbor);
					if (neighborLinks != null) neighborLinks.remove(link);
					it.remove();
					changed = true;
				}
				else if (links.size() == 2)
				{
					OptLink link1 = links.get(0), link2 = links.get(1);
					Node neighbor1 = link1.getNeighbor(node), neighbor2 = link2.getNeighbor(node);
					List<OptLink> links1 = nodeToLinks.get(neighbor1), links2 = nodeToLinks.get(neighbor2);
					if (links1 != null) links1.remove(link1);
					if (links2 != null) links2.remove(link2);

					if (neighbor1 != neighbor2)
					{
						List<Node> skipped = new ArrayList<>(link1.skippedNodes);
						skipped.add(node);
						skipped.addAll(link2.skippedNodes);
						OptLink merged = new OptLink(neighbor1, neighbor2, link1.loss + link2.loss, skipped);
						if (links1 != null) links1.add(merged);
						if (links2 != null) links2.add(merged);
					}
					it.remove();
					changed = true;
				}
			}
		} while (changed);

		return new OptimizedGraph(nodeToLinks);
	}

	private static List<Node> reconstructPathWithLinks(Node srcNode, Node dstNode, Map<Node, Node> parentMap, Map<Node, OptLink> incomingLinkMap)
	{
		List<Node> ret = new ArrayList<>();
		Node node = dstNode;
		while (true)
		{
			Node parent = parentMap.get(node);
			if (parent == null) break;
			OptLink link = incomingLinkMap.get(node);
			if (link != null)
			{
				List<Node> skipped = link.nodeA == parent
					? new ArrayList<>(link.skippedNodes)
					: link.skippedNodes;
				if (link.nodeA == parent) Collections.reverse(skipped);
				ret.addAll(skipped);
			}
			if (parent == srcNode) break;
			ret.add(parent);
			node = parent;
		}
		Collections.reverse(ret);
		return ret;
	}

	// ---- Modern IC distribution (EnergyTransferMath as single source of truth) ----

	private static boolean runCalculation(Grid grid, GridData data)
	{
		if (!data.active) return false;

		List<Node> activeSources = data.activeSources;
		Map<Node, MutableDouble> activeSinks = data.activeSinks;
		activeSources.clear();
		activeSinks.clear();
		int calcId = ++data.currentCalcId;

		for (Node node : grid.getNodes())
		{
			Tile tile = node.getTile();
			if (tile.isDisabled()) continue;

			if (node.getType() == NodeType.Source
				&& data.energySourceToEnergyPathMap.containsKey(node)
				&& tile.getAmount() > 0.0)
			{
				activeSources.add(node);
			}
			else if (node.getType() == NodeType.Sink)
			{
				double amount = ((IEnergySink) tile.getMainTile()).getDemandedEnergy();
				if (amount > 0.0) activeSinks.put(node, new MutableDouble(amount));
			}
		}

		if (activeSources.isEmpty() || activeSinks.isEmpty()) return false;

		Level world = grid.getEnergyNet().getWorld();
		RandomSource rand = world.random;
		boolean shufflePaths = (world.getGameTime() & 3L) != 0L;
		int sourcesOffset = activeSources.size() > 1 ? rand.nextInt(activeSources.size()) : 0;

		for (int i = sourcesOffset; i < activeSources.size() && !activeSinks.isEmpty(); i++)
			distributeFromSource(activeSources.get(i), data, shufflePaths, calcId);
		for (int i = 0; i < sourcesOffset && !activeSinks.isEmpty(); i++)
			distributeFromSource(activeSources.get(i), data, shufflePaths, calcId);

		if (!data.eventPaths.isEmpty())
		{
			data.deferredEventPaths.addAll(data.eventPaths);
			data.eventPaths.clear();
		}

		return true;
	}

	/**
	 * Distribute energy from one source across all its paths using
	 * {@link EnergyTransferMath#icDeliverToSink} / {@link EnergyTransferMath#icSourceConsumed}.
	 */
	private static void distributeFromSource(Node srcNode, GridData data, boolean shufflePaths, int calcId)
	{
		Tile tile = srcNode.getTile();
		int packetCount = tile.getPacketCount();
		if (packetCount <= 0) return;

		List<EnergyPath> paths = data.energySourceToEnergyPathMap.get(srcNode);
		if (paths == null || paths.isEmpty()) return;

		double totalOffer = tile.getAmount();
		if (totalOffer <= 0.0) return;

		IEnergySource source = (IEnergySource) tile.getMainTile();

		if (packetCount == 1)
		{
			totalOffer = deliverAlongPaths(totalOffer, paths, shufflePaths ? null : null, data, calcId);
		}
		else
		{
			double power = ElectricalNodes.getPacketPower(source, 0);
			int remainingPackets = packetCount;
			while (remainingPackets > 0 && totalOffer > 0.0)
			{
				double packetOffer = Math.min(totalOffer, power);
				double used = packetOffer - deliverAlongPaths(packetOffer, paths, null, data, calcId);
				if (used <= 0.0) break;
				totalOffer -= used;
				remainingPackets--;
			}
		}

		double used = tile.getAmount() - Math.max(0.0, totalOffer);
		if (used > 0.0)
		{
			tile.setAmount(Math.max(0.0, totalOffer));
			source.drawEnergy(used);
		}
	}

	/**
	 * Sequential distribution along paths (fixed order).
	 * Uses {@link EnergyTransferMath} for inject / source-consumed arithmetic.
	 */
	private static double deliverAlongPaths(double offer, List<EnergyPath> paths, Object unused, GridData data, int calcId)
	{
		for (EnergyPath path : paths)
		{
			if (offer <= 0.0) break;

			Tile targetTile = path.target.getTile();
			if (targetTile.isDisabled()) continue;

			MutableDouble sinkDemand = data.activeSinks.get(path.target);
			if (sinkDemand == null) continue;

			double injectAmount = EnergyTransferMath.icInjectAmount(offer, path.loss);
			if (injectAmount <= 0.0) continue;

			IEnergySink sink = (IEnergySink) targetTile.getMainTile();
			double amount = Math.min(injectAmount, sinkDemand.doubleValue());
			double rejected = sink.injectEnergy(path.targetDirection, amount,
				ElectricalNodes.getInjectTierParameter(sink, amount));

			if (rejected >= amount) continue;

			double effectiveAmount = Math.max(0.0, amount - rejected + path.loss);

			// Track per-path stats for cable effects and node stats
			if (path.lastCalcId != calcId)
			{
				path.lastCalcId = calcId;
				path.energySupplied = 0.0;
				path.maxPacketConducted = 0.0;
			}
			path.energySupplied += amount - rejected;
			path.maxPacketConducted = Math.max(effectiveAmount, path.maxPacketConducted);

			if (effectiveAmount > path.minEffectEnergy
				|| amount > EnergyNet.instance.getPowerFromTier(sink.getSinkTier()))
			{
				data.eventPaths.add(path);
			}

			if (amount >= sinkDemand.doubleValue() || rejected > 0.0)
				data.activeSinks.remove(path.target);

			offer -= EnergyTransferMath.icSourceConsumed(amount - rejected, path.loss);
		}

		return offer;
	}

	// ---- Cable effects (delegated to EnergyTransferMath) ----

	private static void applyCableEffects(Collection<EnergyPath> eventPaths, Level world)
	{
		if (!IC2RConfig.misc.enableEnetCableMeltdown.get()) return;

		Set<Tile> cablesToRemove = Collections.newSetFromMap(new IdentityHashMap<>());
		Set<Tile> cablesToStrip = Collections.newSetFromMap(new IdentityHashMap<>());
		Map<Tile, MutableDouble> sinksToExplode = new IdentityHashMap<>();
		Map<LivingEntity, MutableDouble> shockEnergyMap = new IdentityHashMap<>();

		for (EnergyPath path : eventPaths)
		{
			if (path == null) continue;

			double amount = path.maxPacketConducted;
			if (amount > path.minConductorBreakdownEnergy || amount > path.minInsulationBreakdownEnergy)
			{
				for (Node node : path.conductors)
				{
					Tile tile = node.getTile();
					IEnergyConductor conductor = (IEnergyConductor) tile.getMainTile();
					double conductorLimit = conductor.getConductorBreakdownEnergy();
					double insulationLimit = conductor.getInsulationBreakdownEnergy();

					if (EnergyTransferMath.icConductorBreakdown(amount, conductorLimit))
						cablesToRemove.add(tile);
					else if (EnergyTransferMath.icInsulationBreakdown(amount, insulationLimit, conductorLimit))
						cablesToStrip.add(tile);
				}
			}

			// Shock nearby entities
			if (amount > path.minInsulationEnergyAbsorption)
			{
				List<LivingEntity> nearbyEntities = world.getEntitiesOfClass(LivingEntity.class,
					new AABB(path.minX - 1, path.minY - 1, path.minZ - 1,
						path.maxX + 2, path.maxY + 2, path.maxZ + 2),
					EntitySelector.LIVING_ENTITY_STILL_ALIVE);

				if (!nearbyEntities.isEmpty())
				{
					Map<LivingEntity, MutableDouble> localShockEnergyMap = new IdentityHashMap<>();
					for (Node node : path.conductors)
					{
						Tile tile = node.getTile();
						IEnergyConductor conductor = (IEnergyConductor) tile.getMainTile();
						if (amount <= conductor.getInsulationEnergyAbsorption()) continue;

						int shockEnergy = (int) (amount - conductor.getInsulationEnergyAbsorption());
						for (IEnergyTile subTile : tile.getSubTiles())
						{
							BlockPos pos = EnergyNet.instance.getPos(subTile);
							for (LivingEntity entity : nearbyEntities)
							{
								MutableDouble prev = localShockEnergyMap.get(entity);
								if ((prev == null || !(prev.doubleValue() >= shockEnergy))
									&& entity.getBoundingBox().intersects(
										new AABB(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
											pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2)))
								{
									if (prev == null) localShockEnergyMap.put(entity, new MutableDouble(shockEnergy));
									else prev.setValue(shockEnergy);
								}
							}
						}
					}

					for (Map.Entry<LivingEntity, MutableDouble> entry : localShockEnergyMap.entrySet())
						shockEnergyMap.merge(entry.getKey(), entry.getValue(),
							(a, b) -> { a.add(b.doubleValue()); return a; });
				}
			}

			// Sink over-voltage
			Tile sinkTile = path.target.getTile();
			IEnergySink sink = (IEnergySink) sinkTile.getMainTile();
			if (EnergyNetExplosions.isOverVoltage(sink, amount))
			{
				MutableDouble prev = sinksToExplode.get(sinkTile);
				if (prev == null || prev.doubleValue() < amount)
				{
					if (prev == null) sinksToExplode.put(sinkTile, new MutableDouble(amount));
					else prev.setValue(amount);
				}
			}
		}

		cablesToStrip.removeAll(cablesToRemove);
		for (Tile tile : cablesToRemove) ((IEnergyConductor) tile.getMainTile()).removeConductor();
		for (Tile tile : cablesToStrip) ((IEnergyConductor) tile.getMainTile()).removeInsulation();
		for (Map.Entry<Tile, MutableDouble> entry : sinksToExplode.entrySet())
			EnergyNetExplosions.explodeTile(world, entry.getKey(), entry.getValue().doubleValue());
		for (Map.Entry<LivingEntity, MutableDouble> entry : shockEnergyMap.entrySet())
		{
			LivingEntity target = entry.getKey();
			int damage = (int) Math.ceil(entry.getValue().doubleValue() / 64.0);
			if (target.isAlive() && damage > 0)
			{
				target.hurt(Ic2rDamageSource.electricity(target.level()), damage);
			}
		}
	}

	// ---- IEnergyCalculator implementation ----

	@Override
	public void handleGridChange(Grid grid)
	{
		updateCache(grid, GridData.get(grid));
	}

	@Override
	public boolean runSyncStep(EnergyNetLocal enet)
	{
		boolean foundAny = false;
		for (Tile tile : enet.getSources())
		{
			IEnergySource source = (IEnergySource) tile.getMainTile();
			double amount;
			int packets = 1;

			if (!tile.isDisabled()
				&& (amount = source.getOfferedEnergy()) > 0.0
				&& (!(source instanceof IMultiEnergySource multi)
					|| !multi.sendMultipleEnergyPackets()
					|| (packets = multi.getMultipleEnergyPacketAmount()) > 0))
			{
				int tier = source.getSourceTier();
				if (tier < 0)
				{
					if (EnergyNetSettings.logGridCalculationIssues)
						IC2R.log.warn(LogCategory.EnergyNet, "Tile %s reported invalid tier (%d).",
							Util.toString(source, enet.getWorld(), EnergyNet.instance.getPos(source)), tier);
					tile.setSourceData(0.0, 0);
				}
				else
				{
					foundAny = true;
					amount = Math.min(amount, ElectricalNodes.getMaxOfferPower(source, packets));
					tile.setSourceData(amount, packets);
				}
			}
			else
			{
				tile.setSourceData(0.0, 0);
			}
		}

		if (!foundAny) GridData.advanceCalcIds(enet);
		return foundAny;
	}

	@Override
	public boolean runSyncStep(Grid grid)
	{
		runCalculation(grid, GridData.get(grid));
		return false; // transfer stays on server thread
	}

	@Override
	public void runAsyncStep(Grid grid) {}

	@Override
	public void applyDeferredEffects(EnergyNetLocal enet)
	{
		Level world = enet.getWorld();
		for (Grid grid : enet.getGrids())
		{
			GridData data = grid.getData();
			if (data == null || data.deferredEventPaths.isEmpty()) continue;
			applyCableEffects(data.deferredEventPaths, world);
			data.deferredEventPaths.clear();
		}
	}

	@Override
	public NodeStats getNodeStats(Tile tile)
	{
		double in = 0.0, out = 0.0, max = 0.0;
		for (Node node : tile.getNodes())
		{
			GridData data = node.getGrid().getData();
			if (data == null || !data.active) continue;

			int calcId = data.currentCalcId;
			Collection<EnergyPath> paths = getPaths(node, data);
			double sum = 0.0;
			for (EnergyPath path : paths)
			{
				if (path.lastCalcId == calcId && path.energySupplied > 0.0)
				{
					sum += path.energySupplied;
					max = Math.max(path.maxPacketConducted, max);
				}
			}

			if (node.getType() == NodeType.Source) out += sum;
			else if (node.getType() == NodeType.Sink) in += sum;
			else { in += sum; out += sum; }
		}
		return new NodeStats(in, out, EnergyNet.instance.getTierFromPower(max));
	}

	@Override
	public void dumpNodeInfo(Node node, String prefix, PrintStream console, PrintStream chat)
	{
		GridData data = GridData.get(node.getGrid());
		Collection<EnergyPath> paths = getPaths(node, data);
		String label = switch (node.getType())
		{
			case Source -> "connected sink nodes";
			case Sink -> "connected source nodes";
			case Conductor -> "paths across this conductor";
		};
		chat.printf("%s%d %s%n", prefix, paths.size(), label);

		double sum = 0.0, max = 0.0;
		int calcId = data.currentCalcId, n = 0;

		for (EnergyPath path : paths)
		{
			boolean printPathEnergy = n < 8;
			if (printPathEnergy)
			{
				chat.printf("%s %s -> %s", prefix,
					node.getType() == NodeType.Source ? path.target : path.source,
					node.getType() == NodeType.Source ? "" : path.target);
			}
			else if (n == 8) chat.printf("%d more %n", paths.size() - 8);

			n++;
			if (path.lastCalcId != calcId || path.energySupplied <= 0.0)
			{
				if (printPathEnergy) chat.println(" (idle)");
			}
			else
			{
				if (printPathEnergy) chat.printf(" (%.2f EU, max packet %.2f EU)%n", path.energySupplied, path.maxPacketConducted);
				sum += path.energySupplied;
				max = Math.max(path.maxPacketConducted, max);
			}
		}
		chat.printf("%s last tick: %.2f EU, max packet %.2f EU%n", prefix, sum, max);
	}

	// ---- Inner types (shared with legacy) ----

	private record OptLink(Node nodeA, Node nodeB, double loss, List<Node> skippedNodes)
	{
		Node getNeighbor(Node node) { return nodeA == node ? nodeB : nodeA; }
	}

	private record OptimizedGraph(Map<Node, List<OptLink>> nodeToLinks) {}
}

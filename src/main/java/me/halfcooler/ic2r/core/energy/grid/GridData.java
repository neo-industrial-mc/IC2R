package me.halfcooler.ic2r.core.energy.grid;

import org.apache.commons.lang3.mutable.MutableDouble;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Per-grid solver state shared by energy calculators (IC path cache and optional GT addon).
 * Public so external calculators (e.g. GT addon) can run against the same structures.
 */
public class GridData
{
	public final Map<Node, List<EnergyPath>> energySourceToEnergyPathMap = new IdentityHashMap<>();
	public final List<Node> activeSources = new ArrayList<>();
	public final Map<Node, MutableDouble> activeSinks = new IdentityHashMap<>();
	public final Set<EnergyPath> eventPaths = Collections.newSetFromMap(new IdentityHashMap<>());
	public final Map<Node, List<EnergyPath>> pathCache = new IdentityHashMap<>();
	public final Set<Tile> deferredCablesToRemove = Collections.newSetFromMap(new IdentityHashMap<>());
	public final Set<Tile> deferredCablesToStrip = Collections.newSetFromMap(new IdentityHashMap<>());
	public final Map<Tile, Double> deferredSinksToExplode = new IdentityHashMap<>();
	public final List<EnergyPath> deferredEventPaths = new ArrayList<>();
	public boolean active;
	public int currentCalcId = -1;

	public static GridData get(Grid grid)
	{
		GridData ret = grid.getData();
		if (ret == null)
		{
			ret = new GridData();
			grid.setData(ret);
		}

		return ret;
	}

	/**
	 * Bump every active grid's calc id so path-local {@code energySupplied} from prior ticks
	 * no longer matches {@code currentCalcId}. Used when the energy net has no sources offering
	 * energy and therefore skips {@code runCalculation} — without this, meters/detectors keep
	 * reporting the last non-zero throughput forever.
	 */
	public static void advanceCalcIds(EnergyNetLocal enet)
	{
		for (Grid grid : enet.getGrids())
		{
			GridData data = grid.getData();
			if (data != null && data.active)
			{
				data.currentCalcId++;
			}
		}
	}
}
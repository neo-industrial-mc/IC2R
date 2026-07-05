package ic2.core.energy.grid;

import org.apache.commons.lang3.mutable.MutableDouble;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class GridData
{
	final Map<Node, List<EnergyPath>> energySourceToEnergyPathMap = new IdentityHashMap<>();
	final List<Node> activeSources = new ArrayList<>();
	final Map<Node, MutableDouble> activeSinks = new IdentityHashMap<>();
	final Set<EnergyPath> eventPaths = Collections.newSetFromMap(new IdentityHashMap<>());
	final Map<Node, List<EnergyPath>> pathCache = new IdentityHashMap<>();
	boolean active;
	int currentCalcId = -1;

	static GridData get(Grid grid)
	{
		GridData ret = grid.getData();
		if (ret == null)
		{
			ret = new GridData();
			grid.setData(ret);
		}

		return ret;
	}
}
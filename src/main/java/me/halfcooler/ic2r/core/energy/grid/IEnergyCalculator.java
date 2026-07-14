package me.halfcooler.ic2r.core.energy.grid;

import me.halfcooler.ic2r.api.energy.NodeStats;

import java.io.PrintStream;

public interface IEnergyCalculator
{
	void handleGridChange(Grid var1);

	boolean runSyncStep(EnergyNetLocal var1);

	boolean runSyncStep(Grid var1);

	void runAsyncStep(Grid var1);

	default void applyDeferredEffects(EnergyNetLocal enet)
	{
	}

	NodeStats getNodeStats(Tile var1);

	void dumpNodeInfo(Node var1, String var2, PrintStream var3, PrintStream var4);
}

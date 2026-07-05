package ic2.core.energy.grid;

import ic2.api.energy.NodeStats;

import java.io.PrintStream;

public class EnergyCalculatorGT implements IEnergyCalculator
{
	private final IEnergyCalculator delegate = new EnergyCalculatorUnified();

	@Override
	public void handleGridChange(Grid grid)
	{
		this.delegate.handleGridChange(grid);
	}

	@Override
	public boolean runSyncStep(EnergyNetLocal enet)
	{
		return this.delegate.runSyncStep(enet);
	}

	@Override
	public boolean runSyncStep(Grid grid)
	{
		return this.delegate.runSyncStep(grid);
	}

	@Override
	public void runAsyncStep(Grid grid)
	{
		this.delegate.runAsyncStep(grid);
	}

	@Override
	public NodeStats getNodeStats(Tile tile)
	{
		return this.delegate.getNodeStats(tile);
	}

	@Override
	public void dumpNodeInfo(Node node, String prefix, PrintStream console, PrintStream chat)
	{
		this.delegate.dumpNodeInfo(node, prefix, console, chat);
	}
}
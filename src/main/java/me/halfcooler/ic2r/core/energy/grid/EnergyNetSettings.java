package me.halfcooler.ic2r.core.energy.grid;

import me.halfcooler.ic2r.core.init.IC2RConfig;

public class EnergyNetSettings
{
	public static final boolean logEnetApiAccesses = IC2RConfig.debug.logEnetApiAccesses.get();
	public static final boolean logEnetApiAccessTraces = IC2RConfig.debug.logEnetApiAccessTraces.get();
	public static final boolean logGridUpdatePerformance = false;
	public static final boolean logGridCalculationPerformance = false;
	public static final boolean roundLossDown = IC2RConfig.misc.roundEnetLoss.get();
	public static final int changesQueueDelay = 1;
	public static final int bfsThreshold = 2048;
	public static boolean logGridUpdateIssues = IC2RConfig.debug.logGridUpdateIssues.get();
	public static boolean logGridUpdatesVerbose = IC2RConfig.debug.logGridUpdatesVerbose.get();
	public static boolean logGridCalculationIssues = IC2RConfig.debug.logGridCalculationIssues.get();
}

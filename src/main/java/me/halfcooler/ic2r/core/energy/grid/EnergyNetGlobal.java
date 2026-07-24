package me.halfcooler.ic2r.core.energy.grid;

import me.halfcooler.ic2r.api.energy.IEnergyNet;
import me.halfcooler.ic2r.api.energy.IEnergyNetEventReceiver;
import me.halfcooler.ic2r.api.energy.NodeStats;
import me.halfcooler.ic2r.api.energy.tile.IEnergyTile;
import me.halfcooler.ic2r.api.info.ILocatable;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.energy.EnergyNetMode;
import me.halfcooler.ic2r.core.event.WorldData;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.util.LogCategory;
import me.halfcooler.ic2r.core.util.Util;

import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class EnergyNetGlobal implements IEnergyNet
{
	private static final List<IEnergyNetEventReceiver> eventReceivers = new CopyOnWriteArrayList<>();
	private static IEnergyCalculator calculator = new IcEnergySolver();

	private EnergyNetGlobal()
	{
	}

	public static EnergyNetGlobal create()
	{
		System.getProperty("IC2RExpEnet");
		return new EnergyNetGlobal();
	}

	public static void initCalculator()
	{
		EnergyNetMode mode = EnergyNetMode.fromConfig(IC2RConfig.misc.useGregTechEnergyNet.get());
		calculator = mode == EnergyNetMode.GT ? new EnergyCalculatorGT() : new IcEnergySolver();
	}

	private static void addTile(IEnergyTile tile, Level world, BlockPos pos)
	{
		if (EnergyNetSettings.logEnetApiAccessTraces)
		{
			IC2R.log.debug(LogCategory.EnergyNet, new Throwable("Called from:"), "API addTile %s.", Util.toString(tile, world, pos));
		} else if (EnergyNetSettings.logEnetApiAccesses)
		{
			IC2R.log.debug(LogCategory.EnergyNet, "API addTile %s.", Util.toString(tile, world, pos));
		}

		getLocal(world).addTile(tile, pos);
	}

	static Iterable<IEnergyNetEventReceiver> getEventReceivers()
	{
		return eventReceivers;
	}

	static IEnergyCalculator getCalculator()
	{
		if (calculator == null)
		{
			calculator = new IcEnergySolver();
		}

		return calculator;
	}

	public static EnergyNetLocal getLocal(Level world)
	{
		if (world.isClientSide)
		{
			throw new IllegalStateException("not applicable clientside");
		}

		assert world.getServer().isSameThread();
		return WorldData.get(world).energyNet;
	}

	@Override
	public IEnergyTile getTile(Level world, BlockPos pos)
	{
		if (world == null)
		{
			throw new NullPointerException("null world");
		} else if (pos == null)
		{
			throw new NullPointerException("null pos");
		} else
		{
			return getLocal(world).getIoTile(pos);
		}
	}

	@Override
	public IEnergyTile getSubTile(Level world, BlockPos pos)
	{
		if (world == null)
		{
			throw new NullPointerException("null world");
		} else if (pos == null)
		{
			throw new NullPointerException("null pos");
		} else
		{
			return getLocal(world).getSubTile(pos);
		}
	}

	@Override
	public <T extends BlockEntity & IEnergyTile> void addBlockEntityTile(T tile)
	{
		if (tile == null)
		{
			throw new NullPointerException("null tile");
		}

		addTile(tile, tile.getLevel(), tile.getBlockPos());
	}

	@Override
	public <T extends ILocatable & IEnergyTile> void addLocatableTile(T tile)
	{
		if (tile == null)
		{
			throw new NullPointerException("null tile");
		}

		addTile(tile, tile.getWorldObj(), tile.getPosition());
	}

	@Override
	public void removeTile(IEnergyTile tile)
	{
		if (tile == null)
		{
			throw new NullPointerException("null tile");
		}

		Level world = this.getWorld(tile);
		BlockPos pos = this.getPos(tile);
		if (EnergyNetSettings.logEnetApiAccessTraces)
		{
			IC2R.log.debug(LogCategory.EnergyNet, new Throwable("Called from:"), "API removeTile %s.", Util.toString(tile, world, pos));
		} else if (EnergyNetSettings.logEnetApiAccesses)
		{
			IC2R.log.debug(LogCategory.EnergyNet, "API removeTile %s.", Util.toString(tile, world, pos));
		}

		getLocal(world).removeTile(tile, pos);
	}

	@Override
	public Level getWorld(IEnergyTile tile)
	{
		return switch (tile)
		{
			case null -> throw new NullPointerException("null tile");
			case ILocatable iLocatable -> iLocatable.getWorldObj();
			case BlockEntity blockEntity -> blockEntity.getLevel();
			default -> throw new UnsupportedOperationException("unlocatable tile type: " + tile.getClass().getName());
		};
	}

	@Override
	public BlockPos getPos(IEnergyTile tile)
	{
		return switch (tile)
		{
			case null -> throw new NullPointerException("null tile");
			case ILocatable iLocatable -> iLocatable.getPosition();
			case BlockEntity blockEntity -> blockEntity.getBlockPos();
			default -> throw new UnsupportedOperationException("unlocatable tile type: " + tile.getClass().getName());
		};
	}

	@Override
	public NodeStats getNodeStats(IEnergyTile tile)
	{
		return getLocal(this.getWorld(tile)).getNodeStats(tile);
	}

	@Override
	public int getAdjacentConnections(IEnergyTile tile)
	{
		return getLocal(this.getWorld(tile)).getAdjacentConnections(tile);
	}

	public boolean dumpDebugInfo(Level world, BlockPos pos, PrintStream console, PrintStream chat)
	{
		return getLocal(world).dumpDebugInfo(pos, console, chat);
	}

	@Override
	public double getPowerFromTier(int tier)
	{
		return EnergyTransferMath.icPowerFromTier(tier);
	}

	@Override
	public int getTierFromPower(double power)
	{
		return EnergyTransferMath.icTierFromPower(power);
	}

	@Override
	public synchronized void registerEventReceiver(IEnergyNetEventReceiver receiver)
	{
		if (!eventReceivers.contains(receiver))
		{
			eventReceivers.add(receiver);
		}
	}

	@Override
	public synchronized void unregisterEventReceiver(IEnergyNetEventReceiver receiver)
	{
		eventReceivers.remove(receiver);
	}
}

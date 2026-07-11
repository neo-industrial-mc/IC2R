package ic2.core.block.comp;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.profile.IElectricalNode;
import ic2.api.energy.profile.VoltageTier;
import ic2.api.energy.tile.IChargingSlot;
import ic2.api.energy.tile.IDischargingSlot;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.tile.IMultiEnergySource;
import ic2.api.info.ILocatable;
import ic2.core.IC2;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.block.wiring.tileentity.TileEntityTransformer;
import ic2.core.energy.EnergyNetMode;
import ic2.core.energy.grid.EnergyNetExplosions;
import ic2.core.energy.grid.EnergyNetGlobal;
import ic2.core.energy.grid.Tile;
import ic2.core.energy.profile.ElectricalProfile;
import ic2.core.init.IC2Config;
import ic2.core.network.GrowingBuffer;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class Energy extends TileEntityComponent implements IElectricalNode
{
	private static final boolean debugLoad = System.getProperty("ic2.comp.energy.debugload") != null;
	private final boolean fullEnergy;
	private double capacity;
	private double storage;
	private int sinkTier;
	private final int nativeSinkTier;
	private int sourceTier;
	private Set<Direction> sinkDirections;
	private Set<Direction> sourceDirections;
	private List<InvSlot> managedSlots;
	private boolean multiSource = false;
	private int sourcePackets = 1;
	private Energy.EnergyNetDelegate delegate;
	private boolean loaded;
	private boolean receivingDisabled;
	private boolean sendingSidabled;
	private final ElectricalProfile profile;

	public Energy(Ic2TileEntity parent, double capacity)
	{
		this(parent, capacity, Collections.emptySet(), Collections.emptySet(), 1);
	}

	public Energy(Ic2TileEntity parent, double capacity, Set<Direction> sinkDirections, Set<Direction> sourceDirections, int tier)
	{
		this(parent, capacity, sinkDirections, sourceDirections, tier, tier, false);
	}

	public Energy(
		Ic2TileEntity parent, double capacity, Set<Direction> sinkDirections, Set<Direction> sourceDirections, int sinkTier, int sourceTier, boolean fullEnergy
	)
	{
		super(parent);
		this.capacity = capacity;
		this.sinkTier = sinkTier;
		this.nativeSinkTier = sinkTier;
		this.sourceTier = sourceTier;
		this.sinkDirections = sinkDirections;
		this.sourceDirections = sourceDirections;
		this.fullEnergy = fullEnergy;
		this.profile = new ElectricalProfile(
			!sinkDirections.isEmpty() ? VoltageTier.fromIcTier(sinkTier) : VoltageTier.fromIcTier(sourceTier)
		);
	}

	public static Energy asBasicSink(Ic2TileEntity parent, double capacity)
	{
		return asBasicSink(parent, capacity, 1);
	}

	public static Energy asBasicSink(Ic2TileEntity parent, double capacity, int tier)
	{
		return new Energy(parent, capacity, Util.allFacings, Collections.emptySet(), tier);
	}

	public static Energy asBasicSource(Ic2TileEntity parent, double capacity)
	{
		return asBasicSource(parent, capacity, 1);
	}

	public static Energy asBasicSource(Ic2TileEntity parent, double capacity, int tier)
	{
		return new Energy(parent, capacity, Collections.emptySet(), Util.allFacings, tier);
	}

	public Energy addManagedSlot(InvSlot slot)
	{
		if (!(slot instanceof IChargingSlot) && !(slot instanceof IDischargingSlot))
		{
			throw new IllegalArgumentException("No charge/discharge slot.");
		}

		if (this.managedSlots == null)
		{
			this.managedSlots = new ArrayList<>(4);
		}

		this.managedSlots.add(slot);
		return this;
	}

	@Override
	public void readFromNbt(CompoundTag nbt)
	{
		this.storage = nbt.getDouble("storage");
	}

	@Override
	public CompoundTag writeToNbt()
	{
		CompoundTag ret = new CompoundTag();
		ret.putDouble("storage", this.storage);
		return ret;
	}

	@Override
	public void onLoaded()
	{
		assert this.delegate == null;
		if (!this.parent.getLevel().isClientSide)
		{
			if (this.sinkDirections.isEmpty() && this.sourceDirections.isEmpty())
			{
				if (debugLoad)
				{
					IC2.log.debug(LogCategory.Component, "Skipping Energy onLoaded for %s at %s.", this.parent, Util.formatPosition(this.parent));
				}
			} else
			{
				if (debugLoad)
				{
					IC2.log.debug(LogCategory.Component, "Energy onLoaded for %s at %s.", this.parent, Util.formatPosition(this.parent));
				}

				this.createDelegate();
				EnergyNet.instance.addLocatableTile(this.delegate);
			}

			this.loaded = true;
		}
	}

	private void createDelegate()
	{
		if (this.delegate != null)
		{
			throw new IllegalStateException();
		}

		assert !this.sinkDirections.isEmpty() || !this.sourceDirections.isEmpty();
		if (this.sinkDirections.isEmpty())
		{
			this.delegate = new Energy.EnergyNetDelegateSource(this.parent);
		} else if (this.sourceDirections.isEmpty())
		{
			this.delegate = new Energy.EnergyNetDelegateSink(this.parent);
		} else
		{
			this.delegate = new Energy.EnergyNetDelegateDual(this.parent);
		}
	}

	@Override
	public void onUnloaded()
	{
		if (this.delegate != null)
		{
			if (debugLoad)
			{
				IC2.log.debug(LogCategory.Component, "Energy onUnloaded for %s at %s.", this.parent, Util.formatPosition(this.parent));
			}

			EnergyNet.instance.removeTile(this.delegate);
			this.delegate = null;
		} else if (debugLoad)
		{
			IC2.log.debug(LogCategory.Component, "Skipping Energy onUnloaded for %s at %s.", this.parent, Util.formatPosition(this.parent));
		}

		this.loaded = false;
	}

	@Override
	public void onContainerUpdate(ServerPlayer player)
	{
		GrowingBuffer buffer = new GrowingBuffer(16);
		buffer.writeDouble(this.capacity);
		buffer.writeDouble(this.storage);
		buffer.flip();
		this.setNetworkUpdate(player, buffer);
	}

	@Override
	public void onNetworkUpdate(DataInput is) throws IOException
	{
		this.capacity = is.readDouble();
		this.storage = is.readDouble();
	}

	@Override
	public boolean enableWorldTick()
	{
		return !this.parent.getLevel().isClientSide && this.managedSlots != null;
	}

	@Override
	public void onWorldTick()
	{
		for (InvSlot slot : this.managedSlots)
		{
			if (slot instanceof IChargingSlot)
			{
				if (this.storage > 0.0)
				{
					this.storage = this.storage - ((IChargingSlot) slot).charge(this.storage);
				}
			} else if (slot instanceof IDischargingSlot)
			{
				double space = this.capacity - this.storage;
				if (space > 0.0)
				{
					this.storage = this.storage + ((IDischargingSlot) slot).discharge(space, false);
				}
			}
		}
	}

	public double getCapacity()
	{
		return this.capacity;
	}

	public void setCapacity(double capacity)
	{
		this.capacity = capacity;
	}

	public double getEnergy()
	{
		return this.storage;
	}

	public double getFreeEnergy()
	{
		return Math.max(0.0, this.capacity - this.storage);
	}

	public double getFillRatio()
	{
		return this.storage / this.capacity;
	}

	public int getComparatorValue()
	{
		return Math.min((int) (this.storage * 15.0 / this.capacity), 15);
	}

	public double addEnergy(double amount)
	{
		amount = Math.min(this.capacity - this.storage, amount);
		this.storage += amount;
		return amount;
	}

	public void forceAddEnergy(double amount)
	{
		this.storage += amount;
	}

	public boolean canUseEnergy(double amount)
	{
		return this.storage >= amount;
	}

	public boolean useEnergy(double amount)
	{
		if (this.storage >= amount)
		{
			this.storage -= amount;
			return true;
		} else
		{
			return false;
		}
	}

	public double useEnergy(double amount, boolean simulate)
	{
		double ret = Math.abs(Math.max(0.0, amount - this.storage) - amount);
		if (simulate)
		{
			return ret;
		}

		this.storage -= ret;
		return ret;
	}

	public int getSinkTier()
	{
		return this.sinkTier;
	}

	public void setSinkTier(int tier)
	{
		this.sinkTier = tier;
	}

	public int getSourceTier()
	{
		return this.sourceTier;
	}

	public void setSourceTier(int tier)
	{
		this.sourceTier = tier;
		if (this.sinkDirections.isEmpty())
		{
			this.profile.setWorkingVoltage(VoltageTier.fromIcTier(tier));
		}
	}

	public ElectricalProfile getElectricalProfile()
	{
		return this.profile;
	}

	public void setRecipePower(int recipePower)
	{
		this.profile.setRecipePower(recipePower);
	}

	public void setWorkingVoltage(VoltageTier workingVoltage)
	{
		this.profile.setWorkingVoltage(workingVoltage);
	}

	public void syncConsumerProfile(int recipePowerEuPerTick)
	{
		VoltageTier voltage = resolveConsumerWorkingVoltage(recipePowerEuPerTick);
		if (this.profile.getRecipePower() == recipePowerEuPerTick && this.profile.getWorkingVoltage() == voltage)
		{
			return;
		}

		this.profile.clearMaxSinkAmperageOverride();
		this.profile.clearSinkWorkingVoltage();
		this.profile.setRecipePower(recipePowerEuPerTick);
		this.profile.setWorkingVoltage(voltage);
	}

	private VoltageTier resolveConsumerWorkingVoltage(int recipePowerEuPerTick)
	{
		VoltageTier nativeTier = VoltageTier.fromIcTier(this.nativeSinkTier);
		if (recipePowerEuPerTick <= 0 || recipePowerEuPerTick <= nativeTier.getVoltage())
		{
			return nativeTier;
		}

		VoltageTier fromPower = VoltageTier.fromPower(recipePowerEuPerTick);
		return fromPower.getIcTier() < nativeTier.getIcTier() ? nativeTier : fromPower;
	}

	public void configureStorageBlock()
	{
		VoltageTier tier = VoltageTier.fromIcTier(this.sinkTier);
		this.profile.clearSinkWorkingVoltage();
		this.profile.setWorkingVoltage(tier);
		this.profile.setRecipePower(0);
		this.profile.setMaxSinkAmperageOverride(2);
	}

	/**
	 * Continuous charge buffer (e.g. matter fabricator): accept at the current sink-tier voltage
	 * with a high amperage cap. Do not pass buffer capacity as recipe power — that inflates the
	 * working voltage and can leave an unfillable remainder under the GT packet model.
	 */
	public void configureEnergyBuffer(int maxAmperage)
	{
		VoltageTier tier = VoltageTier.fromIcTier(this.sinkTier);
		this.profile.clearSinkWorkingVoltage();
		this.profile.setWorkingVoltage(tier);
		this.profile.setRecipePower(0);
		this.profile.setMaxSinkAmperageOverride(Math.max(1, maxAmperage));
	}

	public void configureFixedSource(int productionEuPerTick)
	{
		this.profile.clearSinkWorkingVoltage();
		this.profile.setWorkingVoltage(VoltageTier.LV);
		this.profile.setRecipePower(productionEuPerTick);
		this.setSourceTier(VoltageTier.LV.getIcTier());
	}

	public void configureDynamicSource(double outputEuPerTick)
	{
		VoltageTier tier = VoltageTier.fromPower(outputEuPerTick);
		this.profile.clearSinkWorkingVoltage();
		this.profile.setWorkingVoltage(tier);
		this.profile.setRecipePower((int) Math.round(outputEuPerTick));
		if (this.sinkDirections.isEmpty())
		{
			this.setSourceTier(tier.getIcTier());
		}
	}

	public void configureTransformerProfile(boolean stepUp)
	{
		this.profile.clearMaxSinkAmperageOverride();
		this.profile.setRecipePower(0);
		this.profile.setWorkingVoltage(VoltageTier.fromIcTier(this.sourceTier));
		this.profile.setSinkWorkingVoltage(VoltageTier.fromIcTier(this.sinkTier));
		this.profile.setMaxSinkAmperageOverride(stepUp ? 4 : 1);
	}

	@Override
	public VoltageTier getSinkWorkingVoltage()
	{
		if (!this.sinkDirections.isEmpty() && !this.sourceDirections.isEmpty())
		{
			return this.profile.getSinkWorkingVoltage();
		}

		return this.profile.getWorkingVoltage();
	}

	public boolean applyTransformerModeSwitch(TileEntityTransformer.Mode newMode, TileEntityTransformer.Mode oldMode)
	{
		if (oldMode == null || newMode == oldMode)
		{
			return false;
		}

		if (EnergyNetMode.fromConfig(IC2Config.misc.energyNetMode.get()) != EnergyNetMode.GT)
		{
			return false;
		}

		if (this.storage <= 0.0)
		{
			return false;
		}

		Level world = this.parent.getLevel();
		Tile tile = EnergyNetGlobal.getLocal(world).getTile(this.parent.getBlockPos());
		if (tile != null)
		{
			EnergyNetExplosions.explodeTile(world, tile, EnergyNet.instance.getPowerFromTier(this.sourceTier));
		}

		return true;
	}

	@Override
	public VoltageTier getWorkingVoltage()
	{
		return this.profile.getWorkingVoltage();
	}

	@Override
	public int getWorkingCurrent()
	{
		return this.profile.getWorkingCurrent();
	}

	@Override
	public double getAverageCurrent()
	{
		return this.profile.getDisplayCurrent();
	}

	@Override
	public int getMaxSourceAmperage()
	{
		if (this.multiSource)
		{
			return this.sourcePackets;
		}

		int workingCurrent = this.profile.getWorkingCurrent();
		return workingCurrent > 0 ? workingCurrent : 1;
	}

	@Override
	public int getMaxSinkAmperage()
	{
		return this.profile.getMaxSinkAmperage();
	}

	@Override
	public double getEnergyBufferCapacity()
	{
		return this.capacity;
	}

	@Override
	public double getEnergyBufferFree()
	{
		return this.getFreeEnergy();
	}

	public void setEnabled(boolean enabled)
	{
		this.receivingDisabled = this.sendingSidabled = !enabled;
	}

	public void setReceivingEnabled(boolean enabled)
	{
		this.receivingDisabled = !enabled;
	}

	public void setSendingEnabled(boolean enabled)
	{
		this.sendingSidabled = !enabled;
	}

	public boolean isMultiSource()
	{
		return this.multiSource;
	}

	public Energy setMultiSource(boolean multiSource)
	{
		this.multiSource = multiSource;
		if (!multiSource)
		{
			this.sourcePackets = 1;
		}

		return this;
	}

	public int getPacketOutput()
	{
		return this.sourcePackets;
	}

	public void setPacketOutput(int number)
	{
		if (this.multiSource)
		{
			this.sourcePackets = number;
		}
	}

	public void setDirections(Set<Direction> sinkDirections, Set<Direction> sourceDirections)
	{
		if (sinkDirections.equals(this.sinkDirections) && sourceDirections.equals(this.sourceDirections))
		{
			if (debugLoad)
			{
				IC2.log
					.debug(
						LogCategory.Component,
						"Energy setDirections unchanged for %s at %s, sink: %s, source: %s.",
						this.parent,
						Util.formatPosition(this.parent),
						sinkDirections,
						sourceDirections
					);
			}
		} else
		{
			if (this.delegate != null)
			{
				if (debugLoad)
				{
					IC2.log.debug(LogCategory.Component, "Energy setDirections unload for %s at %s.", this.parent, Util.formatPosition(this.parent));
				}

				assert !this.parent.getLevel().isClientSide;
				EnergyNet.instance.removeTile(this.delegate);
			}

			this.sinkDirections = sinkDirections;
			this.sourceDirections = sourceDirections;
			if (sinkDirections.isEmpty() && sourceDirections.isEmpty())
			{
				this.delegate = null;
			} else if (this.delegate == null && this.loaded)
			{
				this.createDelegate();
			}

			if (this.delegate != null)
			{
				if (debugLoad)
				{
					IC2.log
						.debug(
							LogCategory.Component,
							"Energy setDirections load for %s at %s, sink: %s, source: %s.",
							this.parent,
							Util.formatPosition(this.parent),
							sinkDirections,
							sourceDirections
						);
				}

				assert !this.parent.getLevel().isClientSide;
				EnergyNet.instance.addLocatableTile(this.delegate);
			} else if (debugLoad)
			{
				IC2.log
					.debug(
						LogCategory.Component,
						"Skipping Energy setDirections load for %s at %s, sink: %s, source: %s, loaded: %b.",
						this.parent,
						Util.formatPosition(this.parent),
						sinkDirections,
						sourceDirections,
						this.loaded
					);
			}
		}
	}

	public Set<Direction> getSourceDirs()
	{
		return Collections.unmodifiableSet(this.sourceDirections);
	}

	public Set<Direction> getSinkDirs()
	{
		return Collections.unmodifiableSet(this.sinkDirections);
	}

	public IEnergyTile getDelegate()
	{
		return this.delegate;
	}

	private double getSourceEnergy()
	{
		if (this.fullEnergy)
		{
			return this.storage >= EnergyNet.instance.getPowerFromTier(this.sourceTier) ? this.storage : 0.0;
		} else
		{
			return this.storage;
		}
	}

	private int getPacketCount()
	{
		return this.fullEnergy
			? Math.min(this.sourcePackets, (int) Math.floor(this.storage / EnergyNet.instance.getPowerFromTier(this.sourceTier)))
			: this.sourcePackets;
	}

	private abstract class EnergyNetDelegate implements ILocatable, IEnergyTile, IElectricalNode
	{
		private final Ic2TileEntity parent;

		protected EnergyNetDelegate(Ic2TileEntity parent)
		{
			this.parent = parent;
		}

		@Override
		public Level getWorldObj()
		{
			return this.parent.getLevel();
		}

		@Override
		public BlockPos getPosition()
		{
			return this.parent.getBlockPos();
		}

		@Override
		public VoltageTier getWorkingVoltage()
		{
			return Energy.this.getWorkingVoltage();
		}

		@Override
		public VoltageTier getSinkWorkingVoltage()
		{
			return Energy.this.getSinkWorkingVoltage();
		}

		@Override
		public int getWorkingCurrent()
		{
			return Energy.this.getWorkingCurrent();
		}

		@Override
		public double getAverageCurrent()
		{
			return Energy.this.getAverageCurrent();
		}

		@Override
		public int getMaxSourceAmperage()
		{
			return Energy.this.getMaxSourceAmperage();
		}

		@Override
		public int getMaxSinkAmperage()
		{
			return Energy.this.getMaxSinkAmperage();
		}

		@Override
		public double getEnergyBufferCapacity()
		{
			return Energy.this.getEnergyBufferCapacity();
		}

		@Override
		public double getEnergyBufferFree()
		{
			return Energy.this.getEnergyBufferFree();
		}
	}

	private class EnergyNetDelegateDual extends Energy.EnergyNetDelegate implements IEnergySink, IMultiEnergySource
	{
		protected EnergyNetDelegateDual(Ic2TileEntity parent)
		{
			super(parent);
		}

		@Override
		public boolean acceptsEnergyFrom(IEnergyEmitter emitter, Direction dir)
		{
			return Energy.this.sinkDirections.contains(dir);
		}

		@Override
		public boolean emitsEnergyTo(IEnergyAcceptor receiver, Direction dir)
		{
			return Energy.this.sourceDirections.contains(dir);
		}

		@Override
		public double getDemandedEnergy()
		{
			return !Energy.this.receivingDisabled && !Energy.this.sinkDirections.isEmpty() && Energy.this.storage < Energy.this.capacity
				? Energy.this.capacity - Energy.this.storage
				: 0.0;
		}

		@Override
		public double getOfferedEnergy()
		{
			return !Energy.this.sendingSidabled && !Energy.this.sourceDirections.isEmpty() ? Energy.this.getSourceEnergy() : 0.0;
		}

		@Override
		public int getSinkTier()
		{
			return Energy.this.sinkTier;
		}

		@Override
		public int getSourceTier()
		{
			return Energy.this.sourceTier;
		}

		@Override
		public double injectEnergy(Direction directionFrom, double amount, double voltage)
		{
			Energy.this.storage += amount;
			return 0.0;
		}

		@Override
		public void drawEnergy(double amount)
		{
			assert amount <= Energy.this.storage;
			Energy.this.storage -= amount;
		}

		@Override
		public boolean sendMultipleEnergyPackets()
		{
			return Energy.this.multiSource;
		}

		@Override
		public int getMultipleEnergyPacketAmount()
		{
			return Energy.this.getPacketCount();
		}
	}

	private class EnergyNetDelegateSink extends Energy.EnergyNetDelegate implements IEnergySink
	{
		protected EnergyNetDelegateSink(Ic2TileEntity parent)
		{
			super(parent);
		}

		@Override
		public int getSinkTier()
		{
			return Energy.this.sinkTier;
		}

		@Override
		public boolean acceptsEnergyFrom(IEnergyEmitter emitter, Direction dir)
		{
			return Energy.this.sinkDirections.contains(dir);
		}

		@Override
		public double getDemandedEnergy()
		{
			assert !Energy.this.sinkDirections.isEmpty();
			return !Energy.this.receivingDisabled && Energy.this.storage < Energy.this.capacity ? Energy.this.capacity - Energy.this.storage : 0.0;
		}

		@Override
		public double injectEnergy(Direction directionFrom, double amount, double voltage)
		{
			Energy.this.storage += amount;
			return 0.0;
		}
	}

	private class EnergyNetDelegateSource extends Energy.EnergyNetDelegate implements IMultiEnergySource
	{
		protected EnergyNetDelegateSource(Ic2TileEntity parent)
		{
			super(parent);
		}

		@Override
		public int getSourceTier()
		{
			return Energy.this.sourceTier;
		}

		@Override
		public boolean emitsEnergyTo(IEnergyAcceptor receiver, Direction dir)
		{
			return Energy.this.sourceDirections.contains(dir);
		}

		@Override
		public double getOfferedEnergy()
		{
			assert !Energy.this.sourceDirections.isEmpty();
			return !Energy.this.sendingSidabled ? Energy.this.getSourceEnergy() : 0.0;
		}

		@Override
		public void drawEnergy(double amount)
		{
			assert amount <= Energy.this.storage;
			Energy.this.storage -= amount;
		}

		@Override
		public boolean sendMultipleEnergyPackets()
		{
			return Energy.this.multiSource;
		}

		@Override
		public int getMultipleEnergyPacketAmount()
		{
			return Energy.this.getPacketCount();
		}
	}
}

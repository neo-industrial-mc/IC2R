package me.halfcooler.ic2r.core.block.generator.tileentity;

import me.halfcooler.ic2r.api.energy.EnergyNet;
import me.halfcooler.ic2r.api.energy.profile.IElectricalNode;
import me.halfcooler.ic2r.api.energy.profile.VoltageTier;
import me.halfcooler.ic2r.api.energy.tile.IEnergyAcceptor;
import me.halfcooler.ic2r.api.energy.tile.IEnergySource;
import me.halfcooler.ic2r.core.energy.EnergyNetMode;
import me.halfcooler.ic2r.core.energy.profile.ElectricalProfile;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityInventory;
import me.halfcooler.ic2r.core.gui.dynamic.DynamicContainer;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.network.GuiSynced;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityConversionGenerator extends TileEntityInventory implements IHasGui, IEnergySource, IElectricalNode
{
	private static final NumberFormat FORMAT = new DecimalFormat("#.#");
	@GuiSynced
	private double lastProduction;
	@GuiSynced
	private double maxProduction;
	private double production;
	private double energyBuffer;
	private boolean registeredToEnet;
	private final ElectricalProfile profile = new ElectricalProfile(VoltageTier.LV);

	public TileEntityConversionGenerator(BlockEntityType<? extends TileEntityConversionGenerator> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.energyBuffer = nbt.getDouble("energyBuffer");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putDouble("energyBuffer", this.energyBuffer);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		double tickOutput = this.getEnergyAvailable() * this.getMultiplier();
		double outputEuPerTick = this.clampOutputEuPerTick(tickOutput);
		if (this.isGtEnergyNet() && outputEuPerTick > 0.0)
		{
			int voltage = this.getOutputVoltageTier(outputEuPerTick).getVoltage();
			double maxBuffer = Math.max(voltage, outputEuPerTick);
			double previousBuffer = this.energyBuffer;
			this.energyBuffer = Math.min(this.energyBuffer + outputEuPerTick, maxBuffer);
			double added = this.energyBuffer - previousBuffer;
			if (added > 0.0)
			{
				this.drawEnergyAvailable((int) Math.ceil(added / this.getMultiplier()));
			}
		}

		this.lastProduction = this.production;
		this.production = 0.0;
		this.setActive(tickOutput > 0.0 || this.isGtEnergyNet() && this.energyBuffer > 0.0);
	}

	@Override
	protected void onUnloaded()
	{
		super.onUnloaded();
		if (this.registeredToEnet && !this.level.isClientSide)
		{
			EnergyNet.instance.removeTile(this);
			this.registeredToEnet = false;
		}
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (!this.registeredToEnet && !this.level.isClientSide)
		{
			EnergyNet.instance.addBlockEntityTile(this);
			this.registeredToEnet = true;
		}
	}

	@Override
	protected void setFacing(Level world, Direction facing)
	{
		super.setFacing(world, facing);
		this.refreshEnergyNet();
	}

	protected void refreshEnergyNet()
	{
		if (this.registeredToEnet && !this.level.isClientSide)
		{
			EnergyNet.instance.removeTile(this);
			EnergyNet.instance.addBlockEntityTile(this);
		}
	}

	public String getProduction()
	{
		return FORMAT.format(this.lastProduction);
	}

	public String getMaxProduction()
	{
		return FORMAT.format(this.maxProduction);
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return DynamicContainer.create(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return DynamicContainer.create(syncId, inventory, this);
	}

	protected abstract int getEnergyAvailable();

	protected abstract void drawEnergyAvailable(int var1);

	protected abstract double getMultiplier();

	@Override
	public double getOfferedEnergy()
	{
		this.maxProduction = this.getEnergyAvailable() * this.getMultiplier();
		this.syncSourceProfile(this.clampOutputEuPerTick(this.maxProduction));
		return this.isGtEnergyNet() ? this.energyBuffer : this.clampOutputEuPerTick(this.maxProduction);
	}

	protected double clampOutputEuPerTick(double outputEuPerTick)
	{
		return outputEuPerTick;
	}

	protected VoltageTier clampOutputVoltageTier(VoltageTier tier)
	{
		return tier;
	}

	protected VoltageTier getOutputVoltageTier(double outputEuPerTick)
	{
		if (outputEuPerTick <= 0.0)
		{
			return VoltageTier.ULV;
		}

		return this.clampOutputVoltageTier(VoltageTier.fromPower(outputEuPerTick));
	}

	private void syncSourceProfile(double outputEuPerTick)
	{
		if (outputEuPerTick > 0.0)
		{
			VoltageTier tier = this.getOutputVoltageTier(outputEuPerTick);
			this.profile.setWorkingVoltage(tier);
			this.profile.setRecipePower((int) Math.round(outputEuPerTick));
		} else if (!this.isGtEnergyNet() || this.energyBuffer <= 0.0)
		{
			this.profile.setWorkingVoltage(VoltageTier.ULV);
			this.profile.setRecipePower(0);
		}
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
		return 1;
	}

	@Override
	public int getMaxSinkAmperage()
	{
		return 1;
	}

	private double getCurrentOfferedOutput()
	{
		return this.getEnergyAvailable() * this.getMultiplier();
	}

	@Override
	public double getEnergyBufferCapacity()
	{
		double offered = this.clampOutputEuPerTick(this.getCurrentOfferedOutput());
		if (offered <= 0.0 && this.isGtEnergyNet() && this.energyBuffer > 0.0)
		{
			return this.profile.getWorkingVoltage().getVoltage();
		}

		int voltage = this.getOutputVoltageTier(offered).getVoltage();
		return Math.max(voltage, offered);
	}

	@Override
	public double getEnergyBufferFree()
	{
		double capacity = this.getEnergyBufferCapacity();
		if (this.isGtEnergyNet())
		{
			return Math.max(0.0, capacity - Math.min(this.energyBuffer, capacity));
		}

		double offered = this.getCurrentOfferedOutput();
		return Math.max(0.0, capacity - Math.min(offered, capacity));
	}

	@Override
	public void drawEnergy(double amount)
	{
		if (this.isGtEnergyNet())
		{
			this.energyBuffer = Math.max(0.0, this.energyBuffer - amount);
		} else
		{
			this.drawEnergyAvailable((int) Math.ceil(amount / this.getMultiplier()));
		}

		this.production += amount;
		if (amount > 0.0)
		{
			this.lastProduction = this.production;
		}
	}

	@Override
	public int getSourceTier()
	{
		return this.profile.getWorkingVoltage().getIcTier();
	}

	private boolean isGtEnergyNet()
	{
		return EnergyNetMode.fromConfig(IC2RConfig.misc.energyNetMode.get()) == EnergyNetMode.GT;
	}

	@Override
	public boolean emitsEnergyTo(IEnergyAcceptor receiver, Direction side)
	{
		return side != this.getFacing();
	}
}

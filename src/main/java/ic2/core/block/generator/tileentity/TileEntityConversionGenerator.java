package ic2.core.block.generator.tileentity;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.profile.IElectricalNode;
import ic2.api.energy.profile.VoltageTier;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySource;
import ic2.core.energy.profile.ElectricalProfile;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.GuiSynced;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import net.minecraft.core.BlockPos;
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
	private boolean registeredToEnet;
	private final ElectricalProfile profile = new ElectricalProfile(VoltageTier.LV);

	public TileEntityConversionGenerator(BlockEntityType<? extends TileEntityConversionGenerator> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.lastProduction = this.production;
		this.production = 0.0;
		this.setActive(this.maxProduction > 0.0);
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
		this.syncSourceProfile(this.maxProduction);
		return this.maxProduction;
	}

	private void syncSourceProfile(double outputEuPerTick)
	{
		VoltageTier tier = VoltageTier.fromPower(outputEuPerTick);
		this.profile.setWorkingVoltage(tier);
		this.profile.setRecipePower((int) Math.round(outputEuPerTick));
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
		double offered = this.getCurrentOfferedOutput();
		int voltage = VoltageTier.fromPower(offered).getVoltage();
		return Math.max(voltage, offered);
	}

	@Override
	public double getEnergyBufferFree()
	{
		double offered = this.getCurrentOfferedOutput();
		double capacity = this.getEnergyBufferCapacity();
		return Math.max(0.0, capacity - Math.min(offered, capacity));
	}

	@Override
	public void drawEnergy(double amount)
	{
		this.production += amount;
		this.drawEnergyAvailable((int) Math.ceil(amount / this.getMultiplier()));
	}

	@Override
	public int getSourceTier()
	{
		return VoltageTier.fromPower(this.getCurrentOfferedOutput()).getIcTier();
	}

	@Override
	public boolean emitsEnergyTo(IEnergyAcceptor receiver, Direction side)
	{
		return side != this.getFacing();
	}
}

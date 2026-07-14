package me.halfcooler.ic2r.core.block.reactor.tileentity;

import me.halfcooler.ic2r.api.reactor.IReactorChamber;
import me.halfcooler.ic2r.api.upgrade.IUpgradableBlock;
import me.halfcooler.ic2r.api.upgrade.UpgradableProperty;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.comp.FluidReactorLookup;
import me.halfcooler.ic2r.core.block.comp.Fluids;
import me.halfcooler.ic2r.core.block.invslot.InvSlotUpgrade;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityInventory;
import me.halfcooler.ic2r.core.gui.dynamic.DynamicContainer;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;

import me.halfcooler.ic2r.core.block.tileentity.ServerTicker;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityReactorFluidPort extends TileEntityInventory implements IHasGui, IUpgradableBlock, IReactorChamber, ServerTicker
{
	public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 1);
	protected final Fluids fluids = this.addComponent(new Fluids(this));
	private final FluidReactorLookup lookup = this.addComponent(new FluidReactorLookup(this));

	public TileEntityReactorFluidPort(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.REACTOR_FLUID_PORT, pos, state);
		this.fluids.addUnmanagedTankHook(() ->
		{
			TileEntityNuclearReactorElectric reactor = TileEntityReactorFluidPort.this.getReactorInstance();
			return reactor == null ? Collections.emptySet() : Arrays.asList(reactor.inputTank, reactor.outputTank);
		});
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.upgradeSlot.tick();
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

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing);
	}

	@Override
	public double getEnergy()
	{
		return 40.0;
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return true;
	}

	public TileEntityNuclearReactorElectric getReactorInstance()
	{
		return this.lookup.getReactor();
	}

	@Override
	public boolean isWall()
	{
		return true;
	}
}

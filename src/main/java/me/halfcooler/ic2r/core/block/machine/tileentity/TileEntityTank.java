package me.halfcooler.ic2r.core.block.machine.tileentity;

import me.halfcooler.ic2r.api.network.INetworkClientTileEntityEventListener;
import me.halfcooler.ic2r.api.upgrade.IUpgradableBlock;
import me.halfcooler.ic2r.api.upgrade.UpgradableProperty;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.comp.Fluids;
import me.halfcooler.ic2r.core.block.invslot.InvSlotUpgrade;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityInventory;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidTank;
import me.halfcooler.ic2r.core.gui.dynamic.DynamicContainer;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.network.GuiSynced;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.util.LiquidUtil;
import me.halfcooler.ic2r.core.util.Util;

import me.halfcooler.ic2r.core.block.tileentity.ServerTicker;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityTank extends TileEntityInventory implements IUpgradableBlock, IHasGui, INetworkClientTileEntityEventListener, ServerTicker
{
	public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
	@GuiSynced
	protected final Ic2rFluidTank fluidTank;
	protected final Fluids fluids = this.addComponent(new Fluids(this));

	public TileEntityTank(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.TANK, pos, state);
		this.fluidTank = this.fluids.addTank("fluid", 24000);
		this.comparator.setUpdate(() -> this.fluidTank.getFluidAmount() == 0 ? 0 : (int) Util.lerp(1.0F, 15.0F, (float) this.fluidTank.getFluidAmount() / this.fluidTank.getCapacity()));
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.upgradeSlot.tick();
	}

	@Override
	public double getEnergy()
	{
		return 0.0;
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return false;
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing);
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
	public void onNetworkEvent(Player player, int event)
	{
		if (event == 0 || event == 1)
		{
			LiquidUtil.transferFluidFromGuiClick(player, this.fluidTank, event == 1);
		}
	}
}

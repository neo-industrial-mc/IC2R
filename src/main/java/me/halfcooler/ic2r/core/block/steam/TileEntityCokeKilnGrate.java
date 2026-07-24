package me.halfcooler.ic2r.core.block.steam;

import me.halfcooler.ic2r.api.network.INetworkClientTileEntityEventListener;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.comp.Fluids;
import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityInventory;
import me.halfcooler.ic2r.core.gui.dynamic.DynamicContainer;
import me.halfcooler.ic2r.core.network.GuiSynced;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.util.LiquidUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TileEntityCokeKilnGrate extends TileEntityInventory implements IHasGui, INetworkClientTileEntityEventListener
{
	protected final Fluids fluidsComponent = this.addComponent(new Fluids(this));
	@GuiSynced
	protected final Fluids.InternalFluidTank fluidTank;

	public TileEntityCokeKilnGrate(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.COKE_KILN_GRATE, pos, state);
		this.fluidTank = this.fluidsComponent.addTankExtract("fluidTank", 64000, InvSlot.InvSide.ANY);
	}

	@Override
	protected InteractionResult onActivated(Player player, InteractionHand hand, Direction side, Vec3 hit)
	{
		if (LiquidUtil.transferFluidFromHandClick(player, hand, this.fluidTank, player.isShiftKeyDown()))
		{
			this.setChanged();
			return InteractionResult.SUCCESS;
		}

		return super.onActivated(player, hand, side, hit);
	}

	@Override
	public void onNetworkEvent(Player player, int event)
	{
		if (event == 0 || event == 1)
		{
			LiquidUtil.transferFluidFromGuiClick(player, this.fluidTank, event == 1);
		}
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return DynamicContainer.create(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, net.minecraft.world.entity.player.Inventory inventory, me.halfcooler.ic2r.core.network.GrowingBuffer data)
	{
		return DynamicContainer.create(syncId, inventory, this);
	}
}
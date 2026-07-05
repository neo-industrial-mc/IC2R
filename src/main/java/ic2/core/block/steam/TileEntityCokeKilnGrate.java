package ic2.core.block.steam;

import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.util.FluidContainerOutputMode;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.network.GuiSynced;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TileEntityCokeKilnGrate extends TileEntityInventory implements IHasGui, INetworkClientTileEntityEventListener
{
	protected final Fluids fluidsComponent = this.addComponent(new Fluids(this));
	@GuiSynced
	protected final Fluids.InternalFluidTank fluidTank;

	public TileEntityCokeKilnGrate(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.COKE_KILN_GRATE, pos, state);
		this.fluidTank = this.fluidsComponent.addTankExtract("fluidTank", 64000, InvSlot.InvSide.ANY);
	}

	@Override
	protected InteractionResult onActivated(Player player, InteractionHand hand, Direction side, Vec3 hit)
	{
		ItemStack inHand = StackUtil.get(player, hand);
		if (!LiquidUtil.isFluidContainer(inHand))
		{
			return super.onActivated(player, hand, side, hit);
		}

		Ic2FluidStack fs = this.fluidTank.getFluidStack();
		int amount;
		if (fs != null && !fs.isEmpty())
		{
			amount = LiquidUtil.fillContainer(player, hand, fs, FluidContainerOutputMode.InPlacePreferred, false);
			if (amount != 0)
			{
				fs.decreaseMb(amount);
				this.setChanged();
				return InteractionResult.SUCCESS;
			}
		}

		return InteractionResult.PASS;
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
	public ContainerBase<?> createClientScreenHandler(int syncId, net.minecraft.world.entity.player.Inventory inventory, ic2.core.network.GrowingBuffer data)
	{
		return DynamicContainer.create(syncId, inventory, this);
	}
}
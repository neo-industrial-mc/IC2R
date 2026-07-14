package me.halfcooler.ic2r.core.block.steam;

import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityInventory;
import me.halfcooler.ic2r.core.gui.dynamic.DynamicContainer;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityCokeKilnHatch extends TileEntityInventory implements IHasGui
{
	protected final InvSlot inventory = new InvSlot(this, "inventory", InvSlot.Access.I, 1, InvSlot.InvSide.ANY);

	public TileEntityCokeKilnHatch(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.COKE_KILN_HATCH, pos, state);
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction side)
	{
		return side != this.getFacing() ? false : super.canPlaceItemThroughFace(index, stack, side);
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction side)
	{
		return false;
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

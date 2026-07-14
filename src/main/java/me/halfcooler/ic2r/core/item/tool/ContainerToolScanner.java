package me.halfcooler.ic2r.core.item.tool;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.item.ContainerHandHeldInventory;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.util.Tuple;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ContainerToolScanner extends ContainerHandHeldInventory<HandHeldScanner>
{
	public List<Tuple.T2<ItemStack, Integer>> scanResults;

	public ContainerToolScanner(int syncId, Inventory playerInventory, HandHeldScanner scanner)
	{
		super(Ic2rScreenHandlers.SCANNER, syncId, scanner);
		this.addPlayerInventorySlots(playerInventory, 231);
	}

	public void setResults(List<Tuple.T2<ItemStack, Integer>> results)
	{
		this.scanResults = results;
		IC2R.network.get(true).sendContainerField(this, "scanResults");
	}
}

package me.halfcooler.ic2r.core.block.invslot;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.block.IInventorySlotHolder;
import me.halfcooler.ic2r.core.network.IRpcProvider;
import me.halfcooler.ic2r.core.network.RpcHandler;
import me.halfcooler.ic2r.core.util.StackUtil;
import me.halfcooler.ic2r.core.uu.UuGraph;
import me.halfcooler.ic2r.core.uu.UuIndex;

import net.minecraft.world.item.ItemStack;

public class InvSlotScannable extends InvSlotConsumable
{
	static
	{
		RpcHandler.registerProvider(new InvSlotScannable.ServerScannableCheck());
	}

	public InvSlotScannable(IInventorySlotHolder<?> base1, String name1, int count)
	{
		super(base1, name1, count);
		this.setStackSizeLimit(1);
	}

	private static boolean isValidStack(ItemStack stack)
	{
		stack = UuGraph.find(stack);
		return !StackUtil.isEmpty(stack) && UuIndex.instance.get(stack) < Double.POSITIVE_INFINITY;
	}

	@Override
	public boolean accepts(ItemStack stack)
	{
		if (IC2R.sideProxy.isSimulating())
		{
			return isValidStack(stack);
		}

		// Client side: accept everything, server will validate via isValidStack
		return true;
	}

	public static class ServerScannableCheck implements IRpcProvider<Boolean>
	{
		public Boolean executeRpc(Object... args)
		{
			ItemStack stack = (ItemStack) args[0];
			return InvSlotScannable.isValidStack(stack);
		}
	}
}

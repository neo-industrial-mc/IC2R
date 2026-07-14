package me.halfcooler.ic2r.core.item.reactor;

import me.halfcooler.ic2r.api.reactor.IReactor;
import me.halfcooler.ic2r.api.reactor.IReactorComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public class ItemReactorVentSpread extends AbstractReactorComponent
{
	public final int sideVent;

	public ItemReactorVentSpread(Properties settings, int sidevent)
	{
		super(settings);
		this.sideVent = sidevent;
	}

	@Override
	public void processChamber(ItemStack stack, IReactor reactor, int x, int y, boolean heatrun)
	{
		if (heatrun)
		{
			this.cool(reactor, x - 1, y);
			this.cool(reactor, x + 1, y);
			this.cool(reactor, x, y - 1);
			this.cool(reactor, x, y + 1);
		}
	}

	private void cool(IReactor reactor, int x, int y)
	{
		ItemStack stack = reactor.getItemAt(x, y);
		if (stack != null && stack.getItem() instanceof IReactorComponent comp)
		{
			if (comp.canStoreHeat(stack, reactor, x, y))
			{
				int self = comp.alterHeat(stack, reactor, x, y, -this.sideVent);
				if (self <= 0)
				{
					reactor.addEmitHeat(self + this.sideVent);
				}
			}
		}
	}
}

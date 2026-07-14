package me.halfcooler.ic2r.core.item.reactor;

import me.halfcooler.ic2r.api.reactor.IReactor;
import me.halfcooler.ic2r.api.reactor.IReactorComponent;
import me.halfcooler.ic2r.core.profile.NotExperimental;
import me.halfcooler.ic2r.core.util.StackUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

@NotExperimental
public class ItemReactorHeatpack extends AbstractReactorComponent
{
	protected final int maxPer;
	protected final int heatPer;

	public ItemReactorHeatpack(Properties settings, int maxPer, int heatPer)
	{
		super(settings);
		this.maxPer = maxPer;
		this.heatPer = heatPer;
	}

	@Override
	public void processChamber(ItemStack stack, IReactor reactor, int x, int y, boolean heatrun)
	{
		if (heatrun)
		{
			int size = StackUtil.getSize(stack);
			this.heat(reactor, size, x + 1, y);
			this.heat(reactor, size, x - 1, y);
			this.heat(reactor, size, x, y + 1);
			this.heat(reactor, size, x, y - 1);
		}
	}

	private void heat(IReactor reactor, int size, int x, int y)
	{
		int want = this.maxPer * size;
		if (reactor.getHeat() < want)
		{
			ItemStack stack = reactor.getItemAt(x, y);
			if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IReactorComponent comp)
			{
				if (comp.canStoreHeat(stack, reactor, x, y))
				{
					int add = this.heatPer * size;
					int curr = comp.getCurrentHeat(stack, reactor, x, y);
					if (add > want - curr)
					{
						add = want - curr;
					}

					if (add > 0)
					{
						comp.alterHeat(stack, reactor, x, y, add);
					}
				}
			}
		}
	}

	@Override
	public float influenceExplosion(ItemStack stack, IReactor reactor)
	{
		return StackUtil.getSize(stack) / 10.0F;
	}
}

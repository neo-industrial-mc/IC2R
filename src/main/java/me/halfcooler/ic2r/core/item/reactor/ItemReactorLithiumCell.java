package me.halfcooler.ic2r.core.item.reactor;

import me.halfcooler.ic2r.api.reactor.IReactor;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

@NotClassic
public class ItemReactorLithiumCell extends AbstractDamageableReactorComponent
{
	public ItemReactorLithiumCell(Properties settings)
	{
		super(settings, 10000);
	}

	@Override
	public boolean acceptUraniumPulse(ItemStack stack, IReactor reactor, ItemStack pulsingStack, int youX, int youY, int pulseX, int pulseY, boolean heatrun)
	{
		if (heatrun)
		{
			int myLevel = this.getUse(stack) + reactor.getHeat() / 3000;
			if (myLevel >= this.getMaxUse())
			{
				reactor.setItemAt(youX, youY, new ItemStack(Ic2rItems.TRITIUM_FUEL_ROD));
			} else
			{
				this.setUse(stack, myLevel);
			}
		}

		return true;
	}

	@Override
	public double getUseFraction(ItemStack stack)
	{
		return 1.0 - super.getUseFraction(stack);
	}
}

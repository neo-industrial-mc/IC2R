package me.halfcooler.ic2r.core.item.reactor;

import me.halfcooler.ic2r.api.reactor.IReactor;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

@NotClassic
public class ItemReactorMOX extends ItemReactorUranium
{
	public ItemReactorMOX(Properties settings, int cells)
	{
		super(settings, cells, 10000);
	}

	@Override
	protected int getFinalHeat(ItemStack stack, IReactor reactor, int x, int y, int heat)
	{
		if (reactor.isFluidCooled())
		{
			float breedereffectiveness = (float) reactor.getHeat() / reactor.getMaxHeat();
			if (breedereffectiveness > 0.5)
			{
				heat *= 2;
			}
		}

		return heat;
	}

	@Override
	protected ItemStack getDepletedStack(ItemStack stack, IReactor reactor)
	{
		return new ItemStack(switch (this.numberOfCells)
		{
			case 1 -> Ic2rItems.DEPLETED_MOX_FUEL_ROD;
			case 2 -> Ic2rItems.DEPLETED_DUAL_MOX_FUEL_ROD;
			default -> throw new RuntimeException("invalid cell count: " + this.numberOfCells);
			case 4 -> Ic2rItems.DEPLETED_QUAD_MOX_FUEL_ROD;
		});
	}

	@Override
	public boolean acceptUraniumPulse(ItemStack stack, IReactor reactor, ItemStack pulsingStack, int youX, int youY, int pulseX, int pulseY, boolean heatrun)
	{
		if (!heatrun)
		{
			float breedereffectiveness = (float) reactor.getHeat() / reactor.getMaxHeat();
			float ReaktorOutput = 4.0F * breedereffectiveness + 1.0F;
			reactor.addOutput(ReaktorOutput);
		}

		return true;
	}
}

package me.halfcooler.ic2r.core.block.invslot;

import me.halfcooler.ic2r.api.recipe.ICannerBottleRecipeManager;
import me.halfcooler.ic2r.api.recipe.Recipes;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityCanner;
import net.minecraft.world.item.ItemStack;

public class InvSlotConsumableCanner extends InvSlotConsumableLiquid
{
	public InvSlotConsumableCanner(TileEntityCanner base1, String name1, int count)
	{
		super(base1, name1, count);
	}

	@Override
	public boolean accepts(ItemStack stack)
	{
		return switch (((TileEntityCanner) this.base).getMode())
		{
			case BottleSolid -> Recipes.cannerBottle
				.get(this.base.getParent().getLevel())
				.apply(new ICannerBottleRecipeManager.RawInput(stack, ((TileEntityCanner) this.base).inputSlot.get()), true)
				!= null;
			case BottleLiquid, EmptyLiquid, EnrichLiquid -> super.accepts(stack);
		};
	}
}

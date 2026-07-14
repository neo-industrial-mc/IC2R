package me.halfcooler.ic2r.core.block.invslot;

import me.halfcooler.ic2r.api.recipe.ICannerBottleRecipeManager;
import me.halfcooler.ic2r.api.recipe.Recipes;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntitySolidCanner;
import net.minecraft.world.item.ItemStack;

public class InvSlotConsumableSolidCanner extends InvSlotConsumableLiquid
{
	public InvSlotConsumableSolidCanner(TileEntitySolidCanner base1, String name1, int count)
	{
		super(base1, name1, count);
	}

	@Override
	public boolean accepts(ItemStack stack)
	{
		return Recipes.cannerBottle
			.get(this.base.getParent().getLevel())
			.apply(new ICannerBottleRecipeManager.RawInput(stack, ((TileEntitySolidCanner) this.base).inputSlot.get()), true)
			!= null;
	}
}

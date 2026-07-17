package me.halfcooler.ic2r.integration.jei.recipe.machine;

import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntityBlock;
import me.halfcooler.ic2r.integration.jeirei.SlotPosition;

import java.util.List;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class IORecipeCategory<T extends IJeiRecipeWrapper> implements IRecipeCategory<T>
{
	protected final Ic2rTileEntityBlock block;

	public IORecipeCategory(Ic2rTileEntityBlock block)
	{
		this.block = block;
	}

	public @NotNull Component getTitle()
	{
		return this.getBlockStack().getHoverName();
	}

	protected abstract List<SlotPosition> getInputSlotPos();

	protected abstract List<SlotPosition> getOutputSlotPos();

	protected void addRecipeSlots(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses, int xOffset, int yOffset)
	{
		List<SlotPosition> inputSlots = this.getInputSlotPos();
		List<List<ItemStack>> inputStacks = recipe.getInputs();

		int idx;
		for (idx = 0; idx < inputSlots.size(); idx++)
		{
			SlotPosition pos = inputSlots.get(idx);
			IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, pos.x() + xOffset, pos.y() + yOffset);
			if (idx < inputStacks.size())
			{
				slot.addItemStacks(inputStacks.get(idx));
			}
		}

		List<SlotPosition> outputSlots = this.getOutputSlotPos();
		List<ItemStack> outputStacks = recipe.getOutputs();

		for (idx = 0; idx < outputSlots.size(); idx++)
		{
			SlotPosition pos = outputSlots.get(idx);
			IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.OUTPUT, pos.x() + xOffset, pos.y() + yOffset);
			if (idx < outputStacks.size())
			{
				slot.addItemStack(outputStacks.get(idx));
			}
		}
	}

	public ItemStack getBlockStack()
	{
		return new ItemStack(this.block.asItem());
	}

	public IDrawable getIcon()
	{
		return null;
	}
}

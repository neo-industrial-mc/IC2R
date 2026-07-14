package me.halfcooler.ic2r.integration.jei.recipe.machine;

import net.minecraft.client.gui.GuiGraphics;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.ItemStack;

public class MetalFormerCategory extends DynamicCategory
{
	private static final ItemStack[] ICONS = new ItemStack[] {
		new ItemStack(Ic2rItems.COPPER_CABLE), new ItemStack(Ic2rItems.FORGE_HAMMER), new ItemStack(Ic2rItems.CUTTER)
	};
	private final int mode;

	public MetalFormerCategory(RecipeType<IORecipeWrapper> recipeType, int mode, IGuiHelper guiHelper)
	{
		super(Ic2rBlocks.METAL_FORMER.get(), recipeType, guiHelper);
		this.mode = mode;
	}

	@Override
	public void draw(IORecipeWrapper recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY)
	{
		super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
		guiGraphics.renderItem(ICONS[this.mode], 65 + this.xOffset, 53 + this.yOffset);
	}
}

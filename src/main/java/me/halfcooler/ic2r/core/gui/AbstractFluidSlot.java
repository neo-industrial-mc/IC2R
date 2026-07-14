package me.halfcooler.ic2r.core.gui;

import net.minecraft.client.gui.GuiGraphics;
import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.fluid.FluidHandler;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;

import java.util.List;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

public abstract class AbstractFluidSlot extends GuiElement<TankFluidSlot>
{
	protected AbstractFluidSlot(Ic2rGui<?> gui, int x, int y, int width, int height)
	{
		super(gui, x, y, width, height);
	}

	@Override
	public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		bindCommonTexture();
		Ic2rFluidStack fs = this.getFluidStack();
		this.gui.drawTexturedRect(guiGraphics.pose(), this.x, this.y, this.width, this.height, 8.0, 160.0);
		if (fs != null && !fs.isEmpty())
		{
			int fluidX = this.x + 1;
			int fluidY = this.y + 1;
			int fluidWidth = 16;
			int fluidHeight = 16;
			Fluid fluid = fs.getFluid();
			TextureAtlasSprite sprite = fluid != null ? getBlockTextureMap().getSprite(FluidHandler.getStillSpriteId(fluid)) : null;
			int color = fluid != null ? FluidHandler.getColor(fluid) : -1;
			bindBlockTexture();
			this.gui.drawSprite(guiGraphics.pose(), fluidX, fluidY, fluidWidth, fluidHeight, sprite, color, 1.0, false, false);
		}
	}

	@Override
	protected List<Component> getToolTip()
	{
		List<Component> ret = super.getToolTip();
		Ic2rFluidStack fs = this.getFluidStack();
		if (fs != null && !fs.isEmpty())
		{
			Fluid fluid = fs.getFluid();
			if (fluid != null)
			{
				ret.add(fs.getFluidDisplayName());
				ret.add(Component.translatable("ic2r.generic.text.amount", fs.getAmountMb()));
				String translateKey = FluidHandler.isGaseous(fluid) ? "ic2r.generic.text.gas" : "ic2r.generic.text.liquid";
				ret.add(Component.translatable("ic2r.generic.text.state", Component.translatable(translateKey)));
			} else
			{
				ret.add(Component.literal("Invalid FluidStack instance."));
			}
		} else
		{
			ret.add(Component.translatable("ic2r.generic.text.no_fluid"));
		}

		return ret;
	}

	protected abstract Ic2rFluidStack getFluidStack();
}

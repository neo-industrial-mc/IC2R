package me.halfcooler.ic2r.core.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.ResourceLocation;
import me.halfcooler.ic2r.core.Ic2rGui;

/**
 * Vanilla-styled GUI button.
 * <p>
 * Minecraft 1.20.2+ removed {@code textures/gui/widgets.png} button strips in favor of
 * GUI sprites ({@code widget/button*}). Drawing the old path samples the missing texture
 * (black / magenta checkerboard).
 */
public class VanillaButton extends Button<VanillaButton>
{
	private static final WidgetSprites SPRITES = new WidgetSprites(
		ResourceLocation.withDefaultNamespace("widget/button"),
		ResourceLocation.withDefaultNamespace("widget/button_disabled"),
		ResourceLocation.withDefaultNamespace("widget/button_highlighted")
	);
	private static final int colorNormal = 14737632;
	private static final int colorHover = 16777120;
	private static final int colorDisabled = 10526880;
	protected IEnableHandler disableHandler;

	public VanillaButton(Ic2rGui<?> gui, int x, int y, int width, int height, IClickHandler handler)
	{
		super(gui, x, y, width, height, handler);
	}

	public VanillaButton withDisableHandler(IEnableHandler handler)
	{
		this.disableHandler = handler;
		return this;
	}

	public boolean isDisabled()
	{
		return this.disableHandler != null && !this.disableHandler.isEnabled();
	}

	@Override
	public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		boolean enabled = !this.isDisabled();
		boolean highlighted = enabled && this.isActive(mouseX, mouseY);
		ResourceLocation sprite = SPRITES.get(enabled, highlighted);

		// Element coords are relative to the container origin; blitSprite uses screen space.
		int absX = this.gui.getX() + this.x;
		int absY = this.gui.getY() + this.y;

		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		guiGraphics.blitSprite(sprite, absX, absY, this.width, this.height);
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

		super.drawBackground(guiGraphics, mouseX, mouseY);
	}

	protected boolean isActive(int mouseX, int mouseY)
	{
		return this.contains(mouseX, mouseY);
	}

	@Override
	protected int getTextColor(int mouseX, int mouseY)
	{
		return this.isDisabled() ? colorDisabled : (this.isActive(mouseX, mouseY) ? colorHover : colorNormal);
	}

	@Override
	protected boolean onMouseClick(int mouseX, int mouseY, MouseButton button)
	{
		return this.isDisabled() ? false : super.onMouseClick(mouseX, mouseY, button);
	}
}

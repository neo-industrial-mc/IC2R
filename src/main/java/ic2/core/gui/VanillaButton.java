package ic2.core.gui;

import ic2.core.Ic2Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.ResourceLocation;

public class VanillaButton extends Button<VanillaButton> {
  private static final WidgetSprites sprites =
      new WidgetSprites(
          ResourceLocation.withDefaultNamespace("widget/button"),
          ResourceLocation.withDefaultNamespace("widget/button_disabled"),
          ResourceLocation.withDefaultNamespace("widget/button_highlighted"));
  private static final int colorNormal = 14737632;
  private static final int colorHover = 16777120;
  private static final int colorDisabled = 10526880;
  protected IEnableHandler disableHandler;

  public VanillaButton(Ic2Gui<?> gui, int x, int y, int width, int height, IClickHandler handler) {
    super(gui, x, y, width, height, handler);
  }

  public VanillaButton withDisableHandler(IEnableHandler handler) {
    this.disableHandler = handler;
    return this;
  }

  public boolean isDisabled() {
    return this.disableHandler != null && !this.disableHandler.isEnabled();
  }

  @Override
  public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    guiGraphics.blitSprite(
        sprites.get(!this.isDisabled(), this.isActive(mouseX, mouseY)),
        this.gui.getX() + this.x,
        this.gui.getY() + this.y,
        this.width,
        this.height);
    super.drawBackground(guiGraphics, mouseX, mouseY);
  }

  protected boolean isActive(int mouseX, int mouseY) {
    return this.contains(mouseX, mouseY);
  }

  @Override
  protected int getTextColor(int mouseX, int mouseY) {
    return this.isDisabled() ? 10526880 : (this.isActive(mouseX, mouseY) ? 16777120 : 14737632);
  }

  @Override
  protected boolean onMouseClick(int mouseX, int mouseY, MouseButton button) {
    return this.isDisabled() ? false : super.onMouseClick(mouseX, mouseY, button);
  }
}

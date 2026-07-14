package me.halfcooler.ic2r.core.gui.code;

import me.halfcooler.ic2r.core.gui.GuiDefaultBackground;
import me.halfcooler.ic2r.core.gui.TextLabel;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * W2.4 sample: pure-code Screen companion for {@link CodeGuiSampleMenu}.
 * <p>
 * Prefer {@link GuiDefaultBackground} when no custom full-texture is needed, or extend
 * {@link me.halfcooler.ic2r.core.Ic2rGui} and override {@code getTextureLocation()} for a static GUI texture.
 * Add widgets with {@code addElement(...)} — do not parse guidef XML.
 * See {@code docs/spec/gui_modernization.md}.
 */
public final class CodeGuiSampleScreen extends GuiDefaultBackground<CodeGuiSampleMenu>
{
	public CodeGuiSampleScreen(CodeGuiSampleMenu menu, Inventory playerInventory, Component title)
	{
		super(menu, playerInventory, title, CodeGuiSampleMenu.GUI_HEIGHT);

		// Hard-coded widgets (replaces guidef <text> / gauges / buttons).
		this.addElement(TextLabel.create(this, 8, 6, "Code GUI sample (W2.4)", 0x404040, false));
	}
}

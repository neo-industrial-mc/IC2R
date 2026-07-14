package me.halfcooler.ic2r.core.gui;

import net.minecraft.client.gui.GuiGraphics;
import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.comp.Process;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntity;
import me.halfcooler.ic2r.core.util.Util;

public class ProgressGauge extends GuiElement<ProgressGauge>
{
	private final Process process;
	private final ProgressGauge.ProgressBarType type;

	public ProgressGauge(Ic2rGui<?> gui, int x, int y, Ic2rTileEntity te, ProgressGauge.ProgressBarType type)
	{
		super(gui, x, y, type.w, type.h);
		this.type = type;
		this.process = te.getComponent(Process.class);
	}

	@Override
	public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		bindCommonTexture();
		this.gui.drawTexturedRect(guiGraphics.pose(), this.x, this.y, this.type.w, this.type.h, this.type.emptyX, this.type.emptyY);
		int renderWidth = Util.limit((int) Math.round(this.getProgressRatio() * this.type.w), 0, this.type.w);
		if (renderWidth > 0)
		{
			this.gui.drawTexturedRect(guiGraphics.pose(), this.x, this.y, renderWidth, this.type.h, this.type.fullX, this.type.fullY);
		}
	}

	protected double getProgressRatio()
	{
		return this.process.getProgressRatio();
	}

	public enum ProgressBarType
	{
		type_1(165, 0, 165, 16, 22, 15),
		type_2(165, 35, 165, 52, 21, 11),
		type_3(165, 64, 165, 80, 22, 15),
		type_4(165, 96, 165, 112, 22, 15),
		type_5(133, 64, 133, 80, 18, 15);

		private final int emptyX;
		private final int emptyY;
		private final int fullX;
		private final int fullY;
		private final int w;
		private final int h;

		ProgressBarType(int emptyX, int emptyY, int fullX, int fullY, int w, int h)
		{
			this.emptyX = emptyX;
			this.emptyY = emptyY;
			this.fullX = fullX;
			this.fullY = fullY;
			this.w = w;
			this.h = h;
		}
	}
}

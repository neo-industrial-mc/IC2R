package me.halfcooler.ic2r.core.item.upgrade;

import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.gui.IEnableHandler;
import me.halfcooler.ic2r.core.gui.TextBox;

class MoveableTextBox extends TextBox
{
	protected int normalX;
	protected int normalY;
	protected int shiftedX;
	protected int shiftedY;
	private IEnableHandler moveHandler;

	public MoveableTextBox(Ic2rGui<?> gui, int normalX, int normalY, int shiftedX, int shiftedY, int width, int height, String text)
	{
		super(gui, normalX, normalY, width, height, text);
		this.normalX = normalX;
		this.normalY = normalY;
		this.shiftedX = shiftedX;
		this.shiftedY = shiftedY;
	}

	public MoveableTextBox withMoveHandler(IEnableHandler moveHandler)
	{
		this.moveHandler = moveHandler;
		return this;
	}

	public boolean isMoved()
	{
		return this.moveHandler != null && this.moveHandler.isEnabled();
	}

		public void tick()
	{
		super.tick();
		if (this.isMoved())
		{
			this.x = this.shiftedX;
			this.y = this.shiftedY;
		} else
		{
			this.x = this.normalX;
			this.y = this.normalY;
		}
	}
}

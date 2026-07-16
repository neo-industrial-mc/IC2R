package me.halfcooler.ic2r.core.item.upgrade;

import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.gui.IClickHandler;
import me.halfcooler.ic2r.core.gui.IEnableHandler;
import me.halfcooler.ic2r.core.gui.VanillaButton;

class MoveableButton extends VanillaButton
{
	protected int normalX;
	protected int normalY;
	protected int shiftedX;
	protected int shiftedY;
	private IEnableHandler moveHandler;

	public MoveableButton(Ic2rGui<?> gui, int normalX, int normalY, int shiftedX, int shiftedY, int width, int height, IClickHandler handler)
	{
		super(gui, normalX, normalY, width, height, handler);
		this.normalX = normalX;
		this.normalY = normalY;
		this.shiftedX = shiftedX;
		this.shiftedY = shiftedY;
	}

	public MoveableButton withMoveHandler(IEnableHandler moveHandler)
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

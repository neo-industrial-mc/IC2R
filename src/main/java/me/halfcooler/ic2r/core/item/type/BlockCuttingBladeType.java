package me.halfcooler.ic2r.core.item.type;

import me.halfcooler.ic2r.api.block.IIdProvider;
import me.halfcooler.ic2r.core.profile.NotClassic;

@NotClassic
public enum BlockCuttingBladeType implements IIdProvider
{
	iron(0),
	steel(1),
	diamond(2);

	private final int id;

	BlockCuttingBladeType(int id)
	{
		this.id = id;
	}

	@Override
	public String getName()
	{
		return this.name();
	}

	@Override
	public int getId()
	{
		return this.id;
	}
}

package me.halfcooler.ic2r.api.entity.boat;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import java.util.Set;
import java.util.stream.Stream;

import net.minecraft.world.item.Item;

public record BoatType(String name, Item baseItem)
{
	private static final Set<BoatType> VALUES = new ObjectArraySet<>();

	protected BoatType(Item baseItem, String name)
	{
		this(name, baseItem);
	}

	public static BoatType register(Item baseItem, String name)
	{
		BoatType type = new BoatType(baseItem, name);
		VALUES.add(type);
		return type;
	}

	public static Stream<BoatType> stream()
	{
		return VALUES.stream();
	}


}

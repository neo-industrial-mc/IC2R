package me.halfcooler.ic2r.core.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * NBT read helpers for snake_case migration: prefer a modern key, fall back to legacy keys.
 * <p>
 * Write path should only emit the modern snake_case key (see W1.5 / Modernization §3.2).
 * Do not dual-write legacy keys.
 */
public final class LegacyNbt
{
	private LegacyNbt()
	{
	}

	/**
	 * Reads a double: primary key first, then each legacy key in order.
	 * Returns {@code 0.0} if none are present (matches {@link CompoundTag#getDouble} default).
	 */
	public static double getDouble(CompoundTag tag, String primary, String... legacyKeys)
	{
		if (tag.contains(primary, Tag.TAG_ANY_NUMERIC))
		{
			return tag.getDouble(primary);
		}
		for (String legacy : legacyKeys)
		{
			if (tag.contains(legacy, Tag.TAG_ANY_NUMERIC))
			{
				return tag.getDouble(legacy);
			}
		}
		return 0.0;
	}

	/**
	 * Reads an int: primary key first, then each legacy key in order.
	 * Returns {@code 0} if none are present.
	 */
	public static int getInt(CompoundTag tag, String primary, String... legacyKeys)
	{
		if (tag.contains(primary, Tag.TAG_ANY_NUMERIC))
		{
			return tag.getInt(primary);
		}
		for (String legacy : legacyKeys)
		{
			if (tag.contains(legacy, Tag.TAG_ANY_NUMERIC))
			{
				return tag.getInt(legacy);
			}
		}
		return 0;
	}

	/**
	 * Reads a short: primary key first, then each legacy key in order.
	 * Returns {@code 0} if none are present.
	 */
	public static short getShort(CompoundTag tag, String primary, String... legacyKeys)
	{
		if (tag.contains(primary, Tag.TAG_ANY_NUMERIC))
		{
			return tag.getShort(primary);
		}
		for (String legacy : legacyKeys)
		{
			if (tag.contains(legacy, Tag.TAG_ANY_NUMERIC))
			{
				return tag.getShort(legacy);
			}
		}
		return 0;
	}

	/**
	 * Reads a float: primary key first, then each legacy key in order.
	 * Returns {@code 0.0f} if none are present.
	 */
	public static float getFloat(CompoundTag tag, String primary, String... legacyKeys)
	{
		if (tag.contains(primary, Tag.TAG_ANY_NUMERIC))
		{
			return tag.getFloat(primary);
		}
		for (String legacy : legacyKeys)
		{
			if (tag.contains(legacy, Tag.TAG_ANY_NUMERIC))
			{
				return tag.getFloat(legacy);
			}
		}
		return 0.0F;
	}

	/**
	 * Reads a byte: primary key first, then each legacy key in order.
	 * Returns {@code 0} if none are present (matches {@link CompoundTag#getByte} default).
	 */
	public static byte getByte(CompoundTag tag, String primary, String... legacyKeys)
	{
		if (tag.contains(primary, Tag.TAG_ANY_NUMERIC))
		{
			return tag.getByte(primary);
		}
		for (String legacy : legacyKeys)
		{
			if (tag.contains(legacy, Tag.TAG_ANY_NUMERIC))
			{
				return tag.getByte(legacy);
			}
		}
		return 0;
	}

	/**
	 * True if the primary key or any legacy key is present (any type).
	 */
	public static boolean contains(CompoundTag tag, String primary, String... legacyKeys)
	{
		if (tag.contains(primary))
		{
			return true;
		}
		for (String legacy : legacyKeys)
		{
			if (tag.contains(legacy))
			{
				return true;
			}
		}
		return false;
	}
}

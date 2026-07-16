package me.halfcooler.ic2r.core.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 1.21 replacement for removed {@code NbtUtils#read/writeGameProfile}.
 * Serializes id + name only (enough for IC2R personal ownership checks).
 */
public final class GameProfileNbt
{
	private GameProfileNbt()
	{
	}

	public static CompoundTag write(GameProfile profile)
	{
		CompoundTag tag = new CompoundTag();
		if (profile.getId() != null)
		{
			tag.putUUID("Id", profile.getId());
		}
		if (profile.getName() != null)
		{
			tag.putString("Name", profile.getName());
		}
		return tag;
	}

	public static void write(CompoundTag tag, GameProfile profile)
	{
		CompoundTag written = write(profile);
		for (String key : written.getAllKeys())
		{
			tag.put(key, written.get(key));
		}
	}

	@Nullable
	public static GameProfile read(CompoundTag tag)
	{
		if (tag == null || tag.isEmpty())
		{
			return null;
		}
		UUID id = tag.hasUUID("Id") ? tag.getUUID("Id") : null;
		String name = tag.contains("Name") ? tag.getString("Name") : "";
		if (id == null && (name == null || name.isEmpty()))
		{
			return null;
		}
		if (id == null)
		{
			id = new UUID(0L, 0L);
		}
		return new GameProfile(id, name == null ? "" : name);
	}
}

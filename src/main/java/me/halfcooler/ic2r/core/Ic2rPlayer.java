package me.halfcooler.ic2r.core;

import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;
import me.halfcooler.ic2r.core.util.Util;

import java.util.UUID;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;

public class Ic2rPlayer
{
	public static Player get(Level world)
	{
		return world instanceof ServerLevel ? IC2R.envProxy.createFakePlayer((ServerLevel) world, getGameProfile(Util.getDimId(world))) : null;
	}

	private static GameProfile getGameProfile(ResourceLocation dim)
	{
		String name = "[IC2R " + dim + "]";
		UUID uuid = UUID.nameUUIDFromBytes(name.getBytes(Charsets.UTF_8));
		return new GameProfile(uuid, name);
	}
}

package me.halfcooler.ic2r.api.tile;

import net.minecraft.world.level.block.Block;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public final class ExplosionWhitelist
{
	private static final Set<Block> whitelist = Collections.newSetFromMap(new IdentityHashMap<>());

	public static void addWhitelistedBlock(Block block)
	{
		whitelist.add(block);
	}

	public static void removeWhitelistedBlock(Block block)
	{
		whitelist.remove(block);
	}

	public static boolean isBlockWhitelisted(Block block)
	{
		return whitelist.contains(block);
	}
}

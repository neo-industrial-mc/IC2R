package me.halfcooler.ic2r.core.network;

import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class TeUpdateDataServer
{
	private final Set<String> globalFields = new LinkedHashSet<>();
	private final Map<ServerPlayer, Set<String>> playerFieldMap = new IdentityHashMap<>();

	TeUpdateDataServer()
	{
	}

	void addGlobalField(String name)
	{
		if (this.globalFields.add(name))
		{
			if (!this.playerFieldMap.isEmpty())
			{
				for (Set<String> playerFields : this.playerFieldMap.values())
				{
					playerFields.remove(name);
				}
			}
		}
	}

	void addPlayerField(String name, ServerPlayer player)
	{
		if (!this.globalFields.contains(name))
		{
			Set<String> playerFields = this.playerFieldMap.computeIfAbsent(player, k -> new LinkedHashSet<>());

			playerFields.add(name);
		}
	}

	Collection<String> getGlobalFields()
	{
		return this.globalFields;
	}

	Collection<String> getPlayerFields(ServerPlayer player)
	{
		Set<String> ret = this.playerFieldMap.get(player);
		return ret == null ? Collections.emptyList() : ret;
	}
}

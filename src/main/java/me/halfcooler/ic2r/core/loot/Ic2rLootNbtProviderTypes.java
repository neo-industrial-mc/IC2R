package me.halfcooler.ic2r.core.loot;

import me.halfcooler.ic2r.core.IC2R;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;

public class Ic2rLootNbtProviderTypes
{
	public static final LootNbtProviderType BLOCK_NBT = register(new Ic2rBlockNbtProvider.Serializer());

	public static void init()
	{
	}

	private static LootNbtProviderType register(Serializer<? extends NbtProvider> jsonSerializer)
	{
		return Registry.register(BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE, IC2R.getIdentifier("block_nbt"), new LootNbtProviderType(jsonSerializer));
	}
}

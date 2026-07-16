package me.halfcooler.ic2r.core.loot;

import me.halfcooler.ic2r.core.IC2R;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;
import com.mojang.serialization.MapCodec;

public class Ic2rLootNbtProviderTypes
{
	public static final LootNbtProviderType BLOCK_NBT = register(Ic2rBlockNbtProvider.CODEC);

	public static void init()
	{
	}

	private static LootNbtProviderType register(MapCodec<? extends NbtProvider> codec)
	{
		return Registry.register(BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE, IC2R.getIdentifier("block_nbt"), new LootNbtProviderType(codec));
	}
}

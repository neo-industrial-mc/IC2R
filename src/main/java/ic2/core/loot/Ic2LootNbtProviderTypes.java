package ic2.core.loot;

import ic2.core.IC2;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;

public class Ic2LootNbtProviderTypes
{
	public static final LootNbtProviderType BLOCK_NBT = register(Ic2BlockNbtProvider.CODEC);

	public static void init()
	{
	}

	private static LootNbtProviderType register(com.mojang.serialization.MapCodec<? extends NbtProvider> codec)
	{
		return Registry.register(BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE, IC2.getIdentifier("block_nbt"), new LootNbtProviderType(codec));
	}
}

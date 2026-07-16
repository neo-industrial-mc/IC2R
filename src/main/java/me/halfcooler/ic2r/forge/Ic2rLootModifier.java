package me.halfcooler.ic2r.forge;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class Ic2rLootModifier extends LootModifier
{
	public static final MapCodec<Ic2rLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> codecStart(instance).apply(instance, Ic2rLootModifier::new));
	public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> lootModifiersRegistry =
		DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, "ic2r");

	private static final Map<ResourceLocation, ResourceLocation> AllLootTables = Map.ofEntries(
		Map.entry(BuiltInLootTables.ABANDONED_MINESHAFT.location(), ResourceLocation.fromNamespaceAndPath("ic2r", "chests/abandoned_mineshaft")),
		Map.entry(BuiltInLootTables.DESERT_PYRAMID.location(), ResourceLocation.fromNamespaceAndPath("ic2r", "chests/desert_pyramid")),
		Map.entry(BuiltInLootTables.END_CITY_TREASURE.location(), ResourceLocation.fromNamespaceAndPath("ic2r", "chests/end_city_treasure")),
		Map.entry(BuiltInLootTables.IGLOO_CHEST.location(), ResourceLocation.fromNamespaceAndPath("ic2r", "chests/igloo_chest")),
		Map.entry(BuiltInLootTables.JUNGLE_TEMPLE.location(), ResourceLocation.fromNamespaceAndPath("ic2r", "chests/jungle_temple")),
		Map.entry(BuiltInLootTables.NETHER_BRIDGE.location(), ResourceLocation.fromNamespaceAndPath("ic2r", "chests/nether_bridge")),
		Map.entry(BuiltInLootTables.SIMPLE_DUNGEON.location(), ResourceLocation.fromNamespaceAndPath("ic2r", "chests/simple_dungeon")),
		Map.entry(BuiltInLootTables.SPAWN_BONUS_CHEST.location(), ResourceLocation.fromNamespaceAndPath("ic2r", "chests/spawn_bonus_chest")),
		Map.entry(BuiltInLootTables.STRONGHOLD_CORRIDOR.location(), ResourceLocation.fromNamespaceAndPath("ic2r", "chests/stronghold_corridor")),
		Map.entry(BuiltInLootTables.STRONGHOLD_CROSSING.location(), ResourceLocation.fromNamespaceAndPath("ic2r", "chests/stronghold_crossing")),
		Map.entry(BuiltInLootTables.STRONGHOLD_LIBRARY.location(), ResourceLocation.fromNamespaceAndPath("ic2r", "chests/stronghold_library")),
		Map.entry(BuiltInLootTables.VILLAGE_TOOLSMITH.location(), ResourceLocation.fromNamespaceAndPath("ic2r", "chests/village_toolsmith"))
	);

	static
	{
		lootModifiersRegistry.register("inject", () -> CODEC);
	}

	public Ic2rLootModifier(LootItemCondition[] conditionsIn)
	{
		super(conditionsIn);
	}

	@Override
	protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context)
	{
		ResourceLocation queriedId = context.getQueriedLootTableId();
		ResourceLocation iLootId = AllLootTables.get(queriedId);
		if (iLootId == null)
		{
			return generatedLoot;
		}
		ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, iLootId);
		LootTable table = context.getLevel().getServer().reloadableRegistries().getLootTable(key);
		if (table == LootTable.EMPTY)
		{
			return generatedLoot;
		}
		LootParams params = new LootParams.Builder(context.getLevel())
			.withParameter(LootContextParams.ORIGIN, context.getParam(LootContextParams.ORIGIN))
			.withOptionalParameter(LootContextParams.THIS_ENTITY, context.getParamOrNull(LootContextParams.THIS_ENTITY))
			.withLuck(context.getLuck())
			.create(LootContextParamSets.CHEST);
		generatedLoot.addAll(table.getRandomItems(params));
		return generatedLoot;
	}

	@Override
	public MapCodec<? extends IGlobalLootModifier> codec()
	{
		return CODEC;
	}
}

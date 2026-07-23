package ic2.forge;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Map;
import net.minecraft.core.registries.Registries;
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

public class Ic2LootModifier extends LootModifier {
  public static final MapCodec<Ic2LootModifier> CODEC =
      RecordCodecBuilder.mapCodec(
          instance -> codecStart(instance).apply(instance, Ic2LootModifier::new));
  public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>>
      lootModifiersRegistry =
          DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, "ic2");
  private static final Map<ResourceLocation, net.minecraft.resources.ResourceKey<LootTable>>
      AllLootTables =
          Map.ofEntries(
              Map.entry(
                  BuiltInLootTables.ABANDONED_MINESHAFT.location(),
                  ic2Table("chests/abandoned_mineshaft")),
              Map.entry(
                  BuiltInLootTables.DESERT_PYRAMID.location(), ic2Table("chests/desert_pyramid")),
              Map.entry(
                  BuiltInLootTables.END_CITY_TREASURE.location(),
                  ic2Table("chests/end_city_treasure")),
              Map.entry(BuiltInLootTables.IGLOO_CHEST.location(), ic2Table("chests/igloo_chest")),
              Map.entry(
                  BuiltInLootTables.JUNGLE_TEMPLE.location(), ic2Table("chests/jungle_temple")),
              Map.entry(
                  BuiltInLootTables.NETHER_BRIDGE.location(), ic2Table("chests/nether_bridge")),
              Map.entry(
                  BuiltInLootTables.SIMPLE_DUNGEON.location(), ic2Table("chests/simple_dungeon")),
              Map.entry(
                  BuiltInLootTables.SPAWN_BONUS_CHEST.location(),
                  ic2Table("chests/spawn_bonus_chest")),
              Map.entry(
                  BuiltInLootTables.STRONGHOLD_CORRIDOR.location(),
                  ic2Table("chests/stronghold_corridor")),
              Map.entry(
                  BuiltInLootTables.STRONGHOLD_CROSSING.location(),
                  ic2Table("chests/stronghold_crossing")),
              Map.entry(
                  BuiltInLootTables.STRONGHOLD_LIBRARY.location(),
                  ic2Table("chests/stronghold_library")),
              Map.entry(
                  BuiltInLootTables.VILLAGE_TOOLSMITH.location(),
                  ic2Table("chests/village_toolsmith")));

  private static net.minecraft.resources.ResourceKey<LootTable> ic2Table(String path) {
    return net.minecraft.resources.ResourceKey.create(
        Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("ic2", path));
  }

  static {
    lootModifiersRegistry.register("inject", () -> CODEC);
  }

  public Ic2LootModifier(LootItemCondition[] conditionsIn) {
    super(conditionsIn);
  }

  @Override
  protected @NotNull ObjectArrayList<ItemStack> doApply(
      ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
    net.minecraft.resources.ResourceKey<LootTable> iLootId =
        AllLootTables.get(context.getQueriedLootTableId());
    if (iLootId == null) {
      return generatedLoot;
    }
    LootTable table = context.getLevel().getServer().reloadableRegistries().getLootTable(iLootId);
    LootParams params =
        new LootParams.Builder(context.getLevel())
            .withParameter(LootContextParams.ORIGIN, context.getParam(LootContextParams.ORIGIN))
            .withOptionalParameter(
                LootContextParams.THIS_ENTITY,
                context.getParamOrNull(LootContextParams.THIS_ENTITY))
            .withLuck(context.getLuck())
            .create(LootContextParamSets.CHEST);
    generatedLoot.addAll(table.getRandomItems(params));
    return generatedLoot;
  }

  @Override
  public MapCodec<? extends IGlobalLootModifier> codec() {
    return CODEC;
  }
}

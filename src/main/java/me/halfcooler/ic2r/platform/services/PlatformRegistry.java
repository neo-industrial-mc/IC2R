package me.halfcooler.ic2r.platform.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * Loader-facing registration surface (blocks, items, menus, recipes, worldgen hooks).
 * <p>
 * Draft SPI for W3.1. Existing call sites still use {@code EnvProxy}; migrate in W3.2+.
 * Implementations live under {@code me.halfcooler.ic2r.forge} (or future neoforge/fabric).
 * Must not expose {@code net.neoforged.neoforge.*} types on this interface.
 */
public interface PlatformRegistry
{

	void registerItem(ResourceLocation id, Item item);

	<T extends BlockEntity> BlockEntityType<T> registerBlockEntity(
		ResourceLocation id, BiFunction<BlockPos, BlockState, T> factory, Block... blocks
	);

	<T extends AbstractContainerMenu> MenuType<T> registerMenuType(
		ResourceLocation id, BiFunction<Integer, Inventory, T> factory
	);

	/**
	 * Menu type with extra open-buffer payload (replaces Forge {@code IMenuTypeExtension} usage in common).
	 */
	<T extends AbstractContainerMenu> MenuType<T> registerExtendedMenuType(
		ResourceLocation id, ExtendedMenuFactory<T> factory
	);

	void registerEntity(ResourceLocation id, EntityType<?> type);

	WoodType registerSignType(String name);

	void registerStatusEffect(ResourceLocation id, MobEffect effect);

	void registerFlammableBlock(Block block, int encouragement, int flammability);

	SoundEvent registerSoundEvent(String path);

	GameEvent registerGameEvent(String path, int notificationRadius);

	<FC extends FeatureConfiguration, F extends Feature<FC>> CompletableFuture<Holder<ConfiguredFeature<FC, ?>>>
	registerConfiguredFeature(ResourceLocation id, F feature, FC config);

	<FC extends FeatureConfiguration> void registerPlacedFeature(
		ResourceLocation id, CompletableFuture<Holder<ConfiguredFeature<FC, ?>>> feature, List<PlacementModifier> modifiers
	);

	void registerPlacementModifierType(ResourceLocation id, PlacementModifierType<?> type);

	<T extends FoliagePlacer> FoliagePlacerType<T> registerFoliagePlacer(ResourceLocation id, Codec<T> codec);

	<T extends Recipe<?>> RecipeType<T> registerRecipeType(ResourceLocation id);

	void registerRecipeSerializer(ResourceLocation id, RecipeSerializer<?> serializer);

	/** Run after all deferred / registry events for this mod have completed. */
	void runAfterRegistryInit(Runnable task);

	CreativeModeTab createCreativeTab(ResourceLocation id, Supplier<ItemStack> icon);

	@FunctionalInterface
	interface ExtendedMenuFactory<T extends AbstractContainerMenu>
	{
		T create(int syncId, Inventory inventory, ByteBuf extraData);
	}
}

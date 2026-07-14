package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.Ic2rItemGroupType;
import me.halfcooler.ic2r.core.proxy.EnvProxy;
import me.halfcooler.ic2r.platform.services.PlatformRegistry;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;

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
 * Forge implementation of {@link PlatformRegistry}.
 * <p>
 * G3.2 thin adapter: delegates to the existing {@link EnvProxy} / {@link EnvProxyForge}
 * registration path so SPI is callable without duplicating DeferredRegister logic.
 * Call sites still primarily use {@code IC2R.envProxy}; migration is incremental (E3).
 */
public final class PlatformRegistryForge implements PlatformRegistry
{
	private static EnvProxy proxy()
	{
		EnvProxy env = IC2R.envProxy;
		if (env == null)
		{
			throw new IllegalStateException("IC2R.envProxy not initialized; cannot use PlatformRegistryForge");
		}
		return env;
	}

	@Override
	public void registerItem(ResourceLocation id, Item item)
	{
		proxy().registerItem(id, item);
	}

	@Override
	public <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(
		ResourceLocation id, BiFunction<BlockPos, BlockState, T> factory, Block... blocks
	)
	{
		return proxy().registerBlockEntity(id, factory, blocks);
	}

	@Override
	public <T extends AbstractContainerMenu> MenuType<T> registerMenuType(
		ResourceLocation id, BiFunction<Integer, Inventory, T> factory
	)
	{
		return proxy().registerScreenHandler(id, factory);
	}

	@Override
	public <T extends AbstractContainerMenu> MenuType<T> registerExtendedMenuType(
		ResourceLocation id, ExtendedMenuFactory<T> factory
	)
	{
		return proxy().registerExtendedScreenHandler(id, factory::create);
	}

	@Override
	public void registerEntity(ResourceLocation id, EntityType<?> type)
	{
		proxy().registerEntity(id, type);
	}

	@Override
	public WoodType registerSignType(String name)
	{
		return proxy().registerSignType(name);
	}

	@Override
	public void registerStatusEffect(ResourceLocation id, MobEffect effect)
	{
		proxy().registerStatusEffect(id, effect);
	}

	@Override
	public void registerFlammableBlock(Block block, int encouragement, int flammability)
	{
		proxy().registerFlammableBlock(block, encouragement, flammability);
	}

	@Override
	public SoundEvent registerSoundEvent(String path)
	{
		return proxy().registerSoundEvent(path);
	}

	@Override
	public GameEvent registerGameEvent(String path, int notificationRadius)
	{
		return proxy().registerGameEvent(path, notificationRadius);
	}

	@Override
	public <FC extends FeatureConfiguration, F extends Feature<FC>> CompletableFuture<Holder<ConfiguredFeature<FC, ?>>>
	registerConfiguredFeature(ResourceLocation id, F feature, FC config)
	{
		return proxy().registerConfiguredFeature(id, feature, config);
	}

	@Override
	public <FC extends FeatureConfiguration> void registerPlacedFeature(
		ResourceLocation id, CompletableFuture<Holder<ConfiguredFeature<FC, ?>>> feature, List<PlacementModifier> modifiers
	)
	{
		proxy().registerPlacedFeature(id, feature, modifiers);
	}

	@Override
	public void registerPlacementModifierType(ResourceLocation id, PlacementModifierType<?> type)
	{
		proxy().registerPlacementModifierType(id, type);
	}

	@Override
	public <T extends FoliagePlacer> FoliagePlacerType<T> registerFoliagePlacer(ResourceLocation id, Codec<T> codec)
	{
		return proxy().registerFoliagePlacer(id, codec);
	}

	@Override
	public <T extends Recipe<?>> RecipeType<T> registerRecipeType(ResourceLocation id)
	{
		return proxy().registerRecipeType(id);
	}

	@Override
	public void registerRecipeSerializer(ResourceLocation id, RecipeSerializer<?> serializer)
	{
		proxy().registerRecipeSerializer(id, serializer);
	}

	@Override
	public void runAfterRegistryInit(Runnable task)
	{
		proxy().runAfterRegistryInit(task);
	}

	/**
	 * SPI surface has no {@link Ic2rItemGroupType}; default {@link Ic2rItemGroupType#GENERAL}.
	 * Existing tabs still register via {@code EnvProxy#createItemGroup} with explicit types.
	 */
	@Override
	public CreativeModeTab createCreativeTab(ResourceLocation id, Supplier<ItemStack> icon)
	{
		return proxy().createItemGroup(id, icon, Ic2rItemGroupType.GENERAL);
	}
}

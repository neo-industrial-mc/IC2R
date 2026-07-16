package me.halfcooler.ic2r.forge;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import me.halfcooler.ic2r.api.crops.Crops;
import me.halfcooler.ic2r.api.crops.CropCard;
import me.halfcooler.ic2r.api.energy.ProfileEvent;
import me.halfcooler.ic2r.api.event.ExplosionEvent;
import me.halfcooler.ic2r.api.event.RetextureEvent;
import me.halfcooler.ic2r.api.item.IElectricItem;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.Ic2rItemGroupType;
import me.halfcooler.ic2r.core.Ic2rPotion;
import me.halfcooler.ic2r.core.fluid.EnvFluidHandler;
import me.halfcooler.ic2r.core.item.BlockItemEnergyStorage;
import me.halfcooler.ic2r.core.item.ElectricItemManager;
import me.halfcooler.ic2r.core.item.ItemClassicCell;
import me.halfcooler.ic2r.core.item.ItemCropSeed;
import me.halfcooler.ic2r.core.item.EnvItemHandler;
import me.halfcooler.ic2r.core.item.armor.ItemArmorFluidTank;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.util.LiquidUtil;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.util.StackUtil;
import me.halfcooler.ic2r.core.proxy.EnvProxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class EnvProxyForge implements EnvProxy
{
	static final DeferredRegister<BlockEntityType<?>> blockEntityRegistry = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "ic2r");
	static final DeferredRegister<MenuType<?>> screenHandlerRegistry = DeferredRegister.create(Registries.MENU, "ic2r");
	static final DeferredRegister<EntityType<?>> entityRegistry = DeferredRegister.create(Registries.ENTITY_TYPE, "ic2r");
	static final DeferredRegister<MobEffect> statusEffectRegistry = DeferredRegister.create(Registries.MOB_EFFECT, "ic2r");
	static final DeferredRegister<CreativeModeTab> creativeTabRegistry = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "ic2r");
	static final List<TabRegistration> pendingTabRegistrations = new ArrayList<>();
	static final DeferredRegister<FoliagePlacerType<?>> foliagePlacerRegistry = DeferredRegister.create(Registries.FOLIAGE_PLACER_TYPE, "ic2r");
	static final DeferredRegister<RecipeType<?>> recipeTypeRegistry = DeferredRegister.create(Registries.RECIPE_TYPE, "ic2r");
	static final DeferredRegister<RecipeSerializer<?>> recipeSerializerRegistry = DeferredRegister.create(Registries.RECIPE_SERIALIZER, "ic2r");
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, "ic2r");
	private static final boolean isClient = FMLEnvironment.dist.isClient();
	static List<Runnable> pendingItemRegistrations = new ArrayList<>();
	static List<ConfiguredFeatureRegistration<?, ?>> configuredFeatureRegistrations = new ArrayList<>();
	static List<EnvProxyForge.PlacedFeatureRegistration<?>> placedFeatureRegistrations = new ArrayList<>();
	static List<EnvProxyForge.PlacementModifierTypeRegistration> placementModifierTypeRegistrations = new ArrayList<>();
	static HashMap<Item, Integer> burnTimeRecord = new HashMap<>();

	static void registerPendingItems()
	{
		for (Runnable r : pendingItemRegistrations)
		{
			r.run();
		}

		pendingItemRegistrations.clear();
	}

	// G3.5 / E2: isFabricEnv / isForgeEnv / getServer removed —
	// use PlatformServices.lifecycle().getLoaderKind() / .getServer()

	@Override
	public <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(ResourceLocation id, BiFunction<BlockPos, BlockState, T> factory, Block... blocks)
	{
		BlockEntityType<T> type = Builder.of(factory::apply, blocks).build(null);
		blockEntityRegistry.register(id.getPath(), () -> type);
		return type;
	}

	@Override
	public <T extends AbstractContainerMenu> MenuType<T> registerScreenHandler(ResourceLocation id, BiFunction<Integer, Inventory, T> factory)
	{
		MenuType<T> type = new MenuType<>(factory::apply, FeatureFlags.DEFAULT_FLAGS);
		screenHandlerRegistry.register(id.getPath(), () -> type);
		return type;
	}

	@Override
	public <T extends AbstractContainerMenu> MenuType<T> registerExtendedScreenHandler(
		ResourceLocation id, EnvProxy.ExtendedClientScreenHandlerFactory<T> factory
	)
	{
		MenuType<T> type = IMenuTypeExtension.create(factory::create);
		screenHandlerRegistry.register(id.getPath(), () -> type);
		return type;
	}

	@Override
	public void registerItem(ResourceLocation id, Item item)
	{
		pendingItemRegistrations.add(() -> Registry.register(BuiltInRegistries.ITEM, id, item));
	}

	@Override
	public void registerEntity(ResourceLocation id, EntityType<?> type)
	{
		entityRegistry.register(id.getPath(), () -> type);
	}

	@Override
	public WoodType registerSignType(String name)
	{
		return WoodType.register(new WoodType("ic2r:" + name, BlockSetType.OAK));
	}

	/**
	 * Queue entries on {@link #statusEffectRegistry} <b>before</b> any
	 * {@code RegisterEvent} for {@code MOB_EFFECT}. Calling this (or
	 * {@link #registerStatusEffect}) after that event freezes the DeferredRegister
	 * and throws {@link IllegalStateException}.
	 * <p>
	 * Invoked from {@link FmlMod} constructor — not from {@code onInitEarly}
	 * (which runs during BLOCK registration, after MOB_EFFECT has already frozen).
	 */
	static void queueCoreStatusEffects()
	{
		statusEffectRegistry.register("radiation", () ->
		{
			Ic2rPotion effect = new Ic2rPotion(MobEffectCategory.HARMFUL, 5149489);
			Ic2rPotion.radiation = effect;
			return effect;
		});
	}

	@Override
	public void registerStatusEffect(ResourceLocation id, MobEffect effect)
	{
		// Must be called before MOB_EFFECT RegisterEvent (mod constructor / static init).
		statusEffectRegistry.register(id.getPath(), () -> effect);
	}

	@Override
	public void registerFlammableBlock(Block block, int burn, int spread)
	{
	}

	@Override
	public SoundEvent registerSoundEvent(String id)
	{
		// W1.7: SoundEvents are fully Deferred/Holder via Ic2rSoundEvents.REGISTRY.
		// Immediate ForgeRegistries registration is no longer used for this category.
		throw new UnsupportedOperationException(
			"Sound events must be registered via Ic2rSoundEvents DeferredRegister (W1.7). id=" + id
		);
	}

	@Override
	public GameEvent registerGameEvent(String id, int range)
	{
		ResourceLocation identifier = IC2R.getIdentifier(id);
		// GameEvent is a record of notification radius only; name comes from registry key.
		return Registry.register(BuiltInRegistries.GAME_EVENT, identifier, new GameEvent(range));
	}

	@Override
	public <FC extends FeatureConfiguration, F extends Feature<FC>> CompletableFuture<Holder<ConfiguredFeature<FC, ?>>> registerConfiguredFeature(
		ResourceLocation id, F feature, FC config
	)
	{
		CompletableFuture<Holder<ConfiguredFeature<FC, ?>>> ret = new CompletableFuture<>();
		configuredFeatureRegistrations.add(new ConfiguredFeatureRegistration<>(id, feature, config, ret));
		return ret;
	}

	@Override
	public <FC extends FeatureConfiguration> void registerPlacedFeature(
		ResourceLocation id, CompletableFuture<Holder<ConfiguredFeature<FC, ?>>> feature, List<PlacementModifier> modifiers
	)
	{
		placedFeatureRegistrations.add(new EnvProxyForge.PlacedFeatureRegistration<>(id, feature, modifiers, new CompletableFuture<>()));
	}

	@Override
	public void attachPlacedFeatureToBiome(ResourceLocation id, EnvProxy.BiomeSelector selector, Decoration step)
	{
	}

	@Override
	public void registerPlacementModifierType(ResourceLocation id, PlacementModifierType<?> type)
	{
		placementModifierTypeRegistrations.add(new EnvProxyForge.PlacementModifierTypeRegistration(id, type));
	}

	@Override
	public <T extends FoliagePlacer> FoliagePlacerType<T> registerFoliagePlacer(ResourceLocation id, MapCodec<T> codec)
	{
		// 1.21+: FoliagePlacerType constructor takes MapCodec, not Codec
		FoliagePlacerType<T> type = new FoliagePlacerType<>(codec);
		foliagePlacerRegistry.register(id.getPath(), () -> type);
		return type;
	}

	@Override
	public <T extends Recipe<?>> RecipeType<T> registerRecipeType(ResourceLocation id)
	{
		RecipeType<T> type = RecipeType.simple(id);
		recipeTypeRegistry.register(id.getPath(), () -> type);
		return type;
	}

	@Override
	public void registerRecipeSerializer(ResourceLocation id, RecipeSerializer<?> serializer)
	{
		recipeSerializerRegistry.register(id.getPath(), () -> serializer);
	}

	@Override
	public void runAfterRegistryInit(Runnable runnable)
	{
		FmlMod.instance.runAfterRegistryInit(runnable);
	}

	@Override
	public CreativeModeTab createItemGroup(ResourceLocation id, Supplier<ItemStack> iconSupplier, Ic2rItemGroupType groupType)
	{
		pendingTabRegistrations.add(new TabRegistration(id, iconSupplier, groupType));
		CreativeModeTab tab = CreativeModeTab.builder()
			.title(Component.translatable("itemGroup." + id.getNamespace() + "." + id.getPath()))
			.icon(iconSupplier)
			.displayItems((params, output) ->
			{
				List<Supplier<Item>> items = Ic2rItems.CREATIVE_TAB_ITEMS.get(groupType);
				if (items != null)
				{
					items.sort(Comparator.comparing(s -> BuiltInRegistries.ITEM.getKey(s.get()).toString()));
					for (Supplier<Item> itemSupplier : items)
					{
						Item item = itemSupplier.get();
						output.accept(new ItemStack(item));
						if (item instanceof IElectricItem)
						{
							output.accept(ElectricItemManager.getCharged(item, Double.POSITIVE_INFINITY));
						}
						if (item instanceof BlockItemEnergyStorage energyItem)
						{
							ItemStack chargedStack = new ItemStack(item);
							StackUtil.getOrCreateNbtData(chargedStack).putDouble("energy", energyItem.maxEnergy);
							output.accept(chargedStack);
						}
						if (item instanceof ItemArmorFluidTank tankItem)
						{
							ItemStack filledStack = new ItemStack(item);
							tankItem.fillTank(filledStack);
							output.accept(filledStack);
						}
					}
				}

				if (groupType == Ic2rItemGroupType.FARMING)
				{
					for (CropCard crop : Crops.instance.getCrops())
					{
						output.accept(ItemCropSeed.generateItemStackFromValues(crop, 1, 1, 1, 4));
					}
				}

				// AE2-facade style: empty cell + special cells + one entry per still fluid
				// (dedicated item when registered, otherwise facade_cell with fluid NBT).
				if (groupType == Ic2rItemGroupType.FLUID_CELLS)
				{
					output.accept(new ItemStack(Ic2rItems.FACADE_CELL));
					output.accept(new ItemStack(Ic2rItems.ELECTROLYZED_WATER_CELL));
					output.accept(new ItemStack(Ic2rItems.HYDRATION_CELL));
					for (Fluid fluid : LiquidUtil.getAllFluidsSorted())
					{
						ItemStack filled = ItemClassicCell.createFilledStack(fluid);
						if (!filled.isEmpty())
						{
							output.accept(filled);
						}
					}
				}
			})
			.build();
		creativeTabRegistry.register(id.getPath(), () -> tab);
		return tab;
	}

	@Override


	public EnvFluidHandler createFluidStackHandler()
	{
		if (isClient)
		{
			try
			{
				return (EnvFluidHandler) Class.forName("me.halfcooler.ic2r.forge.ClientEnvFluidHandlerForge").getConstructor().newInstance();
			} catch (ReflectiveOperationException e)
			{
				throw new RuntimeException(e);
			}
		} else
		{
			return new EnvFluidHandlerForge();
		}
	}

	@Override
	public EnvItemHandler createItemHandler()
	{
		return new EnvItemHandlerForge();
	}

	@Override
	public float getBlastResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion)
	{
		return state.getBlock().getExplosionResistance(state, world, pos, explosion);
	}

	@Override
	public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation rotation)
	{
		return state.rotate(world, pos, rotation);
	}

	@Override
	public boolean hasRecipeRemainder(ItemStack stack)
	{
		return stack.getItem().hasCraftingRemainingItem(stack);
	}

	@Override
	public ItemStack getRecipeRemainder(ItemStack stack)
	{
		return stack.getItem().getCraftingRemainingItem(stack);
	}

	@Override
	public void registerBurnTime(ItemLike stack, int value)
	{
		burnTimeRecord.put(stack.asItem(), value);
	}

	@Override
	public int getBurnTime(ItemStack stack)
	{
		return stack.getBurnTime(null);
	}

	@Override
	public boolean biomeHasType(Holder<Biome> biome, EnvProxy.BiomeType type)
	{
		return false;
	}

	@Override
	public Collection<EnvProxy.BiomeType> getBiomeTypes(Holder<Biome> biome)
	{
		return Collections.emptySet();
	}

	@Override
	public boolean openHandledScreen(Player player, MenuProvider factory, GrowingBuffer data)
	{
		((ServerPlayer) player).openMenu(factory, data::writeTo);
		return true;
	}

	@Override
	public boolean isFakePlayer(Player entity)
	{
		return entity instanceof FakePlayer;
	}

	@Override
	public Player createFakePlayer(ServerLevel world, GameProfile profile)
	{
		return FakePlayerFactory.get(world, profile);
	}

	@Override
	public void announceProfileLoad(Set<String> loaded, String active)
	{
		NeoForge.EVENT_BUS.post(new ProfileEvent.Load(loaded, active));
	}

	@Override
	public void announceProfileSwitch(String from, String to)
	{
		NeoForge.EVENT_BUS.post(new ProfileEvent.Switch(from, to));
	}

	@Override
	public boolean announceRetexture(
		Level world,
		BlockPos pos,
		BlockState state,
		Direction side,
		Player player,
		BlockState refState,
		String refVariant,
		Direction refSide,
		int[] refColorMultipliers
	)
	{
		RetextureEvent event = new RetextureEvent(world, pos, state, side, player, refState, refVariant, refSide, refColorMultipliers);
		NeoForge.EVENT_BUS.post(event);
		return event.applied;
	}

	@Override
	public boolean announceExplosion(Level world, Entity entity, Vec3 pos, double power, LivingEntity igniter, int radiationRange, double rangeLimit)
	{
		ExplosionEvent event = new ExplosionEvent(world, entity, pos, power, igniter, radiationRange, rangeLimit);
		NeoForge.EVENT_BUS.post(event);
		return !event.isCanceled();
	}

	record TabRegistration(ResourceLocation id, Supplier<ItemStack> icon, Ic2rItemGroupType groupType)
	{
	}

	record ConfiguredFeatureRegistration<FC extends FeatureConfiguration, F extends Feature<FC>>(
		ResourceLocation id,
		F feature,
		FC config,
		CompletableFuture<Holder<ConfiguredFeature<FC, ?>>> future
	)
	{
	}

	record PlacedFeatureRegistration<FC extends FeatureConfiguration>(
		ResourceLocation id,
		CompletableFuture<Holder<ConfiguredFeature<FC, ?>>> feature,
		List<PlacementModifier> modifiers,
		CompletableFuture<Holder<PlacedFeature>> placedFeature
	)
	{
	}

	record PlacementModifierTypeRegistration(ResourceLocation id, PlacementModifierType<?> type)
	{
	}
}

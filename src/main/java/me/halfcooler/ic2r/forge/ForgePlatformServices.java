package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.fluid.EnvFluidHandler;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.item.EnvItemHandler;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.platform.services.PlatformConfig;
import me.halfcooler.ic2r.platform.services.PlatformEnergyBridge;
import me.halfcooler.ic2r.platform.services.PlatformFluidBridge;
import me.halfcooler.ic2r.platform.services.PlatformItemTransfer;
import me.halfcooler.ic2r.platform.services.PlatformNetwork;
import me.halfcooler.ic2r.platform.services.PlatformPlayerUi;
import me.halfcooler.ic2r.platform.services.PlatformRegistry;
import me.halfcooler.ic2r.platform.services.PlatformServices;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
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
import org.jetbrains.annotations.Nullable;

/**
 * Wires Forge SPI adapters into {@link PlatformServices}.
 * <p>
 * W3.2: only {@link PlatformLifecycleForge} is used by common call sites;
 * other facets are stubs until later Work Units migrate them (EnvProxy dual-track).
 */
public final class ForgePlatformServices
{
	private ForgePlatformServices()
	{
	}

	/** Idempotent install from the Forge mod entry. */
	public static void install()
	{
		if (PlatformServices.isInstalled())
		{
			return;
		}

		PlatformServices.install(
			new StubRegistry(),
			new StubEnergy(),
			new StubFluid(),
			new StubItemTransfer(),
			new StubNetwork(),
			new StubPlayerUi(),
			new StubConfig(),
			new PlatformLifecycleForge()
		);
	}

	private static UnsupportedOperationException notMigrated(String facet)
	{
		return new UnsupportedOperationException(
			"Platform SPI facet not migrated yet: " + facet + " (still dual-track via EnvProxy/SideProxy)"
		);
	}

	private static final class StubRegistry implements PlatformRegistry
	{
		@Override
		public void registerBlock(ResourceLocation id, Block block)
		{
			throw notMigrated("PlatformRegistry");
		}

		@Override
		public void registerItem(ResourceLocation id, Item item)
		{
			throw notMigrated("PlatformRegistry");
		}

		@Override
		public <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(
			ResourceLocation id, BiFunction<BlockPos, BlockState, T> factory, Block... blocks
		)
		{
			throw notMigrated("PlatformRegistry");
		}

		@Override
		public <T extends AbstractContainerMenu> MenuType<T> registerMenuType(
			ResourceLocation id, BiFunction<Integer, Inventory, T> factory
		)
		{
			throw notMigrated("PlatformRegistry");
		}

		@Override
		public <T extends AbstractContainerMenu> MenuType<T> registerExtendedMenuType(
			ResourceLocation id, ExtendedMenuFactory<T> factory
		)
		{
			throw notMigrated("PlatformRegistry");
		}

		@Override
		public void registerEntity(ResourceLocation id, EntityType<?> type)
		{
			throw notMigrated("PlatformRegistry");
		}

		@Override
		public WoodType registerSignType(String name)
		{
			throw notMigrated("PlatformRegistry");
		}

		@Override
		public void registerStatusEffect(ResourceLocation id, MobEffect effect)
		{
			throw notMigrated("PlatformRegistry");
		}

		@Override
		public void registerFlammableBlock(Block block, int encouragement, int flammability)
		{
			throw notMigrated("PlatformRegistry");
		}

		@Override
		public SoundEvent registerSoundEvent(String path)
		{
			throw notMigrated("PlatformRegistry");
		}

		@Override
		public GameEvent registerGameEvent(String path, int notificationRadius)
		{
			throw notMigrated("PlatformRegistry");
		}

		@Override
		public <FC extends FeatureConfiguration, F extends Feature<FC>> CompletableFuture<Holder<ConfiguredFeature<FC, ?>>>
		registerConfiguredFeature(ResourceLocation id, F feature, FC config)
		{
			throw notMigrated("PlatformRegistry");
		}

		@Override
		public <FC extends FeatureConfiguration> void registerPlacedFeature(
			ResourceLocation id, CompletableFuture<Holder<ConfiguredFeature<FC, ?>>> feature, List<PlacementModifier> modifiers
		)
		{
			throw notMigrated("PlatformRegistry");
		}

		@Override
		public void registerPlacementModifierType(ResourceLocation id, PlacementModifierType<?> type)
		{
			throw notMigrated("PlatformRegistry");
		}

		@Override
		public <T extends FoliagePlacer> FoliagePlacerType<T> registerFoliagePlacer(ResourceLocation id, Codec<T> codec)
		{
			throw notMigrated("PlatformRegistry");
		}

		@Override
		public <T extends Recipe<?>> RecipeType<T> registerRecipeType(ResourceLocation id)
		{
			throw notMigrated("PlatformRegistry");
		}

		@Override
		public void registerRecipeSerializer(ResourceLocation id, RecipeSerializer<?> serializer)
		{
			throw notMigrated("PlatformRegistry");
		}

		@Override
		public void runAfterRegistryInit(Runnable task)
		{
			throw notMigrated("PlatformRegistry");
		}

		@Override
		public CreativeModeTab createCreativeTab(ResourceLocation id, Supplier<ItemStack> icon)
		{
			throw notMigrated("PlatformRegistry");
		}
	}

	private static final class StubEnergy implements PlatformEnergyBridge
	{
		@Override
		public boolean canReceive(BlockEntity be, @Nullable Direction side)
		{
			throw notMigrated("PlatformEnergyBridge");
		}

		@Override
		public boolean canExtract(BlockEntity be, @Nullable Direction side)
		{
			throw notMigrated("PlatformEnergyBridge");
		}

		@Override
		public long insert(BlockEntity be, @Nullable Direction side, long amount, boolean simulate)
		{
			throw notMigrated("PlatformEnergyBridge");
		}

		@Override
		public long extract(BlockEntity be, @Nullable Direction side, long maxAmount, boolean simulate)
		{
			throw notMigrated("PlatformEnergyBridge");
		}
	}

	private static final class StubFluid implements PlatformFluidBridge
	{
		@Override
		public EnvFluidHandler createHandler()
		{
			throw notMigrated("PlatformFluidBridge");
		}

		@Override
		public Ic2rFluidStack drainWorld(
			BlockState state, Level level, BlockPos pos, @Nullable BlockEntity be,
			Direction side, int maxMb, boolean simulate
		)
		{
			throw notMigrated("PlatformFluidBridge");
		}

		@Override
		public int fillWorld(
			BlockState state, Level level, BlockPos pos, @Nullable BlockEntity be,
			Direction side, Ic2rFluidStack stack, boolean simulate
		)
		{
			throw notMigrated("PlatformFluidBridge");
		}

		@Override
		public Ic2rFluidStack getContained(ItemStack stack)
		{
			throw notMigrated("PlatformFluidBridge");
		}
	}

	private static final class StubItemTransfer implements PlatformItemTransfer
	{
		@Override
		public EnvItemHandler createHandler()
		{
			throw notMigrated("PlatformItemTransfer");
		}

		@Override
		public int insert(BlockEntity be, @Nullable Direction side, ItemStack stack, boolean simulate)
		{
			throw notMigrated("PlatformItemTransfer");
		}

		@Override
		public ItemStack extract(BlockEntity be, @Nullable Direction side, int maxCount, boolean simulate)
		{
			throw notMigrated("PlatformItemTransfer");
		}
	}

	private static final class StubNetwork implements PlatformNetwork
	{
		@Override
		public void registerChannel(ResourceLocation channelId)
		{
			throw notMigrated("PlatformNetwork");
		}

		@Override
		public void sendToPlayer(ServerPlayer player, ResourceLocation channelId, FriendlyByteBuf payload)
		{
			throw notMigrated("PlatformNetwork");
		}

		@Override
		public void sendToServer(ResourceLocation channelId, FriendlyByteBuf payload)
		{
			throw notMigrated("PlatformNetwork");
		}

		@Override
		public void sendToTracking(@Nullable Player around, ResourceLocation channelId, FriendlyByteBuf payload)
		{
			throw notMigrated("PlatformNetwork");
		}
	}

	private static final class StubPlayerUi implements PlatformPlayerUi
	{
		@Override
		public boolean openMenu(Player player, MenuProvider provider, @Nullable GrowingBuffer extraData)
		{
			throw notMigrated("PlatformPlayerUi");
		}

		@Override
		public void messagePlayer(Player player, Component message)
		{
			throw notMigrated("PlatformPlayerUi");
		}

		@Override
		public void messagePlayer(Player player, String translationKey, Object... args)
		{
			throw notMigrated("PlatformPlayerUi");
		}

		@Override
		public void displayError(String message, Object... args)
		{
			throw notMigrated("PlatformPlayerUi");
		}

		@Override
		public void displayError(Exception exception, String message, Object... args)
		{
			throw notMigrated("PlatformPlayerUi");
		}
	}

	private static final class StubConfig implements PlatformConfig
	{
		@Override
		public Path getConfigDirectory()
		{
			throw notMigrated("PlatformConfig");
		}

		@Override
		public void registerCommonConfig(Object spec, @Nullable String relativeFileName)
		{
			throw notMigrated("PlatformConfig");
		}

		@Override
		public void registerClientConfig(Object spec, @Nullable String relativeFileName)
		{
			throw notMigrated("PlatformConfig");
		}

		@Override
		public boolean isCommonConfigLoaded()
		{
			throw notMigrated("PlatformConfig");
		}
	}
}

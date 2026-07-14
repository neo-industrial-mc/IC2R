package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.fluid.EnvFluidHandler;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.item.EnvItemHandler;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.platform.services.PlatformConfig;
import me.halfcooler.ic2r.platform.services.PlatformFluidBridge;
import me.halfcooler.ic2r.platform.services.PlatformItemTransfer;
import me.halfcooler.ic2r.platform.services.PlatformNetwork;
import me.halfcooler.ic2r.platform.services.PlatformPlayerUi;
import me.halfcooler.ic2r.platform.services.PlatformServices;

import java.nio.file.Path;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Wires Forge SPI adapters into {@link PlatformServices}.
 * <p>
 * Real facets: {@link PlatformLifecycleForge}, {@link PlatformRegistryForge} (G3.2),
 * {@link PlatformEnergyBridgeForge} (G2.8). Other facets remain stubs until later Units
 * migrate them (EnvProxy dual-track).
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
			new PlatformRegistryForge(),
			new PlatformEnergyBridgeForge(),
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

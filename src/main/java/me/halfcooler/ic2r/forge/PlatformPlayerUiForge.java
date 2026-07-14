package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.proxy.EnvProxy;
import me.halfcooler.ic2r.core.proxy.SideProxy;
import me.halfcooler.ic2r.platform.services.PlatformPlayerUi;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Forge implementation of {@link PlatformPlayerUi} (G3.6).
 * <p>
 * Thin adapter: {@link #openMenu} → {@link EnvProxy#openHandledScreen};
 * messaging / errors → {@link SideProxy#messagePlayer} / {@link SideProxy#displayError}.
 */
public final class PlatformPlayerUiForge implements PlatformPlayerUi
{
	private static EnvProxy env()
	{
		EnvProxy env = IC2R.envProxy;
		if (env == null)
		{
			throw new IllegalStateException("IC2R.envProxy not initialized; cannot use PlatformPlayerUiForge");
		}
		return env;
	}

	private static SideProxy side()
	{
		SideProxy side = IC2R.sideProxy;
		if (side == null)
		{
			throw new IllegalStateException("IC2R.sideProxy not initialized; cannot use PlatformPlayerUiForge");
		}
		return side;
	}

	@Override
	public boolean openMenu(Player player, MenuProvider provider, @Nullable GrowingBuffer extraData)
	{
		return env().openHandledScreen(player, provider, extraData);
	}

	@Override
	public void messagePlayer(Player player, Component message)
	{
		side().messagePlayer(player, message);
	}

	@Override
	public void messagePlayer(Player player, String translationKey, Object... args)
	{
		side().messagePlayer(player, translationKey, args);
	}

	@Override
	public void displayError(String message, Object... args)
	{
		side().displayError(message, args);
	}

	@Override
	public void displayError(Exception exception, String message, Object... args)
	{
		side().displayError(exception, message, args);
	}
}

package me.halfcooler.ic2r.platform.services;

import me.halfcooler.ic2r.core.network.GrowingBuffer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Player-facing UI / messaging hooks that differ by loader or side.
 * <p>
 * Draft SPI. Covers {@code EnvProxy#openHandledScreen}, {@code SideProxy#messagePlayer},
 * and related player feedback. Client-only screen registration remains client package /
 * future client SPI (not required for W3.1).
 */
public interface PlatformPlayerUi
{
	/**
	 * Open a server-side menu with optional extra sync buffer for the client factory.
	 *
	 * @return true if the screen was opened
	 */
	boolean openMenu(Player player, MenuProvider provider, @Nullable GrowingBuffer extraData);

	void messagePlayer(Player player, Component message);

	void messagePlayer(Player player, String translationKey, Object... args);

	/**
	 * Fatal / blocking error presentation (crash screen vs log-only on dedicated server).
	 * Maps from {@code SideProxy#displayError}.
	 */
	void displayError(String message, Object... args);

	void displayError(Exception exception, String message, Object... args);
}

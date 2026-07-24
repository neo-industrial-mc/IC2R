package me.halfcooler.ic2r.platform.services;

import me.halfcooler.ic2r.core.network.GrowingBuffer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Player-facing UI and messaging hooks that differ by loader or side.
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
}

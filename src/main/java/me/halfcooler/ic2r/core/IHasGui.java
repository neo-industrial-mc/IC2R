package me.halfcooler.ic2r.core;

import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.util.Util;
import me.halfcooler.ic2r.platform.services.PlatformServices;

import java.io.IOException;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public interface IHasGui extends Container
{
	static Component getBeName(BlockEntity be)
	{
		ResourceLocation id = Util.getName(be.getBlockState().getBlock());
		return Component.translatable(String.format("container.%s.%s", id.getNamespace(), id.getPath().replace('/', '.')));
	}

	static Component getItemName(Item item, Integer subGuiId)
	{
		ResourceLocation id = Util.getName(item);
		return Component.translatable(
			String.format("container.%s.%s%s", id.getNamespace(), id.getPath().replace('/', '.'), subGuiId != null ? String.format(".%d", subGuiId) : "")
		);
	}

	ContainerBase<?> createServerScreenHandler(int var1, Player var2);

	default void writeScreenOpenData(Player player, InteractionHand hand, GrowingBuffer buffer)
	{
	}

	ContainerBase<?> createClientScreenHandler(int var1, Inventory var2, GrowingBuffer var3);

	default void onScreenClosed(Player player)
	{
	}

	default boolean openManagedBe(Player player, InteractionHand hand)
	{
		GrowingBuffer buffer = new GrowingBuffer(50);

		try
		{
			Ic2rScreenHandlers.writeManagedBeData((BlockEntity) this, buffer);
			this.writeScreenOpenData(player, hand, buffer);
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		buffer.flip();
		// G3.6: open menu via PlatformPlayerUi (Forge → EnvProxy#openHandledScreen)
		return PlatformServices.playerUi().openMenu(player, new MenuProvider()
		{
			public AbstractContainerMenu createMenu(int syncId, @NotNull Inventory playerInventory, @NotNull Player playerx)
			{
				return IHasGui.this.createServerScreenHandler(syncId, playerx);
			}

			public @NotNull Component getDisplayName()
			{
				return IHasGui.getBeName((BlockEntity) IHasGui.this);
			}
		}, buffer);
	}

	default boolean openManagedItem(Player player, InteractionHand hand, Integer subGuiId)
	{
		GrowingBuffer buffer = new GrowingBuffer(50);

		Ic2rScreenHandlers.writeManagedItemData(player, hand, subGuiId, buffer);
		this.writeScreenOpenData(player, hand, buffer);

		buffer.flip();
		final Item item = player.getItemInHand(hand).getItem();
		// G3.6: open menu via PlatformPlayerUi (Forge → EnvProxy#openHandledScreen)
		return PlatformServices.playerUi().openMenu(player, new MenuProvider()
		{
			public AbstractContainerMenu createMenu(int syncId, @NotNull Inventory playerInventory, @NotNull Player playerx)
			{
				return IHasGui.this.createServerScreenHandler(syncId, playerx);
			}

			public @NotNull Component getDisplayName()
			{
				return IHasGui.getItemName(item, subGuiId);
			}
		}, buffer);
	}
}
